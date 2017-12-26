package jme3test.model.anim;

import com.jme3.anim.AnimComposer;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.util.AnimMigrationUtils;
import com.jme3.app.ChaseCameraAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.*;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.custom.ArmatureDebugAppState;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Nehon on 18/12/2017.
 */
public class TestAnimMigration extends SimpleApplication {

    ArmatureDebugAppState debugAppState;
    AnimComposer composer;
    Queue<String> anims = new LinkedList<>();
    boolean playAnim = false;

    public static void main(String... argv) {
        TestAnimMigration app = new TestAnimMigration();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setTimer(new EraseTimer());
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 100f);
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);
        rootNode.addLight(new DirectionalLight(new Vector3f(-1, -1, -1).normalizeLocal()));
        rootNode.addLight(new AmbientLight(ColorRGBA.DarkGray));

        //Spatial model = assetManager.loadModel("Models/Jaime/Jaime.j3o");
        //Spatial model = assetManager.loadModel("Models/Oto/Oto.mesh.xml").scale(0.2f).move(0, 1, 0);
        //Spatial model = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        Spatial model = assetManager.loadModel("Models/Elephant/Elephant.mesh.xml").scale(0.02f);

        AnimMigrationUtils.migrate(model);

        rootNode.attachChild(model);


        debugAppState = new ArmatureDebugAppState();
        stateManager.attach(debugAppState);

        setupModel(model);

        flyCam.setEnabled(false);

        Node target = new Node("CamTarget");
        //target.setLocalTransform(model.getLocalTransform());
        target.move(0, 1, 0);
        ChaseCameraAppState chaseCam = new ChaseCameraAppState();
        chaseCam.setTarget(target);
        getStateManager().attach(chaseCam);
        chaseCam.setInvertHorizontalAxis(true);
        chaseCam.setInvertVerticalAxis(true);
        chaseCam.setZoomSpeed(0.5f);
        chaseCam.setMinVerticalRotation(-FastMath.HALF_PI);
        chaseCam.setRotationSpeed(3);
        chaseCam.setDefaultDistance(3);
        chaseCam.setMinDistance(0.01f);
        chaseCam.setZoomSpeed(0.01f);
        chaseCam.setDefaultVerticalRotation(0.3f);

        initInputs();
    }

    public void initInputs() {
        inputManager.addMapping("toggleAnim", new KeyTrigger(KeyInput.KEY_RETURN));

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (isPressed) {
                    playAnim = !playAnim;
                    if (playAnim) {
                        String anim = anims.poll();
                        anims.add(anim);
                        composer.setCurrentAnimClip(anim);
                        System.err.println(anim);
                    } else {
                        composer.reset();
                    }
                }
            }
        }, "toggleAnim");
        inputManager.addMapping("nextAnim", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (isPressed && composer != null) {
                    String anim = anims.poll();
                    anims.add(anim);
                    composer.setCurrentAnimClip(anim);
                    System.err.println(anim);
                }
            }
        }, "nextAnim");
        inputManager.addMapping("toggleArmature", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (isPressed) {
                    debugAppState.setEnabled(!debugAppState.isEnabled());
                }
            }
        }, "toggleArmature");
    }

    private void setupModel(Spatial model) {
        if (composer != null) {
            return;
        }
        composer = model.getControl(AnimComposer.class);
        if (composer != null) {

            SkinningControl sc = model.getControl(SkinningControl.class);
            debugAppState.addArmatureFrom(sc);

            anims.clear();
            for (String name : composer.getAnimClipsNames()) {
                anims.add(name);
            }
            if (anims.isEmpty()) {
                return;
            }
            if (playAnim) {
                String anim = anims.poll();
                anims.add(anim);
                composer.setCurrentAnimClip(anim);
                System.err.println(anim);
            }

        } else {
            if (model instanceof Node) {
                Node n = (Node) model;
                for (Spatial child : n.getChildren()) {
                    setupModel(child);
                }
            }
        }

    }
}
