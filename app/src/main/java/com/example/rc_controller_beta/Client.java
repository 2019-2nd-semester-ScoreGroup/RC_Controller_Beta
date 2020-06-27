package com.example.rc_controller_beta;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private static Client c;
    private RL callback;
    private Socket sock = null;
    private BufferedReader in = null;        //Server로부터 데이터를 읽어들이기 위한 입력스트림
    private PrintWriter out = null;            //서버로 내보내기 위한 출력 스트림
    private String line = null;
    private Context context;
    public boolean connect = false;

    private Client(){}

    public static Client getInstance(){
        if(c == null)
            c = new Client();
        return c;
    }

    public Client setOnReceiveListener(RL callback) {
        this.callback = callback;
        return this;
    }

    public boolean getWIFI(){
        return connect;
    }

    public void connection(String IP, int PORT, Context context){
        this.context = context;
        new Thread(()->{
            try {
                InetSocketAddress sock_address = new InetSocketAddress(IP, PORT);
                sock = new Socket();
                sock.connect(sock_address, 2000);
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())));
                ReadThread();
                connect = true;
                callback.onReceive(this);
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.post(()-> Toast.makeText(context, "RC와 통신 성공", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                sock = null;
                connect = false;
                callback.onReceive(this);
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.post(()-> Toast.makeText(context, "RC와 통신 실패", Toast.LENGTH_SHORT).show());
                Log.i("ju", e.getLocalizedMessage());
            }
        }).start();
    }

    public void PushMsg(String msg){
        new Thread(()->{
            if(!sock.isConnected() || sock.isClosed()){
                CloseSock();
                return;
            }
            out.println(msg);                        //서버로 데이터 전송
            out.flush();
        }).start();
        Log.i("ju", msg);
    }

    public void ReadThread(){
        new Thread(()->{
            while(true){
                if(!sock.isConnected() || sock.isClosed()){
                    CloseSock();
                    return;
                }
                else{
                    try {
                        line = in.readLine();                //Client로부터 데이터를 읽어옴
                        Log.i("ju", line);
                        if(line.equals("C"))
                            PushMsg("C");
                        if(line == null)
                            CloseSock();
                    } catch (IOException e) {
                        Log.i("ju", e.getLocalizedMessage());
                        CloseSock();
                        break;
                    }
                }
            }
        }).start();
    }

    public void CloseSock(){
        new Thread(()->{
            try {
                connect = false;
                sock.close();
                sock = null;
                callback.onReceive(this);
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.post(()-> Toast.makeText(context, "RC와 통신이 해제되었습니다", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
