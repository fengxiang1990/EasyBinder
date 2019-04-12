package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RemoteServise extends Service {

    IRemoteDateService remoteDateService = new RemoteDateServiceImpl() {
        @Override
        public String getDate() throws RemoteException {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return simpleDateFormat.format(new Date());
        }

        @Override
        public IBinder asBinder() {
            return this;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return remoteDateService.asBinder();
    }
}
