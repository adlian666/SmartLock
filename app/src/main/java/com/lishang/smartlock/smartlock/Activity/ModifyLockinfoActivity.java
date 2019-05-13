package com.lishang.smartlock.smartlock.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lishang.smartlock.R;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ModifyLockinfoActivity extends Activity {
    Integer id;
    String res, msg, state, cookie,server,port;
    private String lockName, address, longitude, latitude, authCode, blueCode;
    private EditText et_blueCode, et_authCode, et_latitude, et_longitude, et_address, et_lockName;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        Intent intent = getIntent();
        final HashMap <String, Object> myinfo = (HashMap <String, Object>)
                intent.getSerializableExtra("myinfo");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_lockinfo);
        initView();
        pref = PreferenceManager.getDefaultSharedPreferences(ModifyLockinfoActivity.this);
        mEditor = pref.edit();
        cookie = pref.getString("SmartLockId", "");
        id = (Integer) myinfo.get("id");
        Log.i("TAG2",""+ id);
        server = pref.getString("server","");
        port = pref.getString("port","");
        et_lockName.setText(myinfo.get("lockName").toString());
        et_address.setText(myinfo.get("address").toString());
        et_longitude.setText(myinfo.get("longitude").toString());
        et_latitude.setText(myinfo.get("latitude").toString());
        et_authCode.setText(myinfo.get("authCode").toString());
        et_blueCode.setText(myinfo.get("blueCode").toString());

    }

    private void initView() {
        et_blueCode = findViewById(R.id.et_blueCode);
        et_authCode = findViewById(R.id.et_authCode);
        et_latitude = findViewById(R.id.et_latitude);
        et_longitude = findViewById(R.id.et_longitude);
        et_address = findViewById(R.id.et_address);
        et_lockName = findViewById(R.id.et_lockName);

    }

    public void backLast( View view ) {
        finish();
    }

    public void modifySubmit( View view ) {

        lockName = et_lockName.getText().toString();
        address = et_address.getText().toString();
        longitude = et_longitude.getText().toString();
        latitude = et_latitude.getText().toString();
        authCode = et_authCode.getText().toString();
        blueCode = et_blueCode.getText().toString();
        if(lockName.equals("")||address.equals("")||longitude.equals("")||latitude.equals("")||authCode.equals("")
                ||blueCode.equals("")){
            Toast.makeText(ModifyLockinfoActivity.this, "输入不能为空！", Toast.LENGTH_SHORT).show();
        }else {
            okHttp(id);
        }
    }
    private void okHttp( Integer id ) {

        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.构造request
        Request.Builder builder = new Request.Builder();
        Log.i("TAG2", lockName+address+latitude+longitude+authCode+blueCode+id);
        //构造formbody
        FormBody formBody = new FormBody
                .Builder()
                .add("sLock.lockName", lockName)
                .add("sLock.address", address)
                .add("sLock.latitude", latitude)
                .add("sLock.longitude", longitude)
                .add("sLock.authCode", authCode)
                .add("sLock.blueCode", blueCode)//设置参数名称和参数值
                .build();
        Request request = builder
                .url(server + ":" +  port +"/app/lock/updateLock?id=" + id)
                .addHeader("cookie", "SmartLockId=" + cookie)
                .post(formBody)
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
                        Toast.makeText(ModifyLockinfoActivity.this, "无法连接服务器！", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ModifyLockinfoActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ModifyLockinfoActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

        });

    }
}
