package com.lishang.smartlock.smartlock.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lishang.smartlock.R;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LockInfoActivity extends Activity {
    public HashMap <String, String> mlist;//列表存放数据
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView rc_lockinfo;
    private TextView tv_openlock_title, tv_lockinfo_locknumber, tv_lockinfo_address,
            tv_lockinfo_purpose, tv_lockinfo_latitude, tv_lockinfo_onlineStatus, tv_lockinfo_heartCycle,
            tv_lockinfo_description, tv_lockinfo_sectionId, tv_lockinfo_blueCode, tv_lockinfo_useStatus,
            tv_lockinfo_id, tv_lockinfo_, tv_lockinfo_lockName, tv_lockinfo_enableStatus, tv_lockinfo_longitude,
            tv_lockinfo_activeCode;
    private List <Map <String, Object>> list = new ArrayList <>();
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    String res, msg, state, cookie, server, port;

    private Button btn_modify_lockinfo, btn_modify_delete;
    Integer id;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_info);
        initView();
        //接收传过来的数据
        Intent intent = getIntent();
        HashMap <String, Object> lockinfo = (HashMap <String, Object>)
                intent.getSerializableExtra("lockinfo");
        //传到map中
        pref = PreferenceManager.getDefaultSharedPreferences(LockInfoActivity.this);
        mEditor = pref.edit();
        cookie = pref.getString("SmartLockId", "");
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        id = (Integer) lockinfo.get("id");
        okHttp();
        btn_modify_lockinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Map <String, Object> mlist = list.get(0);
                Intent intent = new Intent(LockInfoActivity.this, ModifyLockinfoActivity.class);
                intent.putExtra("myinfo", (Serializable) mlist);
                startActivity(intent);
                System.out.println(mlist.toString());
            }
        });
        btn_modify_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                showdialog();
            }

        });
       /*Message msg=new Message();
        msg.what=1;
        handler.sendMessage(msg);*/
    }

    public void okHttp() {
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.构造request
        Request.Builder builder = new Request.Builder();
        Log.i("TAG333", "DDDDDDDDDDDDDD");
        Request request = builder
                .url(server + ":" + port + "/app/lock/getLock?id=" + id)
                .addHeader("cookie", "SmartLockId=" + cookie)
                .get()
                .build();

        //3.将request封装成call
        Call call = okHttpClient.newCall(request);
        Log.i("TAG333", "2");
        //4.执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("TAG3", "3");
                        Toast.makeText(LockInfoActivity.this, "无法连接服务器！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                res = response.body().string();
                Log.i("TAG", res);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = JSON.parseObject(res);
                        Log.i("TAG333",""+res);
                        JSONObject jsonObject1 = jsonObject.getJSONObject("lock");
                        Log.i("TAG333",""+jsonObject1);
                        tv_lockinfo_locknumber.setText(jsonObject1.get("lockNumber").toString());
                        //tv_lockinfo_id.setText(jsonObject1.get("id").toString());
                        tv_lockinfo_blueCode.setText(jsonObject1.get("blueCode").toString());
                        tv_lockinfo_address.setText(jsonObject1.get("address").toString());
                        tv_lockinfo_purpose.setText(jsonObject1.get("purpose").toString());
                        tv_lockinfo_latitude.setText(jsonObject1.get("latitude").toString());
                        //tv_lockinfo_onlineStatus.setText(jsonObject1.get("onlineStatus").toString());
                       // tv_lockinfo_heartCycle.setText(jsonObject1.get("heartCycle").toString());
                        tv_lockinfo_description.setText(jsonObject1.get("description").toString());
                        tv_lockinfo_sectionId.setText(jsonObject1.get("sectionId").toString());
                        tv_lockinfo_lockName.setText(jsonObject1.get("lockName").toString());
                       // tv_lockinfo_useStatus.setText(jsonObject1.get("useStatus").toString());
                        tv_lockinfo_longitude.setText(jsonObject1.get("longitude").toString());
                       // tv_lockinfo_enableStatus.setText(jsonObject1.get("enableStatus").toString());
                        tv_lockinfo_activeCode.setText(jsonObject1.get("activeCode").toString());
                        tv_lockinfo_longitude.setText(jsonObject1.get("longitude").toString());
                        Map <String, Object> map = new HashMap <>();
                        map.put("lockName", jsonObject1.get("lockName"));
                        map.put("lockNumber", jsonObject1.get("lockNumber"));
                        map.put("blueCode", jsonObject1.get("blueCode"));
                        map.put("address", jsonObject1.get("address"));
                        map.put("purpose", jsonObject1.get("purpose"));
                        map.put("authCode", jsonObject1.get("authCode"));
                        map.put("latitude", jsonObject1.get("latitude"));
                        map.put("description", jsonObject1.get("description"));
                        map.put("sectionId", jsonObject1.get("sectionId"));
                       // map.put("useStatus", jsonObject1.get("useStatus"));
                        map.put("id", jsonObject1.get("id"));
                      //  map.put("enableStatus", jsonObject1.get("enableStatus"));
                      //  map.put("onlineStatus", jsonObject1.get("onlineStatus"));
                        map.put("activeCode", jsonObject1.get("activeCode"));
                        map.put("longitude", jsonObject1.get("longitude"));
                      //  map.put("heartCycle", jsonObject1.get("heartCycle"));
                        map.put("activeCode", jsonObject1.get("activeCode"));
                        list.add(map);
                    }
                });

            }

        });

    }

    private void initView() {
        tv_lockinfo_locknumber = findViewById(R.id.tv_lockinfo_locknumber);
        tv_lockinfo_lockName = findViewById(R.id.tv_lockinfo_lockName);
        tv_lockinfo_blueCode = findViewById(R.id.tv_lockinfo_blueCode);
        tv_lockinfo_address = findViewById(R.id.tv_lockinfo_address);
        tv_lockinfo_purpose = findViewById(R.id.tv_lockinfo_purpose);
        tv_lockinfo_latitude = findViewById(R.id.tv_lockinfo_latitude);
        tv_lockinfo_blueCode = findViewById(R.id.tv_lockinfo_blueCode);
        tv_lockinfo_description = findViewById(R.id.tv_lockinfo_description);
        tv_lockinfo_sectionId = findViewById(R.id.tv_lockinfo_sectionId);
        tv_lockinfo_longitude = findViewById(R.id.tv_lockinfo_longitude);
        tv_lockinfo_activeCode = findViewById(R.id.tv_lockinfo_activeCode);
        btn_modify_lockinfo = findViewById(R.id.btn_modify_lockinfo);
        btn_modify_delete = findViewById(R.id.btn_modify_delete);
    }

    @Override
    public void onResume() {
        super.onResume();
        //在Fragment执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        list.clear();
        okHttp();
        //调用LocationClient的start()方法，便可发起定位请求
    }
    public void btn_back5( View view ) {
        LockInfoActivity.this.finish();
    }
    public void showdialog(){
        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(LockInfoActivity.this);
        //    设置Title的图标
        builder.setIcon(R.mipmap.close);
        //    设置Title的内容
        builder.setTitle("废弃锁");
        //    设置Content来显示一个信息
        builder.setMessage("确定废弃当前锁吗？");
        //    设置一个PositiveButton
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick( DialogInterface dialog, int which ) {
                discard();
            }
        });
        //    设置一个NegativeButton
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int which ) {

            }
        });

        //    显示出该对话框
        builder.show();
    }
    public void discard(){
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.构造request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(server + ":" +  port +"/app/lock/setUseStatusDiscarded?id=" + id)
                .addHeader("cookie", "SmartLockId=" + cookie)
                .get()
                .build();
        //3.将request封装成call
        Call call = okHttpClient.newCall(request);
        //4.执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                Looper.prepare();
                Toast.makeText(LockInfoActivity.this, msg, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                res = response.body().string();
                JSONObject jsonObject = JSON.parseObject(res);
                msg = jsonObject.getString("msg");
                state = jsonObject.getString("state");
                if (state.equals("ok")) {
                    Looper.prepare();
                    Toast.makeText(LockInfoActivity.this, msg, Toast.LENGTH_SHORT).show();
                    Looper.loop();
                } else {
                    Looper.prepare();
                    Toast.makeText(LockInfoActivity.this, msg, Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }

        });
    }
}
