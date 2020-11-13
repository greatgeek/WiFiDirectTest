package com.panghui.wifidirecttest;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread implements Runnable {
    @Override
    public void run() {

        try{
            /**
             * Create a server socket and wait for client connections.
             * This call back until a connection is accepted from a client
             */
            Log.d(MainActivity.TAG,"开启服务端线程");
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket client = serverSocket.accept();
            /**
             * If this code is reached, a client has connected and transferred data.
             */
            // 接收客户端信息
            DataInputStream input = new DataInputStream(client.getInputStream());
            String msg = input.readUTF();
            // 得到客户端信息

            Log.d(MainActivity.TAG,"接收到客户端信息："+msg);
            serverSocket.close();
        }catch (IOException e){
            Log.e(MainActivity.TAG,e.getMessage());
        }
    }
}
