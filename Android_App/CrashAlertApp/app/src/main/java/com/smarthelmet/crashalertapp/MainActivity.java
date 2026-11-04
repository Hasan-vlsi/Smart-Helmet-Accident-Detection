package com.smarthelmet.crashalertapp;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;

import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    TextView locationText;
    Button btnShare, btnEdit;

    FusedLocationProviderClient fusedLocationClient;

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    InputStream inputStream;

    String DEVICE_ADDRESS = "1C:C3:AB:C3:78:CA";

    String PHONE1 = "", PHONE2 = "", PHONE3 = "";

    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    double lat = 0, lon = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationText = findViewById(R.id.locationText);
        btnShare = findViewById(R.id.btnShare);
        btnEdit = findViewById(R.id.btnEdit);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadContacts();
        askContactsIfNeeded();
        getLocation();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectBluetooth();

        btnShare.setOnClickListener(v -> shareLocationOnWhatsApp());

        btnEdit.setOnClickListener(v -> {
            getSharedPreferences("app", MODE_PRIVATE).edit().clear().apply();
            PHONE1 = "";
            askContactsIfNeeded();
        });
    }

    // ================= CONTACTS =================
    void loadContacts() {
        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        PHONE1 = prefs.getString("phone1", "");
        PHONE2 = prefs.getString("phone2", "");
        PHONE3 = prefs.getString("phone3", "");
    }

    void askContactsIfNeeded() {
        if (!PHONE1.isEmpty()) return;

        EditText input1 = new EditText(this);
        input1.setHint("Contact 1 (Required)");

        EditText input2 = new EditText(this);
        input2.setHint("Contact 2");

        EditText input3 = new EditText(this);
        input3.setHint("Contact 3");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input1);
        layout.addView(input2);
        layout.addView(input3);

        new AlertDialog.Builder(this)
                .setTitle("Emergency Contacts")
                .setMessage("Enter up to 3 contacts")
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("Save", (d, w) -> {

                    PHONE1 = input1.getText().toString();
                    PHONE2 = input2.getText().toString();
                    PHONE3 = input3.getText().toString();

                    getSharedPreferences("app", MODE_PRIVATE).edit()
                            .putString("phone1", PHONE1)
                            .putString("phone2", PHONE2)
                            .putString("phone3", PHONE3)
                            .apply();
                })
                .show();
    }

    // ================= LOCATION =================
    void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                locationText.setText("Lat: " + lat + "\nLon: " + lon);
            }
        });
    }

    // ================= BLUETOOTH =================
    void connectBluetooth() {
        new Thread(() -> {
            try {
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
                        runOnUiThread(this::handleCrash);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ================= CRASH FLOW =================
    void handleCrash() {

        locationText.setText("🚨 CRASH DETECTED!");

        // 1. CALL FIRST
        makeEmergencyCall();

        // 2. SMS AFTER 5 SEC
        new Handler().postDelayed(() -> {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS}, 3);
                return;
            }

            sendSMS();

        }, 5000);

        // 3. WHATSAPP AFTER 8 SEC
        new Handler().postDelayed(this::shareLocationOnWhatsApp, 8000);
    }

    // ================= CALL =================
    void makeEmergencyCall() {
        if (PHONE1.isEmpty()) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, 2);
            return;
        }

        startActivity(new Intent(Intent.ACTION_CALL,
                Uri.parse("tel:" + PHONE1)));
    }

    // ================= SMS =================
    void sendSMS() {
        try {
            String msg = "🚨 Emergency! Accident detected.\n"
                    + "📍 Location:\nhttps://maps.google.com/?q=" + lat + "," + lon;

            SmsManager sms = SmsManager.getDefault();

            if (!PHONE1.isEmpty()) sms.sendTextMessage(PHONE1, null, msg, null, null);
            if (!PHONE2.isEmpty()) sms.sendTextMessage(PHONE2, null, msg, null, null);
            if (!PHONE3.isEmpty()) sms.sendTextMessage(PHONE3, null, msg, null, null);

            locationText.setText("SMS sent to contacts");

        } catch (Exception e) {
            locationText.setText("SMS Failed!");
        }
    }

    // ================= WHATSAPP =================
    void shareLocationOnWhatsApp() {
        try {
            String message = "🚨 Emergency! Accident detected.\n"
                    + "📍 Location:\nhttps://maps.google.com/?q=" + lat + "," + lon;

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setPackage("com.whatsapp");

            startActivity(intent);

        } catch (Exception e) {
            locationText.setText("WhatsApp not installed!");
        }
    }

    // ================= PERMISSIONS =================
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) getLocation();
        if (requestCode == 2) makeEmergencyCall();
        if (requestCode == 3) sendSMS();
    }
}