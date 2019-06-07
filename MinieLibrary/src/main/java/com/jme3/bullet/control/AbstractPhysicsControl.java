/*
 * Copyright (c) 2009-2018 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.bullet.control;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import java.io.IOException;
import java.util.logging.Logger;
import jme3utilities.MySpatial;

/**
 * Manage the lifecycle of a physics object linked to a Spatial in a scene
 * graph.
 *
 * @author normenhansen
 */
abstract public class AbstractPhysicsControl
        implements JmeCloneable, PhysicsControl {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(AbstractPhysicsControl.class.getName());
    /**
     * local copy of {@link com.jme3.math.Quaternion#IDENTITY}
     */
    final private static Quaternion rotateIdentity = new Quaternion();
    /**
     * local copy of {@link com.jme3.math.Vector3f#ZERO}
     */
    final private static Vector3f translateIdentity = new Vector3f(0f, 0f, 0f);
    // *************************************************************************
    // fields

    /**
     * true &rarr; physics-space coordinates match local transform, false &rarr;
     * physics-space coordinates match world transform TODO rename localPhysics
     */
    private boolean applyLocal = false;
    /**
     * true&rarr;body is added to the PhysicsSpace, false&rarr;not added
     */
    protected boolean added = false;
    /**
     * true&rarr;Control is enabled, false&rarr;Control is disabled
     */
    private boolean enabled = true;
    /**
     * space to which the physics object is (or would be) added
     */
    private PhysicsSpace space = null;
    /**
     * temporary storage during calculations TODO thread safety
     */
    private Quaternion tmp_inverseWorldRotation = new Quaternion();
    /**
     * Spatial to which this Control is added, or null if none TODO rename
     * controlledSpatial
     */
    private Spatial spatial;
    // *************************************************************************
    // new methods exposed

    /**
     * Access the Spatial to which this Control is added.
     *
     * @return the Spatial, or null if none
     */
    public Spatial getSpatial() {
        return spatial;
    }

    /**
     * Test whether physics-space coordinates should match the spatial's local
     * coordinates.
     *
     * @return true if matching local coordinates, false if matching world
     * coordinates
     */
    public boolean isApplyPhysicsLocal() {
        return applyLocal;
    }

    /**
     * Alter whether physics-space coordinates should match the Spatial's local
     * coordinates.
     *
     * @param applyPhysicsLocal true&rarr;match local coordinates,
     * false&rarr;match world coordinates (default=false)
     */
    public void setApplyPhysicsLocal(boolean applyPhysicsLocal) {
        applyLocal = applyPhysicsLocal;
    }
    // *************************************************************************
    // new protected methods

    /**
     * Add all managed physics objects to the PhysicsSpace.
     */
    protected abstract void addPhysics();

    /**
     * Apply a physics transform to the spatial. TODO use MySpatial
     *
     * @param worldLocation location vector (in physics-space coordinates, not
     * null, unaffected)
     * @param worldRotation orientation (in physics-space coordinates, not null,
     * unaffected)
     */
    protected void applyPhysicsTransform(Vector3f worldLocation,
            Quaternion worldRotation) {
        if (enabled && spatial != null) {
            Vector3f localLocation = spatial.getLocalTranslation();
            Quaternion localRotationQuat = spatial.getLocalRotation();
            if (!applyLocal && spatial.getParent() != null) {
                localLocation.set(worldLocation).subtractLocal(spatial.getParent().getWorldTranslation());
                localLocation.divideLocal(spatial.getParent().getWorldScale());
                tmp_inverseWorldRotation.set(spatial.getParent().getWorldRotation()).inverseLocal().multLocal(localLocation);
                localRotationQuat.set(worldRotation);
                tmp_inverseWorldRotation.set(spatial.getParent().getWorldRotation()).inverseLocal().mult(localRotationQuat, localRotationQuat);

                spatial.setLocalTranslation(localLocation);
                spatial.setLocalRotation(localRotationQuat);
            } else {
                spatial.setLocalTranslation(worldLocation);
                spatial.setLocalRotation(worldRotation);
            }
        }
    }

    /**
     * Create spatial-dependent data. Invoked when this Control is added to a
     * Spatial.
     *
     * @param spat the controlled spatial (not null)
     */
    protected abstract void createSpatialData(Spatial spat);

    /**
     * Access whichever rotation corresponds to the physics rotation.
     *
     * @return the pre-existing Quaternion (in physics-space coordinates, not
     * null)
     */
    protected Quaternion getSpatialRotation() {
        if (MySpatial.isIgnoringTransforms(spatial)) {
            return rotateIdentity;
        } else if (applyLocal) {
            return spatial.getLocalRotation();
        } else {
            return spatial.getWorldRotation();
        }
    }

    /**
     * Access whichever spatial translation corresponds to the physics location.
     *
     * @return the pre-existing location vector (in physics-space coordinates,
     * not null) TODO
     */
    protected Vector3f getSpatialTranslation() {
        if (MySpatial.isIgnoringTransforms(spatial)) {
            return translateIdentity;
        } else if (applyLocal) {
            return spatial.getLocalTranslation();
        } else {
            return spatial.getWorldTranslation();
        }
    }

    /**
     * Destroy spatial-dependent data. Invoked when this Control is removed from
     * a Spatial.
     *
     * @param spat the previously controlled spatial (not null)
     */
    protected abstract void removeSpatialData(Spatial spat);

    /**
     * Translate the physics object to the specified location.
     *
     * @param vec the desired location (not null, unaffected)
     */
    protected abstract void setPhysicsLocation(Vector3f vec);

    /**
     * Rotate the physics object to the specified orientation.
     *
     * @param quat the desired orientation (not null, unaffected)
     */
    protected abstract void setPhysicsRotation(Quaternion quat);

    /**
     * Remove all managed physics objects from the PhysicsSpace.
     */
    protected abstract void removePhysics();
    // *************************************************************************
    // JmeCloneable methods

    /**
     * Callback from {@link com.jme3.util.clone.Cloner} to convert this
     * shallow-cloned Control into a deep-cloned one, using the specified Cloner
     * and original to resolve copied fields.
     *
     * @param cloner the Cloner that's cloning this Control (not null)
     * @param original the Control from which this Control was shallow-cloned
     * (unused)
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        tmp_inverseWorldRotation = cloner.clone(tmp_inverseWorldRotation);
        spatial = cloner.clone(spatial);
        // space not cloned
    }

    /**
     * Create a shallow clone for the JME cloner.
     *
     * @return a new instance
     */
    @Override
    public AbstractPhysicsControl jmeClone() {
        try {
            AbstractPhysicsControl clone
                    = (AbstractPhysicsControl) super.clone();
            return clone;
        } catch (CloneNotSupportedException exception) {
            throw new RuntimeException(exception);
        }
    }
    // *************************************************************************
    // PhysicsControl methods

    /**
     * Clone this Control for a different Spatial. No longer used as of JME 3.1.
     *
     * @param spatial (unused)
     * @return never
     * @throws UnsupportedOperationException always
     */
    @Override
    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException();
    }

    /**
     * Access the PhysicsSpace to which the object is (or would be) added.
     *
     * @return the pre-existing space, or null for none
     */
    @Override
    public PhysicsSpace getPhysicsSpace() {
        return space;
    }

    /**
     * Test whether this Control is enabled.
     *
     * @return true if enabled, otherwise false
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * De-serialize this Control from the specified importer, for example when
     * loading from a J3O file.
     *
     * @param importer (not null)
     * @throws IOException from the importer
     */
    @Override
    public void read(JmeImporter importer) throws IOException {
        InputCapsule capsule = importer.getCapsule(this);

        enabled = capsule.readBoolean("enabled", true);
        spatial = (Spatial) capsule.readSavable("spatial", null);
        applyLocal = capsule.readBoolean("applyLocalPhysics", false);
    }

    /**
     * Enable or disable this control. TODO add a render() method
     * <p>
     * When the Control is disabled, the physics object is removed from any
     * PhysicsSpace. When the Control is enabled again, the physics object is
     * moved to the location of the Spatial and then added to the PhysicsSpace.
     *
     * @param enabled true&rarr;enable the Control, false&rarr;disable it TODO
     * rename enable
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (space != null) {
            if (enabled && !added) {
                if (spatial != null) {
                    setPhysicsLocation(getSpatialTranslation());
                    setPhysicsRotation(getSpatialRotation());
                }
                addPhysics();
                added = true;
            } else if (!enabled && added) {
                removePhysics();
                added = false;
            }
        }
    }

    /**
     * If enabled, add this control's physics objects to the specified
     * PhysicsSpace. If not enabled, alter where the objects would be added. The
     * objects are removed from any other space they're in.
     *
     * @param newSpace where to add, or null to simply remove
     */
    @Override
    public void setPhysicsSpace(PhysicsSpace newSpace) {
        if (space == newSpace) {
            return;
        }

        if (added) {
            removePhysics();
            added = false;
        }
        space = newSpace;
        if (newSpace != null && isEnabled()) {
            addPhysics();
            added = true;
        }
        /*
         * If the Control isn't enabled, its physics objects will be
         * added to the new space when the Control becomes enabled.
         */
    }

    /**
     * Alter which Spatial is controlled. Invoked when the Control is added to
     * or removed from a Spatial. Should be invoked only by a subclass or from
     * Spatial. Do not invoke directly from user code.
     *
     * @param controlledSpatial the Spatial to control (or null)
     */
    @Override
    public void setSpatial(Spatial controlledSpatial) {
        if (spatial == controlledSpatial) {
            return;
        } else if (spatial != null) {
            removeSpatialData(spatial);
        }

        spatial = controlledSpatial;

        if (controlledSpatial != null) {
            createSpatialData(spatial);
            setPhysicsLocation(getSpatialTranslation());
            setPhysicsRotation(getSpatialRotation());
        }
    }

    /**
     * Serialize this Control to the specified exporter, for example when saving
     * to a J3O file.
     *
     * @param exporter (not null)
     * @throws IOException from the exporter
     */
    @Override
    public void write(JmeExporter exporter) throws IOException {
        OutputCapsule capsule = exporter.getCapsule(this);

        capsule.write(enabled, "enabled", true);
        capsule.write(applyLocal, "applyLocalPhysics", false);
        capsule.write(spatial, "spatial", null);
    }
}
