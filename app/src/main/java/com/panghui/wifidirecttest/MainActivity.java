package com.panghui.wifidirecttest;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DeviceInfoListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ChannelListener , ConnectionInfoListener , GroupInfoListener {

    public static final String TAG = "wifidirecttest";
    private WifiP2pManager manager;
    private WifiManager wifiManager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver=null;

    private List<WifiP2pDevice> peers = new ArrayList<>();

    private WifiP2pInfo info;

    private String deviceName;

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button discoverPeersBT = findViewById(R.id.discoverPeers);
        Button becomeGOBT = findViewById(R.id.become_GO);
        Button connectBT = findViewById(R.id.connect);
        Button removeGroupBT = findViewById(R.id.removeGroup);
        Button serverBT = findViewById(R.id.Server);
        Button clientBT = findViewById(R.id.Client);
        Button requestGroupInfoBT = findViewById(R.id.requestGroupInfo);
        Button getIpAddr = findViewById(R.id.getIPaddr);
        Button getBTmac = findViewById(R.id.getBTmac);
        Button setDeviceName = findViewById(R.id.setDeviceName);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // 2. 发现对等设备
        discoverPeersBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.discoverPeers(channel,new ActionListener(){
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Discovery Initiated",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.d(MainActivity.TAG, "设备名"+Build.DEVICE);
            }
        });

        // 连接到指定设备
        connectBT.setOnClickListener(new View.OnClickListener(){
            WifiP2pDevice device;
            WifiP2pConfig config = new WifiP2pConfig(); // A class representing a Wi-Fi P2p configuration for setting up a connection

            @Override
            public void onClick(View v) {
                int i=0;
                if(peers.size() == 0){
                    Log.d(MainActivity.TAG,"No devices found");
                    return;
                }else{
                    for(i=0;i<peers.size();i++){
                        // 392：Android_d660
                        // 483：Android_71a3
                        if(peers.get(i).deviceName.equals("Android_d660")){
                            Log.d(MainActivity.TAG,"找到了该设备："+peers.get(i).deviceName);
                            break;
                        }
                    }

                    if(i<peers.size()){
                        device=peers.get(i);
                        config.deviceAddress = device.deviceAddress;
//                        config.groupOwnerIntent = 5; // 代表该设备想成为 GO 的意愿强度
                        manager.connect(channel, config, new ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(MainActivity.TAG,"成功连接到："+device.deviceName);
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(MainActivity.TAG,"连接失败！");
                            }
                        });
                    }else {
                        // Android_71a3
                        Log.d(MainActivity.TAG,"未发现该设备：Android_d660");
                        // Android_d660
                    }

                }
            }
        });

        // 成为GO
        becomeGOBT.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                manager.createGroup(channel, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(MainActivity.TAG,"成功成为 GO");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(MainActivity.TAG,"失败成为 GO");
                    }
                });
            }
        });

        removeGroupBT.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                manager.removeGroup(channel, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(MainActivity.TAG,"成功移除当前组");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(MainActivity.TAG,"失败移除当前组");
                    }
                });
            }
        });

        // 开启服务端线程
        serverBT.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d(MainActivity.TAG,"点击了SERVER");
                new Thread(new ServerThread()).start();
            }
        });

        // 开启客户端线程
        clientBT.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d(MainActivity.TAG,"点击了CLIENT");
                new Thread(new ClientThread()).start();
            }
        });

        requestGroupInfoBT.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                manager.requestGroupInfo(channel,MainActivity.this);
            }
        });

        getIpAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = Utils.getIPAddress(true);
                String deviceName = getPersistedDeviceName();
                Log.d(MainActivity.TAG,"ip 地址为："+ip);
                Log.d(MainActivity.TAG,"设备名为："+deviceName);
            }
        });

        getBTmac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String BTmac = Utils.getBluetoothMacAddress();
                Log.d(MainActivity.TAG,"BT mac 地址为："+BTmac);
            }
        });

        setDeviceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String suffix="xxxxxxxxxxxxxxxxxx";
                try {
                    Method m = manager.getClass().getMethod("setDeviceName",new Class[]{
                       channel.getClass(),String.class,WifiP2pManager.ActionListener.class });

                    m.invoke(manager,channel,"Android_d660_1"+suffix,new WifiP2pManager.ActionListener(){
                        @Override
                        public void onSuccess() {
                            Log.d(MainActivity.TAG,"修改名称成功");
                            Log.d(MainActivity.TAG,"字符长度："+"Android_d660_1xxxxxxxxxxxxxxxxxx".length());
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d(MainActivity.TAG,"修改名称失败");
                        }
                    });
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    Log.d(MainActivity.TAG,"修改名称失败");
                    e.printStackTrace();
                }
            }
        });

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        // 1. 通过WLAN框架注册应用。必须调用此方法，然后再调用任何其他WLAN P2P 方法
        channel = manager.initialize(this,getMainLooper(),null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager,channel,this,peers);
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if(manager != null && !retryChannel){
            Toast.makeText(this,"Channel lost. Trying again",Toast.LENGTH_LONG).show();
            retryChannel=true;
            manager.initialize(this,getMainLooper(),this);
        }else{
            Toast.makeText(this,"Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        this.info = info;
        // InetAddress from WifiP2pInfo struct.
        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

        // After the group negotiation, we can determine the group owner
        // (server).
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
        }

        Log.d(MainActivity.TAG,"IP地址："+info.groupOwnerAddress.getHostAddress());
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {

    }

    public String getPersistedDeviceName() {
        Context mContext = getApplicationContext();
        String deviceName = Settings.Global.getString(mContext.getContentResolver(),
                Settings.Global.DEVICE_NAME);
        if (deviceName == null) {
            // We use the 4 digits of the ANDROID_ID to have a friendly
            // default that has low likelihood of collision with a peer
            String id = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            return "Android_" + id.substring(0, 4);
        }
        return deviceName;
    }
}