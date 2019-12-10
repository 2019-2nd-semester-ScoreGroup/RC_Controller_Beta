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
    Client c = Client.getInstance();
    private Button exit;
    private ImageButton go, back, left, right, option;
    private TextView net, event;
    private SeekBar speedBar;
    private long time= 0;

    /**뒤로가기 2번 시 앱 종료*/
    public void onBackPressed(){
        if(System.currentTimeMillis()-time>=2000){
            time=System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르면 종료합니다.",Toast.LENGTH_SHORT).show();
        }else if(System.currentTimeMillis()-time<2000){
            if(!c.SockLife()){
                c.PushMsg("E");
                c.CloseSock();
            } else
                Option.connect_false();
            ActivityCompat.finishAffinity(this);
            System.exit(0);
        }
    }

    /**액티비티 전환 시 실행*/
    @Override
    protected void onResume() {
        super.onResume();
        /**서버와 연결 시*/
        if(Option.get_connect()) {
            net.setText("Network: ON");
            /**스피드바 리스너*/
            speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    String speed;

                    speed = Integer.toString(progress);

                    c.PushMsg(speed);
                }
            });
            /**연결 해제 리스너*/
            exit.setOnClickListener(v -> {
                net.setText("Network: OFF");
                event.setText("EventLog");
                if(!c.SockLife()){
                    c.PushMsg("E");
                    c.CloseSock();
                } else
                    Option.connect_false();
            });
            /**전진 버튼 리스너*/
            go.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    /**버튼을 눌렀을 때*/
                    event.setText("F");
                    c.PushMsg("F");
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    /**버튼에서 손을 떼었을 때*/
                    event.setText("N");
                    c.PushMsg("N");
                }
                return false;
            });
            /**후진 버튼 리스너*/
            back.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    /**버튼을 눌렀을 때*/
                    event.setText("B");
                    c.PushMsg("B");
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    /**버튼에서 손을 떼었을 때*/
                    event.setText("N");
                    c.PushMsg("N");
                }
                return false;
            });
            /**좌향 버튼 리스너*/
            LR L_Run = new LR("L");
            left.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // 버튼을 누르고 있을 때
                    event.setText("L");
                    L_Run.ing_true();
                    new Thread(L_Run).start();
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // 버튼에서 손을 떼었을 때
                    L_Run.ing_false();
                }
                return false;
            });
            /**우향 버튼 리스너*/
            LR R_Run = new LR("R");
            right.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // 버튼을 누르고 있을 때
                    event.setText("R");
                    R_Run.ing_true();
                    new Thread(R_Run).start();
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // 버튼에서 손을 떼었을 때
                    R_Run.ing_false();
                }
                return false;
            });
        }else{
            net.setText("Network: OFF");
        }
    }

    /**앱 생성 시 실행*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_control_portrait);
        } else {
            setContentView(R.layout.activity_control);
        }
        /**레이아웃 객체 생성*/
        speedBar = (SeekBar)findViewById(R.id.speedValue);
        exit = findViewById(R.id.exitButton);
        go = findViewById(R.id.up_img);
        back = findViewById(R.id.down_img);
        left = findViewById(R.id.left_img);
        right = findViewById(R.id.right_img);
        option = findViewById(R.id.option_button);
        net = findViewById(R.id.net_state);
        event = findViewById(R.id.event_log);
        /**옵션 액티비티로 전환*/
        option.setOnClickListener(v -> {
            Intent in = new Intent(getApplicationContext(), Option.class);
            startActivity(in);
        });
    }

    /**좌향과 우향 지속 신호 전송을 위한 런어블 클래스*/
    private class LR implements Runnable {
        private String s;
        private boolean ing = true;

        public LR(String s){
            this.s = s;
        }

        public void ing_true(){
            ing = true;
        }

        public void ing_false(){
            ing = false;
        }

        @Override
        public void run() {
            while (ing){
                c.PushMsg(s);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
