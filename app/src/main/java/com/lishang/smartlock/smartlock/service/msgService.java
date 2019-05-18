package com.lishang.smartlock.smartlock.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.Activity.LoginActivity;
import com.lishang.smartlock.smartlock.Activity.MainActivity;
import com.lishang.smartlock.smartlock.Activity.WelcomeActivity;
import com.lishang.smartlock.smartlock.badgenumberlibrary.BadgeNumberManager;
import com.lishang.smartlock.smartlock.badgenumberlibrary.BadgeNumberManagerXiaoMi;
import com.lishang.smartlock.smartlock.badgenumberlibrary.MobileBrand;
import com.lishang.smartlock.smartlock.functionclass.FileUtil;

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

import static android.os.Build.ID;
import static android.provider.ContactsContract.Intents.Insert.NAME;
import static com.baidu.mapapi.BMapManager.getContext;

public class msgService extends Service {
    URI uri;
    boolean isFirst;
    int msgNum = 0;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    public List <Map <String, Object>> list = new ArrayList <>();
    int accountId;
    public int id = 0;
    Boolean ck_remember_psw;private String account, state, msg, SmartLockId;
    private static final String ID = "PUSH_NOTIFY_ID";
    private static final String NAME = "PUSH_NOTIFY_NAME";WebSocketClient mWebSocketClient = null;
    @Override
    public void onCreate() {
        try {
            pref = PreferenceManager.getDefaultSharedPreferences(getContext());
            mEditor = pref.edit();
            accountId = pref.getInt("accountId", 0);
            super.onCreate();
            Log.i("TAG121", "OnCreate 服务启动时调用");
            ck_remember_psw = pref.getBoolean("ck_remember_psw", false);
            Log.i("TAG18", "是否登陆" + ck_remember_psw);
            if (ck_remember_psw) {
                webSocket();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public IBinder onBind( Intent intent ) {
        return (null);
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        //onCreate();
        return super.onStartCommand(intent, flags, startId);
    }

    //服务被关闭时调用
    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebSocketClient.close();
        Log.i("TAG000", "onDestroy 服务关闭时");
    }

    public void webSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String address = "ws://132.232.168.115:80/websocket.ws/" + accountId;

                try {
                    uri = new URI(address);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                mWebSocketClient = new WebSocketClient(uri) {

                    @Override
                    public void onOpen( ServerHandshake arg0 ) {
                        // TODO Auto-generated method stub
                        System.out.println("open --> " + arg0);
                        Log.i("TAG18", "open");
                    }
                    @Override
                    public void onMessage( String arg0 ) {
                        // TODO Auto-generated method stub
                        //设内部未读消息数
                        isFirst = pref.getBoolean("isFirst", true);
                        msgNum = pref.getInt("msgNum", 0);
                        msgNum = msgNum + 1;
                        if (!Build.MANUFACTURER.equalsIgnoreCase(MobileBrand.XIAOMI)) {
                            BadgeNumberManager.from(getContext()).setBadgeNumber(msgNum);
                        } else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(3000);//延时
                                        //do something
                                        setXiaomiBadgeNumber();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                        mEditor.putInt("msgNum", msgNum);
                        mEditor.commit();
                        Log.i("TAG18", "" + arg0);
                        System.out.println("receive -->  " + arg0);
                        JSONObject jsonObject = JSON.parseObject(arg0);
                        Integer lockId = jsonObject.getInteger("lockId");
                        Integer type = jsonObject.getInteger("type");
                        String time = jsonObject.getString("time");
                        String description = jsonObject.getString("description");
                        Integer onlineStatus = jsonObject.getInteger("onlineStatus");
                        Integer enableStatus = jsonObject.getInteger("enableStatus");
                        Integer useStatus = jsonObject.getInteger("useStatus");
                        //存入map
                        Map <String, Object> map = new HashMap <>();
                        map.put("lockId", lockId);
                        map.put("time", time);
                        map.put("description", description);
                        map.put("onlineStatus", onlineStatus);
                        map.put("enableStatus", enableStatus);
                        map.put("useStatus", useStatus);
                        map.put("type", type);
                        list.add(map);
                        //文件不存在，创建文件
                        File file = new File(Environment.getExternalStorageDirectory(), "list.txt");
                        if (!file.exists()) {
                            JSONArray jsonArray1 = JSONArray.parseArray(JSONObject.toJSONString(list));
                            FileUtil.saveFile(jsonArray1.toString(), "list.txt");
                        } else {
                            String str = FileUtil.getFile("list.txt");
                            JSONArray jsonArray1 = JSON.parseArray(str);
                            jsonArray1.add(0, map);
                            FileUtil.saveFile(jsonArray1.toString(), "list.txt");
                        }

                        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
                        Intent intent1 = new Intent(getContext(), MainActivity.class);
                        okhttp();
                        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent1, 0);
                        //安卓8.0以上版本
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel mChannel = new NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_HIGH);
                            builder.setChannelId(ID);
                            notificationManager.createNotificationChannel(mChannel);
                        }
                        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        builder.setContentTitle("" + lockId)
                                .setTicker("通知来啦")
                                .setContentText(description)
                                .setWhen(System.currentTimeMillis())
                                .setSmallIcon(R.mipmap.lock_close)
                                .setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.btn))
                                .setDefaults(Notification.DEFAULT_VIBRATE)//设置震动
                                .setDefaults(Notification.DEFAULT_SOUND)
                                .setDefaults(Notification.DEFAULT_LIGHTS)//设置指示灯
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                         .setDefaults(Notification.DEFAULT_ALL);
                        Notification notification = builder.build();
                        notification.priority = Notification.PRIORITY_MAX;
                        notification.defaults |= Notification.DEFAULT_SOUND;
                        notification.defaults |= Notification.DEFAULT_VIBRATE;
                        notification.defaults |= Notification.DEFAULT_LIGHTS;
                        notificationManager.notify(0, notification);
                        notification.flags=Notification.FLAG_AUTO_CANCEL;
                        //发送广播
                        Intent intent = new Intent();
                        intent.putExtra("msg", (Serializable) map);
                        intent.setAction("sendMsg");
                        sendBroadcast(intent);
                    }

                    @Override
                    public void onError( Exception arg0 ) {
                        // TODO Auto-generated method stub
                        System.out.println("error -->  " + arg0);
                    }

                    @Override
                    public void onClose( int arg0, String arg1, boolean arg2 ) {
                        // TODO Auto-generated method stub
                        System.out.println("close -->  " + arg0);
                        Log.i("TAG18", "CLOSE");
                    }
                };
                mWebSocketClient.connect();
            }
        }).start();

    }
    private void setXiaomiBadgeNumber() {
        NotificationManager notificationManager = (NotificationManager) getContext().
                getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(getContext())
                .setSmallIcon(getContext().getApplicationInfo().icon)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("推送标题")
                .setContentText("我是推送内容")
                .setTicker("ticker")
                .setAutoCancel(true)
                .build();
        //相邻的两次角标设置如果数字相同的话，好像下一次会不生效
        BadgeNumberManagerXiaoMi.setBadgeNumber(notification, msgNum);
        notificationManager.notify(1000, notification);
        Toast.makeText(getContext(), "设置桌面角标成功", Toast.LENGTH_SHORT).show();

    }
    private void okhttp() {

        OkHttpClient okHttpClient = new OkHttpClient();
        String server = pref.getString("server", "");
        String  port = pref.getString("port", "");
        String workername = pref.getString("et_workername", "");
        String password = pref.getString("et_password", "");
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
                        Toast.makeText(getContext(), "无法连接服务器！", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse( Call call, Response response ) throws IOException {
               String  res = response.body().string();
                        JSONObject jsonObject = JSON.parseObject(res);

                        state = jsonObject.getString("state");
                        msg = jsonObject.getString("msg");
                        if (state.equals("ok")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("loginAccount");
                            SmartLockId = jsonObject.getString("SmartLockId");
                            accountId = (int) jsonObject1.get("id");
                            mEditor.putString("SmartLockId", SmartLockId);
                            mEditor.putInt("accountId", accountId);
                            mEditor.commit();
                        } else if (state.equals("fail")) {
                            //子线程中弹出土司
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }

            }

        });
    }
}
