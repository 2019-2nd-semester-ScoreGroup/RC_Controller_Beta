package com.example.rc_controller_beta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageButton;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.rendering.ViewSizer;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ARcon extends AppCompatActivity {
    private ArFragment arFragment;
    private ViewRenderable v;
    private ImageButton option;
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

        ViewRenderable.builder()
                .setView(this, R.layout.rc)
                .setSizer(vs)
                .build()
                .thenAccept(renderable -> v = renderable);

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (v == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable rc and add it to the anchor.
                    TransformableNode rc = new TransformableNode(arFragment.getTransformationSystem());
                    rc.setParent(anchorNode);
                    rc.setRenderable(v);
                    v.setShadowReceiver(false);
                    v.setShadowCaster(false);
                    rc.select();
                });
    }
}
