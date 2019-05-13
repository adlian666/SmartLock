package com.lishang.smartlock.smartlock.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.Activity.OpenLockActivity;
import com.lishang.smartlock.smartlock.JavaBean.HomeLockListBean;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeRecycleAdapter extends RecyclerView.Adapter <HomeRecycleAdapter.homeViewHodler> {
    private Context context;
    public List <Map <String, Object>> list = new ArrayList <>();
    public List <Map <String, Object>> mmlist = new ArrayList <>();
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    public LayoutInflater inflater;

    //创建构造函数
    public HomeRecycleAdapter( Context context, List <Map <String, Object>> list ) {
        //将传递过来的数据，赋值给本地变量
        this.context = context;//上下文
        this.list = list;
        inflater = LayoutInflater.from(context);
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = pref.edit();
    }

    @Override
    public homeViewHodler onCreateViewHolder( ViewGroup parent, int viewType ) {
        //创建itemlayout布局
        View homeitemView = (LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home, parent, false));
        return new homeViewHodler(homeitemView);
    }

    class homeViewHodler extends RecyclerView.ViewHolder {
        private TextView tv_lockname, tv_lockinfo_blueCode, tv_locknumber, tv_status;
        private ImageView im_usestatus;

        public homeViewHodler( View homeitemView ) {

            super(homeitemView);
            try {
                //mRefreshLayout = homeitemView.findViewById(R.id.refreshLayout);
                tv_status = homeitemView.findViewById(R.id.tv_status);
                im_usestatus = homeitemView.findViewById(R.id.im_usestatus);
                tv_locknumber = homeitemView.findViewById(R.id.tv_locknumber);
                tv_lockname = homeitemView.findViewById(R.id.tv_lockname);
                tv_lockinfo_blueCode = homeitemView.findViewById(R.id.tv_lockinfo_blueCode);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        int i = getLayoutPosition();
                        //将过滤后的数据传过去
                        Map <String, Object> mlist = list.get(getLayoutPosition());
                        Intent intent = new Intent(context, OpenLockActivity.class);
                        intent.putExtra("lockinfo", (Serializable) mlist);
                        context.startActivity(intent);
                        //此处回传点击监听事件
                        if (onItemClickListener != null) {
                            onItemClickListener.OnItemClick(v, mlist);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置item的监听事件的接口
     */
    public interface OnItemClickListener {
        void OnItemClick( View view, Map <String, Object> mlist );

    }

    //需要外部访问，所以需要设置set方法，方便调用
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener( OnItemClickListener onItemClickListener ) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onBindViewHolder( homeViewHodler holder, int position ) {
        try {
            Integer useStatus = (Integer) list.get(position).get("useStatus");
            Integer enableStatus = (Integer) list.get(position).get("enableStatus");
            Integer onlineStatus = (Integer) list.get(position).get("onlineStatus");
            switch (useStatus) {
                case 0:
                    //未激活
                    holder.im_usestatus.setImageResource(R.mipmap.lock_notactivated);
                    holder.tv_status.setText("未激活状态");
                    break;
                case 1:
                    if (enableStatus == 10) {
                        //在线状态
                        switch (onlineStatus) {
                            case 100: {
                                holder.im_usestatus.setImageResource(R.mipmap.lock_close);
                                holder.tv_status.setText("关闭状态");
                            }
                            break;
                            case 101: {
                                holder.im_usestatus.setImageResource(R.mipmap.lock_open);
                                holder.tv_status.setText("打开状态");
                            }
                            break;
                            case 102: {
                                holder.im_usestatus.setImageResource(R.mipmap.lock_warn);
                                holder.tv_status.setText("异常打开");
                            }
                            break;
                            case -1: {
                                holder.im_usestatus.setImageResource(R.mipmap.lock_na);
                                holder.tv_status.setText("未知");
                            }
                            break;
                        }
                    } else {
                        //离线状态
                        holder.im_usestatus.setImageResource(R.mipmap.lock_miss);
                        holder.tv_status.setText("离线状态");
                    }
                    break;
                case 2: {
                    holder.im_usestatus.setImageResource(R.mipmap.lock_stop);
                    holder.tv_status.setText("停用状态");
                }
                break;
                case 3: {
                    holder.im_usestatus.setImageResource(R.mipmap.lock_discard);
                    holder.tv_status.setText("废弃状态");
                }
                break;
            }
            String tvstatus;
            tvstatus = holder.tv_status.getText().toString();
            mEditor.putString("tvstatus", tvstatus);
            mEditor.commit();
            holder.tv_lockname.setText(list.get(position).get("lockName").toString());
            holder.tv_locknumber.setText(list.get(position).get("lockNumber").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
