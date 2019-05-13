package com.lishang.smartlock.smartlock.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.service.msgService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SetPswActivity extends Activity {
    Integer id;
    String res, msg, state, cookie, tvstatus,oldPassword,newPassword,reNewPassword,server,port;;
private EditText et_psw,et_newpsw,et_repsw;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_psw);
        initView();
        pref = PreferenceManager.getDefaultSharedPreferences(SetPswActivity.this);
        mEditor = pref.edit();
        cookie = pref.getString("SmartLockId", "");
        server = pref.getString("server","");
        port = pref.getString("port","");
    }

    private void initView() {
        et_psw = findViewById(R.id.et_psw);
        et_newpsw = findViewById(R.id.et_newpsw);
        et_repsw = findViewById(R.id.et_repsw);
    }

    public void setPsw( View view ) {
        oldPassword = et_psw.getText().toString();
        newPassword = et_newpsw.getText().toString();
        reNewPassword = et_repsw.getText().toString();
        if(newPassword.equals(reNewPassword) ){
            okHttp();
        }else{
            Toast.makeText(SetPswActivity.this,"输入两次密码不一致，请重新输入！",Toast.LENGTH_SHORT).show();
        }

    }

    private void okHttp() {
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
                .url(server + ":" +  port +"/app/worker/updatePassword")
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
                        Toast.makeText(SetPswActivity.this, "无法连接服务器！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                res = response.body().string();
                final  com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(res);
                msg = jsonObject.getString("msg");
                state = jsonObject.getString("state");
                Log.i("TAG12","测试" + state);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (state.equals("ok")){
                            Intent intent1 = new Intent(SetPswActivity.this, LoginActivity.class);
                            startActivity(intent1);
                            Toast.makeText(SetPswActivity.this, msg, Toast.LENGTH_SHORT).show();
                            mEditor.putString("et_password", "");
                            mEditor.putBoolean("ck_remember_psw", false);
                            mEditor.commit();
                            Intent intent = new Intent(SetPswActivity.this, msgService.class);
                            stopService(intent);
                            finish();

                        } else {
                             Toast.makeText(SetPswActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                  }
                });


            }
        });
    }

    public void backLast1( View view ) {
        finish();
    }
}
