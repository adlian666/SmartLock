package com.lishang.smartlock.smartlock.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.adapter.HomeRecycleAdapter;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OpenLockActivity extends Activity implements HomeRecycleAdapter.OnItemClickListener {
    private ImageButton imbtn_lockinfo;
    private Button blue_open, btn_remote_open;
    private TextView tv_openlock_title;
    private ImageButton img_setBluePsw;
    Integer id;
    String res, msg, state, cookie, tvstatus, server, port;
    Switch switch_open;

    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        Intent intent = getIntent();
        final Map <String, Object> lockinfo = (HashMap <String, Object>)
                intent.getSerializableExtra("lockinfo");
        super.onCreate(savedInstanceState);
        int usestatus = (int) lockinfo.get("useStatus");
        setContentView(R.layout.activity_open_lock);
        id = (Integer) lockinfo.get("id");
        pref = PreferenceManager.getDefaultSharedPreferences(OpenLockActivity.this);
        mEditor = pref.edit();
        tvstatus = pref.getString("tvstatus", tvstatus);
        cookie = pref.getString("SmartLockId", "");
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        //找到相应的组件
        initView();
        if (usestatus == 1) {
            switch_open.setChecked(true);
        } else if (usestatus == 2) {
            switch_open.setChecked(false);
        }
        //按钮监听事件
        imbtn_lockinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //跳转到锁信息页面
                Intent intent = new Intent(OpenLockActivity.this, LockInfoActivity.class);
                intent.putExtra("lockinfo", (Serializable) lockinfo);
                startActivity(intent);
            }
        });
        blue_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //跳转到蓝牙开锁页面
                Intent intent = new Intent(OpenLockActivity.this, BluetoothOpenActivity.class);
                intent.putExtra("lockinfo", (Serializable) lockinfo);
                startActivity(intent);
            }
        });
        btn_remote_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //跳转到远程开锁页面
                Intent intent = new Intent(OpenLockActivity.this, RemoteOpenActivity.class);
                intent.putExtra("lockinfo", (Serializable) lockinfo);
                startActivity(intent);
            }
        });
        //跳转到设置蓝牙密码页面
        /*img_setBluePsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Intent intent = new Intent(OpenLockActivity.this, SetBluePswActivity.class);
                intent.putExtra("lockinfo", (Serializable) lockinfo);
                startActivity(intent);
            }
        });*/
        switch_open.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
                if (isChecked) {
                    //启用锁
                    okHttp1();
                } else {
                    //停用锁
                    okHttp2();
                }
            }
        });

        tv_openlock_title.setText(lockinfo.get("lockName").toString());
    }

    //启用锁
    private void okHttp1() {
//1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.构造request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(server + ":" + port + "/app/lock/setUseStatusEnable?id=" + id)
                .addHeader("cookie", "SmartLockId=" + cookie)
                .get()
                .build();
        //3.将request封装成call
        Call call = okHttpClient.newCall(request);
        //4.执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(OpenLockActivity.this, "无法连接服务器！", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                res = response.body().string();
                Log.i("TAG2", res);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("TAG2", "AAAAAA");
                        JSONObject jsonObject = JSON.parseObject(res);
                        msg = jsonObject.getString("msg");
                        state = jsonObject.getString("state");
                        if (state.equals("ok")) {
                            Toast.makeText(OpenLockActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OpenLockActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    //停用锁
    private void okHttp2() {
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.构造request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(server + ":" + port + "/app/lock/setUseStatusDisable?id=" + id)
                .addHeader("cookie", "SmartLockId=" + cookie)
                .get()
                .build();
        //3.将request封装成call
        Call call = okHttpClient.newCall(request);
        //4.执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(OpenLockActivity.this, "无法连接服务器！", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                res = response.body().string();
                Log.i("TAG2", res);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("TAG2", "AAAAAA");
                        JSONObject jsonObject = JSON.parseObject(res);
                        msg = jsonObject.getString("msg");
                        state = jsonObject.getString("state");
                        if (state.equals("ok")) {
                            Toast.makeText(OpenLockActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OpenLockActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

        });
    }

    //找到相应的组件
    private void initView() {
        switch_open = findViewById(R.id.switch_open);
        imbtn_lockinfo = findViewById(R.id.imbtn_lockinfo);
        blue_open = findViewById(R.id.blue_open);
        btn_remote_open = findViewById(R.id.btn_remote_open);
        tv_openlock_title = findViewById(R.id.tv_openlock_title);
        img_setBluePsw = findViewById(R.id.img_setBluePsw);
    }

    @Override
    public void OnItemClick( View view, Map <String, Object> mlist ) {
        System.out.println(mlist.get("lockName"));
    }

    public void btn_back( View view ) {
        OpenLockActivity.this.finish();
    }


}
