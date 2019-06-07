/*
 * Copyright (c) 2019 Stephen Gold
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
package jme3utilities.minie;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.AbstractPhysicsControl;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.util.clone.Cloner;
import java.io.IOException;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * A PhysicsControl to link a PhysicsCharacter to a Spatial. Compare with
 * {@link com.jme3.bullet.control.BetterCharacterControl} and JME's
 * CharacterControl.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class MinieCharacterControl extends AbstractPhysicsControl {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(MinieCharacterControl.class.getName());
    // *************************************************************************
    // fields

    /**
     * underlying collision object
     */
    private PhysicsCharacter character = null;
    /**
     * per-thread temporary storage for a Quaternion
     */
    final private static ThreadLocal<Quaternion> tmpQuaternionTL
            = new ThreadLocal<Quaternion>() {
        @Override
        protected Quaternion initialValue() {
            return new Quaternion();
        }
    };
    /**
     * per-thread temporary storage for a Vector3f
     */
    final private static ThreadLocal<Vector3f> tmpVectorTL
            = new ThreadLocal<Vector3f>();
    /**
     * view direction
     */
    private Vector3f viewDirection = new Vector3f(Vector3f.UNIT_Z);
    // *************************************************************************
    // constructors

    /**
     * No-argument constructor needed by SavableClassUtil. Do not invoke
     * directly!
     */
    public MinieCharacterControl() {
    }

    /**
     * Instantiate an enabled Control with the specified CollisionShape and step
     * height.
     *
     * @param shape the desired shape (not null, alias created)
     * @param stepHeight the maximum amount of normal vertical movement (in
     * physics-space units)
     */
    public MinieCharacterControl(CollisionShape shape, float stepHeight) {
        character = new PhysicsCharacter(shape, stepHeight);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Access the PhysicsCharacter managed by this Control.
     *
     * @return the pre-existing instance (not null)
     */
    public PhysicsCharacter getCharacter() {
        assert character != null;
        return character;
    }

    /**
     * Copy the character's location.
     *
     * @return a new location vector (in physics-space coordinates, not null)
     */
    public Vector3f getPhysicsLocation() {
        Vector3f result = character.getPhysicsLocation(null);
        return result;
    }

    /**
     * Jump in the "up" direction.
     */
    public void jump() {
        character.jump();
    }

    /**
     * Test whether the character is on the ground.
     *
     * @return true if on the ground, otherwise false
     */
    public boolean onGround() {
        boolean result = character.onGround();
        return result;
    }

    /**
     * Alter the character's fall speed.
     *
     * @param speed the desired speed (in physics-space units per second,
     * default=55)
     */
    public void setFallSpeed(float speed) {
        character.setFallSpeed(speed);
    }

    /**
     * Alter the character's gravitational acceleration.
     *
     * @param downwardAcceleration the desired downward acceleration (in
     * physics-space units per second squared, not null, unaffected,
     * default=29.4)
     */
    public void setGravity(float downwardAcceleration) {
        character.setGravity(downwardAcceleration);
    }

    /**
     * Alter the character's jump speed.
     *
     * @param speed the desired speed (in physics-space units per second,
     * default=10)
     */
    public void setJumpSpeed(float speed) {
        character.setJumpSpeed(speed);
    }

    /**
     * Alter the view direction.
     *
     * @param direction the desired direction (in physics-space coordinates, not
     * null, not zero)
     */
    public void setViewDirection(Vector3f direction) {
        Validate.nonZero(direction, "direction");

        viewDirection.set(direction);
        viewDirection.normalizeLocal();
    }

    /**
     * Alter the character's walk offset. The offset will continue to be applied
     * until altered again.
     *
     * @param offset the desired position increment for each physics tick (in
     * physics-space coordinates, not null, unaffected)
     */
    public void setWalkDirection(Vector3f offset) {
        Validate.nonNull(offset, "offset");
        character.setWalkDirection(offset);
    }
    // *************************************************************************
    // AbstractPhysicsControl methods

    /**
     * Add all managed physics objects to the PhysicsSpace.
     */
    @Override
    protected void addPhysics() {
        PhysicsSpace space = getPhysicsSpace();
        space.addCollisionObject(character);
    }

    /**
     * Callback from {@link com.jme3.util.clone.Cloner} to convert this
     * shallow-cloned Control into a deep-cloned one, using the specified Cloner
     * and original to resolve copied fields.
     *
     * @param cloner the Cloner that's cloning this Control (not null, modified)
     * @param original the Control from which this Control was shallow-cloned
     * (not null, unaffected)
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        super.cloneFields(cloner, original);

        character = cloner.clone(character);
        // tmpQuaternion not cloned
        // tmpVector not cloned
        viewDirection = cloner.clone(viewDirection);
    }

    /**
     * Create spatial-dependent data. Invoked when this Control is added to a
     * Spatial.
     *
     * @param spat the controlled Spatial (not null, alias created) TODO rename
     */
    @Override
    protected void createSpatialData(Spatial spat) {
        character.setUserObject(spat);
    }

    /**
     * Create a shallow clone for the JME cloner.
     *
     * @return a new Control (not null)
     */
    @Override
    public MinieCharacterControl jmeClone() {
        try {
            MinieCharacterControl clone
                    = (MinieCharacterControl) super.clone();
            return clone;
        } catch (CloneNotSupportedException exception) {
            throw new RuntimeException(exception);
        }
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
        super.read(importer);
        InputCapsule capsule = importer.getCapsule(this);

        character = (PhysicsCharacter) capsule.readSavable("character", null);
        viewDirection = (Vector3f) capsule.readSavable("viewDirection",
                new Vector3f(Vector3f.UNIT_Z));
    }

    /**
     * Remove all managed physics objects from the PhysicsSpace.
     */
    @Override
    protected void removePhysics() {
        PhysicsSpace space = getPhysicsSpace();
        space.removeCollisionObject(character);
    }

    /**
     * Destroy spatial-dependent data. Invoked when this Control is removed from
     * its Spatial.
     *
     * @param spat the previously controlled Spatial (not null) TODO rename
     */
    @Override
    protected void removeSpatialData(Spatial spat) {
        character.setUserObject(null);
    }

    /**
     * Render this Control. Invoked once per ViewPort per frame, provided the
     * Control is added to a scene. Should be invoked only by a subclass or by
     * the RenderManager.
     *
     * @param rm the RenderManager (unused)
     * @param vp the ViewPort to render (unused)
     */
    @Override
    public void render(RenderManager rm, ViewPort vp) {
        // do nothing
    }

    /**
     * Translate the PhysicsCharacter to the specified location.
     *
     * @param vec the desired location (not null, unaffected) TODO rename
     */
    @Override
    public void setPhysicsLocation(Vector3f vec) {
        character.setPhysicsLocation(vec);
    }

    /**
     * Rotate the PhysicsCharacter to the specified orientation.
     *
     * @param quat the desired orientation (not null, unaffected) TODO rename
     */
    @Override
    protected void setPhysicsRotation(Quaternion quat) {
        // do nothing
    }

    /**
     * Update this Control. Invoked once per frame during the logical-state
     * update, provided the Control is added to a scene. Do not invoke directly
     * from user code.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        // TODO test isEnabled()
        Quaternion orientation = tmpQuaternionTL.get();
        if (orientation == null) {
            orientation = new Quaternion();
            tmpQuaternionTL.set(orientation);
        }
        Vector3f up = tmpVectorTL.get();
        if (up == null) {
            up = new Vector3f();
            tmpVectorTL.set(up);
        }

        character.getUpDirection(up);
        orientation.lookAt(viewDirection, up);

        Vector3f location = tmpVectorTL.get();
        assert location != null;
        character.getPhysicsLocation(location);
        applyPhysicsTransform(location, orientation);
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
        super.write(exporter);
        OutputCapsule capsule = exporter.getCapsule(this);

        capsule.write(character, "character", null);
        capsule.write(viewDirection, "viewDirection", null);
    }
}
