package com.example.rc_controller_beta;

import android.util.Log;

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
    private Socket sock = null;
    private BufferedReader in = null;        //Server로부터 데이터를 읽어들이기 위한 입력스트림
    private PrintWriter out = null;            //서버로 내보내기 위한 출력 스트림
    private String line = null;
    /**클라이언트 객체는 싱글톤으로 구현*/
    private Client(){}
    public static Client getInstance(){
        if(c == null)
            c = new Client();
        return c;
    }
    /**서버 연결 설정*/
    public void connection(String IP, int PORT){
        new Thread(()->{
            try {
                InetSocketAddress sock_address = new InetSocketAddress(IP, PORT);
                sock = new Socket();
                sock.connect(sock_address, 2000);
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())));
                ReadThread();
                Option.connect_true();
            } catch (IOException e) {
                sock = null;
                Option.connect_false();
                Log.i("ju", e.getLocalizedMessage());
            }
        }).start();
    }
    /**서버에 메시지 보내기*/
    public void PushMsg(String msg){
        new Thread(()->{
            if(sock == null || sock.isClosed())
                return;
            out.println(msg);                        //서버로 데이터 전송
            out.flush();
        }).start();
        Log.i("ju", msg);
    }
    /**서버에 메시지 받기*/
    public void ReadThread(){
        new Thread(()->{
            while(true){
                if(sock == null || sock.isClosed())
                    break;
                else{
                    try {
                        line = in.readLine();                //Client로부터 데이터를 읽어옴
                        Log.i("ju", line);
                        if(line.equals("C"))
                            PushMsg("C");
                    } catch (IOException e) {
                        Log.i("ju", e.getLocalizedMessage());
                        break;
                    }
                }
            }
        }).start();
    }
    /**소켓 상태 반환*/
    public boolean SockLife(){
        if(sock == null || sock.isClosed())
            return true;
        else
            return false;
    }
    /**소켓 클로즈*/
    public void CloseSock(){
        new Thread(()->{
            try {
                Option.connect_false();
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
