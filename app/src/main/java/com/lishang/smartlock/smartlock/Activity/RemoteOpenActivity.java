package com.lishang.smartlock.smartlock.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.JavaBean.StatusUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemoteOpenActivity extends Activity {
    private TextView tv_blue_lockname, tv_blue_status;
    private Button btn_confirm, btn_cancel;
    Integer id;
    String res, msg, state, cookie, tvstatus, server, port;

    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_open);
        pref = PreferenceManager.getDefaultSharedPreferences(RemoteOpenActivity.this);
        mEditor = pref.edit();
        cookie = pref.getString("SmartLockId", "");
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        //接收传过来的数据
        Intent intent = getIntent();
        final Map <String, Object> lockinfo = (HashMap <String, Object>)
                intent.getSerializableExtra("lockinfo");
        //找到相应的组件

        initView();
        //看是否为开启状态
        setStatus(lockinfo);
        tv_blue_lockname.setText(lockinfo.get("lockName").toString());
        id = (Integer) lockinfo.get("id");
    }

    private void okHttp1( Integer id ) {
        tv_blue_status = findViewById(R.id.tv_blue_status);
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        //2.构造request
        Request request = builder.get()
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
    public void onResume() {
        super.onResume();

        //进页面时加载历史消息
        okHttp1(id);
    }

    private void setStatus( Map <String, Object> lockinfo ) {
        Integer useStatus = (Integer) lockinfo.get("useStatus");
        Integer enableStatus = (Integer) lockinfo.get("useStatus");
        Integer onlineStatus = (Integer) lockinfo.get("useStatus");
        switch (useStatus) {
            case 0:
                //未激活
                tv_blue_status.setText("未激活状态");
                break;
            case 1:
                if (enableStatus == 10) {
                    //在线状态
                    switch (onlineStatus) {
                        case 100: {
                            tv_blue_status.setText("关闭状态");
                        }
                        break;
                        case 101: {

                            tv_blue_status.setText("打开状态");
                        }
                        break;
                        case 102: {
                            tv_blue_status.setText("异常打开");
                        }
                        break;
                        case -1: {
                            tv_blue_status.setText("未知");
                        }
                        break;
                    }
                } else {
                    //离线状态
                    tv_blue_status.setText("离线状态");
                }
                break;
            case 2: {
                tv_blue_status.setText("停用状态");
            }
            break;
            case 3: {
                tv_blue_status.setText("废弃状态");
            }
            break;
        }
    }

    //找到相应的组件
    private void initView() {
        tv_blue_lockname = findViewById(R.id.tv_blue_lockname);
        tv_blue_status = findViewById(R.id.tv_blue_status);
        btn_confirm = findViewById(R.id.btn_cancel);
        btn_confirm = findViewById(R.id.btn_confirm);
    }

    public void btn_back7( View view ) {
        RemoteOpenActivity.this.finish();
    }

    public void remoteCancel( View view ) {
        finish();
    }

    public void remoteOpen( View view ) {
        //if (tv_blue_status.getText().equals("关闭状态")) {
            Toast.makeText(RemoteOpenActivity.this, "远程开锁中，请稍后", Toast.LENGTH_SHORT).show();
            okHttp();
       /* } else if (tv_blue_status.getText().equals("打开状态")) {
            Toast.makeText(RemoteOpenActivity.this, "智能锁已打开", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RemoteOpenActivity.this, "智能锁不在线", Toast.LENGTH_SHORT).show();
        }*/
    }

    public void okHttp() {
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.构造request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(server + ":" + port + "/app/lock/open?id=" + id)
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
                        Toast.makeText(RemoteOpenActivity.this, "无法连接服务器！", Toast.LENGTH_SHORT).show();
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

                            Toast.makeText(RemoteOpenActivity.this, "远程开锁成功!", Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(RemoteOpenActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

        });

    }
}
