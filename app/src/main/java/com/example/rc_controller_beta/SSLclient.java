package com.example.rc_controller_beta;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.*;

import javax.net.ssl.*;

public class SSLclient {
    private Context con;
    private String ip;
    private int port;

    public SSLclient(Context con, String ip, int port){
        this.con = con;
        this.ip = ip;
        this.port = port;
    }

    public boolean Connection(){
        try {
            // 키스토어 설정.
            KeyStore keystore = KeyStore.getInstance("BKS");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            InputStream trustStoreStream = con.getResources().openRawResource(R.raw.client);
            keystore.load(trustStoreStream, "12142".toCharArray()); // BKS 파일, 비밀번호 설정.
            tmf.init(keystore);

            // 키스토어 사용을 위해 SSL context 설정.
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            context.init(null, trustManagers, null);

            // 클라이언트 소켓 팩토리 생성.
            SSLSocketFactory sslsocketfactory = context.getSocketFactory();

            // 클라이언트 소켓 생성.
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);

            // SSL RSA 통신을 통해 서버에 로그인 시도.
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream())));
            out.println("OK");
            out.flush();
            Log.i("ju", "SSL 로그인 성공");

            // 로그인 성공 시 true
            return true;
        } catch (Exception e) {
            Log.i("ju", e.getLocalizedMessage());

            // 로그인 실패 시 false
            return false;
        }
    }
}
