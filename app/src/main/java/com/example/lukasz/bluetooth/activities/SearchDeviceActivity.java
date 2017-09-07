package com.example.lukasz.bluetooth.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lukasz.bluetooth.R;
import com.example.lukasz.bluetooth.dataTransfer.BluetoothDataTransfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.jar.Manifest;

public class SearchDeviceActivity extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout deviceListLayout;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private TextView console;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_search_activity);
        console = (TextView) findViewById(R.id.consoleView);
        console.setMovementMethod(new ScrollingMovementMethod());   //ustawienie scrollowaia konsoli
        deviceListLayout = (LinearLayout) findViewById(R.id.bluetoothDevicesList);
        IntentFilter bluetoothFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(deviceReceiver, bluetoothFilter);
        IntentFilter discoveryFilter = new IntentFilter((BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(stopDiscoveryReceiver, discoveryFilter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(deviceReceiver);
    }

    /*
        przerywa szukanie urządzeń bluetooth, w naciśniętym przycisku zapisany jest MAC, po MACu wyszukuje urządzenie z listy,
        UUID 1234 jest domyślnie ustawiony w odbiorniku przy arduino
     */
    @Override
    public void onClick(View v) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = null;
        Button button = (Button)v;
        String mac = (String) button.getContentDescription();
        for(BluetoothDevice d : deviceList){
            if(d.getAddress().equals(mac)){
                device = d;
                break;
            }
        }
        if(device != null){
            addConsoleLog("Łączenie...");
            try {
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                socket.connect();
                BluetoothDataTransfer dataTransfer = (BluetoothDataTransfer) getApplication();
                dataTransfer.createStream(socket);
                Intent intent = new Intent(this, MainDesktopActivity.class);
                addConsoleLog("Połączono");
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
                addConsoleLog(e.toString());
            }
        }
    }

    private void addConsoleLog(String text){
        console.setText(text+"\n"+console.getText());
    }


    public void searchDevices(View view){
        //zapytanie o pozwolenie na dostęp do bluetooth
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH)!= getPackageManager().PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH}, 111);
        }
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN)!= getPackageManager().PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_ADMIN}, 112);
        }
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= getPackageManager().PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 113);
        }

        if(bluetoothAdapter==null){
            addConsoleLog("brak nadajnika bluetooth");
        }
        else{
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1337);
            }
            if (bluetoothAdapter.isEnabled()) {
                if(!bluetoothAdapter.isDiscovering()){
                    addConsoleLog("Szukam urządzeń...");
                    deviceListLayout.removeAllViews();
                    deviceList.clear();
                    bluetoothAdapter.startDiscovery();
                }
            }
        }
    }

    /*
        pierwsza linijka ustawia przycisk na całą szerokość ekranu
        setContentDescription - tutaj zapisuję MAC dzięki któremu po wciśnięciu przycisku wyszukuję urządzenie z listy
     */
    private void addDeviceButton(BluetoothDevice device){
        LinearLayout.LayoutParams matchParentForButton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Button button = new Button(this);
        button.setBackgroundColor(Color.parseColor("#737373"));
        button.setTextColor(Color.WHITE);
        button.setText("Nazwa: "+device.getName()+"   MAC: "+device.getAddress());
        button.setContentDescription(device.getAddress());
        button.setLayoutParams(matchParentForButton);
        button.setOnClickListener(this);
        deviceListLayout.addView(button);
        deviceList.add(device);
    }

    // gdy znajdzie urządzenie
    private final BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addDeviceButton(device);
            }
        }
    };

    // gdy przestanie wyszukiwać urządzeń
    private final BroadcastReceiver stopDiscoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                addConsoleLog("Zakończono wyszukiwanie urządzeń");
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        addConsoleLog(Integer.toString(requestCode));
    }
}
