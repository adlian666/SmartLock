package com.lishang.smartlock.smartlock.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lishang.smartlock.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyModifyActivity extends Activity {
    public List <Map <String, Object>> list = new ArrayList <Map <String, Object>>();
    private Button btn_recy_date, btn_modify_submit;
    private EditText et_recy_wokername, et_recy_idcard, et_recy_phone;
    private TextView tv_modify_id, tv_modify_workerId;
    private RadioButton rbtn_recy_male, rbtn_recy_famale;
    int y, m, d;
    private String workerName, gender, birth, idCard, tel;
    ImageButton imbtn_blueopen_back;
    String res, msg, state, cookie, server, port;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        Intent intent = getIntent();
        final HashMap <String, Object> myinfo = (HashMap <String, Object>)
                intent.getSerializableExtra("myinfo");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_my_modify);
        initView();
        pref = PreferenceManager.getDefaultSharedPreferences(MyModifyActivity.this);
        mEditor = pref.edit();
        cookie = pref.getString("SmartLockId", "");
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        et_recy_wokername.setText(myinfo.get("workerName").toString());
        btn_recy_date.setText(myinfo.get("birth").toString());
        et_recy_idcard.setText(myinfo.get("idCard").toString());
        et_recy_phone.setText(myinfo.get("tel").toString());

        btn_recy_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                AlertDialog.Builder localBuilder = new AlertDialog.Builder(MyModifyActivity.this);
                //localBuilder.setTitle("选择出生日期").setIcon(R.mipmap.ic_launcher);
                Log.i("TAG15", "被电击了");
                final LinearLayout layout_alert = (LinearLayout) View.inflate(MyModifyActivity.this, R.layout.dateselect, null);
                localBuilder.setView(layout_alert);
                localBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt ) {
                        DatePicker datepicker1 = (DatePicker) layout_alert.findViewById(R.id.datepicker1);
                        y = datepicker1.getYear();
                        m = datepicker1.getMonth() + 1;
                        d = datepicker1.getDayOfMonth();
                        System.out.println("y:" + y + " m:" + m + " d:" + d);
                        btn_recy_date.setText(y + "年" + m + "月" + d + "日"); //  获取时间
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt ) {

                    }
                }).create().show();
            }
        });
        btn_modify_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //提交到服务器
                workerName = et_recy_wokername.getText().toString();
                birth = y + "-" + m + "-" + d;
                idCard = et_recy_idcard.getText().toString();
                tel = et_recy_phone.getText().toString();
                if (rbtn_recy_male.isChecked()) {
                    gender = "男";
                } else {
                    gender = "女";
                }
                if (workerName.equals("") || birth.equals("") || idCard.equals("") || gender.equals("") || tel.equals("")) {
                    Toast.makeText(MyModifyActivity.this, "输入不能为空！", Toast.LENGTH_SHORT).show();
                } else {
                    okHttp();

                }
            }
        });
        imbtn_blueopen_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                finish();
            }
        });
    }

    private void initView() {
        btn_modify_submit = findViewById(R.id.btn_modify_submit);
        et_recy_wokername = findViewById(R.id.et_recy_wokername);
        rbtn_recy_male = findViewById(R.id.rbtn_recy_male);
        rbtn_recy_famale = findViewById(R.id.rbtn_recy_famale);
        btn_recy_date = findViewById(R.id.btn_recy_date);
        et_recy_idcard = findViewById(R.id.et_recy_idcard);
        et_recy_phone = findViewById(R.id.et_recy_phone);
        rbtn_recy_male.isChecked();
        imbtn_blueopen_back = findViewById(R.id.imbtn_blueopen_back);
    }

    //提交
    private void okHttp() {

        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.构造request
        Request.Builder builder = new Request.Builder();
        //构造formbody
        FormBody formBody = new FormBody
                .Builder()
                .add("sWorker.workerName", workerName)
                .add("sWorker.birth", birth)
                .add("sWorker.idCard", idCard)
                .add("sWorker.gender", gender)
                .add("sWorker.tel", tel)
                .build();
        Request request = builder
                .url(server + ":" + port + "/app/worker/update")
                .addHeader("cookie", "SmartLockId=" + cookie)
                .post(formBody)
                .build();
        //3.将request封装成call
        Call call = okHttpClient.newCall(request);
        //4.执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                Looper.prepare();
                Toast.makeText(MyModifyActivity.this, "无法连接服务器！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                res = response.body().string();
                Log.i("TAG2", res);
                JSONObject jsonObject = JSON.parseObject(res);
                msg = jsonObject.getString("msg");
                state = jsonObject.getString("state");
                if (state.equals("ok")) {
                    Looper.prepare();
                    Toast.makeText(MyModifyActivity.this, msg, Toast.LENGTH_SHORT).show();
                    Looper.loop();
                } else {
                    Looper.prepare();
                    Toast.makeText(MyModifyActivity.this, msg, Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }

        });

    }
}
