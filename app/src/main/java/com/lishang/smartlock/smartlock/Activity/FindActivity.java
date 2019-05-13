package com.lishang.smartlock.smartlock.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.Utils.LoadingDialogUtils;
import com.lishang.smartlock.smartlock.adapter.FindlockRecycleAdapter;
import com.lishang.smartlock.smartlock.adapter.HomeRecycleAdapter;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FindActivity extends Activity {
    String input;private com.alibaba.fastjson.JSONArray jsonArray;
    private EditText et_find_input;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView rc_findlock;
    private FindlockRecycleAdapter findlockRecycleAdapter;
    public List <Map <String, Object>> list = new ArrayList <Map <String, Object>>();
    public List <Map <String, Object>> mlist = new ArrayList <Map <String, Object>>();
    public List <Map <String, Object>> mmlist = new ArrayList <Map <String, Object>>();
    public HomeRecycleAdapter homeRecycleAdapter;
    String et;int i = 2;private String cookie,server,port;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
     Integer page;ImageButton ibtn_find;  RefreshLayout mRefreshLayout;
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        //接收homeFragment传过来的数据
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = pref.edit();
        cookie = pref.getString("SmartLockId", "");
        server = pref.getString("server","");
        port = pref.getString("port","");
        //
        /*Intent intent = this.getIntent();
        list = (List <Map <String, Object>>) intent.getSerializableExtra("findlockinfo");*/
        initView();


        ibtn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                LoadingDialogUtils.show(FindActivity.this,"正在查询，请稍后...");

                list.clear();
                findHttp();
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
                LoadingDialogUtils.dismiss();
            }
        });


    }

    private void findHttp() {
        //1.拿到httpClient对象
        et = et_find_input.getText().toString();
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        //2.构造request
        Log.i("TAG14",et);
        Request request = builder.get()
                .url(server+":" + port + "/app/lock/getLockList?p=1&keyword=" + et)
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
                Log.i("TAG121",res);
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(res);
                com.alibaba.fastjson.JSONObject jsonObject1 = jsonObject.getJSONObject("lockList");
                page = jsonObject1.getInteger("totalRow");
                if (page == 0){
                    Looper.prepare();
                    Toast.makeText(FindActivity.this, "没有找到该锁！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else{
                    jsonArray = jsonObject1.getJSONArray("list");
                    int lengths = jsonArray.size();
                    for (int i = 0; i < lengths; i++) {
                        com.alibaba.fastjson.JSONObject jsonObjects = jsonArray.getJSONObject(i);
                        Map <String, Object> map = new HashMap <>();
                        //获取到json数据中的数组里的内容
                        String lockName = jsonObjects.getString("lockName");
                        String address = jsonObjects.getString("address");
                        String authCode = jsonObjects.getString("authCode");
                        String purpose = jsonObjects.getString("purpose");
                        Float latitude = jsonObjects.getFloat("latitude");
                        Integer onlineStatus = jsonObjects.getInteger("onlineStatus");
                        Integer heartCycle = jsonObjects.getInteger("heartCycle");
                        String description = jsonObjects.getString("description");
                        Integer sectionId = jsonObjects.getInteger("sectionId");
                        String blueCode = jsonObjects.getString("blueCode");
                        Integer useStatus = jsonObjects.getInteger("useStatus");
                        Integer useStatusSyn = jsonObjects.getInteger("useStatusSyn");
                        Integer id = jsonObjects.getInteger("id");
                        Integer enableStatus = jsonObjects.getInteger("enableStatus");
                        Float longitude = jsonObjects.getFloat("longitude");
                        String activeCode = jsonObjects.getString("activeCode");
                        String lockNumber = jsonObjects.getString("lockNumber");
                        //存入map
                        map.put("useStatusSyn", useStatusSyn);
                        map.put("lockNumber", lockNumber);
                        map.put("lockName", lockName);
                        map.put("address", address);
                        map.put("authCode", authCode);
                        map.put("purpose", purpose);
                        map.put("latitude", latitude);
                        map.put("onlineStatus", onlineStatus);
                        map.put("heartCycle", heartCycle);
                        map.put("description", description);
                        map.put("sectionId", sectionId);
                        map.put("blueCode", blueCode);
                        map.put("useStatus", useStatus);
                        map.put("id", id);
                        map.put("enableStatus", enableStatus);
                        map.put("longitude", longitude);
                        map.put("activeCode", activeCode);
                        //ArrayList集合
                        list.add(map);
                        Message msg = new Message();
                        msg.what = 1;
                        handler.sendMessage(msg);
                }

                }

            }

        });
            }
        });
    }

    private void initList() {
        for (int i = 0; i < list.size(); i++) {
            mlist.add(list.get(i));
        }
    }

    private void initView() {
        rc_findlock = findViewById(R.id.rc_findlock);
        et_find_input = findViewById(R.id.et_find_input);
        ibtn_find = findViewById(R.id.ibtn_find);
        mRefreshLayout = findViewById(R.id.find_refreshLayout);
    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage( Message msg ) {
            switch (msg.what) {
                case 1:
                    //加载更多

                    mRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
                        @Override
                        public void onLoadmore( RefreshLayout refreshlayout ) {
                            onLoadHttp();
                            findlockRecycleAdapter.notifyDataSetChanged();
                            refreshlayout.finishLoadmore();
                        }
                    });
                    //添加分割线
                    // rc_home_list.addItemDecoration(new DividerItemDecoration(
                    //       getActivity(), DividerItemDecoration.VERTICAL));
                    findlockRecycleAdapter = new FindlockRecycleAdapter(FindActivity.this, list);
                    rc_findlock.setAdapter(findlockRecycleAdapter);
                    Log.i("TAG1111", "asdafsdf");
                    rc_findlock.setLayoutManager(new LinearLayoutManager(FindActivity.this, LinearLayoutManager.VERTICAL, false));
                    Log.i("TAG1111", "1234234");

                    //设置搜索框的监听事件
                    /*et_find_input.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged( CharSequence sequence, int i, int i1, int i2 ) {
                            homeRecycleAdapter = new HomeRecycleAdapter(FindActivity.this, mlist);
                            rc_findlock.setAdapter(homeRecycleAdapter);
                        }
                        @Override
                        public void onTextChanged( CharSequence sequence, int i, int i1, int i2 ) {
                            homeRecycleAdapter.getFilter().filter(sequence.toString());

                        }

                        @Override
                        public void afterTextChanged( Editable editable ) {

                        }
                    });*/
                    //设置布局显示格式

                    // rc_home_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
                    break;
            }
        }
    };

    private void onLoadHttp() {
        Log.i("TAG7", "6");
        et = et_find_input.getText().toString();
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Log.i("TAG7", "5");
        //2.构造request
        Log.i("TAG8", "" + i);
        Request request = builder.get()
                .url(server+":" + port +"/app/lock/getLockList?p=" + i+"&keyword="+ et)
                .addHeader("Cookie", "SmartLockId=" + cookie)
                .build();
        Log.i("TAG7", "4");
        //3.将request封装成call
        Call call = okHttpClient.newCall(request);
        Log.i("TAG7", "3");
        //4.执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                Log.i("TAG1", "shibas");
            }

            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                Log.i("TAG7", "2");
                String res = response.body().string();
                Log.i("TAG7", "21" + res);
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(res);
                Log.i("TAG7", "1");
                com.alibaba.fastjson.JSONObject jsonObject1 = jsonObject.getJSONObject("lockList");
                jsonArray = jsonObject1.getJSONArray("list");
                int lengths = jsonArray.size();
                Log.i("TAG7", "1");
                for (int j = 0; j < lengths; j++) {
                    com.alibaba.fastjson.JSONObject jsonObjects = jsonArray.getJSONObject(j);
                    Map <String, Object> map = new HashMap <>();
                    //获取到json数据中的数组里的内容
                    String lockName = jsonObjects.getString("lockName");
                    String address = jsonObjects.getString("address");
                    String authCode = jsonObjects.getString("authCode");
                    String purpose = jsonObjects.getString("purpose");
                    Float latitude = jsonObjects.getFloat("latitude");
                    Integer onlineStatus = jsonObjects.getInteger("onlineStatus");
                    Integer heartCycle = jsonObjects.getInteger("heartCycle");
                    String description = jsonObjects.getString("description");
                    Integer sectionId = jsonObjects.getInteger("sectionId");
                    String blueCode = jsonObjects.getString("blueCode");
                    Integer useStatus = jsonObjects.getInteger("useStatus");
                    Integer useStatusSyn = jsonObjects.getInteger("useStatusSyn");
                    Integer id = jsonObjects.getInteger("id");
                    Integer enableStatus = jsonObjects.getInteger("enableStatus");
                    Float longitude = jsonObjects.getFloat("longitude");
                    String activeCode = jsonObjects.getString("activeCode");
                    String lockNumber = jsonObjects.getString("lockNumber");
                    //存入map
                    map.put("useStatusSyn", useStatusSyn);
                    map.put("lockNumber", lockNumber);
                    map.put("lockName", lockName);
                    map.put("address", address);
                    map.put("authCode", authCode);
                    map.put("purpose", purpose);
                    map.put("latitude", latitude);
                    map.put("onlineStatus", onlineStatus);
                    map.put("heartCycle", heartCycle);
                    map.put("description", description);
                    map.put("sectionId", sectionId);
                    map.put("blueCode", blueCode);
                    map.put("useStatus", useStatus);
                    map.put("id", id);
                    map.put("enableStatus", enableStatus);
                    map.put("longitude", longitude);
                    map.put("activeCode", activeCode);
                    //ArrayList集合
                    list.add(map);

                }
                //addOverLays();
            }
        });

        i++;


    }

    public void btn_back3( View view ) {
        Intent intent = new Intent(FindActivity.this
                , MainActivity.class);
        startActivity(intent);
        finish();
    }
}
