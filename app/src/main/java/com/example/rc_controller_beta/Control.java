package com.example.rc_controller_beta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class Control extends AppCompatActivity {
    Client c = Client.getInstance();
    private Button exit, apply;
    private ImageButton option;
    private TextView net, event, degree, doTime, server;
    private Switch directionSwitch;
    private long time= 0;
    private String directionText = "F";

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

    @Override
    protected void onResume() {
        super.onResume();
        if(Option.get_connect()) {
            net.setText("Network: ON");

            exit.setOnClickListener(v -> {
                net.setText("Network: OFF");
                event.setText("EventLog");
                if(!c.SockLife()){
                    c.PushMsg("E");
                    c.CloseSock();
                } else
                    Option.connect_false();
            });

            directionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        directionText = "F";
                    directionText = "B";
                }
            });

            apply.setOnClickListener(v -> {
                String degreeText = (String) degree.getText();
                String timeText = (String) doTime.getText();

                if(!"".equals(degreeText)) {
                    Toast.makeText(getApplicationContext(), "degree를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(Integer.parseInt(degreeText) < 30 || Integer.parseInt(degreeText) > 120) {
                    Toast.makeText(getApplicationContext(), "degree는 30 ~ 120사이여야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!"".equals(timeText)) {
                    Toast.makeText(getApplicationContext(), "time을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                c.PushMsg(degreeText + " " + timeText + " " + directionText);
            });
        }else{
            net.setText("Network: OFF");
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

        apply = findViewById(R.id.apply);
        exit = findViewById(R.id.exitButton);
        option = findViewById(R.id.option_button);
        net = findViewById(R.id.net_state);
        event = findViewById(R.id.event_log);
        server = findViewById(R.id.event_server);
        degree = findViewById(R.id.degree);
        doTime = findViewById(R.id.doTime);

        option.setOnClickListener(v -> {
            Intent in = new Intent(getApplicationContext(), Option.class);
            startActivity(in);
        });
    }

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
