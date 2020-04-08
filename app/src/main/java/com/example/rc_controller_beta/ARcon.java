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

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.rendering.ViewSizer;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ARcon extends AppCompatActivity {
    private ArFragment arFragment;
    private ImageButton option;
    private TextView txtDistance;
    private ModelRenderable start, end, box;
    private TransformableNode tfn1, tfn2;
    private AnchorNode an1, an2, dbar;
    private Material custom;
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
        // 옵션 버튼 리스너
        option.setOnClickListener(v -> {
            Intent in = new Intent(getApplicationContext(), Option.class);
            startActivity(in);
        });
        // rc카 모델 렌더블 빌더
        ModelRenderable.builder()
                // To load as an asset from the 'assets' folder ('src/main/assets/andy.sfb'):
                .setSource(this, Uri.parse("start.sfb"))
                // Instead, load as a resource from the 'res/raw' folder ('src/main/res/raw/andy.sfb'):
                //.setSource(this, R.raw.andy)
                .build()
                .thenAccept(renderable -> start = renderable)
                .exceptionally(
                        throwable -> {
                            Log.e("TAG", "Unable to load Renderable.", throwable);
                            return null;
                        });
        // 목적지 모델 렌더블 빌더
        ModelRenderable.builder()
                // To load as an asset from the 'assets' folder ('src/main/assets/andy.sfb'):
                .setSource(this, Uri.parse("end.sfb"))
                // Instead, load as a resource from the 'res/raw' folder ('src/main/res/raw/andy.sfb'):
                //.setSource(this, R.raw.andy)
                .build()
                .thenAccept(renderable -> end = renderable)
                .exceptionally(
                        throwable -> {
                            Log.e("TAG", "Unable to load Renderable.", throwable);
                            return null;
                        });
        // 거리 막대 속성 빌더
        MaterialFactory.makeTransparentWithColor(this, new Color(0,255,255))
                .thenAccept(material -> {
                    material.setFloat4(MaterialFactory.MATERIAL_COLOR, new Color(0,100,100));
                    material.setFloat(MaterialFactory.MATERIAL_METALLIC, 0);
                    custom = material;
                });
        // 뷰 렌더블 빌더
//        ViewRenderable.builder()
//                .setView(this, R.layout.rc)
//                .setSizer(vs)
//                .build()
//                .thenAccept(renderable -> start = renderable);
        //rc카 노드 터치 리스너
        tfn1 = new TransformableNode(arFragment.getTransformationSystem());
        tfn1.setOnTouchListener((hitTestResult, motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_MOVE:{
                    // 월드 위치 재설정
                    an1 = new AnchorNode();
                    an1.setWorldPosition(hitTestResult.getPoint());
                    Log.e("ju an1", an1.getWorldPosition().toString());
                    // 목적지 설정 시
                    if(an2 != null){
                        // 거리 계산
                        setDistance(an2.getWorldPosition(), an1.getWorldPosition());
                        // 거리 막대 생성
//                        drawDistanceBar(an2.getWorldPosition(), an1.getWorldPosition());
                    }
                    break;
                }
                default:
                    break;
            }
            return false;
        });
        //목적지 노드 터치 리스너
        tfn2 = new TransformableNode(arFragment.getTransformationSystem());
        tfn2.setOnTouchListener((hitTestResult, motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_MOVE:{
                    // 월드 위치 재설정
                    an2 = new AnchorNode();
                    an2.setWorldPosition(hitTestResult.getPoint());
                    Log.e("ju an2", an2.getWorldPosition().toString());
                    // 거리 계산
                    setDistance(an1.getWorldPosition(), an2.getWorldPosition());
                    // 거리 막대 생성
//                    drawDistanceBar(an1.getWorldPosition(), an2.getWorldPosition());
                    break;
                }
                default:
                    break;
            }
            return false;
        });
        // ar 공간 탭 리스너
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (start == null || end == null) {
                        return;
                    }
                    // 첫 번째 터치시
                    if(count == 0){
                        setRC(hitResult);
                    // 두 번째 터치시
                    }else {
                        // 목적지가 설정되어 있으면 초기화
                        if (an2 != null) {
                            arFragment.getArSceneView().getScene().removeChild(an2);
                            an2 = null;
                        }
                        setDestination(hitResult);
                    }
                });
    }

    private void setRC(HitResult hitResult){
        // rc카 노드 생성
        //a1 = hitResult.createAnchor();
        an1 = new AnchorNode(hitResult.createAnchor());
        an1.setParent(arFragment.getArSceneView().getScene());
        Log.e("ju an1", an1.getWorldPosition().toString());
        // rc카 모델 생성
        tfn1.getScaleController().setMaxScale(2.0f);
        tfn1.getScaleController().setMinScale(1.8f);
        tfn1.setParent(an1);
        tfn1.setRenderable(start);
        Toast.makeText(getApplicationContext(), "RC 인식", Toast.LENGTH_SHORT).show();
        txtDistance.setText("목적지를 설정하세요");
        // 그림자 제거
        //start.setShadowReceiver(false);
        //start.setShadowCaster(false);
        tfn1.select();
        count++;
    }

    private void setDestination(HitResult hitResult){
        // 목적지 노드 생성
        //a2 = hitResult.createAnchor();
        an2 = new AnchorNode(hitResult.createAnchor());
        an2.setParent(arFragment.getArSceneView().getScene());
        Log.e("ju an2", an2.getWorldPosition().toString());
        // 목적지 모델 생성
        tfn2.getScaleController().setMaxScale(2.0f);
        tfn2.getScaleController().setMinScale(1.8f);
        tfn2.setParent(an2);
        tfn2.setRenderable(end);
        // 그림자 제거
        //end.setShadowReceiver(false);
        //end.setShadowCaster(false);
        tfn2.select();
        // 거리 계산
        setDistance(an1.getWorldPosition(), an2.getWorldPosition());
        // 거리 막대 생성
//        drawDistanceBar(an1.getWorldPosition(), an2.getWorldPosition());
    }

    private void drawDistanceBar(Vector3 from, Vector3 to){
        if(dbar != null)
            arFragment.getArSceneView().getScene().removeChild(dbar);
        // 앵커 위치 설정
        Quaternion camQ = arFragment.getArSceneView().getScene().getCamera().getWorldRotation();
        float[] f1 = new float[]{to.x, to.y, to.z};
        float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, camQ.w};
        Pose pose = new Pose(f1, f2);
        // 거리 막대 앵커노드 생성
        dbar = new AnchorNode(arFragment.getArSceneView().getSession().createAnchor(pose));
        dbar.setParent(arFragment.getArSceneView().getScene());
        // 벡터 간 길이로 거리 추출
        float lineLength = Vector3.subtract(from, to).length();
        // rc와 목적지 간 막대 생성
        box = ShapeFactory.makeCylinder(0.005f, lineLength, new Vector3(0f, lineLength / 2, 0f), custom);
        box.setShadowReceiver(false);
        box.setShadowCaster(false);
        // 노드 할당
        Node node = new Node();
        node.setRenderable(box);
        node.setParent(dbar);
        // 노드 위치 설정
        final Vector3 difference = Vector3.subtract(to, from);
        final Vector3 directionFromTopToBottom = difference.normalized();
        final Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
        node.setWorldRotation(Quaternion.multiply(rotationFromAToB, Quaternion.axisAngle(new Vector3(1.0f, 0.0f, 0.0f), 90)));
    }

    private void setDistance(Vector3 from, Vector3 to){
        // 거리 계산
        float d = getDistanceMeters(from, to);
        Log.e("ju getDistanceMeters ", an1.getWorldPosition().toString() + " / " + an2.getWorldPosition().toString());
        // 거리 표시
        txtDistance.setText("거리\n" + String.valueOf(d) + "m");
    }

    private float getDistanceMeters(Vector3 from, Vector3 to) {
        float distanceX = from.x - to.x;
        float distanceY = from.y - to.y;
        float distanceZ = from.z - to.z;
        return (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
    }
}
