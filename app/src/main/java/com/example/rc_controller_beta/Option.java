package com.example.rc_controller_beta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Option extends AppCompatActivity {
    Button b1, b2, b3;
    EditText ip, port;
    TextView v1, v2;
    public static Client c = new Client();
    public static String IP = "", PORT = "";
    public static boolean connect = false;

    public void set_ipport(){
        v1.setText(IP);
        v2.setText(PORT);
        ip.setText(IP);
        port.setText(PORT);
    }

    public void set_edit(){
        IP = ip.getText().toString();
        PORT = port.getText().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_option);
        b1 = findViewById(R.id.ipport_button);
        b2 = findViewById(R.id.developer_button);
        b3 = findViewById(R.id.back_button);
        ip = findViewById(R.id.ip_edit);
        port = findViewById(R.id.port_edit);
        v1 = findViewById(R.id.ip_view);
        v2 = findViewById(R.id.port_view);
        set_ipport();

        b1.setOnClickListener(v -> {
            set_edit();
            if(IP.equals("") && PORT.equals("")){
                IP = "172.30.1.28";
                PORT = "8888";
                set_ipport();
            }else if(IP.equals("") || PORT.equals(""))
                Toast.makeText(getApplicationContext(), "잘못된 입력입니다!", Toast.LENGTH_SHORT).show();
            else {
                set_ipport();
            }
            c.connection(IP, Integer.parseInt((PORT)));
        });

        b2.setOnClickListener(v -> {
            if(connect == false){
                connect = true;
                Toast.makeText(getApplicationContext(), "Connection = ON", Toast.LENGTH_SHORT).show();
            }else{
                connect = false;
                Toast.makeText(getApplicationContext(), "Connection = OFF", Toast.LENGTH_SHORT).show();
            }
        });

        b3.setOnClickListener(v -> {
            Intent in = new Intent(getApplicationContext(), Control.class);
            startActivity(in);
        });
    }
}
