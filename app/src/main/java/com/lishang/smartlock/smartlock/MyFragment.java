package com.lishang.smartlock.smartlock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.Activity.LoginActivity;
import com.lishang.smartlock.smartlock.Activity.MyModifyActivity;
import com.lishang.smartlock.smartlock.Activity.SetPswActivity;
import com.lishang.smartlock.smartlock.service.msgService;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment {
    private com.alibaba.fastjson.JSONArray jsonArray;
    public List <Map <String, Object>> list = new ArrayList <>();
    public String data;
    private View view;
    String workerId;
    String gender;
    String idCard;
    String birth;
    String tel;
    Integer id;
    String workerName;
    private TextView tv_recy_item0, tv_recy_item1, tv_recy_item2, tv_recy_item3, tv_recy_item4, tv_recy_item5,
            tv_recy_item6, tv_recy_item7;
    private Button btn_my_modify, btn_set_psw, logout;
    String res, cookie, server, port;
    Boolean ck_remember_psw;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;

    public MyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        //获取fragment的layout
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mEditor = pref.edit();
        cookie = pref.getString("SmartLockId", "");
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        ck_remember_psw = pref.getBoolean("ck_remember_psw", false);
        view = inflater.inflate(R.layout.fragment_my, container, false);
        intView(view);
        okhttpData();
        //退出登陆
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                mEditor.putString("et_password", "");
                mEditor.putBoolean("ck_remember_psw", false);
                mEditor.commit();

                Intent intent1 = new Intent(getActivity(), LoginActivity.class);
                //intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
                Intent intent = new Intent(getActivity(), msgService.class);
                getActivity().stopService(intent);
                getActivity().finish();
            }
        });

        //修改密码
        btn_set_psw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {

                Intent intent = new Intent(getActivity(), SetPswActivity.class);

                getActivity().startActivity(intent);

            }
        });
        //修改个人信息
        btn_my_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Map <String, Object> mlist = list.get(0);
                Intent intent = new Intent(getActivity(), MyModifyActivity.class);
                intent.putExtra("myinfo", (Serializable) mlist);
                getActivity().startActivity(intent);
            }
        });
        return view;
    }

    private void intView( View view ) {
        tv_recy_item0 = view.findViewById(R.id.tv_recy_item0);
        tv_recy_item1 = view.findViewById(R.id.tv_recy_item1);
        tv_recy_item2 = view.findViewById(R.id.tv_recy_item2);
        tv_recy_item3 = view.findViewById(R.id.tv_recy_item3);
        tv_recy_item4 = view.findViewById(R.id.tv_recy_item4);
        tv_recy_item5 = view.findViewById(R.id.tv_recy_item5);
        tv_recy_item6 = view.findViewById(R.id.tv_recy_item6);
        tv_recy_item7 = view.findViewById(R.id.tv_recy_item7);
        logout = view.findViewById(R.id.logout);
        btn_set_psw = view.findViewById(R.id.btn_set_psw);
        btn_my_modify = view.findViewById(R.id.btn_my_modify);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            list.clear();
            okhttpData();//刷新数据
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void okhttpData() {
        //1.okhttpClent对象
        OkHttpClient client = new OkHttpClient();
        //post传递参数
        // RequestBody formBody = new FormBody.Builder().build();
        //2.构造request
        Request request = new Request.Builder()
                .get()
                .addHeader("cookie", "SmartLockId=" + cookie)
                .url(server + ":" + port + "/app/worker")
                .build();
        //3将Request封装成call
        Call call = client.newCall(request);
        //4，执行call，这个方法是异步请求数据
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String myinfo = pref.getString("myinfo", "");
                        final com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(myinfo);
                        if (myinfo != null) {
                            //获取到json数据中的activity数组里的内容
                            workerId = jsonObject.getString("workerId");
                            gender = jsonObject.getString("gender");
                            idCard = jsonObject.getString("idCard");
                            birth = jsonObject.getString("birth");
                            tel = jsonObject.getString("tel");
                            id = jsonObject.getInteger("id");
                            workerName = jsonObject.getString("workerName");
                            tv_recy_item0.setText(workerName);
                            tv_recy_item1.setText("" + id);
                            tv_recy_item2.setText("" + workerId);
                            tv_recy_item3.setText(workerName);
                            tv_recy_item4.setText(gender);
                            tv_recy_item5.setText(birth);
                            tv_recy_item6.setText(idCard);
                            tv_recy_item7.setText(tel);
                        }
                        Toast.makeText(getActivity(), "无法连接服务器！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                res = response.body().string();
                Log.i("TAG13", res);
                final com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(res);
                mEditor.putString("myinfo", res);
                mEditor.commit();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (res != null) {
                            //获取到json数据中的activity数组里的内容
                            workerId = jsonObject.getString("workerId");
                            gender = jsonObject.getString("gender");
                            idCard = jsonObject.getString("idCard");
                            birth = jsonObject.getString("birth");
                            tel = jsonObject.getString("tel");
                            id = jsonObject.getInteger("id");
                            workerName = jsonObject.getString("workerName");
                            tv_recy_item0.setText(workerName);
                            tv_recy_item1.setText("" + id);
                            tv_recy_item2.setText("" + workerId);
                            tv_recy_item3.setText(workerName);
                            tv_recy_item4.setText(gender);
                            tv_recy_item5.setText(birth);
                            tv_recy_item6.setText(idCard);
                            tv_recy_item7.setText(tel);
                            //存入map
                            Map <String, Object> map = new HashMap <>();
                            map.put("workerId", workerId);
                            map.put("gender", gender);
                            map.put("idCard", idCard);
                            map.put("birth", birth);
                            map.put("tel", tel);
                            map.put("id", id);
                            map.put("workerName", workerName);
                            //ArrayList集合
                            list.add(map);
                           /* Message msg = new Message();
                            msg.what = 1;
                            handler.sendMessage(msg);*/
                        } else {
                            // Toast.makeText(getActivity(), "无法连接服务器！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
    }

}
