package com.panghui.wifidirecttest;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientThread implements Runnable {
    private static final int SOCKET_TIMEOUT = 50;

    @Override
    public void run() {
        String host = "192.168.49.1";
        int port = 8888;

        Socket socket = new Socket();

        try{
            Log.d(MainActivity.TAG,"Opening client socket -");
            socket.bind(null);
            socket.connect(new InetSocketAddress(host,port),SOCKET_TIMEOUT);

            Log.d(MainActivity.TAG,"Opening client socket -"+socket.isConnected());
            // 向服务端回复消息
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("hello");
            out.close();
        }catch (IOException e){
            Log.e(MainActivity.TAG,e.getMessage());
        }finally {
            if(socket != null){
                if(socket.isConnected()){
                    try {
                        socket.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
