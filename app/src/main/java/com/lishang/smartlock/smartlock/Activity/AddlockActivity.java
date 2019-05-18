package com.lishang.smartlock.smartlock.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.HomeFragment;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddlockActivity extends Activity {
    private EditText et_activecode, et_locknumber, et_add_latitude, et_add_longitude;
    private Button btn_active;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    String cookie, res, lockNumber, activeCode, msg, state, server, port,longitude, latitude;
    BDLocation location;
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addlock);
        pref = PreferenceManager.getDefaultSharedPreferences(AddlockActivity.this);
        mEditor = pref.edit();
        cookie = pref.getString("SmartLockId", "");
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        longitude = pref.getString("longitude", "");
        latitude = pref.getString("latitude", "");
        initView();
    }
    private void initView() {
        et_add_longitude = findViewById(R.id.et_add_longitude);
        et_add_latitude = findViewById(R.id.et_add_latitude);
        et_locknumber = findViewById(R.id.et_locknumber);
        et_activecode = findViewById(R.id.et_activecode);
        btn_active = findViewById(R.id.btn_active);
        lockNumber = et_locknumber.getText().toString();
        activeCode = et_activecode.getText().toString();
        et_add_longitude.setText(longitude);
        et_add_latitude.setText(latitude);

    }

    public void backLast( View view ) {
        Intent intent = new Intent(AddlockActivity.this
                , MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void activeLock( View view ) {
            okHttp();
    }

    public void okHttp() {
        lockNumber = et_locknumber.getText().toString();
        activeCode = et_activecode.getText().toString();
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.构造request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(server + ":" + port + "/app/lock/active?lockNumber=" + lockNumber + "&activeCode=" + activeCode
                +"&longitude" + longitude + "&latitude"+ latitude)
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
                        Toast.makeText(AddlockActivity.this, "无法连接服务器！", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AddlockActivity.this, "激活成功！", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddlockActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

        });

    }


}

