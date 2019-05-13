package com.lishang.smartlock.smartlock;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.Activity.AddlockActivity;
import com.lishang.smartlock.smartlock.Activity.FindActivity;
import com.lishang.smartlock.smartlock.Activity.OpenLockActivity;
import com.lishang.smartlock.smartlock.adapter.HomeRecycleAdapter;
import com.lishang.smartlock.smartlock.functionclass.MyOrientationListener;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

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
public class HomeFragment extends Fragment {
    boolean isFirst = true;//判断是否是第一次进去
    private MyLocationListener myListener = new MyLocationListener();//继承BDAbstractLocationListener的class
    BaiduMap mBaiduMap;//定义地图实例
    public LocationClient mLocationClient = null;//定义LocationClient
    MapView mMapView = null;
    private com.alibaba.fastjson.JSONArray jsonArray;
    private View view;
    private ImageButton ibtn_findlock, ibtn_addlock, ibtn_change;
    private TextView home_top_tittle1, home_top_tittle2, home_top_tittle3, tv_change, info_lockname, info_locknumber, info_status;
    private RecyclerView rc_home_list;
    private HomeRecycleAdapter homeRecycleAdapter;
    public List <Map <String, Object>> list = new ArrayList <>();
    public List <Map <String, Object>> mlist = new ArrayList <>();
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    Boolean change = true;
    private RefreshLayout mRefreshLayout;
    //覆盖物相关
    private BitmapDescriptor mMarker;
    private LinearLayout mMarkerLy;
    private ImageView im_mk_usestatus, info_open;
    private String cookie, server, port;
    private int page, i = 2;
    double swlng, swlat, nelng, nelat;
    LatLng right, left;
    Marker marker;
    AlertDialog.Builder dialog;
    RefreshLayout refreshLayout;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        try {
            SDKInitializer.initialize(getActivity().getApplicationContext());
            dialog = new AlertDialog.Builder(getContext());
            view = inflater.inflate(R.layout.fragment_home, null);
            pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mEditor = pref.edit();
            cookie = pref.getString("SmartLockId", "");
            server = pref.getString("server", "");
            port = pref.getString("port", "");
            initView();
            refreshLayout = view.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableScrollContentWhenLoaded(true);//是否在加载完成时滚动列表显示新的内容
            mBaiduMap = mMapView.getMap();//获取地图实例对象
            if (!isOPen(getContext())) {
                openGPSSettings();
            }
            // 开启定位图层
            mBaiduMap.setMyLocationEnabled(true);
            //设置定位图标是否有箭头
            mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, false, null));
            //声明LocationClient类
            mLocationClient = new LocationClient(getActivity().getApplicationContext());
            //注册监听函数
            mLocationClient.registerLocationListener(myListener);
            final List <String> qlist = new ArrayList <>();
            //运行时权限，没有注册就重新注册
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    qlist.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
           /* if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                qlist.add(Manifest.permission.READ_PHONE_STATE);
            }*/
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    qlist.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                if (!qlist.isEmpty()) {//没有权限就添加
                    String[] permissions = qlist.toArray(new String[qlist.size()]);//如果list不为空，就调用ActivityCompat.requestPermissions添加权限
                    requestPermissions(permissions, 1);
                } else {//6.0以下则执行程序
                    initLocation();
                }
            } else {//6.0以下则执行程序
                initLocation();
            }
            okHttp();
            mapOkHttp();
            //切换模式的按钮点击事件
            ibtn_change.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    if (change) {
                        rc_home_list.setVisibility(View.VISIBLE);
                        mMapView.setVisibility(View.GONE);
                        ibtn_change.setImageResource(R.mipmap.list1);
                        change = false;
                        mEditor.putBoolean("change", false);
                        mEditor.commit();
                    } else {
                        rc_home_list.setVisibility(View.GONE);
                        mMapView.setVisibility(View.VISIBLE);
                        ibtn_change.setImageResource(R.mipmap.map1);
                        change = true;
                        mEditor.putBoolean("change", true);
                        mEditor.commit();
                    }
                }
            });
            //marker点击事件
            mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick( Marker marker ) {
                    Bundle extraInfo = marker.getExtraInfo();
                    final Map <String, Object> mmlist;
                    mmlist = (Map <String, Object>) extraInfo.getSerializable("list");
                    Log.i("TAG44", mmlist.toString());
                    View infoview = View.inflate(getActivity(), R.layout.info_marker, null);
                    info_status = infoview.findViewById(R.id.info_status);
                    info_locknumber = infoview.findViewById(R.id.info_locknumber);
                    info_lockname = infoview.findViewById(R.id.info_lockname);
                    info_open = infoview.findViewById(R.id.info_open);
                    info_locknumber.setText(mmlist.get("lockNumber").toString());
                    info_lockname.setText(mmlist.get("lockName").toString());
                    final Integer useStatus = (Integer) mmlist.get("useStatus");
                    final Integer enableStatus = (Integer) mmlist.get("enableStatus");
                    final Integer onlineStatus = (Integer) mmlist.get("onlineStatus");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (useStatus) {
                                case 0:
                                    //未激活
                                    info_status.setText("未激活状态");
                                    break;
                                case 1:
                                    if (enableStatus == 10) {
                                        //在线状态
                                        switch (onlineStatus) {
                                            case 100: {
                                                info_status.setText("关闭状态");
                                            }
                                            break;
                                            case 101: {
                                                info_status.setText("打开状态");
                                            }
                                            break;
                                            case 102: {
                                                info_status.setText("异常打开");
                                            }
                                            break;
                                            case -1: {
                                                info_status.setText("未知");
                                            }
                                            break;
                                        }
                                    } else {
                                        //离线状态
                                        info_status.setText("离线状态");
                                    }
                                    break;
                                case 2: {
                                    info_status.setText("停用状态");
                                }
                                break;
                                case 3: {
                                    info_status.setText("废弃状态");
                                }
                                break;
                            }
                        }
                    });
                    final InfoWindow mInfoWindow = new InfoWindow(infoview, marker.getPosition(), -47);
                    mBaiduMap.showInfoWindow(mInfoWindow);
                    info_open.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick( View v ) {
                            //带锁的信息跳转到开锁页面
                            mBaiduMap.hideInfoWindow();
                            Intent intent = new Intent(getActivity(), OpenLockActivity.class);
                            intent.putExtra("lockinfo", (Serializable) mmlist);
                            startActivity(intent);
                        }
                    });
                    return true;
                }
            });
            //地图点击时隐藏infowindow
            mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
                @Override
                public void onMapClick( LatLng latLng ) {
                    mBaiduMap.hideInfoWindow();
                }

                @Override
                public boolean onMapPoiClick( MapPoi mapPoi ) {
                    return false;
                }
            });
            ibtn_addlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    //跳转到激活锁页面
                    Intent intent = new Intent(getActivity(), AddlockActivity.class);
                    startActivity(intent);
                }
            });
            ibtn_findlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    //跳转到查找锁
                    Intent intent = new Intent(getActivity(), FindActivity.class);
                    intent.putExtra("findlockinfo", (Serializable) jsonArray);
                    Log.i("TAG444", jsonArray.toString());
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BaiduMap.OnMapStatusChangeListener listener = new BaiduMap.OnMapStatusChangeListener() {
                @Override
                public void onMapStatusChangeStart( MapStatus mapStatus ) {

                }

                @Override
                public void onMapStatusChangeStart( MapStatus mapStatus, int i ) {
                    mBaiduMap.clear();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);//延时
                                //do something
                                mlist.clear();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                @Override
                public void onMapStatusChange( MapStatus mapStatus ) {

                }

                @Override
                public void onMapStatusChangeFinish( MapStatus mapStatus ) {
                    Log.i("TAG9999", "结束");
                    // initPoint();
                    mBaiduMap.clear();
                    final float zoom = mBaiduMap.getMapStatus().zoom;
                    Log.i("TAG9", "" + zoom);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(200);//延时
                                //do something
                                initPoint();
                                if (zoom >= 15.6f) {
                                    mapOkHttp();

                                } else {
                                    mBaiduMap.clear();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            };
            mMapView.getMap().setOnMapStatusChangeListener(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    //设置权限允许后进行定位
    public void onRequestPermissionsResult( int requestCode, String[] permissions, int[] grantResults ) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // openGPSSettings();
                    mLocationClient.start();
                    //initLocation();

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode,
                        permissions, grantResults);
        }
    }

    public static final boolean isOPen( final Context context ) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    private void mapOkHttp() {

        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        //2.构造request
        Request request = builder.get()
                .url(server + ":" + port + "/app/lock/getLockListByLngLat?" +
                        "swlng=" + swlng + "&swlat=" + swlat + "&nelng=" + nelng + "&nelat=" + nelat)
                .addHeader("Cookie", "SmartLockId=" + cookie)
                .build();

        //3.将request封装成call
        Call call = okHttpClient.newCall(request);
        //4.执行call

        call.enqueue(new Callback() {

            @Override
            public void onFailure( Call call, IOException e ) {
                //Toast.makeText(getActivity(), "无法连接服务器", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse( Call call, Response response ) throws IOException {

                String res = response.body().string();
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(res);
                jsonArray = jsonObject.getJSONArray("lockList");
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
                    final Integer onlineStatus = jsonObjects.getInteger("onlineStatus");
                    Integer heartCycle = jsonObjects.getInteger("heartCycle");
                    String description = jsonObjects.getString("description");
                    Integer sectionId = jsonObjects.getInteger("sectionId");
                    String blueCode = jsonObjects.getString("blueCode");
                    final Integer useStatus = jsonObjects.getInteger("useStatus");
                    Integer useStatusSyn = jsonObjects.getInteger("useStatusSyn");
                    Integer id = jsonObjects.getInteger("id");
                    final Integer enableStatus = jsonObjects.getInteger("enableStatus");
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
                    //添加覆盖物

                    switch (useStatus) {
                        case 0:
                            //未激活
                            mMarker = BitmapDescriptorFactory.fromResource(R.mipmap.mlock_unactived);
                            break;
                        case 1:
                            if (enableStatus == 10) {
                                //在线状态
                                switch (onlineStatus) {
                                    case 100: {
                                        mMarker = BitmapDescriptorFactory.fromResource(R.mipmap.mlock_close);
                                    }
                                    break;
                                    case 101: {
                                        mMarker = BitmapDescriptorFactory.fromResource(R.mipmap.mlock_open);
                                    }
                                    break;
                                    case 102: {
                                        mMarker = BitmapDescriptorFactory.fromResource(R.mipmap.mlock_warn);
                                    }
                                    break;
                                    case -1: {
                                        mMarker = BitmapDescriptorFactory.fromResource(R.mipmap.mlock_na);
                                    }
                                    break;
                                }
                            } else {
                                //离线状态
                                mMarker = BitmapDescriptorFactory.fromResource(R.mipmap.mlock_miss);
                            }
                            break;
                        case 2: {
                            mMarker = BitmapDescriptorFactory.fromResource(R.mipmap.mlock_stop);
                        }
                        break;
                        case 3: {
                            mMarker = BitmapDescriptorFactory.fromResource(R.mipmap.mlock_discard);
                        }
                        break;
                    }
                    LatLng latLng = null;
                    Marker marker = null;
                    OverlayOptions options;
                    latLng = new LatLng(latitude, longitude);
                    options = new MarkerOptions()
                            .position(latLng)
                            .icon(mMarker)
                            .zIndex(5);
                    marker = (Marker) mBaiduMap.addOverlay(options);
                    Bundle arg0 = new Bundle();
                    arg0.putSerializable("list", (Serializable) jsonObjects);
                    marker.setExtraInfo(arg0);
                    //ArrayList集合
                    mlist.add(map);
                }
            }
        });
    }

    private void initPoint() {

        int b = mMapView.getBottom();
        int t = mMapView.getTop();
        int r = mMapView.getRight();
        int l = mMapView.getLeft();
        LatLng ne = mBaiduMap.getProjection().fromScreenLocation(new Point(r, t));
        LatLng sw = mBaiduMap.getProjection().fromScreenLocation(new Point(l, b));
        swlng = sw.longitude;
        swlat = sw.latitude;
        nelng = ne.longitude;
        nelat = ne.latitude;

    }

    //统计在线、离线
    private void count() {
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Log.i("TAG7777",cookie);
        //2.构造request
        Request request = builder.get()
                .url(server + ":" + port + "/app/lock/getLockCount")
                .addHeader("cookie", "SmartLockId=" + cookie)
                .build();
        //3.将request封装成call
        Call call = okHttpClient.newCall(request);
        //4.执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                Log.i("TAG77", "失败");
            }

            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                String res = response.body().string();
                Log.i("TAG77", res);
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(res);
                com.alibaba.fastjson.JSONObject jsonObject1 = jsonObject.getJSONObject("lockCount");
                Map <String, Object> map = new HashMap <>();
                //获取到json数据中的数组里的内容

                final Integer openWarn = jsonObject1.getInteger("openWarn");
                final Integer offline = jsonObject1.getInteger("offline");
                final Integer online = jsonObject1.getInteger("online");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("TAG6", "" + openWarn);
                        home_top_tittle1.setText("" + online);
                        home_top_tittle2.setText("" + offline);
                        home_top_tittle3.setText("" + openWarn);
                    }
                });


            }
        });
    }

    //添加覆盖物
    private void addOverLays() {

        mBaiduMap.clear();
        mMarker = BitmapDescriptorFactory.fromResource(R.mipmap.map2);
        LatLng latLng = null;
        OverlayOptions options;
        int len = mlist.size();
        for (int x = 0; x < mlist.size(); x++) {
            latLng = new LatLng((Float) mlist.get(x).get("latitude"), (Float) mlist.get(x).get("longitude"));
            options = new MarkerOptions()
                    .position(latLng)
                    .icon(mMarker)
                    .zIndex(5);
            marker = (Marker) mBaiduMap.addOverlay(options);
            Bundle arg0 = new Bundle();
            arg0.putSerializable("list", (Serializable) mlist.get(x));
            marker.setExtraInfo(arg0);
        }
        // MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        //mBaiduMap.setMapStatus(msu);
    }

    //初始化地图
    private void initLocation() {
        // BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
        // .fromResource(R.mipmap.dir);
        LocationClientOption locationOption = new LocationClientOption();
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("bd09ll");
//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(30000);
//可选，设置是否需要地址信息，默认不需要
        // locationOption.setIsNeedAddress(true);
//可选，设置是否需要地址描述
        // locationOption.setIsNeedLocationDescribe(true);
//可选，设置是否需要设备方向结果
        //locationOption.setNeedDeviceDirect(false);
//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(false);
//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(false);
//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true);
//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true);
//可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
//可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false);
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        // locationOption.setOpenAutoNotifyMode();
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        //locationOption.setOpenAutoNotifyMode(3000, 1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        mLocationClient.setLocOption(locationOption);
        // myOrientationListener = new MyOrientationListener(getContext());
       /* myOrientationListener.setmOnOrientationListner(new MyOrientationListener.OnOrientationListner() {
            @Override
            public void onOrientationChanged( float x ) {
                mCurrentX = x;
            }
        });*/
        mLocationClient.start();
        // myOrientationListener.start();

    }

    @Override
    public void onResume() {
        i = 2;
        super.onResume();
        //在Fragment执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        try {
           // list.clear();
           // okHttp();
            //mapOkHttp();
            mMapView.onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //在<span style="font-family: 微软雅黑, 'Microsoft YaHei', sans-serif;">Fragment</span>执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在Fragment执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        // myOrientationListener.stop();
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation( BDLocation location ) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            //获取经度信息
            try {
                double latitude = location.getLatitude();    //获取纬度信息
                double longitude = location.getLongitude();
                float radius = location.getRadius();    //获取定位精度，默认值为0.0f
                //获得当前定位
                mEditor.putString("latitude", "" + latitude);
                mEditor.putString("longitude", "" + longitude);
                mEditor.commit();
                String coorType = location.getCoorType();
                //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
                int errorCode = location.getLocType();
                // Log.i("---------", location.getCityCode() + "---" + latitude + "--" + longitude + "----" + coorType + "--" + location.getCountry() + "--" + location.getCity() + "--" + location.getAddrStr());
                //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明

                // 构造定位数据
                MyLocationData locData = new MyLocationData.Builder()
                        // .direction(mCurrentX)
                        .accuracy(radius)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();

                // 设置定位数据
                mBaiduMap.setMyLocationData(locData);

                if (isFirst) {

                    LatLng ll = new LatLng(location.getLatitude(),
                            location.getLongitude());
              /*  MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));*/
                    MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                    // 移动到某经纬
                    update = MapStatusUpdateFactory.zoomBy(4f);
                    // 放大
                    mBaiduMap.animateMapStatus(update);
                    isFirst = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initView() {
        rc_home_list = view.findViewById(R.id.rc_home_list);
        mMapView = view.findViewById(R.id.bdmap);
        home_top_tittle1 = view.findViewById(R.id.home_top_tittle1);
        home_top_tittle2 = view.findViewById(R.id.home_top_tittle2);
        home_top_tittle3 = view.findViewById(R.id.home_top_tittle3);
        ibtn_findlock = view.findViewById(R.id.ibn_findlock);
        ibtn_addlock = view.findViewById(R.id.ibtn_addlock);
        ibtn_change = view.findViewById(R.id.ibtn_change);
        tv_change = view.findViewById(R.id.tv_change);
        mRefreshLayout = view.findViewById(R.id.refreshLayout);
    }

    //从服务器获取数据
    private void okHttp() {
        count();
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        //2.构造request
        Log.i("TAG77",server + ":" + port + cookie);
        Request request = builder.get()
                .url(server + ":" + port + "/app/lock/getLockList?p=" + 1)
                .addHeader("Cookie", "SmartLockId=" + cookie)
                .build();
        //3.将request封装成call
        Call call = okHttpClient.newCall(request);
        //4.执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                refreshLayout.finishRefresh(false);//结束刷新（刷新失败）
                refreshLayout.finishLoadmore(false);
                Looper.prepare();
                Toast.makeText(getActivity(), "无法连接服务器！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                String res = response.body().string();
                if(response.isSuccessful()){
                    //响应成功
                    Log.i("TAG666",res);
                }
                Log.i("TAG6",res);
                JSONObject jsonObject = JSON.parseObject(res);
               JSONObject jsonObject1 = jsonObject.getJSONObject("lockList");
                page = jsonObject1.getInteger("totalPage");
                jsonArray = jsonObject1.getJSONArray("list");
                int lengths = jsonArray.size();
                for (int i = 0; i < lengths; i++) {
                    JSONObject jsonObjects = jsonArray.getJSONObject(i);
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
                    Log.i("TAG66","0"+map);
                    //ArrayList集合
                    list.add(map);

                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                    refreshLayout.finishRefresh();//结束刷新
                    refreshLayout.finishLoadmore();
                }
                //addOverLays();
            }
        });
    }

    //打开gps
    private void openGPSSettings() {

        //没有打开则弹出对话框
        //new AlertDialog.Builder(getContext())
        dialog.setTitle("设置")
                .setMessage("请打开位置服务")
                // 拒绝, 退出应用
                .setNegativeButton("取消并退出",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick( DialogInterface dialog, int which ) {
                                System.exit(0);
                            }
                        })

                .setPositiveButton("前往设置",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick( DialogInterface dialog, int which ) {
                                //跳转GPS设置界面
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                getActivity().startActivityForResult(intent, 10);
                            }
                        })

                .setCancelable(true)
                .show();


    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 10) {
                //做需要做的事情，比如再次检测是否打开GPS了 或者定位
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss( DialogInterface dialog ) {
                        if (!isOPen(getContext())) {
                            openGPSSettings();
                        } else {
                            dialog.dismiss();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Handler handler = new Handler() {

        @Override
        public void handleMessage( Message msg ) {
            switch (msg.what) {
                case 1:
                    try {
                        homeRecycleAdapter = new HomeRecycleAdapter(getActivity(), list);
                    mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

                        public void onRefresh( RefreshLayout refreshlayout ) {

                                list.clear();
                                i = 2;
                                okHttp();
                                refreshlayout.finishRefresh();
                                homeRecycleAdapter.notifyDataSetChanged();

                        }
                    });
                    //加载更多

                    mRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
                        @Override
                        public void onLoadmore( RefreshLayout refreshlayout ) {

                                onLoadHttp();
                                refreshlayout.finishRefresh();
                                homeRecycleAdapter.notifyDataSetChanged();

                        }
                    });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ///添加分割线

                    rc_home_list.setAdapter(homeRecycleAdapter);
                    //设置布局显示格式
                    rc_home_list.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                    //rc_home_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
                    break;
            }
        }
    };

    //下拉刷新
    private void onLoadHttp() {
        //1.拿到httpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        //2.构造request
        Request request = builder.get()
                .url(server + ":" + port + "/app/lock/getLockList?p=" + i)
                .addHeader("Cookie", "SmartLockId=" + cookie)
                .build();
        //3.将request封装成call
        Call call = okHttpClient.newCall(request);
        //4.执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure( Call call, IOException e ) {
                refreshLayout.finishRefresh(false);//结束刷新（刷新失败）
                refreshLayout.finishLoadmore(false);
                Looper.prepare();
                Toast.makeText(getActivity(), "无法连接服务器！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse( Call call, Response response ) throws IOException {
                String res = response.body().string();
                Log.i("TAG7", "21" + res);
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(res);
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
                    // homeRecycleAdapter.notifyDataSetChanged();
                    refreshLayout.finishRefresh();//结束刷新
                    refreshLayout.finishLoadmore();
                }
            }
        });
        i++;
    }
}