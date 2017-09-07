package com.example.lukasz.bluetooth.dataTransfer;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * Created by Lukasz on 2017-07-05.
 */

/*
    Klasa w rodzaju singletona. Dzięki dziedziczeniu Application mogę pobierać ten sam obiekt w Activity metodą getApplication().
    Trzeba było dodać w Manifeście android:name=".dataTransfer.BluetoothDataTransfer"
 */
public class BluetoothDataTransfer extends Application {

    private OutputStream stream;


    public void createStream(BluetoothSocket socket){
        try {
            stream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void send(String command){
        try {
            stream.write(command.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
