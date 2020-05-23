/*
 Copyright (c) 2018-2020, Stephen Gold
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
package jme3utilities.minie.test;

import com.jme3.app.Application;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.PhysicsSweepTestResult;
import com.jme3.bullet.collision.shapes.Box2dShape;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.ConeCollisionShape;
import com.jme3.bullet.collision.shapes.Convex2dShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.MultiSphere;
import com.jme3.bullet.collision.shapes.SimplexCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.bullet.util.DebugShapeFactory;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.CameraInput;
import com.jme3.input.KeyInput;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.AbstractBox;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainQuad;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyCamera;
import jme3utilities.MySpatial;
import jme3utilities.debug.PointVisualizer;
import jme3utilities.math.MyArray;
import jme3utilities.math.MyBuffer;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;
import jme3utilities.math.RectangularSolid;
import jme3utilities.math.VectorSet;
import jme3utilities.mesh.Cone;
import jme3utilities.mesh.Icosphere;
import jme3utilities.mesh.Prism;
import jme3utilities.mesh.RectangleMesh;
import jme3utilities.mesh.Tetrahedron;
import jme3utilities.minie.DumpFlags;
import jme3utilities.minie.PhysicsDumper;
import jme3utilities.minie.test.common.AbstractDemo;
import jme3utilities.minie.test.terrain.MinieTestTerrains;
import jme3utilities.ui.CameraOrbitAppState;
import jme3utilities.ui.InputMode;
import vhacd.VHACD;
import vhacd.VHACDParameters;
import vhacd.VHACDProgressListener;

/**
 * Test various shapes, scales, and collision margins on a kinematic
 * RigidBodyControl. Features tested include bounding boxes, collision
 * listeners, debug visualization, ray casting, and sweep tests.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestRbc
        extends AbstractDemo
        implements PhysicsCollisionListener {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestRbc.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName = TestRbc.class.getSimpleName();
    /**
     * list of test names, in ascending lexicographic order
     */
    final private static String[] testNames = {
        "Box", "BoxGImpact", "BoxHull", "BoxMesh", "Cone", "ConeGImpact",
        "ConeHull", "ConeMesh", "Cylinder", "CylinderGImpact", "CylinderHull",
        "CylinderMesh", "FourSphere", "KissCapsule", "KissHull", "KissMesh",
        "KissMultiSphere", "KissSphere", "LargeTerrain", "OneSphere", "Prism",
        "Simplex", "SmallTerrain", "SmallTerrainVhacd", "Sphere",
        "SphereCapsule", "SphereGImpact", "SphereHull", "SphereMesh", "Square",
        "SquareBox", "SquareConvex2d", "SquareGImpact", "SquareHeightfield",
        "SquareHull", "SquareMesh", "TetraGImpact", "TetraHull", "TetraMesh",
        "TwoSphere"
    };
    // *************************************************************************
    // fields

    /**
     * text displayed in the upper-left corner of the GUI node
     */
    final private BitmapText[] statusLines = new BitmapText[2];
    /**
     * AppState to manage the PhysicsSpace
     */
    private BulletAppState bulletAppState;
    /**
     * shape being tested
     */
    private CollisionShape testShape;
    /**
     * part index returned by the most recent ray/sweep test
     */
    private int partIndex = -1;
    /**
     * triangle index returned by the most recent ray/sweep test
     */
    private int triangleIndex = -1;
    /**
     * cursor shown when the raytest finds a collision object
     */
    private JmeCursor hitCursor;
    /**
     * cursor shown when the raytest doesn't find a collision object
     */
    private JmeCursor missCursor;
    /**
     * parent for added spatials
     */
    final private Node meshesNode = new Node("meshes node");
    /**
     * visualizer for the most recent raytest/sweeptest hit
     */
    private PointVisualizer hitPoint;
    /**
     * geometry for the missile, or null if none
     */
    private Spatial missileSpatial;
    /**
     * spatial(s) being tested
     */
    private Spatial testSpatial;
    /**
     * name of the test being run
     */
    private String testName = "Box";
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestRbc application.
     *
     * @param ignored array of command-line arguments (not null)
     */
    public static void main(String[] ignored) {
        /*
         * Mute the chatty loggers in certain packages.
         */
        Heart.setLoggingLevels(Level.WARNING);

        Application application = new TestRbc();
        /*
         * Customize the window's title bar.
         */
        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setTitle(applicationName);

        settings.setGammaCorrection(true);
        settings.setSamples(4); // anti-aliasing
        settings.setVSync(true);
        application.setSettings(settings);

        application.start();
    }
    // *************************************************************************
    // AbstractDemo methods

    /**
     * Initialize this application.
     */
    @Override
    public void actionInitializeApplication() {
        MinieTestTerrains.initialize(assetManager);
        assert MyArray.isSorted(testNames);

        configureCamera();
        configureDumper();
        configurePhysics();
        generateCursors();
        generateMaterials();
        generateShapes();

        ColorRGBA skyColor = new ColorRGBA(0.1f, 0.2f, 0.4f, 1f);
        viewPort.setBackgroundColor(skyColor);

        addLighting();
        attachWorldAxes(0.8f);
        addStatusLines();
        /*
         * Hide the render-statistics overlay initially.
         */
        stateManager.getState(StatsAppState.class).toggleStats();

        hitPoint = new PointVisualizer(assetManager, 16, ColorRGBA.Red,
                "saltire");
        rootNode.attachChild(hitPoint);
        hitPoint.setEnabled(false);

        rootNode.attachChild(meshesNode);

        restartTest();
    }

    /**
     * Configure the PhysicsDumper during startup.
     */
    @Override
    public void configureDumper() {
        PhysicsDumper dumper = getDumper();
        dumper.setEnabled(DumpFlags.ChildShapes, true);
        //dumper.setMaxChildren(3);
    }

    /**
     * Generate materials during startup.
     */
    @Override
    public void generateMaterials() {
        super.generateMaterials();

        Material wireMaterial = MyAsset.createWireframeMaterial(assetManager,
                ColorRGBA.Green);
        RenderState ars = wireMaterial.getAdditionalRenderState();
        ars.setFaceCullMode(RenderState.FaceCullMode.Off);
        registerMaterial("green wire", wireMaterial);

        Material redMaterial
                = MyAsset.createUnshadedMaterial(assetManager, ColorRGBA.Red);
        registerMaterial("red", redMaterial);
    }

    /**
     * Initialize V-HACD shapes during startup.
     */
    @Override
    public void generateShapes() {
        //super.generateShapes();

        VHACD.addProgressListener(new VHACDProgressListener() {
            double lastOP = -1.0;

            @Override
            public void update(double overallPercent, double stagePercent,
                    double operationPercent, String stageName,
                    String operationName) {
                if (overallPercent > lastOP) {
                    System.out.printf("smallTerrainVhacd %.0f%% complete%n",
                            overallPercent);
                    lastOP = overallPercent;
                }
            }
        });

        CollisionShape shape = CollisionShapeFactory.createVhacdShape(
                MinieTestTerrains.smallQuad, new VHACDParameters(), null);
        registerShape("smallTerrainVhacd", shape);
    }

    /**
     * Access the active BulletAppState.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    protected BulletAppState getBulletAppState() {
        assert bulletAppState != null;
        return bulletAppState;
    }

    /**
     * Determine the length of physics-debug arrows when visible.
     *
     * @return the desired length (in physics-space units, &ge;0)
     */
    @Override
    protected float maxArrowLength() {
        return 1f;
    }

    /**
     * Add application-specific hotkey bindings and override existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();

        dim.bind("cast ray", "RMB");
        dim.bind("cast ray", KeyInput.KEY_R);

        dim.bind("clear shapes", KeyInput.KEY_BACK);
        dim.bind("clear shapes", KeyInput.KEY_DELETE);

        dim.bind(AbstractDemo.asDumpPhysicsSpace, KeyInput.KEY_O);
        dim.bind(AbstractDemo.asDumpViewport, KeyInput.KEY_P);

        dim.bind("launch missile", KeyInput.KEY_L);

        dim.bind("less margin", KeyInput.KEY_V);
        dim.bind("more margin", KeyInput.KEY_F);

        dim.bind("next test", KeyInput.KEY_EQUALS);
        dim.bind("next test", KeyInput.KEY_NUMPAD6);
        dim.bind("previous test", KeyInput.KEY_MINUS);
        dim.bind("previous test", KeyInput.KEY_NUMPAD4);

        dim.bind("scale identity", KeyInput.KEY_I);
        dim.bind("scale nonuniform", KeyInput.KEY_N);
        dim.bind("scale uniform", KeyInput.KEY_U);
        dim.bind("scale x-yz", KeyInput.KEY_X);
        dim.bind("scale y-xz", KeyInput.KEY_Y);
        dim.bind("scale z-xy", KeyInput.KEY_T);

        dim.bind("signal " + CameraInput.FLYCAM_LOWER, KeyInput.KEY_DOWN);
        dim.bind("signal " + CameraInput.FLYCAM_RISE, KeyInput.KEY_UP);
        dim.bind("signal orbitLeft", KeyInput.KEY_LEFT);
        dim.bind("signal orbitRight", KeyInput.KEY_RIGHT);

        dim.bind("sweep", KeyInput.KEY_K);

        dim.bind(AbstractDemo.asToggleAabbs, KeyInput.KEY_APOSTROPHE);
        dim.bind(AbstractDemo.asToggleHelp, KeyInput.KEY_H);
        dim.bind(AbstractDemo.asTogglePcoAxes, KeyInput.KEY_SEMICOLON);
        dim.bind("toggle view", KeyInput.KEY_SLASH);

        float x = 10f;
        float y = cam.getHeight() - 60f;
        float width = cam.getWidth() - 20f;
        float height = cam.getHeight() - 20f;
        Rectangle rectangle = new Rectangle(x, y, width, height);

        attachHelpNode(rectangle);
    }

    /**
     * Process an action that wasn't handled by the active input mode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        if (ongoing) {
            switch (actionString) {
                case "cast ray":
                    castRay();
                    return;

                case "clear shapes":
                    clearShapes();
                    return;

                case "launch missile":
                    launchMissile();
                    return;

                case "less margin":
                    multiplyMargin(0.5f);
                    return;
                case "more margin":
                    multiplyMargin(2f);
                    return;

                case "next test":
                    advanceTest(+1);
                    return;
                case "previous test":
                    advanceTest(-1);
                    return;

                case "scale identity":
                    setScale(1f, 1f, 1f);
                    return;
                case "scale nonuniform":
                    setScale(0.8f, 1f, 1.3f);
                    return;
                case "scale uniform":
                    setScale(0.8f, 0.8f, 0.8f);
                    return;
                case "scale x-yz":
                    setScale(0.8f, 1.3f, 1.3f);
                    return;
                case "scale y-xz":
                    setScale(0.8f, 1.3f, 0.8f);
                    return;
                case "scale z-xy":
                    setScale(0.8f, 0.8f, 1.3f);
                    return;

                case "sweep":
                    sweep();
                    return;

                case "toggle view":
                    toggleMeshes();
                    togglePhysicsDebug();
                    return;
            }
        }
        super.onAction(actionString, ongoing, tpf);
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        /*
         * Set mouse-cursor shape based on a raytest result.
         */
        Vector2f screenXY = inputManager.getCursorPosition();
        Vector3f nearLocation = cam.getWorldCoordinates(screenXY, nearZ);
        Vector3f farLocation = cam.getWorldCoordinates(screenXY, farZ);

        PhysicsSpace physicsSpace = getPhysicsSpace();
        List rayTest = physicsSpace.rayTestRaw(nearLocation, farLocation);
        if (rayTest.size() > 0) {
            inputManager.setMouseCursor(hitCursor);
        } else {
            inputManager.setMouseCursor(missCursor);
        }

        updateStatusLines();
    }
    // *************************************************************************
    // PhysicsCollisionListener methods

    /**
     * Callback to report collisions in the PhysicsSpace. Invoked on the render
     * thread (not the physics thread) during the BulletAppState update.
     *
     * @param event the event that occurred (not null, reusable)
     */
    @Override
    public void collision(PhysicsCollisionEvent event) {
        Spatial userA = event.getNodeA();
        Spatial userB = event.getNodeB();
        if (userA == testSpatial && userB == missileSpatial
                || userA == missileSpatial && userB == testSpatial) {
            /*
             * Put the missile's RBC into kinematic mode, so it will stick.
             */
            RigidBodyControl rigidBodyControl
                    = missileSpatial.getControl(RigidBodyControl.class);
            rigidBodyControl.setKinematic(true);
            rigidBodyControl.setKinematicSpatial(true);

            Vector3f location = new Vector3f();
            if (userA == testSpatial) {
                event.getPositionWorldOnA(location);
            } else {
                event.getPositionWorldOnB(location);
            }
            missileSpatial.setLocalTranslation(location);
        } else {
            System.out.println("Unexpected collision!");
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Add a test shape to both the scene and PhysicsSpace.
     */
    private void addAShape() {
        switch (testName) {
            case "Box":
            case "BoxGImpact":
            case "BoxHull":
            case "BoxMesh":
            case "FourSphere":
                addBox();
                break;

            case "Cone":
            case "ConeGImpact":
            case "ConeHull":
            case "ConeMesh":
                addCone();
                break;

            case "Cylinder":
            case "CylinderGImpact":
            case "CylinderHull":
            case "CylinderMesh":
                addCylinder();
                break;

            case "KissCapsule":
            case "KissHull":
            case "KissMesh":
            case "KissMultiSphere":
            case "KissSphere":
            case "TwoSphere":
                addKiss();
                break;

            case "LargeTerrain":
                addLargeTerrain();
                break;

            case "Prism":
                addPrism();
                break;

            case "SmallTerrain":
            case "SmallTerrainVhacd":
                addSmallTerrain();
                break;

            case "OneSphere":
            case "Sphere":
            case "SphereCapsule":
            case "SphereGImpact":
            case "SphereHull":
            case "SphereMesh":
                addSphere();
                break;

            case "Square":
            case "SquareBox":
            case "SquareConvex2d":
            case "SquareGImpact":
            case "SquareHeightfield":
            case "SquareHull":
            case "SquareMesh":
                addSquare();
                break;

            case "Simplex":
            case "TetraGImpact":
            case "TetraHull":
            case "TetraMesh":
                addTetrahedron();
                break;

            default:
                throw new IllegalArgumentException(testName);
        }
    }

    /**
     * Add a box (rectangular solid) to the scene and PhysicsSpace.
     */
    private void addBox() {
        float halfThickness = 0.5f;
        float radius = 1.9f;
        AbstractBox mesh = new Box(radius, halfThickness, radius);
        testSpatial = new Geometry("box", mesh);

        switch (testName) {
            case "Box":
                testShape
                        = new BoxCollisionShape(radius, halfThickness, radius);
                break;

            case "BoxGImpact":
                testShape = new GImpactCollisionShape(mesh);
                break;

            case "BoxHull":
                testShape = new HullCollisionShape(mesh);
                break;

            case "BoxMesh":
                testShape = new MeshCollisionShape(mesh);
                break;

            case "FourSphere":
                RectangularSolid rs = new RectangularSolid(mesh);
                testShape = new MultiSphere(rs);
                break;

            default:
                throw new IllegalArgumentException(testName);
        }

        makeTestShape();
    }

    /**
     * Add a right circular cone to the scene and PhysicsSpace.
     */
    private void addCone() {
        boolean makePyramid = false;
        float height = 3f;
        float radius = 2f;
        int circularSamples = 64;
        Mesh mesh = new Cone(circularSamples, radius, height, makePyramid);
        testSpatial = new Geometry("cone", mesh);

        switch (testName) {
            case "Cone":
                testShape = new ConeCollisionShape(radius, height,
                        PhysicsSpace.AXIS_Y);
                break;

            case "ConeGImpact":
                testShape = new GImpactCollisionShape(mesh);
                break;

            case "ConeHull":
                testShape = new HullCollisionShape(mesh);
                break;

            case "ConeMesh":
                testShape = new MeshCollisionShape(mesh);
                break;

            default:
                throw new IllegalArgumentException(testName);
        }

        makeTestShape();
    }

    /**
     * Add a right circular cylinder to the scene and PhysicsSpace.
     */
    private void addCylinder() {
        boolean closedFlag = true;
        float height = 1f;
        float radius = 2f;
        int heightSample = 2;
        int circSample = 18;
        Mesh mesh = new Cylinder(heightSample, circSample, radius, height,
                closedFlag);
        testSpatial = new Geometry("cylinder", mesh);

        switch (testName) {
            case "Cylinder":
                testShape = new CylinderCollisionShape(radius, height,
                        PhysicsSpace.AXIS_Z);
                break;

            case "CylinderGImpact":
                testShape = new GImpactCollisionShape(mesh);
                break;

            case "CylinderHull":
                testShape = new HullCollisionShape(mesh);
                break;

            case "CylinderMesh":
                testShape = new MeshCollisionShape(mesh);
                break;

            default:
                throw new IllegalArgumentException(testName);
        }

        makeTestShape();
    }

    /**
     * Add a pair of kissing spheres to the scene and PhysicsSpace.
     */
    private void addKiss() {
        int numRefineSteps = 2;
        float radius = 1f;
        Mesh mesh = new Icosphere(numRefineSteps, radius);
        Spatial g1 = new Geometry("g1", mesh).move(radius, 0f, 0f);
        Spatial g2 = new Geometry("g2", mesh).move(-radius, 0f, 0f);
        Node node = new Node("kiss");
        node.attachChild(g1);
        node.attachChild(g2);
        testSpatial = node;

        switch (testName) {
            case "KissCapsule":
                testShape = new CapsuleCollisionShape(radius, 2f * radius,
                        PhysicsSpace.AXIS_X);
                break;

            case "KissHull":
                testShape = CollisionShapeFactory.createDynamicMeshShape(
                        testSpatial);
                break;

            case "KissMesh":
                testShape = CollisionShapeFactory.createMeshShape(testSpatial);
                break;

            case "KissMultiSphere":
                CollisionShape ms = new MultiSphere(radius);
                CompoundCollisionShape tms = new CompoundCollisionShape(2);
                tms.addChildShape(ms, radius, 0f, 0f);
                tms.addChildShape(ms, -radius, 0f, 0f);
                testShape = tms;
                break;

            case "KissSphere":
                CollisionShape s = new SphereCollisionShape(radius);
                CompoundCollisionShape ts = new CompoundCollisionShape(2);
                ts.addChildShape(s, radius, 0f, 0f);
                ts.addChildShape(s, -radius, 0f, 0f);
                testShape = ts;
                break;

            case "TwoSphere":
                testShape = new MultiSphere(radius, 2f * radius,
                        PhysicsSpace.AXIS_X);
                break;

            default:
                throw new IllegalArgumentException(testName);
        }

        makeTestShape();
    }

    /**
     * Add a 513x513 terrain to the scene and PhysicsSpace.
     */
    private void addLargeTerrain() {
        testSpatial = MinieTestTerrains.largeQuad;
        testShape = CollisionShapeFactory.createMeshShape(testSpatial);
        makeTestShape();
        Material greenMaterial = findMaterial("platform");
        testSpatial.setMaterial(greenMaterial);

        testShape = CollisionShapeFactory.createMeshShape(testSpatial);
        makeTestShape();
    }

    /**
     * Add lighting to the scene.
     */
    private void addLighting() {
        ColorRGBA ambientColor = new ColorRGBA(0.5f, 0.5f, 0.5f, 1f);
        AmbientLight ambient = new AmbientLight(ambientColor);
        rootNode.addLight(ambient);
        ambient.setName("ambient");

        Vector3f direction = new Vector3f(1f, -2f, -1f).normalizeLocal();
        DirectionalLight sun = new DirectionalLight(direction);
        rootNode.addLight(sun);
        sun.setName("sun");
    }

    /**
     * Add a petagonal prism to the scene and PhysicsSpace.
     */
    private void addPrism() {
        int numSides = 5;
        float radius = 2.5f;
        float thickness = 1f;
        boolean normals = true;
        Mesh mesh = new Prism(numSides, radius, thickness, normals);
        testSpatial = new Geometry("prism", mesh);

        testShape = CollisionShapeFactory.createDynamicMeshShape(testSpatial);
        makeTestShape();
    }

    /**
     * Add a missile to both the scene and PhysicsSpace.
     *
     * @return a new RigidBodyControl (enabled)
     */
    private RigidBodyControl addMissile() {
        assert missileSpatial == null;

        int numRefineSteps = 1;
        float radius = 0.05f;
        Mesh mesh = new Icosphere(numRefineSteps, radius);

        missileSpatial = new Geometry("missile", mesh);
        meshesNode.attachChild(missileSpatial);
        Material redMaterial = findMaterial("red");
        missileSpatial.setMaterial(redMaterial);

        CollisionShape shape = new MultiSphere(radius);
        // TODO - Why doesn't a SphereCollisionShape work well here?
        float mass = 0.01f;
        RigidBodyControl rbc = new RigidBodyControl(shape, mass);
        addCollisionObject(rbc);
        missileSpatial.addControl(rbc);

        rbc.setCcdMotionThreshold(0.01f);
        rbc.setCcdSweptSphereRadius(0.04f);

        return rbc;
    }

    /**
     * Add a 3x3 terrain to the scene and PhysicsSpace.
     */
    private void addSmallTerrain() {
        testSpatial = MinieTestTerrains.smallQuad;
        Material wireMaterial = findMaterial("green wire");
        assert wireMaterial != null;
        testSpatial.setMaterial(wireMaterial);

        switch (testName) {
            case "SmallTerrain":
                testShape = CollisionShapeFactory.createMeshShape(testSpatial);
                break;

            case "SmallTerrainVhacd":
                testShape = findShape("smallTerrainVhacd");
                break;

            default:
                throw new IllegalArgumentException(testName);
        }

        makeTestShape();
    }

    /**
     * Add a sphere to the scene and PhysicsSpace.
     */
    private void addSphere() {
        int numRefineSteps = 3;
        float radius = 2.5f;
        Mesh mesh = new Icosphere(numRefineSteps, radius);
        testSpatial = new Geometry("sphere", mesh);

        switch (testName) {
            case "OneSphere":
                testShape = new MultiSphere(radius);
                break;

            case "Sphere":
                testShape = new SphereCollisionShape(radius);
                break;

            case "SphereCapsule":
                float height = 0f;
                testShape = new CapsuleCollisionShape(radius, height);
                break;

            case "SphereGImpact":
                testShape = new GImpactCollisionShape(mesh);
                break;

            case "SphereHull":
                testShape = new HullCollisionShape(mesh);
                break;

            case "SphereMesh":
                testShape = new MeshCollisionShape(mesh);
                break;

            default:
                throw new IllegalArgumentException(testName);
        }

        makeTestShape();
    }

    /**
     * Add a square in the X-Y plane to the scene and PhysicsSpace.
     */
    private void addSquare() {
        float he = 1f;
        Mesh mesh = new RectangleMesh(-he, +he, -he, +he, 1f);
        testSpatial = new Geometry("square", mesh);

        switch (testName) {
            case "Square":
                testShape = new Box2dShape(he);
                break;

            case "SquareBox":
                testShape = new BoxCollisionShape(he, he, 0.04f);
                break;

            case "SquareConvex2d":
                FloatBuffer buffer
                        = mesh.getFloatBuffer(VertexBuffer.Type.Position);
                testShape = new Convex2dShape(buffer);
                break;

            case "SquareGImpact":
                testShape = new GImpactCollisionShape(mesh);
                break;

            case "SquareHeightfield":
                int stickLength = 3;
                int stickWidth = 3;
                float[] heightmap = new float[stickLength * stickWidth];
                Vector3f scale = Vector3f.UNIT_XYZ;
                int upAxis = PhysicsSpace.AXIS_Z;
                boolean flipQuadEdges = false;
                boolean flipTriangleWinding = false;
                boolean useDiamond = false;
                boolean useZigzag = false;
                testShape = new HeightfieldCollisionShape(stickLength,
                        stickWidth, heightmap, scale, upAxis, flipQuadEdges,
                        flipTriangleWinding, useDiamond, useZigzag);
                break;

            case "SquareHull":
                testShape = new HullCollisionShape(mesh);
                break;

            case "SquareMesh":
                testShape = new MeshCollisionShape(mesh);
                break;

            default:
                throw new IllegalArgumentException(testName);
        }

        makeTestShape();
    }

    /**
     * Add status lines to the GUI.
     */
    private void addStatusLines() {
        for (int lineIndex = 0; lineIndex < statusLines.length; ++lineIndex) {
            statusLines[lineIndex] = new BitmapText(guiFont, false);
            float y = cam.getHeight() - 20f * lineIndex;
            statusLines[lineIndex].setLocalTranslation(0f, y, 0f);
            guiNode.attachChild(statusLines[lineIndex]);
        }
    }

    /**
     * Add a regular tetrahedron to the scene and PhysicsSpace.
     */
    private void addTetrahedron() {
        float radius = 2.5f;
        Mesh mesh = new Tetrahedron(radius, true);
        testSpatial = new Geometry("tetrahedron", mesh);

        switch (testName) {
            case "Simplex":
                FloatBuffer buffer
                        = mesh.getFloatBuffer(VertexBuffer.Type.Position);
                VectorSet vs = MyBuffer.distinct(buffer, 0, buffer.limit());
                buffer = vs.toBuffer();
                testShape
                        = new SimplexCollisionShape(buffer, 0, buffer.limit());
                break;

            case "TetraGImpact":
                testShape = new GImpactCollisionShape(mesh);
                break;

            case "TetraHull":
                testShape = new HullCollisionShape(mesh);
                break;

            case "TetraMesh":
                testShape = new MeshCollisionShape(mesh);
                break;

            default:
                throw new IllegalArgumentException(testName);
        }

        makeTestShape();
    }

    /**
     * Advance the test selection by the specified amount and start the selected
     * test.
     *
     * @param amount the number of tests to advance the selection
     */
    private void advanceTest(int amount) {
        int index = Arrays.binarySearch(testNames, testName);
        if (index < 0) {
            testName = testNames[0];
        } else {
            assert testNames[index].equals(testName);
            index = MyMath.modulo(index + amount, testNames.length);
            testName = testNames[index];
        }

        restartTest();
    }

    /**
     * Cast a physics ray from the mouse pointer and move the PointVisualizer to
     * the nearest hit.
     */
    private void castRay() {
        Vector2f screenXY = inputManager.getCursorPosition();
        Vector3f nearLocation = cam.getWorldCoordinates(screenXY, nearZ);
        Vector3f farLocation = cam.getWorldCoordinates(screenXY, farZ);

        PhysicsSpace physicsSpace = getPhysicsSpace();
        List<PhysicsRayTestResult> rayTest
                = physicsSpace.rayTest(nearLocation, farLocation);

        if (rayTest.size() > 0) {
            PhysicsRayTestResult nearestHit = rayTest.get(0);
            float fraction = nearestHit.getHitFraction();
            Vector3f location = MyVector3f.lerp(fraction, nearLocation,
                    farLocation, null);

            hitPoint.setLocalTranslation(location);
            hitPoint.setEnabled(true);

            partIndex = nearestHit.partIndex();
            triangleIndex = nearestHit.triangleIndex();
        } else {
            hitPoint.setEnabled(false);
        }
    }

    /**
     * Delete all added shapes from the scene and PhysicsSpace. Also disable the
     * visualizer for hit points.
     */
    private void clearShapes() {
        hitPoint.setEnabled(false);
        partIndex = -1;
        triangleIndex = -1;
        /*
         * Remove any added spatials from the scene.
         */
        PhysicsSpace physicsSpace = getPhysicsSpace();
        physicsSpace.removeAll(meshesNode);
        meshesNode.detachAllChildren();
        missileSpatial = null;
        testSpatial = null;
        /*
         * Remove all collision objects from the PhysicsSpace,
         * which also removes their debug meshes.
         */
        Collection<PhysicsCollisionObject> pcos = physicsSpace.getPcoList();
        for (PhysicsCollisionObject pco : pcos) {
            physicsSpace.removeCollisionObject(pco);
        }
        assert physicsSpace.isEmpty();
    }

    /**
     * Configure the camera during startup.
     */
    private void configureCamera() {
        float near = 0.02f;
        float far = 20f;
        MyCamera.setNearFar(cam, near, far);

        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(4f);
        flyCam.setZoomSpeed(4f);

        cam.setLocation(new Vector3f(5.9f, 5.4f, 2.3f));
        cam.setRotation(new Quaternion(0.1825f, -0.76803f, 0.2501f, 0.56058f));

        CameraOrbitAppState orbitState
                = new CameraOrbitAppState(cam, "orbitLeft", "orbitRight");
        stateManager.attach(orbitState);
    }

    /**
     * Configure physics during startup.
     */
    private void configurePhysics() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        PhysicsSpace physicsSpace = getPhysicsSpace();
        physicsSpace.addCollisionListener(this);
        setGravityAll(0f);
    }

    /**
     * Initialize cursors during startup.
     */
    private void generateCursors() {
        String hitPath = "Textures/cursors/green.cur";
        hitCursor = (JmeCursor) assetManager.loadAsset(hitPath);

        String missPath = "Textures/cursors/default.cur";
        missCursor = (JmeCursor) assetManager.loadAsset(missPath);
    }

    /**
     * Launch a missile, its starting position and velocity determined by the
     * camera and mouse cursor.
     */
    private void launchMissile() {
        RigidBodyControl rbc;
        if (missileSpatial == null) {
            rbc = addMissile();
        } else {
            // Re-use the previously added missile.
            rbc = missileSpatial.getControl(RigidBodyControl.class);
        }
        rbc.setEnabled(false);
        rbc.setKinematic(false);
        rbc.setKinematicSpatial(false);

        Vector2f screenXY = inputManager.getCursorPosition();
        Vector3f nearLocation = cam.getWorldCoordinates(screenXY, nearZ);
        Vector3f farLocation = cam.getWorldCoordinates(screenXY, farZ);
        Vector3f direction
                = farLocation.subtract(nearLocation).normalizeLocal();
        float initialSpeed = 20f;
        Vector3f initialVelocity = direction.mult(initialSpeed);

        missileSpatial.setLocalTranslation(nearLocation);
        rbc.setEnabled(true);
        rbc.setPhysicsLocation(nearLocation);
        rbc.setLinearVelocity(initialVelocity);
    }

    /**
     * Convert the test shape and test spatial(s) into a kinematic
     * RigidBodyControl and add it to the main scene and also to the
     * PhysicsSpace.
     */
    private void makeTestShape() {
        meshesNode.attachChild(testSpatial);

        if (!(testSpatial instanceof TerrainQuad)) {
            List<Geometry> list
                    = MySpatial.listSpatials(testSpatial, Geometry.class, null);
            Material material = findMaterial("platform");
            assert material != null;
            for (Geometry geometry : list) {
                geometry.setMaterial(material);
            }
        }

        RigidBodyControl rbc = new RigidBodyControl(testShape);
        rbc.setApplyScale(true);
        rbc.setDebugMeshResolution(DebugShapeFactory.highResolution);
        rbc.setDebugNumSides(2);
        rbc.setKinematic(true);
        PhysicsSpace physicsSpace = getPhysicsSpace();
        rbc.setPhysicsSpace(physicsSpace);
        testSpatial.addControl(rbc);

        setScale(1f, 1f, 1f);
    }

    /**
     * Alter the collision margin for the test shape.
     *
     * @param factor the factor to increase the margin (&gt;0)
     */
    private void multiplyMargin(float factor) {
        assert factor > 0f : factor;

        float margin = testShape.getMargin();
        margin *= factor;
        testShape.setMargin(margin);
    }

    /**
     * Start a new test using the named shape.
     */
    private void restartTest() {
        clearShapes();
        addAShape();
    }

    /**
     * Alter the scale of the test spatial. Because the control is in kinematic
     * mode with setApplyScale(true), it will attempt to scale the test shape
     * accordingly.
     *
     * @param xScale desired X-axis scale factor (default=1)
     * @param yScale desired Y-axis scale factor (default=1)
     * @param zScale desired Z-axis scale factor (default=1)
     */
    private void setScale(float xScale, float yScale, float zScale) {
        if (testName.equals("LargeTerrain")) {
            testSpatial.setLocalScale(
                    xScale / 100f, yScale / 100f, zScale / 100f);
        } else {
            testSpatial.setLocalScale(xScale, yScale, zScale);
        }
    }

    /**
     * Sweep a sphere from the mouse pointer and move the PointVisualizer to the
     * nearest hit.
     */
    private void sweep() {
        float radius = 0.05f;
        CollisionShape shape = new SphereCollisionShape(radius);

        Vector2f screenXY = inputManager.getCursorPosition();

        Transform nearTransform = new Transform();
        Vector3f nearLocation = cam.getWorldCoordinates(screenXY, nearZ);
        nearTransform.setTranslation(nearLocation);

        Transform farTransform = new Transform();
        Vector3f farLocation = cam.getWorldCoordinates(screenXY, farZ);
        farTransform.setTranslation(farLocation);

        List<PhysicsSweepTestResult> sweepTest = new LinkedList<>();
        float penetration = 0f; // physics-space units
        PhysicsSpace physicsSpace = getPhysicsSpace();
        physicsSpace.sweepTest(shape, nearTransform, farTransform, sweepTest,
                penetration);

        if (sweepTest.size() > 0) {
            PhysicsSweepTestResult nearestHit = sweepTest.get(0);
            float fraction = nearestHit.getHitFraction();
            Vector3f fromLocation = nearTransform.getTranslation();
            Vector3f toLocation = farTransform.getTranslation();
            Vector3f location = MyVector3f.lerp(fraction, fromLocation,
                    toLocation, null);

            hitPoint.setLocalTranslation(location);
            hitPoint.setEnabled(true);

            partIndex = nearestHit.partIndex();
            triangleIndex = nearestHit.triangleIndex();
        } else {
            hitPoint.setEnabled(false);
        }
    }

    /**
     * Toggle rendering of the test/missile spatial(s).
     */
    private void toggleMeshes() {
        Spatial.CullHint hint = meshesNode.getLocalCullHint();
        if (hint == Spatial.CullHint.Inherit
                || hint == Spatial.CullHint.Never) {
            hint = Spatial.CullHint.Always;
        } else if (hint == Spatial.CullHint.Always) {
            hint = Spatial.CullHint.Never;
        }
        meshesNode.setCullHint(hint);
    }

    /**
     * Update the status lines in the GUI.
     */
    private void updateStatusLines() {
        int index = 1 + Arrays.binarySearch(testNames, testName);
        int count = testNames.length;
        String message
                = String.format("Test #%d of %d: %s", index, count, testName);
        statusLines[0].setText(message);

        String viewName;
        boolean debug = bulletAppState.isDebugEnabled();
        if (debug) {
            viewName = describePhysicsDebugOptions();
        } else {
            viewName = "Mesh";
        }
        if (areWorldAxesEnabled()) {
            viewName += "+WorldAxes";
        }

        float margin = testShape.getMargin();
        Vector3f scale = testShape.getScale(null);
        message = String.format("view=%s, margin=%.3f, scale=%s",
                viewName, margin, scale);
        if (partIndex >= 0) {
            message += String.format(", part[%d]", partIndex);
        }
        if (triangleIndex >= 0) {
            message += String.format(", tri[%d]", triangleIndex);
        }
        statusLines[1].setText(message);
    }
}
