package com.example.rc_controller_beta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class Control extends AppCompatActivity {
    Button exit;
    SeekBar speedBar;

    ImageButton go, back, left, right, option;
    TextView net, event, server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_control);

        speedBar = (SeekBar)findViewById(R.id.speedValue);
        exit = findViewById(R.id.exitButton);
        go = findViewById(R.id.up_img);
        back = findViewById(R.id.down_img);
        left = findViewById(R.id.left_img);
        right = findViewById(R.id.right_img);
        option = findViewById(R.id.option_button);
        net = findViewById(R.id.net_state);
        event = findViewById(R.id.event_log);
        server = findViewById(R.id.event_server);

        option.setOnClickListener(v -> {
            Intent in = new Intent(getApplicationContext(), Option.class);
            startActivity(in);
        });

        if(Option.connect) {
            net.setText("Network: ON");

            speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    String speed;

                    speed = Integer.toString(progress);

                    Option.c.PushMsg(speed);
                }
            });

            exit.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    event.setText("exit...");
                    Option.c.PushMsg("E");
                    if(Option.c.sock != null)
                        Option.c.CloseSock();
                }
                return false;
            });

            go.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // 버튼을 눌렀을 때
                    event.setText("F");
                    Option.c.PushMsg("F");

                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // 버튼에서 손을 떼었을 때
                    event.setText("N");
                    Option.c.PushMsg("N");
                }
                return false;
            });

            back.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // 버튼을 눌렀을 때
                    event.setText("B");
                    Option.c.PushMsg("B");
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // 버튼에서 손을 떼었을 때
                    event.setText("N");
                    Option.c.PushMsg("N");
                }
                return false;
            });

            left.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // 버튼을 눌렀을 때
                    event.setText("L");
                    Option.c.PushMsg("L");
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // 버튼에서 손을 떼었을 때
                    event.setText("N");
                    Option.c.PushMsg("N");
                }
                return false;
            });

            right.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // 버튼을 눌렀을 때
                    event.setText("R");
                    Option.c.PushMsg("R");
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // 버튼에서 손을 떼었을 때
                    event.setText("N");
                    Option.c.PushMsg("N");
                }
                return false;
            });
        }else{
            net.setText("Network: OFF");
        }
    }
}
