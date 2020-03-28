package com.example.rc_controller_beta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.rendering.ViewSizer;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ARcon extends AppCompatActivity {
    private ArFragment arFragment;
    private ImageButton option;
    private ModelRenderable start, end;
    private Anchor a1, a2;
    private AnchorNode an1, an2;
    private TransformableNode tfn1, tfn2;
    private int count = 0;
    private Vector3 down_scaled = new Vector3((float) 0.1, (float) 0.1, (float) 0.1);
    private ViewSizer vs = view -> down_scaled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcon);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        option = findViewById(R.id.option_button);

        option.setOnClickListener(v -> {
            Intent in = new Intent(getApplicationContext(), Option.class);
            startActivity(in);
        });

        ModelRenderable.builder()
                // To load as an asset from the 'assets' folder ('src/main/assets/andy.sfb'):
                .setSource(this, Uri.parse("for1.sfb"))
                // Instead, load as a resource from the 'res/raw' folder ('src/main/res/raw/andy.sfb'):
                //.setSource(this, R.raw.andy)
                .build()
                .thenAccept(renderable -> start = renderable)
                .exceptionally(
                        throwable -> {
                            Log.e("TAG", "Unable to load Renderable.", throwable);
                            return null;
                        });

        ModelRenderable.builder()
                // To load as an asset from the 'assets' folder ('src/main/assets/andy.sfb'):
                .setSource(this, Uri.parse("here.sfb"))
                // Instead, load as a resource from the 'res/raw' folder ('src/main/res/raw/andy.sfb'):
                //.setSource(this, R.raw.andy)
                .build()
                .thenAccept(renderable -> end = renderable)
                .exceptionally(
                        throwable -> {
                            Log.e("TAG", "Unable to load Renderable.", throwable);
                            return null;
                        });

//        ViewRenderable.builder()
//                .setView(this, R.layout.rc)
//                .setSizer(vs)
//                .build()
//                .thenAccept(renderable -> start = renderable);

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (start == null || end == null) {
                        return;
                    }

                    if(count == 0){
                        // Create the Anchor.
                        a1 = hitResult.createAnchor();
                        an1 = new AnchorNode(a1);
                        an1.setParent(arFragment.getArSceneView().getScene());
                        // Create the transformable and add it to the anchor.
                        tfn1 = new TransformableNode(arFragment.getTransformationSystem());
                        tfn1.getScaleController().setMaxScale(0.7f);
                        tfn1.getScaleController().setMinScale(0.5f);
                        tfn1.setParent(an1);
                        tfn1.setRenderable(start);
//                        start.setShadowReceiver(false);
//                        start.setShadowCaster(false);
                        tfn1.select();
                        count++;
                    }else if(count == 1){
                        if(an2 != null)
                            arFragment.getArSceneView().getScene().removeChild(an2);
                        // Create the Anchor.
                        a2 = hitResult.createAnchor();
                        an2 = new AnchorNode(a2);
                        an2.setParent(arFragment.getArSceneView().getScene());
                        // Create the transformable and add it to the anchor.
                        tfn2 = new TransformableNode(arFragment.getTransformationSystem());
                        tfn2.getScaleController().setMaxScale(1.7f);
                        tfn2.getScaleController().setMinScale(1.5f);
                        tfn2.setParent(an2);
                        tfn2.setRenderable(end);
//                        end.setShadowReceiver(false);
//                        end.setShadowCaster(false);
                        tfn2.select();
                    }
                });
    }

//    private void addLineBetweenPoints(Scene scene, Vector3 from, Vector3 to) {
//        // prepare an anchor position
//        Quaternion camQ = scene.getCamera().getWorldRotation();
//        float[] f1 = new float[]{to.x, to.y, to.z};
//        float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, camQ.w};
//        Pose anchorPose = new Pose(f1, f2);
//
//        // make an ARCore Anchor
//        Anchor anchor = mCallback.getSession().createAnchor(anchorPose);
//        // Node that is automatically positioned in world space based on the ARCore Anchor.
//        AnchorNode anchorNode = new AnchorNode(anchor);
//        anchorNode.setParent(scene);
//
//        // Compute a line's length
//        float lineLength = Vector3.subtract(from, to).length();
//
//        // Prepare a color
//        Color colorOrange = new Color(android.graphics.Color.parseColor("#ffa71c"));
//
//        // 1. make a material by the color
//        MaterialFactory.makeOpaqueWithColor(getContext(), colorOrange)
//                .thenAccept(material -> {
//                    // 2. make a model by the material
//                    ModelRenderable model = ShapeFactory.makeCylinder(0.0025f, lineLength,
//                            new Vector3(0f, lineLength / 2, 0f), material);
//                    model.setShadowReceiver(false);
//                    model.setShadowCaster(false);
//
//                    // 3. make node
//                    Node node = new Node();
//                    node.setRenderable(model);
//                    node.setParent(anchorNode);
//
//                    // 4. set rotation
//                    final Vector3 difference = Vector3.subtract(to, from);
//                    final Vector3 directionFromTopToBottom = difference.normalized();
//                    final Quaternion rotationFromAToB =
//                            Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
//                    node.setWorldRotation(Quaternion.multiply(rotationFromAToB,
//                            Quaternion.axisAngle(new Vector3(1.0f, 0.0f, 0.0f), 90)));
//                });
//    }
}
