package com.lishang.smartlock.smartlock.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.JavaBean.StatusUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.support.constraint.Constraints.TAG;

public class BluetoothOpenActivity extends Activity {
    private TextView tv_blue_lockname, tv_blue_status, tv_status;
    private EditText et_blue_psw;
    private Button btn_blcancel, btn_blconfirm, btn_blue_cancel, btn_blue_confirm;
    String res, msg, state, cookie, tvstatus, name,port,server;
    private Dialog mdialog;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    BluetoothAdapter mAdapter;
    BluetoothSocket socket;
    Integer id;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_open);
        Intent intent = getIntent();
        final Map <String, Object> lockinfo = (HashMap <String, Object>)
                intent.getSerializableExtra("lockinfo");
        initView();
        pref = PreferenceManager.getDefaultSharedPreferences(BluetoothOpenActivity.this);
        mEditor = pref.edit();
        cookie = pref.getString("SmartLockId", "");
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        tv_blue_lockname.setText(lockinfo.get("lockName").toString());
        name = lockinfo.get("lockNumber").toString();
        id = (Integer) lockinfo.get("id");
        Log.i("TAG16","ID:"+ id);
        btn_blcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //unregisterReceiver(receiver);
                BluetoothOpenActivity.this.finish();


            }
        });
        btn_blconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {

                //开启蓝牙
                if (!mAdapter.isEnabled()) {
                    Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enabler, 0);
                }
              //  if (tv_blue_status.getText().equals("关闭状态")) {
                    startDiscovery();
                    new WriteTask("").start();
                    Toast.makeText(BluetoothOpenActivity.this, "开锁中请稍后", Toast.LENGTH_SHORT).show();
            /*    } else if(tv_blue_status.getText().equals("打开状态")){
                    Toast.makeText(BluetoothOpenActivity.this, "智能锁已打开", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(BluetoothOpenActivity.this, "智能锁不在线", Toast.LENGTH_SHORT).show();
                }*/


            }
        });
    }
    private void startDiscovery() {
        // 找到设备的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // 注册广播
        registerReceiver(receiver, filter);
        // 搜索完成的广播
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 注册广播
        registerReceiver(receiver, filter1);
        Log.e(TAG, "startDiscovery: 注册广播");
        startScanBluth();

    }

    private void startScanBluth() {
        // 判断是否在搜索,如果在搜索，就取消搜索
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
        // 开始搜索
        mAdapter.startDiscovery();


    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive( Context context, Intent intent ) {
            String action = intent.getAction();
//找到设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i("TAG4", "find device:" + device.getName() + device.getAddress());
                if (("" + device.getName()).equals(name)) {
                    // 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
                    mAdapter.cancelDiscovery();
                    // 获取蓝牙设备的连接状态
                    int connectState = device.getBondState();
                    switch (connectState) {
                        // 未配对
                        case BluetoothDevice.BOND_NONE:
                            // 配对
                            try {
                                //  boolean ret = ClsUtils.setPin( device.getClass(),device, "123456");
                                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                                createBondMethod.invoke(device);
                                new WriteTask("").start();
                                Toast.makeText(BluetoothOpenActivity.this, "开锁中请稍后。。。", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        // 已配对
                        case BluetoothDevice.BOND_BONDED:
                            try {
                                // 连接
                                Log.i("TAG7", "已配对");
                                connect(device);
                                new WriteTask("").start();
                                //Toast.makeText(BluetoothOpenActivity.this, "开锁中请稍后。。。", Toast.LENGTH_SHORT).show();
                                //setPsw();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }

                }

//搜索完成
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("TAG4", "完成搜索");
                unregisterReceiver(receiver);
            }//执行更新列表的代码
        }
    };
    @Override
    public void onResume() {
        super.onResume();

        //进页面时加载历史消息
        okHttp1(id);
    }

    private void okHttp1( Integer id ) {
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        //2.构造request
        Request request = builder.get()
                //.url(server + ":" + port + "/app/lock/getLockList?p=1&keyword=" + id)
                .url(server + ":" + port + "/app/lock/getLock?id=" + id)
                .addHeader("Cookie", "SmartLockId=" + cookie)
                .build();
        //3.将request封装成call
        final Call call = okHttpClient.newCall(request);
        //4.执行call
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                call.enqueue(new Callback() {
                    @Override
                    public void onFailure( Call call, IOException e ) {
                        Log.i("TAG1", "shibas");
                    }

                    @Override
                    public void onResponse( Call call, Response response ) throws IOException {
                        String res = response.body().string();
                        Log.i("TAG16", res);
                        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(res);
                        com.alibaba.fastjson.JSONObject jsonObject1 = jsonObject.getJSONObject("lock");
                        //获取到json数据中的数组里的内容

                        Integer onlineStatus = jsonObject1.getInteger("onlineStatus");
                        Integer useStatus = jsonObject1.getInteger("useStatus");
                        Integer enableStatus = jsonObject1.getInteger("enableStatus");
                        final String str = StatusUtils.setStatus(useStatus, enableStatus, onlineStatus);
                        Log.i("TAG16", str);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_blue_status.setText(str);
                            }
                        });
                    }

                });
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if (receiver != null) {
            //取消注册,防止内存泄露（onDestroy被回调代不代表Activity被回收？：具体回收看系统，由GC回收，同时广播会注册到系统
            //管理的ams中，即使activity被回收，reciver也不会被回收，所以一定要取消注册），
            unregisterReceiver(receiver);
        }*/
    }

    //蓝牙传输，发送消息开锁
    public class WriteTask extends Thread {
        private String srt;

        public WriteTask( String str ) {
            this.srt = str;
            Log.i("TAG6", srt);
        }

        @Override
        public void run() {
            OutputStream outputStream = null;
            byte[] st = {0x6f, 0x0a};
            Log.i("TAG", "" + st);
            try {
                outputStream = socket.getOutputStream();
                outputStream.write(st);
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //连接设备
    private void connect( BluetoothDevice device ) throws IOException {
        // 固定的UUID
        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        UUID uuid = UUID.fromString(SPP_UUID);
        socket = device.createRfcommSocketToServiceRecord(uuid);
        socket.connect();
    }

    private void initView() {
        tv_status = findViewById(R.id.tv_status);
        btn_blcancel = findViewById(R.id.btn_blcancel);
        btn_blconfirm = findViewById(R.id.btn_blconfirm);
        tv_blue_lockname = findViewById(R.id.tv_blue_lockname);
        tv_blue_status = findViewById(R.id.tv_blue_status);
    }

    public void btn_back2( View view ) {
        //onDestroy();
        //unregisterReceiver(receiver);
        finish();
    }

}
