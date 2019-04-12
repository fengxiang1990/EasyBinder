# EasyBinder
### 这是一个remote进程获取日期字符串的示例，如果能充分理解这里面的逻辑和调用关系，那么对于Android应用开发人员来说Binder的知识就算是掌握了。

####  第一步，定义一个远程服务接口,它继承了IInterface接口，这个接口其实也可以在IRemoteDateService的具体实现类中实现，
```Java
  public interface IRemoteDateService extends IInterface {

    //服务的字符转描述，相当于自定Binder的名称
    String DESCRIPTION  = "com.example.myapplication.IRemoteDateService";
  
    //远程方法的序号
    int DESCRIPTION_getDate  = IBinder.FIRST_CALL_TRANSACTION;

    //远程方法
    String getDate() throws RemoteException;

}

```

#### 第二步，定义IRemoteDateService的具体实现类RemoteDateServiceImpl，这也是我们手写Binder服务的核心，它直接继承自Binder类，

```Java
public abstract class RemoteDateServiceImpl extends Binder implements IRemoteDateService {

    //这一步很重要，这是在Binder服务中注册我们自己的远程服务
    RemoteDateServiceImpl() {
        attachInterface(this, DESCRIPTION);
    }

    //这是一个静态方法,它的主要作用是在bindServise方法启动Service的时候，
    //在ServiceConnection中调用，以获取远程服务的实例
     static IRemoteDateService asInterface(IBinder binder) {
        if (binder == null) {
            return null;
        }
        if (binder instanceof IRemoteDateService) {
            return (IRemoteDateService) binder;
        }
        return new Proxy(binder);
    }

    //自定义Binder必须实现的方法，重中之重
    @Override
    protected boolean onTransact(int code, @NonNull Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
        //相当于自定义的Binder注册成功后的回调
            case INTERFACE_TRANSACTION:
                reply.writeString(DESCRIPTION);
                return true;
           
           //照葫芦画瓢就可以了     
            case DESCRIPTION_getDate:
                data.enforceInterface(DESCRIPTION);
                reply.writeNoException();
                reply.writeString(getDate());
                return true;
        }
        return super.onTransact(code, data, reply, flags);
    }


//这是一个代理类,如果进程是远程的,则会调用该类,通过IBinder
//的transact方法把调用的方法传给具体的Binder实现类，上面的Binder实现类
//RemoteDateServiceImpl在收到onTransact方法后，根据方法的序列号DESCRIPTIO//N_getDate写入remote //process中返回的实际结果，也就是getDate()的值，并将这个值以reply.writeSt//ring(getDate())的方式写入,这后面实际上是由binder驱动来完成,最终binder驱//动把远程进程的数据传到调用的进程.
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
                
            //这里是阻塞的,等待binder驱动把把数据传过来
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
```

#### 第三部，定义我们的android service 类，并且设置为remote 进程


```
 <service android:name=".RemoteServise" android:process=":remote"/>
```

```Java
public class RemoteServise extends Service {

    IRemoteDateService remoteDateService = new RemoteDateServiceImpl() {
      
      //具体的返回时期的实现
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

```

#### 第四部，在MainActivity中调用getDate()


```Java
public class MainActivity extends AppCompatActivity {

    String tag = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_bind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindService(new Intent(MainActivity.this, RemoteServise.class), connection, Context.BIND_AUTO_CREATE);
            }
        });

        findViewById(R.id.btn_unbind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindService(connection);
            }
        });

        findViewById(R.id.btn_getdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String date = remoteDateService.getDate();
                    Log.e(tag, "date from remote->" + date);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    IRemoteDateService remoteDateService;

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteDateService = RemoteDateServiceImpl.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            remoteDateService = null;
        }
    };
}

```

Logcat 输出如下：E/MainActivity: date from remote->2019-04-12 14:25:06


至此通过调用一个远程服务获取日期的自定义binder 逻辑就写完了，很简单，
AIDL其实就是在帮助我们完成这些步骤，我建议对于简单的逻辑手写就行了，这样可以加深对binder的理解。
