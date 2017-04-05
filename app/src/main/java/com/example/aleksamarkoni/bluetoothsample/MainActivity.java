package com.example.aleksamarkoni.bluetoothsample;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public final static String LOG_MESSAGE = "ACA_BRE";

    BluetoothA2dp bluetoothA2dp;
    BluetoothDevice bluetoothDevice;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.d(LOG_MESSAGE, "does not support bluetooth");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.d(LOG_MESSAGE, "bluetooth adapter not enabled");
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(LOG_MESSAGE, "device found: " + deviceName + " " + deviceHardwareAddress);
            }
        }


        mBluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.A2DP);

        BluetoothPtt  ptt = new BluetoothPtt(new BluetoothPtt.OnMessageReceived(){
            @Override
            public void isPressed(boolean state){
                Log.d(LOG_MESSAGE, "PTT click " + state);
            }
        });

        Log.d(LOG_MESSAGE, "App over");
    }

    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(LOG_MESSAGE, "profile connected: " + profile);
            if (profile == BluetoothProfile.A2DP) {
                Log.d(LOG_MESSAGE, "a2dp connected");
                bluetoothA2dp = (BluetoothA2dp) proxy;
                List<BluetoothDevice> connectedDevices = bluetoothA2dp.getConnectedDevices();
                for (BluetoothDevice device : connectedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.d(LOG_MESSAGE, "a2dp device found: " + deviceName + " " + deviceHardwareAddress);
                    bluetoothDevice = device;
                }
                ((BluetoothA2dp) proxy).isA2dpPlaying(bluetoothDevice);

                thread = new Thread(){
                    @Override
                    public void run(){
                        Log.d("ACA_BRE", "Connected and reading");
                        while (true){
                            Log.d("ACA_BRE", "Still connected");
                            try {
                                Thread.sleep(1000);
                                boolean playing = bluetoothA2dp.isA2dpPlaying(bluetoothDevice);
                                Log.d(LOG_MESSAGE, "Playing: " + playing);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                thread.start();
            }
        }
        public void onServiceDisconnected(int profile) {
            Log.d(LOG_MESSAGE, "profile disconnected: " + profile);
            if (profile == BluetoothProfile.A2DP) {
                Log.d(LOG_MESSAGE, "a2dp disconnected");
                bluetoothA2dp = null;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(LOG_MESSAGE, "keypress: " + keyCode + " " + event.getAction());
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                //yourMediaController.dispatchMediaButtonEvent(event);
                return true;
        }
        return false;
    }

}
