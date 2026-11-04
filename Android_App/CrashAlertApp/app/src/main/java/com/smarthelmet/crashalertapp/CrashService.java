package com.smarthelmet.crashalertapp;

import android.app.*;
import android.bluetooth.*;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.InputStream;
import java.util.UUID;

public class CrashService extends Service {

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    InputStream inputStream;

    String DEVICE_ADDRESS = "1C:C3:AB:C3:78:CA";
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public void onCreate() {
        super.onCreate();

        startForeground(1, createNotification());
        startBluetoothListener();
    }

    private Notification createNotification() {

        String channelId = "crash_channel";

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Crash Detection",
                    NotificationManager.IMPORTANCE_LOW
            );

            manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Smart Helmet Active")
                .setContentText("Monitoring crash...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
    }

    private void startBluetoothListener() {

        new Thread(() -> {
            try {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
                socket = device.createRfcommSocketToServiceRecord(uuid);
                socket.connect();

                inputStream = socket.getInputStream();

                byte[] buffer = new byte[1024];
                int bytes;

                while (true) {
                    bytes = inputStream.read(buffer);
                    String msg = new String(buffer, 0, bytes);

                    if (msg.contains("CRASH")) {

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("CRASH", true);
                        startActivity(intent);
                    }
                }

            } catch (Exception e) {
                Log.e("CrashService", "Error: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}