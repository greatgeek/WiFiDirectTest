package com.panghui.wifidirecttest;

import android.app.ProgressDialog;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyPeerListListener implements PeerListListener {
    private List<WifiP2pDevice> peers;
    ProgressDialog progressDialog = null;
    private WifiP2pDevice device;

    // 将 Device 列表传递到 MainActivity
    public MyPeerListListener(List<WifiP2pDevice> peers){
        this.peers=peers;
    }
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peersList) {
        peers.clear();
        peers.addAll(peersList.getDeviceList());
        if(peers.size() == 0){
            Log.d(MainActivity.TAG,"No devices found");
            return;
        }else{
            for(int i=0;i<peers.size();i++){
                Log.d(MainActivity.TAG,peers.get(i).deviceName);
            }
        }
    }
}
