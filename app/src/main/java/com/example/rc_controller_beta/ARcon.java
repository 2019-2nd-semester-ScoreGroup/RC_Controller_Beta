package com.example.rc_controller_beta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.rendering.ViewSizer;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;

public class ARcon extends AppCompatActivity {
    ArrayList<Float> arrayList1 = new ArrayList<>();
    ArrayList<Float> arrayList2 = new ArrayList<>();
    private ArFragment arFragment;
    private ImageButton option;
    private TextView txtDistance;
    private ModelRenderable start, end;
    private Anchor a1, a2;
    private AnchorNode an1, an2;
    private TransformableNode tfn1, tfn2;
    private Vector3 point1, point2;
    private int count = 0;
//    private Vector3 down_scaled = new Vector3((float) 0.1, (float) 0.1, (float) 0.1);
//    private ViewSizer vs = view -> down_scaled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcon);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        option = findViewById(R.id.option_button);
        txtDistance = findViewById(R.id.txtDistance);

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

                        Pose pose = a1.getPose();
                        if (arrayList1.isEmpty()) {
                            arrayList1.add(pose.tx());
                            arrayList1.add(pose.ty());
                            arrayList1.add(pose.tz());
                        }
                        // Create the transformable and add it to the anchor.
                        tfn1 = new TransformableNode(arFragment.getTransformationSystem());
                        tfn1.getScaleController().setMaxScale(0.7f);
                        tfn1.getScaleController().setMinScale(0.5f);
                        tfn1.setParent(an1);
                        tfn1.setRenderable(start);
                        Toast.makeText(getApplicationContext(), "RC 인식", Toast.LENGTH_SHORT).show();
                        txtDistance.setText("목적지를 설정하세요");
//                        start.setShadowReceiver(false);
//                        start.setShadowCaster(false);
                        tfn1.select();
                        count++;
                    }else if(count == 1){
                        if(an2 != null){
                            arFragment.getArSceneView().getScene().removeChild(an2);
                            arrayList2.clear();
                            a2 = null;
                            an2 = null;
                        }
                        int val = motionEvent.getActionMasked();
                        float axisVal = motionEvent.getAxisValue(MotionEvent.AXIS_X, motionEvent.getPointerId(motionEvent.getPointerCount() - 1));
                        Log.e("Values:", String.valueOf(val) + String.valueOf(axisVal));
                        // Create the Anchor.
                        a2 = hitResult.createAnchor();
                        an2 = new AnchorNode(a2);
                        an2.setParent(arFragment.getArSceneView().getScene());

                        Pose pose = a2.getPose();
                        arrayList2.add(pose.tx());
                        arrayList2.add(pose.ty());
                        arrayList2.add(pose.tz());
                        float d = getDistanceMeters(arrayList1, arrayList2);
                        txtDistance.setText("거리\n" + String.valueOf(d) + "m");
                        // Create the transformable and add it to the anchor.
                        tfn2 = new TransformableNode(arFragment.getTransformationSystem());
                        tfn2.getScaleController().setMaxScale(1.7f);
                        tfn2.getScaleController().setMinScale(1.5f);
                        tfn2.setParent(an2);
                        tfn2.setRenderable(end);
//                        end.setShadowReceiver(false);
//                        end.setShadowCaster(false);
                        tfn2.select();

                        point1 = an1.getWorldPosition();
                        point2 = an2.getWorldPosition();
                        final Vector3 difference = Vector3.subtract(point1, point2);
                        final Vector3 directionFromTopToBottom = difference.normalized();
                        final Quaternion rotationFromAToB =
                                Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
                        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new Color(0, 255, 244))
                                .thenAccept(
                                        material -> {
                                            ModelRenderable model = ShapeFactory.makeCube(
                                                    new Vector3(.01f, .01f, difference.length()),
                                                    Vector3.zero(), material);
                                            Node node = new Node();
                                            node.setParent(an2);
                                            node.setRenderable(model);
                                            node.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                                            node.setWorldRotation(rotationFromAToB);
                                        }
                                );
                    }
                });
    }

    private float getDistanceMeters(ArrayList<Float> arayList1, ArrayList<Float> arrayList2) {

        float distanceX = arayList1.get(0) - arrayList2.get(0);
        float distanceY = arayList1.get(1) - arrayList2.get(1);
        float distanceZ = arayList1.get(2) - arrayList2.get(2);
        return (float) Math.sqrt(distanceX * distanceX +
                distanceY * distanceY +
                distanceZ * distanceZ);
    }
}
