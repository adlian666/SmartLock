package com.lishang.smartlock.smartlock.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.Activity.FindActivity;
import com.lishang.smartlock.smartlock.Activity.OpenLockActivity;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

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
import q.rorbin.badgeview.QBadgeView;

import static android.media.CamcorderProfile.get;
import static com.baidu.mapapi.BMapManager.getContext;
import static java.lang.String.*;

public class MsgRecycleAdapter extends RecyclerView.Adapter <MsgRecycleAdapter.msgViewHodler> {
    private Context context;
    public QBadgeView qBadgeView;
    private ImageView image_msg;
    public List <Map <String, Object>> list = new ArrayList <>();
    public List <Map <String, Object>> mmlist = new ArrayList <>();
    public LayoutInflater inflater;
    boolean isRead;
    private com.alibaba.fastjson.JSONArray jsonArray;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    private URI uri;
    private String cookie, server, port;
    View msgitemView;
    private TextView tv_msg_lockname, tv_msg_time, tv_msg;
    Integer type;
    private ImageView img_pot;
    private Map <String, Object> map;

    //创建构造函数
    public MsgRecycleAdapter( Context context, List <Map <String, Object>> list ) {
        //将传递过来的数据，赋值给本地变量
        this.context = context;//上下文
        this.list = list;
        inflater = LayoutInflater.from(context);
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = pref.edit();
        cookie = pref.getString("SmartLockId", "");
        server = pref.getString("server", "");
        port = pref.getString("port", "");
        isRead = pref.getBoolean("isRead", false);

    }

    public msgViewHodler onCreateViewHolder( ViewGroup parent, int viewType ) {
        //创建itemlayout布局
        msgitemView = (LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg, parent, false));
        return new msgViewHodler(msgitemView);
    }


    class msgViewHodler extends RecyclerView.ViewHolder {
        private RecyclerView rc_msg_list;
        private TextView tv_msg_lockname, tv_msg_time, tv_msg;
        private ImageView image_msg, img_pot;

        public msgViewHodler( View msgitemView ) {
            super(msgitemView);

            tv_msg_lockname = msgitemView.findViewById(R.id.tv_msg_lockname);
            tv_msg_time = msgitemView.findViewById(R.id.tv_msg_time);
            tv_msg = msgitemView.findViewById(R.id.tv_msg);
            image_msg = msgitemView.findViewById(R.id.image_msg);
            rc_msg_list = msgitemView.findViewById(R.id.rc_msg_list);
            img_pot = msgitemView.findViewById(R.id.img_pot);
            img_pot.setVisibility(View.GONE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    if (type == 2) {
                        int i = getLayoutPosition();
                        String lockId = (list.get(i).get("lockId").toString());
                        okhttp(lockId);
                    }


                }

            });
        }
    }

    private void okhttp( String lockId ) {
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        //2.构造request
        Request request = builder.get()
                .url(server + ":" + port + "/app/lock/getLock?id=" + lockId)
                .addHeader("Cookie", "SmartLockId=" + cookie)
                .build();
        //3.将request封装成call
        final Call call = okHttpClient.newCall(request);
        //4.执行call
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


                    map = new HashMap <>();
                    //获取到json数据中的数组里的内容
                    String lockName = jsonObject1.getString("lockName");
                    String address = jsonObject1.getString("address");
                    String authCode = jsonObject1.getString("authCode");
                    String purpose = jsonObject1.getString("purpose");
                    Float latitude = jsonObject1.getFloat("latitude");
                    Integer onlineStatus = jsonObject1.getInteger("onlineStatus");
                    Integer heartCycle = jsonObject1.getInteger("heartCycle");
                    String description = jsonObject1.getString("description");
                    Integer sectionId = jsonObject1.getInteger("sectionId");
                    String blueCode = jsonObject1.getString("blueCode");
                    Integer useStatus = jsonObject1.getInteger("useStatus");
                    Integer useStatusSyn = jsonObject1.getInteger("useStatusSyn");
                    Integer id = jsonObject1.getInteger("id");
                    Integer enableStatus = jsonObject1.getInteger("enableStatus");
                    Float longitude = jsonObject1.getFloat("longitude");
                    String activeCode = jsonObject1.getString("activeCode");
                    String lockNumber = jsonObject1.getString("lockNumber");
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
                    Intent intent = new Intent(context, OpenLockActivity.class);
                    intent.putExtra("lockinfo", (Serializable) map);
                    context.startActivity(intent);
                }


        });

    }

    /**
     * 设置item的监听事件的接口
     */
    public interface OnItemClickListener {
        void OnItemClick( View view, Map <String, Object> list );

    }

    //需要外部访问，所以需要设置set方法，方便调用
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener( OnItemClickListener onItemClickListener ) {
        this.onItemClickListener = onItemClickListener;

    }

    @Override
    public void onBindViewHolder( msgViewHodler holder, int position ) {

        holder.tv_msg_lockname.setText(list.get(position).get("lockId").toString());
        holder.tv_msg_time.setText(list.get(position).get("time").toString());
        holder.tv_msg.setText(list.get(position).get("description").toString());
        Integer onlineStatus = (Integer) list.get(position).get("onlineStatus");
        Integer enableStatus = (Integer) list.get(position).get("enableStatus");
        Integer useStatus = (Integer) list.get(position).get("useStatus");
        type = (Integer) list.get(position).get("type");
        String str = list.get(position).get("useStatus").toString();
        int msgNum = pref.getInt("msgNum", 0);
           /* qBadgeView = new QBadgeView(getContext());
            qBadgeView.bindTarget( holder.tv_msg)
                    .setBadgeText("New")
                    .setBadgeBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary))

                    .setGravityOffset(32, -2, true);*/
        String tvstatus = "";
        if (type == 2) {
            switch (useStatus) {
                case 0:
                    //未激活
                    holder.image_msg.setImageResource(R.mipmap.lock_notactivated);
                    tvstatus = "未激活状态";
                    break;
                case 1:
                    if (enableStatus == 10) {
                        //在线状态
                        switch (onlineStatus) {
                            case 100: {
                                holder.image_msg.setImageResource(R.mipmap.lock_close);
                                tvstatus = "关闭状态";
                            }
                            break;
                            case 101: {
                                holder.image_msg.setImageResource(R.mipmap.lock_open);
                                tvstatus = "打开状态";
                            }
                            break;
                            case 102: {
                                holder.image_msg.setImageResource(R.mipmap.lock_warn);
                                tvstatus = "异常打开";
                            }
                            break;
                            case -1: {
                                holder.image_msg.setImageResource(R.mipmap.lock_na);
                                tvstatus = "未知";
                            }
                            break;
                        }
                    } else {
                        //离线状态
                        holder.image_msg.setImageResource(R.mipmap.lock_miss);
                        tvstatus = "离线状态";
                    }
                    break;
                case 2: {
                    holder.image_msg.setImageResource(R.mipmap.lock_stop);
                    tvstatus = "停用状态";
                }
                break;
                case 3: {
                    holder.image_msg.setImageResource(R.mipmap.lock_discard);
                    tvstatus = "废弃状态";
                }
                break;
            }


            mEditor.putString("tvstatus", tvstatus);
            mEditor.commit();
        } else {
            holder.image_msg.setImageResource(R.mipmap.gonggao);
            holder.tv_msg.setText("公告");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
