package com.lishang.smartlock.smartlock.Activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import android.widget.Toast;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.lishang.smartlock.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 登陆界面：通过用户名和密码登陆.
 */
public class LoginActivity extends Activity {

    private com.alibaba.fastjson.JSONArray jsonArray;
    private Dialog mdialog;
    private EditText et_password, et_port, et_server;
    private EditText et_workername;
    private Button btn_setcancel, btn_setconfirm;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    private CheckBox ck_remember_psw;
    String workername;
    String password;
    String port;
    String server;
    String res;
    int accountId;
    private String account, state, msg, SmartLockId;
    ClearableCookieJar cookieJar;
    OkHttpClient okHttpClient = new OkHttpClient();
    boolean isRemember;
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = pref.edit();
        workername = pref.getString("et_workername", "");
        password = pref.getString("et_password", "");
        isRemember = pref.getBoolean("ck_remember_psw", false);
        try {
           /* if (isRemember) {
                Intent intent = new Intent(LoginActivity.this
                        , MainActivity.class);
                startActivity(intent);
                finish();
            }*/
            setContentView(R.layout.activity_login);
            //找到控件
            initView();
            //从SharedPreferences中读取数据,先判断复选框是否选中；若选中则直接把账号、密码读取显示在页面中
            //1.打开一个SharedPreferences的数据Map
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isRemember) {
                        //设置到文本框中
                        et_workername.setText(workername);
                        et_password.setText(password);
                        ck_remember_psw.setChecked(true);
                    } else {
                        et_workername.setText(workername);
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {

        et_password = findViewById(R.id.et_password);
        et_workername = findViewById(R.id.et_workername);
        ck_remember_psw = findViewById(R.id.ck_remember_psw);
    }

    //登陆按钮
    public void login( View v ) {

        if (checklogin() && setserver() ) {
            Toast.makeText(LoginActivity.this, "正在登陆，请稍后", Toast.LENGTH_SHORT).show();
            workername = et_workername.getText().toString();
            password = et_password.getText().toString();
            okHttp();
        }else{
           // Toast.makeText(LoginActivity.this,"输入不能为空!",Toast.LENGTH_SHORT).show();
        }
    }

    public void okHttp() {
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        Log.i("TAG7777",server+workername+password);
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
                        Toast.makeText(LoginActivity.this, "无法连接服务器！", Toast.LENGTH_SHORT).show();
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
                            rememberPsw();
                            Intent intent = new Intent(LoginActivity.this
                                    , MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (state.equals("fail")) {
                            //子线程中弹出土司
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

        });

    }

    private void rememberPsw() {
        if (ck_remember_psw.isChecked()) {
            mEditor.putBoolean("ck_remember_psw", true);
            mEditor.putString("et_workername", workername);
            mEditor.putString("et_password", password);
        } else {
            mEditor.putString("et_password", "");
            mEditor.putString("et_workername", workername);
        }
        mEditor.commit();
    }

    //检查用户名和密码是否为空
    private boolean checklogin() {
        workername = et_workername.getText().toString();
        password = et_password.getText().toString();

        if (workername.equals("")) {
            Toast.makeText(LoginActivity.this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.equals("")) {
            Toast.makeText(LoginActivity.this, "密码不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        mEditor.putString("workername", workername);
        mEditor.putString("password", password);
        mEditor.commit();
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    //设置服务器参数按钮点击事件
    public void setServer( View view ) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        final View dialogview = View.inflate(LoginActivity.this, R.layout.dialog_sethttp, null);
        //初始化组件
        btn_setconfirm = dialogview.findViewById(R.id.btn_setconfirm);
        btn_setcancel = dialogview.findViewById(R.id.btn_setcancel);
        et_server = dialogview.findViewById(R.id.et_server);
        et_port = dialogview.findViewById(R.id.et_port);
        builder.setView(dialogview);
        final AlertDialog dialog = builder.show();
        //3.数据的读取
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        et_server.setText(server);
        et_port.setText(port);
//dialog取消和确定按钮的监听
        btn_setconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //2.数据的写入
                //mEditor = pref.edit();
                server = et_server.getText().toString();
                port = et_port.getText().toString();
                mEditor.putString("server", server);
                mEditor.putString("port", port);
                mEditor.commit();
                dialog.dismiss();
            }
        });
        btn_setcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                dialog.dismiss();
            }
        });

    }

    //检查服务器格式
    public boolean setserver() {
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        if (TextUtils.isEmpty(server+":"+port)) {
            Toast.makeText(this, "服务器不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.WEB_URL.matcher(server+":"+port).matches()) {
            Toast.makeText(this, "服务器地址非法，请输入有效的服务器地址", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}


