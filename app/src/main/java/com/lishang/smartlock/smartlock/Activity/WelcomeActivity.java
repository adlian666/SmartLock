package com.lishang.smartlock.smartlock.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.Utils.LoadingDialogUtils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WelcomeActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    boolean isRemember;
    String workername;
    String password;
    String port;
    String server;
    String res;
    int accountId;
    private String account, state, msg, SmartLockId;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = pref.edit();
        isRemember = pref.getBoolean("ck_remember_psw", false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getHome();
            }
        },1000);
        //handler.removeCallbacks(runnable);
        //handler.postDelayed(runnable,1000);

    }
    /*private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {
            handler.removeCallbacks(runnable);
            getHome();
        }
    };*/
    public void getHome() {
        if (isRemember) {
            okhttp();
        } else {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void okhttp() {

        OkHttpClient okHttpClient = new OkHttpClient();
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        workername = pref.getString("et_workername", "");
        password = pref.getString("et_password", "");
        //1.拿到httpClient对象
        //2.构造request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(server + ":" + port + "/login/doLogin?userName=" + workername + "&password=" + password + "&captcha=9ga5")
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
                        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(WelcomeActivity.this, "无法连接服务器！", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                res = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = JSON.parseObject(res);
                        JSONObject jsonObject1 = jsonObject.getJSONObject("loginAccount");
                        state = jsonObject.getString("state");
                        msg = jsonObject.getString("msg");
                        if (state.equals("ok")) {

                            SmartLockId = jsonObject.getString("SmartLockId");
                            accountId = (int) jsonObject1.get("id");
                            mEditor.putString("SmartLockId", SmartLockId);
                            mEditor.putInt("accountId", accountId);
                            mEditor.commit();
                            Intent intent = new Intent(WelcomeActivity.this
                                    , MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (state.equals("fail")) {
                            //子线程中弹出土司
                            Toast.makeText(WelcomeActivity.this, msg, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(WelcomeActivity.this, msg, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(WelcomeActivity.this
                                    , LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

            }

        });
    }
}
