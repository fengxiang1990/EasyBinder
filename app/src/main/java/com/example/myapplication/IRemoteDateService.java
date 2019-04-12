package com.example.myapplication;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IRemoteDateService extends IInterface {

    String DESCRIPTION  = "com.example.myapplication.IRemoteDateService";

    int DESCRIPTION_getDate  = IBinder.FIRST_CALL_TRANSACTION;

    String getDate() throws RemoteException;

}
