package com.example.rc_controller_beta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class Control extends AppCompatActivity {
    Button exit;
    SeekBar speedBar;
    ImageButton go, back, left, right, option;
    TextView net, event, server;
    private long time= 0;

    public void onBackPressed(){
        if(System.currentTimeMillis()-time>=2000){
            time=System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르면 종료합니다.",Toast.LENGTH_SHORT).show();
        }else if(System.currentTimeMillis()-time<2000){
            if(Option.c.sock != null){
                Option.c.PushMsg("E");
                Option.connect = false;
                Option.c.CloseSock();
            }
            ActivityCompat.finishAffinity(this);
            System.exit(0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_control_portrait);
        } else {
            setContentView(R.layout.activity_control);
        }

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
                    if(progress == 100)
                        speed = "00000100";
                    else if(progress < 10)
                        speed = "0000000" + progress;
                    else
                        speed = "000000" + progress;

                    Option.c.PushMsg(speed);
                }
            });

            exit.setOnClickListener(v -> {
                event.setText("exit...");
                Option.c.PushMsg("E");
                Option.connect = false;
                if(Option.c.sock != null)
                    Option.c.CloseSock();
                Intent in = new Intent(this, MainActivity.class);
                startActivity(in);
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
