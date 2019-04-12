package com.example.myapplication;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;

public abstract class RemoteDateServiceImpl extends Binder implements IRemoteDateService {

    RemoteDateServiceImpl() {
        attachInterface(this, DESCRIPTION);
    }

     static IRemoteDateService asInterface(IBinder binder) {
        if (binder == null) {
            return null;
        }
        if (binder instanceof IRemoteDateService) {
            return (IRemoteDateService) binder;
        }
        return new Proxy(binder);
    }

    @Override
    protected boolean onTransact(int code, @NonNull Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case INTERFACE_TRANSACTION:
                reply.writeString(DESCRIPTION);
                return true;
            case DESCRIPTION_getDate:
                data.enforceInterface(DESCRIPTION);
                reply.writeNoException();
                reply.writeString(getDate());
                return true;
        }
        return super.onTransact(code, data, reply, flags);
    }


    static class Proxy extends RemoteDateServiceImpl {

        IBinder remote;

        Proxy(IBinder remote) {
            this.remote = remote;
        }

        public String getInterfaceDescriptor() {
            return DESCRIPTION;
        }

        @Override
        public String getDate() throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            String date;
            try {
                _data.writeInterfaceToken(DESCRIPTION);
                remote.transact(DESCRIPTION_getDate, _data, _reply, 0);
                _reply.readException();
                date = _reply.readString();
            } finally {
                _reply.recycle();
                _data.recycle();
            }
            return date;
        }

        @Override
        public IBinder asBinder() {
            return remote;
        }
    }
}
