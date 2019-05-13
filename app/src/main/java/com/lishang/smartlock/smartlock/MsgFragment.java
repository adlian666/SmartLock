package com.lishang.smartlock.smartlock;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.Activity.MainActivity;
import com.lishang.smartlock.smartlock.adapter.MsgRecycleAdapter;
import com.lishang.smartlock.smartlock.badgenumberlibrary.BadgeNumberManager;
import com.lishang.smartlock.smartlock.badgenumberlibrary.MobileBrand;
import com.lishang.smartlock.smartlock.functionclass.FileUtil;
import com.lishang.smartlock.smartlock.service.msgService;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.baidu.mapapi.BMapManager.getContext;


/**
 * A simple {@link Fragment} subclass.
 */
public class MsgFragment extends Fragment {
    public List <Map <String, Object>> list = new ArrayList <>();
    public List <String> mlist = new ArrayList <String>();
    private com.alibaba.fastjson.JSONArray jsonArray;
    private TextView tv_msg_lockname, tv_msg_time, tv_msg;
    private ImageView image_msg;
    private View view;
    private boolean lastPage;
    private RecyclerView rc_msg_list;
    private MsgRecycleAdapter msgRecycleAdapter;
    private URI uri;
    private RadioButton tab_btn_home, tab_btn_home1;
    private RadioButton tab_btn_msg, tab_btn_my1;
    private RadioButton tab_btn_my, tab_btn_msg1;
    private Button btn_clear;
    int msgNum;
    private RefreshLayout mRefreshLayout;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    private MyReceiver receiver = null;
    QBadgeView qBadgeView;
    private String server, port, cookie;
    private Integer page;
    int i = 2, p, rest;
    int size;
    JSONArray jsonArray1;

    public MsgFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        Intent intent = new Intent(getActivity(), msgService.class);
        //启动servicce服务
        getActivity().startService(intent);
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mEditor = pref.edit();
        msgNum = pref.getInt("msgNum", 0);
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        cookie = pref.getString("SmartLockId", "");
        view = inflater.inflate(R.layout.fragment_msg, null);
        mRefreshLayout = view.findViewById(R.id.refreshLayout_msg);
        mRefreshLayout.setEnableScrollContentWhenLoaded(true);
        initView(view);
        File file = new File(Environment.getExternalStorageDirectory(), "list.txt");
        try {
            if (file.exists()) {
                String str = FileUtil.getFile("list.txt");
                jsonArray1 = JSON.parseArray(str);
                Log.i("TAG12", "" + jsonArray1);
                size = jsonArray1.size();
                rest = size % 50;
                if (rest == 0) {
                    p = size / 50;
                } else {
                    p = size / 50 + 1;
                }
                if (p == 1) {
                    for (int j = 0; j < rest; j++) {
                        Log.i("TAG15", "asdfa = " + rest);
                        list.add(j, (Map <String, Object>) jsonArray1.get(j));

                    }
                } else {
                    for (int k = 0; k < 50; k++) {
                        list.add(k, (Map <String, Object>) jsonArray1.get(k));
                    }

                }
            } else {
                p = 10;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //注册广播接收器
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("sendMsg");
        getActivity().registerReceiver(receiver, filter);
        qBadgeView = new QBadgeView(getContext());
        setMsg(msgNum);
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                FileUtil.deletefile("list.txt");
                list.clear();
                JSONArray jsonArray1 = JSONArray.parseArray(JSONObject.toJSONString(list));
                FileUtil.saveFile(jsonArray1.toString(), "list.txt");
                mEditor.putInt("msgNum", 0);
                mEditor.commit();
                BadgeNumberManager.from(getContext()).setBadgeNumber(0);
                setMsg(0);
                Message m = new Message();
                m.what = 1;
                handler.sendMessage(m);
            }
        });


        //拖拽消除数字
        qBadgeView.setOnDragStateChangedListener(new Badge.OnDragStateChangedListener() {
            @Override
            public void onDragStateChanged( int dragState, Badge badge, View targetView ) {
                mEditor.putInt("msgNum", 0);
                mEditor.commit();
                msgNum = pref.getInt("msgNum", 0);
                BadgeNumberManager.from(getActivity()).setBadgeNumber(msgNum);
            }
        });
        return view;
    }

    private void setMsg( int msgNum ) {
        qBadgeView.bindTarget(tab_btn_msg1)
                .setBadgeText(String.valueOf(msgNum))
                .setBadgeBackgroundColor(getResources().getColor(R.color.red))
                .setGravityOffset(32, 0, true);
        if (msgNum == 0) {
            qBadgeView.hide(true);
        } else if (msgNum > 99) {
            qBadgeView.setBadgeText("99+");
        }
    }

    //获取广播数据
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive( Context context, Intent intent ) {
            String intentAction = intent.getAction();

            if (intentAction.equals("sendMsg")) {
                Map <String, Object> msg = (Map <String, Object>) intent.getSerializableExtra("msg");
                list.add(0, msg);
                Log.i("TAG14", list.toString());
                //设置内部消息数量提醒
                msgNum = pref.getInt("msgNum", 0);
                //setMsg(msgNum);
                Log.i("TAG15", "" + msgNum);
                setMsg(msgNum);
                Message m = new Message();
                m.what = 1;
                handler.sendMessage(m);
            }
        }
    }

    private void initView( View view ) {

        tab_btn_msg = getActivity().findViewById(R.id.tab_btn_msg);
        tab_btn_msg1 = getActivity().findViewById(R.id.tab_btn_msg1);
        tab_btn_my = getActivity().findViewById(R.id.tab_btn_my);
        tab_btn_my1 = getActivity().findViewById(R.id.tab_btn_my1);
        tab_btn_home1 = getActivity().findViewById(R.id.tab_btn_home1);
        tab_btn_home = getActivity().findViewById(R.id.tab_btn_home);
        rc_msg_list = view.findViewById(R.id.rc_msg_list);
        tv_msg_lockname = view.findViewById(R.id.tv_msg_lockname);
        tv_msg_time = view.findViewById(R.id.tv_msg_time);
        tv_msg = view.findViewById(R.id.tv_msg);
        image_msg = view.findViewById(R.id.image_msg);
        btn_clear = view.findViewById(R.id.btn_clear);
        rc_msg_list.setItemViewCacheSize(200);
    }

    @Override
    public void onResume() {
        super.onResume();
        //进页面时加载历史消息
        // list.clear();
        //okHttp();
        Message m = new Message();
        m.what = 1;
        handler.sendMessage(m);

    }

    private void okHttp() {
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        //2.构造request
        Log.i("TAG13", server);
        Log.i("TAG13", port);
        Request request = builder.get()
                // .url(server + ":" + port + "/app/msg/getSent")
                .url(server + ":" + port + "/app/msg/getSent?p=" + 1)
                .addHeader("Cookie", "SmartLockId=" + cookie)
                .build();
        //3.将request封装成call
        Call call = okHttpClient.newCall(request);
        //4.执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                // refreshLayout.finishRefresh(false);//结束刷新（刷新失败）
                // refreshLayout.finishLoadmore(false);
                Looper.prepare();
                Toast.makeText(getActivity(), "无法连接服务器！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                String res = response.body().string();
                Log.i("TAG17", res);
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(res);
                com.alibaba.fastjson.JSONObject jsonObject1 = jsonObject.getJSONObject("appmsgSentPage");
                page = jsonObject1.getInteger("totalPage");
                jsonArray = jsonObject1.getJSONArray("list");
                int lengths = jsonArray.size();
                for (int i = 0; i < lengths; i++) {
                    com.alibaba.fastjson.JSONObject jsonObjects = jsonArray.getJSONObject(i);
                    Map <String, Object> map = new HashMap <>();
                    //获取到json数据中的数组里的内容
                    Integer lockId = jsonObjects.getInteger("lockId");
                    Integer onlineStatus = jsonObjects.getInteger("onlineStatus");
                    String description = jsonObjects.getString("description");
                    Integer type = jsonObjects.getInteger("type");
                    String time = jsonObjects.getString("time");
                    Integer useStatus = jsonObjects.getInteger("useStatus");
                    Integer enableStatus = jsonObjects.getInteger("enableStatus");
                    //存入map
                    map.put("lockId", lockId);
                    map.put("time", time);
                    map.put("description", description);
                    map.put("onlineStatus", onlineStatus);
                    map.put("enableStatus", enableStatus);
                    map.put("useStatus", useStatus);
                    map.put("type", type);
                    //ArrayList集合
                    list.add(map);
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                    //refreshLayout.finishRefresh();//结束刷新
                    //refreshLayout.finishLoadmore();
                }
                //addOverLays();
            }
        });
    }

    //下拉刷新
    private void onLoadHttp() {
        File file = new File(Environment.getExternalStorageDirectory(), "list.txt");
        if (file.exists()||size!=0) {
            if (i < p) {
                for (int j = 50 * (i - 1); j < 50 * i; j++) {
                    list.add(50 * (i - 1), (Map <String, Object>) jsonArray1.get(j));
                }
                i++;
                mRefreshLayout.finishRefresh();//结束刷新
                mRefreshLayout.finishLoadmore();
                Message m = new Message();
                m.what = 1;
                handler.sendMessage(m);
            } else if (i == p) {
                for (int j = 50 * (i - 1); j < rest; j++) {
                    Log.i("TAG15", "asdfa = " + rest);
                    list.add(50 * (i - 1), (Map <String, Object>) jsonArray1.get(j));
                }
                mRefreshLayout.finishRefresh();//结束刷新
                mRefreshLayout.finishLoadmore();
                i = i + 1;
                Message m = new Message();
                m.what = 1;
                handler.sendMessage(m);
            } else if (i > p) {
                Log.i("TAG151","ASDFG");
                mRefreshLayout.finishRefresh(false);//结束刷新
                mRefreshLayout.finishLoadmore(false);
                Toast.makeText(getActivity(), "已经滑到底啦", Toast.LENGTH_SHORT).show();
            }

        }else{

            Log.i("TAG151","ASDFG");
            mRefreshLayout.finishRefresh(false);//结束刷新
            mRefreshLayout.finishLoadmore(false);
            Toast.makeText(getActivity(), "已经滑到底啦", Toast.LENGTH_SHORT).show();
        }

    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage( Message m ) {
            try {
                switch (m.what) {
                    case 1:
                        //qBadgeView.hide(false);
                        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

                            public void onRefresh( RefreshLayout refreshlayout ) {
                                try {
                                    i = 2;
                                    refreshlayout.finishRefresh();
                                    refreshlayout.finishRefresh();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        //加载更多
                        mRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
                            @Override
                            public void onLoadmore( RefreshLayout refreshlayout ) {
                                try {
                                    onLoadHttp();
                                    //msgRecycleAdapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        //添加分割线
                        // rc_home_list.addItemDecoration(new DividerItemDecoration(
                        //       getActivity(), DividerItemDecoration.VERTICAL));
                        msgRecycleAdapter = new MsgRecycleAdapter(getActivity(), list);
                        rc_msg_list.setAdapter(msgRecycleAdapter);
                        //设置布局显示格式
                        rc_msg_list.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                        // rc_home_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
