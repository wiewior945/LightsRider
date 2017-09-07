package com.example.lukasz.bluetooth.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.lukasz.bluetooth.R;
import com.example.lukasz.bluetooth.dataTransfer.BluetoothDataTransfer;

/**
 * Created by Lukasz on 2017-07-05.
 */

public class MainDesktopActivity extends AppCompatActivity {

    private BluetoothDataTransfer dataTransfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_desktop_activity);
        dataTransfer = (BluetoothDataTransfer) getApplication();
    }

    public void buttonClick(View view){
        System.out.println(view.getContentDescription().toString());
        dataTransfer.send(view.getContentDescription().toString());
    }
}
