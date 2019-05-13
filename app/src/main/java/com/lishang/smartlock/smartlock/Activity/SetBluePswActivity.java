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
import android.widget.ImageButton;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.service.msgService;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SetBluePswActivity extends Activity {
private ImageButton imbtn_setblue_back;
    private EditText et_bluepsw;
    private EditText et_newbluepsw;
    private EditText et_rebluepsw;
    private Button btn_set_newbluepsw;
    String res, msg, state, cookie,oldPassword,newPassword,reNewPassword,server,port;
    Integer lockid;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_blue_psw);
        initView();
        Intent intent = getIntent();
        final Map<String, Object> lockinfo = (HashMap<String, Object>)
                intent.getSerializableExtra("lockinfo");
        pref = PreferenceManager.getDefaultSharedPreferences(SetBluePswActivity.this);
        mEditor = pref.edit();
        cookie = pref.getString("SmartLockId", "");
        server = pref.getString("server","");
        port = pref.getString("port","");
        lockid  = (Integer) lockinfo.get("id");

        imbtn_setblue_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Intent intent = new Intent(SetBluePswActivity.this, OpenLockActivity.class);
                startActivity(intent);
            }
        });
        btn_set_newbluepsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                oldPassword = et_bluepsw.getText().toString();
                newPassword = et_newbluepsw.getText().toString();
                reNewPassword = et_rebluepsw.getText().toString();
                if (oldPassword.equals("")) {
                    Toast.makeText(SetBluePswActivity.this, "原密码不能为空！", Toast.LENGTH_SHORT).show();

                }else if (newPassword.equals("")) {
                    Toast.makeText(SetBluePswActivity.this, "新密码不能为空！", Toast.LENGTH_SHORT).show();

                } else if(newPassword.equals(reNewPassword) ){
                    Log.i("lockid","测试" + lockid);
                    okHttp(lockid);
                }else{
                    Toast.makeText(SetBluePswActivity.this,"输入两次密码不一致，请重新输入！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void okHttp( Integer lockid ) {
        //1.okhttpClent对象
        OkHttpClient client = new OkHttpClient();
        //post传递参数
        RequestBody formBody = new FormBody
                .Builder()
                .add("oldPassword",oldPassword)
                .add("newPassword",newPassword)
                .build();
        //2.构造request
        Request request = new Request.Builder()
                .post(formBody)
                .addHeader("cookie", "SmartLockId=" + cookie)
                .url(server + ":" +  port +"/app/lock/setBluecode?id="+lockid)
                .build();
        //3将Request封装成call
        Call call = client.newCall(request);
        //4，执行call，这个方法是异步请求数据
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SetBluePswActivity.this, "无法连接服务器！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                res = response.body().string();
                Log.i("SetBluePswActivity","测试" + res);
                final  com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(res);
                msg = jsonObject.getString("msg");
                state = jsonObject.getString("state");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (state.equals("ok")){
                            Intent intent1 = new Intent(SetBluePswActivity.this, OpenLockActivity.class);
                            startActivity(intent1);
                            Toast.makeText(SetBluePswActivity.this, msg, Toast.LENGTH_SHORT).show();
                            finish();

                        } else {
                            Toast.makeText(SetBluePswActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
    }

    private void initView() {
        imbtn_setblue_back = (ImageButton)findViewById(R.id.imbtn_setblue_back);
        et_bluepsw = (EditText)findViewById(R.id.et_bluepsw);
        et_newbluepsw = (EditText)findViewById(R.id.et_newbluepsw);
        et_rebluepsw = (EditText)findViewById(R.id.et_rebluepsw);
        btn_set_newbluepsw = (Button)findViewById(R.id.btn_set_newbluepsw);

    }
}
