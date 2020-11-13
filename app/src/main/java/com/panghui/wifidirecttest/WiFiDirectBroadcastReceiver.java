package com.panghui.wifidirecttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.text.format.Formatter;
import android.util.Log;

import java.util.List;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;
    private MyPeerListListener myPeerListListener;

    private WifiP2pInfo info;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       MainActivity activity, List<WifiP2pDevice> peers){
        super();
        this.manager=manager;
        this.channel=channel;
        this.activity=activity;
        myPeerListListener=new MyPeerListListener(peers);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){

            // UI update to indicate wifi p2p status
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);
            }else {
                activity.setIsWifiP2pEnabled(false);
            }
            Log.d(MainActivity.TAG,"P2P state changed - " + state);
        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if(manager !=null){
                // 请求发现对等设备的当前列表
                manager.requestPeers(channel,myPeerListListener);
            }
            Log.d(MainActivity.TAG,"P2P peers changed");
        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            if(manager == null){
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo)intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected()){
                Log.d(MainActivity.TAG,"连接成功！");

                manager.requestConnectionInfo(channel,activity);
            }else {
                Log.d(MainActivity.TAG,"连接失败！");
            }

        }else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
           WifiP2pDevice device = (WifiP2pDevice)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE); // 获取设备名称
           Log.d(MainActivity.TAG,"显示设备名："+device.deviceName);
        }
    }
}
