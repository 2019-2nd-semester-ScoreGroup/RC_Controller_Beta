package com.example.rc_controller_beta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Option extends AppCompatActivity {
    Client c = Client.getInstance();
    private Button b1;
    private EditText ip, port;
    private TextView v1, v2;
    private String IP = "", PORT = "";

    /**쉐얼드 프리퍼런스 아이피, 패스워드 저장*/
    private void SaveIPPW(String ip, String port){
        SharedPreferences pref = getSharedPreferences("temp", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("IP", ip);
        editor.putString("PORT", port);
        editor.commit();
    }

    /**쉐얼드 프리퍼런스 아이피, 패스워드 불러오기*/
    private void ImportIPPW(){
        SharedPreferences pref = getSharedPreferences("temp", MODE_PRIVATE);
        IP = pref.getString("IP", "localhost");
        PORT = pref.getString("PORT", "8080");
        set_ipport();
    }

    private void set_ipport(){
        v1.setText(IP);
        v2.setText(PORT);
        ip.setText(IP);
        port.setText(PORT);
    }

    private void set_edit(){
        IP = ip.getText().toString();
        PORT = port.getText().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_option);
        b1 = findViewById(R.id.ipport_button);
        ip = findViewById(R.id.ip_edit);
        port = findViewById(R.id.port_edit);
        v1 = findViewById(R.id.ip_view);
        v2 = findViewById(R.id.port_view);
        ImportIPPW();

        b1.setOnClickListener(v -> {
            set_edit();
            if(IP.equals("") || PORT.equals(""))
                Toast.makeText(getApplicationContext(), "잘못된 입력입니다!", Toast.LENGTH_SHORT).show();
            else {
                set_ipport();
                SaveIPPW(IP, PORT);
            }
            c.connection(IP, Integer.parseInt((PORT)), this);
        });
    }
}
