package com.example.aleksamarkoni.bluetoothsample;


        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothSocket;
        import android.util.Log;

        import java.io.IOException;
        import java.io.InputStream;
        import java.util.Set;
        import java.util.UUID;

public class BluetoothPtt {
    private  byte[] mmBuffer = new byte[1024];
    private InputStream in;
    private BluetoothSocket socket;
    private boolean isRunning = true;
    private OnMessageReceived pttState = null;

    public BluetoothPtt(OnMessageReceived listener){
        pttState = listener;
        Set<BluetoothDevice> bs = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        for (BluetoothDevice device : bs){
            if (device.getName().equalsIgnoreCase("Nighthawk X Remote PTT v1.26")){
                try{
                    Log.d("ACA_BRE", "Trying to connect");
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                    start();
                }
                catch (IOException e){
                    Log.d("ACA_BRE", "Ne moze: " + e.getLocalizedMessage());
                    return;
                }
            }
            return;
        }
    }

    private void start(){
        new Thread(){
            @Override
            public void run(){
                try {
                    socket.connect();
                    in = socket.getInputStream();
                }
                catch (IOException e){
                    socket = null;
                    return;
                }
                Log.d("ACA_BRE", "Connected and reading");
                while (isRunning){
                    Log.d("ACA_BRE", "Still connected");
                    try {
                        Thread.sleep(1000);
                        if (socket.isConnected()){
                            Log.d("ACA_BRE", "Waiting for data");
                            in.available();

                            in.read(mmBuffer);
                            Log.d("ACA_BRE", "" + mmBuffer);
                            if (mmBuffer[5] == 80){
                                pttState.isPressed(true);
                            }
                            else
                            {
                                pttState.isPressed(false);
                            }
                        }
                    } catch (IOException e) {

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void release(){
        isRunning = false;
        try {
            socket.close();
        }
        catch (IOException e){

        }
        socket = null;
    }

    public interface OnMessageReceived {
        public void isPressed(boolean state);
    }
}