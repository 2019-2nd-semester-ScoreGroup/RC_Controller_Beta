package com.example.rc_controller_beta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rc_controller_beta.augmentedimage.AugmentedImageNode;
import com.example.rc_controller_beta.helpers.SnackbarHelper;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ARcon extends AppCompatActivity {
    private ArFragment arFragment;
    private ImageButton option, go, reset;
    private ImageView fixbox;
    private TextView txtDistance;
    private ModelRenderable Destination_ModelRenderable;
    private TransformableNode Destination_TransformableNode;
    private AnchorNode Destination_AnchorNode;
    private AugmentedImageNode RClocationNode;
    private long backKeyPressedTime = 0;
    private Toast toast;

//    private ModelRenderable start, end, box;
//    private TransformableNode tfn1, tfn2;
//    private AnchorNode an1, an2, dbar;
//    private Material custom;
//    private int count = 0;
//    private Vector3 down_scaled = new Vector3((float) 0.1, (float) 0.1, (float) 0.1);
//    private ViewSizer vs = view -> down_scaled;

    // Augmented image and its associated center pose anchor, keyed by the augmented image in the database.
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentedImageMap.isEmpty()) {
            fixbox.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcon);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        option = findViewById(R.id.option_button);
        go = findViewById(R.id.go_button);
        reset = findViewById(R.id.reset_button);
        fixbox = findViewById(R.id.fitbox_img);
        txtDistance = findViewById(R.id.txtDistance);

        go.setVisibility(View.INVISIBLE);
        reset.setVisibility(View.INVISIBLE);

        // rc 인식 리스너
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
        arFragment.getArSceneView().getScene().removeOnUpdateListener(this::onUpdateFrame);

        // 옵션 버튼 리스너
        option.setOnClickListener(v -> {
            Intent in = new Intent(getApplicationContext(), Option.class);
            startActivity(in);
        });

        // 이동 버튼 리스너
        go.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "go", Toast.LENGTH_SHORT).show();
        });

        // 초기화 버튼 리스너
        reset.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "reset", Toast.LENGTH_SHORT).show();
            go.setVisibility(View.INVISIBLE);
            reset.setVisibility(View.INVISIBLE);
            txtDistance.setText("RC를 인식하세요");
            augmentedImageMap.clear();
            if(RClocationNode != null){
                arFragment.getArSceneView().getScene().removeChild(RClocationNode);
                RClocationNode = null;
            }
            if(Destination_AnchorNode != null){
                arFragment.getArSceneView().getScene().removeChild(Destination_AnchorNode);
                Destination_AnchorNode = null;
            }
        });

        // 목적지 모델 렌더블 빌더
        ModelRenderable.builder()
                // To load as an asset from the 'assets' folder ('src/main/assets/andy.sfb'):
                .setSource(this, Uri.parse("end.sfb"))
                // Instead, load as a resource from the 'res/raw' folder ('src/main/res/raw/andy.sfb'):
                //.setSource(this, R.raw.andy)
                .build()
                .thenAccept(renderable -> Destination_ModelRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Log.e("TAG", "Unable to load Renderable.", throwable);
                            return null;
                        });

        //목적지 노드 터치 리스너
        Destination_TransformableNode = new TransformableNode(arFragment.getTransformationSystem());
        Destination_TransformableNode.setOnTouchListener((hitTestResult, motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_MOVE:{
                    // 월드 위치 재설정
                    Destination_AnchorNode = new AnchorNode();
                    Destination_AnchorNode.setWorldPosition(hitTestResult.getPoint());
                    Log.e("ju an2", Destination_AnchorNode.getWorldPosition().toString());
                    // 거리 계산
                    setDistance(RClocationNode.getWorldPosition(), Destination_AnchorNode.getWorldPosition());
                    // 각도 계산
                    setRotate(RClocationNode.getWorldPosition(), Destination_AnchorNode.getWorldPosition());
                    break;
                }
                default:
                    break;
            }
            return false;
        });

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if(augmentedImageMap.isEmpty())
                return;
            else {
                // 목적지가 설정되어 있으면 초기화
                if (Destination_AnchorNode != null) {
                    arFragment.getArSceneView().getScene().removeChild(Destination_AnchorNode);
                    Destination_AnchorNode = null;
                }
                setDestination(hitResult);
            }
        });
    }

    // rc 인식 프레임 리스너
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame, just return.
        if (frame == null) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    Log.e("trackingJu", "PAUSED");
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    txtDistance.setText("RC를 인식중입니다...");

//                    String text = "RC를 인식중입니다...";
//                    SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    Log.e("trackingJu", "TRACKING");
                    if(augmentedImageMap.isEmpty()){
                        Toast.makeText(getApplicationContext(), "RC 인식", Toast.LENGTH_SHORT).show();
                        txtDistance.setText("목적지를 인식하세요");
                        go.setVisibility(View.VISIBLE);
                        reset.setVisibility(View.VISIBLE);
                    }

                    // Have to switch to UI Thread to update View.
                    fixbox.setVisibility(View.GONE);

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        Log.e("trackingJu", " if (!augmentedImageMap.containsKey(augmentedImage))");
                        RClocationNode = new AugmentedImageNode(this);
                        RClocationNode.setImage(augmentedImage);
                        augmentedImageMap.put(augmentedImage, RClocationNode);
                        arFragment.getArSceneView().getScene().addChild(RClocationNode);
                    }
                    break;

                case STOPPED:
                    Log.e("trackingJu", "PAUSED");
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }

    // 목적지 생성
    private void setDestination(HitResult hitResult){
        // 목적지 노드 생성
        //a2 = hitResult.createAnchor();
        Destination_AnchorNode = new AnchorNode(hitResult.createAnchor());
        Destination_AnchorNode.setParent(arFragment.getArSceneView().getScene());
        Log.e("Log Destination_AnchorNode", Destination_AnchorNode.getWorldPosition().toString());
        // 목적지 모델 생성
        Destination_TransformableNode.getScaleController().setMaxScale(1.0f);
        Destination_TransformableNode.getScaleController().setMinScale(0.8f);
        Destination_TransformableNode.setParent(Destination_AnchorNode);
        Destination_TransformableNode.setRenderable(Destination_ModelRenderable);
        // 그림자 제거
        //end.setShadowReceiver(false);
        //end.setShadowCaster(false);
        Destination_TransformableNode.select();
        // 거리 계산
        setDistance(RClocationNode.getWorldPosition(), Destination_AnchorNode.getWorldPosition());
        // 각도 계산
        setRotate(RClocationNode.getWorldPosition(), Destination_AnchorNode.getWorldPosition());
    }

    // 거리 표시
    private void setDistance(Vector3 from, Vector3 to){
        float d = getDistanceMeters(from, to);
        txtDistance.setText("목적지까지\n" + String.format("%.2f", d) + "m");
    }

    // 거리 계산
    private float getDistanceMeters(Vector3 from, Vector3 to) {
        float distanceX = from.x - to.x;
        float distanceY = from.y - to.y;
        float distanceZ = from.z - to.z;
        return (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
    }

    // 각도 표시
    private void setRotate(Vector3 from, Vector3 to) {
        float rotate = getRotate(from, to);
        txtDistance.setText(txtDistance.getText().toString() + "/" + String.format("%.2f", rotate) + "°");
    }

    // 각도 계산
    private float getRotate(Vector3 from, Vector3 to){
        float dz = from.z - to.z;
        float dx = from.x - to.x;
        return (float) Math.toDegrees(Math.atan2(dz, dx));
    }

    // 뒤로가기 종료
    @Override
    public void onBackPressed() {
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
        }
    }
}

/**rc카 모델 렌더블 빌더*/
//        ModelRenderable.builder()
//                // To load as an asset from the 'assets' folder ('src/main/assets/andy.sfb'):
//                .setSource(this, Uri.parse("start.sfb"))
//                // Instead, load as a resource from the 'res/raw' folder ('src/main/res/raw/andy.sfb'):
//                //.setSource(this, R.raw.andy)
//                .build()
//                .thenAccept(renderable -> start = renderable)
//                .exceptionally(
//                        throwable -> {
//                            Log.e("TAG", "Unable to load Renderable.", throwable);
//                            return null;
//                        });

/** 거리 막대 속성 빌더*/
//        MaterialFactory.makeTransparentWithColor(this, new Color(0,255,255))
//                .thenAccept(material -> {
//                    material.setFloat4(MaterialFactory.MATERIAL_COLOR, new Color(0,100,100));
//                    material.setFloat(MaterialFactory.MATERIAL_METALLIC, 0);
//                    custom = material;
//                });

/** 뷰 렌더블 빌더*/
//        ViewRenderable.builder()
//                .setView(this, R.layout.rc)
//                .setSizer(vs)
//                .build()
//                .thenAccept(renderable -> start = renderable);

/**rc카 노드 터치 리스너*/
//        tfn1 = new TransformableNode(arFragment.getTransformationSystem());
//        tfn1.setOnTouchListener((hitTestResult, motionEvent) -> {
//            switch (motionEvent.getAction()){
//                case MotionEvent.ACTION_MOVE:{
//                    // 월드 위치 재설정
//                    an1 = new AnchorNode();
//                    an1.setWorldPosition(hitTestResult.getPoint());
//                    Log.e("ju an1", an1.getWorldPosition().toString());
//                    // 목적지 설정 시
//                    if(an2 != null){
//                        // 거리 계산
//                        setDistance(an2.getWorldPosition(), an1.getWorldPosition());
//                        // 거리 막대 생성
////                        drawDistanceBar(an2.getWorldPosition(), an1.getWorldPosition());
//                    }
//                    break;
//                }
//                default:
//                    break;
//            }
//            return false;
//        });

/**rc 생성*/
//    private void setRC(HitResult hitResult){
//        // rc카 노드 생성
//        //a1 = hitResult.createAnchor();
//        an1 = new AnchorNode(hitResult.createAnchor());
//        an1.setParent(arFragment.getArSceneView().getScene());
//        Log.e("ju an1", an1.getWorldPosition().toString());
//        // rc카 모델 생성
//        tfn1.getScaleController().setMaxScale(2.0f);
//        tfn1.getScaleController().setMinScale(1.8f);
//        tfn1.setParent(an1);
//        tfn1.setRenderable(start);
//        Toast.makeText(getApplicationContext(), "RC 인식", Toast.LENGTH_SHORT).show();
//        txtDistance.setText("목적지를 설정하세요");
//        // 그림자 제거
//        //start.setShadowReceiver(false);
//        //start.setShadowCaster(false);
//        tfn1.select();
//        count++;
//    }

/**거리 막대 그리기*/
//    private void drawDistanceBar(Vector3 from, Vector3 to){
//        if(dbar != null)
//            arFragment.getArSceneView().getScene().removeChild(dbar);
//        // 앵커 위치 설정
//        Quaternion camQ = arFragment.getArSceneView().getScene().getCamera().getWorldRotation();
//        float[] f1 = new float[]{to.x, to.y, to.z};
//        float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, camQ.w};
//        Pose pose = new Pose(f1, f2);
//        // 거리 막대 앵커노드 생성
//        dbar = new AnchorNode(arFragment.getArSceneView().getSession().createAnchor(pose));
//        dbar.setParent(arFragment.getArSceneView().getScene());
//        // 벡터 간 길이로 거리 추출
//        float lineLength = Vector3.subtract(from, to).length();
//        // rc와 목적지 간 막대 생성
//        box = ShapeFactory.makeCylinder(0.005f, lineLength, new Vector3(0f, lineLength / 2, 0f), custom);
//        box.setShadowReceiver(false);
//        box.setShadowCaster(false);
//        // 노드 할당
//        Node node = new Node();
//        node.setRenderable(box);
//        node.setParent(dbar);
//        // 노드 위치 설정
//        final Vector3 difference = Vector3.subtract(to, from);
//        final Vector3 directionFromTopToBottom = difference.normalized();
//        final Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
//        node.setWorldRotation(Quaternion.multiply(rotationFromAToB, Quaternion.axisAngle(new Vector3(1.0f, 0.0f, 0.0f), 90)));
//    }
