/*
 Copyright (c) 2019, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities.minie.wizard;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.animation.DynamicAnimControl;
import com.jme3.bullet.animation.RagUtils;
import com.jme3.bullet.animation.TorsoLink;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.logging.Logger;
import jme3utilities.Misc;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.ui.InputMode;

/**
 * The screen controller for the "test" screen of DacWizard.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class TestScreen extends GuiScreenController {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final static Logger logger = Logger.getLogger(TestScreen.class.getName());
    // *************************************************************************
    // fields

    /**
     * horizontal plane added to physics space, or null if not added
     */
    private PhysicsRigidBody groundPlane = null;
    /**
     * root spatial of the C-G model being previewed
     */
    private Spatial viewedSpatial = null;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized, disabled screen that will not be enabled
     * during initialization.
     */
    TestScreen() {
        super("test", "Interface/Nifty/screens/wizard/test.xml", false);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Toggle mesh rendering.
     */
    void toggleMesh() {
        Spatial.CullHint cull = rootNode.getLocalCullHint();
        if (cull == Spatial.CullHint.Always) {
            rootNode.setCullHint(Spatial.CullHint.Never);
        } else {
            rootNode.setCullHint(Spatial.CullHint.Always);
        }
    }
    // *************************************************************************
    // GuiScreenController methods

    /**
     * Initialize this (disabled) screen prior to its first update.
     *
     * @param stateManager (not null)
     * @param application (not null)
     */
    @Override
    public void initialize(AppStateManager stateManager,
            Application application) {
        super.initialize(stateManager, application);

        InputMode inputMode = InputMode.findMode("test");
        assert inputMode != null;
        setListener(inputMode);
        inputMode.influence(this);
    }

    /**
     * A callback from Nifty, invoked each time this screen shuts down.
     */
    @Override
    public void onEndScreen() {
        super.onEndScreen();
        removeGroundPlane();
    }

    /**
     * A callback from Nifty, invoked each time this screen starts up.
     */
    @Override
    public void onStartScreen() {
        super.onStartScreen();

        DacWizard wizard = DacWizard.getApplication();
        wizard.clearScene();
        viewedSpatial = null;

        BulletAppState bulletAppState
                = DacWizard.findAppState(BulletAppState.class);
        bulletAppState.setDebugEnabled(true);
    }

    /**
     * Update this ScreenController prior to rendering. (Invoked once per
     * frame.) TODO divide up this method
     *
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        Model model = DacWizard.getModel();
        BulletAppState bulletAppState
                = DacWizard.findAppState(BulletAppState.class);

        String debugButton;
        if (bulletAppState.isDebugEnabled()) {
            debugButton = "Debug off";
        } else {
            debugButton = "Debug on";
        }
        setButtonText("debug", debugButton);

        Spatial.CullHint cull = rootNode.getCullHint();
        String meshButton;
        if (cull == Spatial.CullHint.Always) {
            meshButton = "Render meshes";
        } else {
            meshButton = "Hide meshes";
        }
        setButtonText("mesh", meshButton);

        DacWizard wizard = DacWizard.getApplication();
        DynamicAnimControl dac = wizard.findDac();
        Spatial nextSpatial = model.getRootSpatial();
        if (nextSpatial != viewedSpatial) {
            removeGroundPlane();
            wizard.clearScene();
            dac = null;
            viewedSpatial = nextSpatial;
            if (nextSpatial != null) {
                Spatial cgModel = (Spatial) Misc.deepCopy(nextSpatial);
                wizard.makeScene(cgModel);

                AbstractControl control = RagUtils.findSControl(cgModel);
                Spatial controlledSpatial = control.getSpatial();

                dac = model.copyRagdoll();
                controlledSpatial.addControl(dac);
                PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
                physicsSpace.add(dac);

                if (groundPlane == null) {
                    Plane plane = new Plane(Vector3f.UNIT_Y, 0f); // X-Z plane
                    PlaneCollisionShape shape = new PlaneCollisionShape(plane);
                    float mass = PhysicsRigidBody.massForStatic;
                    groundPlane = new PhysicsRigidBody(shape, mass);
                    physicsSpace.add(groundPlane);
                }
            }
        }

        String ragdollButton = "";
        if (dac != null && dac.isReady()) {
            TorsoLink torso = dac.getTorsoLink();
            if (torso.isKinematic()) {
                ragdollButton = "Go limp";
            } else {
                ragdollButton = "Reset skeleton";
            }
        }
        setButtonText("ragdoll", ragdollButton);
    }
    // *************************************************************************
    // private methods

    /**
     * Remove the ground plane from the physics space.
     */
    private void removeGroundPlane() {
        if (groundPlane != null) {
            BulletAppState bulletAppState
                    = DacWizard.findAppState(BulletAppState.class);
            PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
            physicsSpace.remove(groundPlane);
            groundPlane = null;
        }
    }
}
