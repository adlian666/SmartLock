package com.lishang.smartlock.smartlock.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.Activity.OpenLockActivity;
import com.lishang.smartlock.smartlock.JavaBean.StatusUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FindlockRecycleAdapter extends RecyclerView.Adapter<FindlockRecycleAdapter.findlockViewHodler>  {
    private Context context;
    public List <Map <String, Object>> list = new ArrayList <>();
    public LayoutInflater inflater;
    //创建构造函数
    public FindlockRecycleAdapter(Context context, List<Map<String, Object>> list ) {
        //将传递过来的数据，赋值给本地变量
       // Log.i("TAG14",""+list.get(0));
        this.context = context;//上下文
        this.list = list;

       inflater = LayoutInflater.from(context);
    }
    @Override
    public findlockViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建itemlayout布局
        Log.i("TAG14",""+1);
        View findlockView = LayoutInflater.from(parent.getContext()).inflate( R.layout.item_home ,parent,false);
        Log.i("TAG14",""+2);
        return new findlockViewHodler(findlockView);
    }
    class findlockViewHodler extends RecyclerView.ViewHolder {
        TextView tv_lockname,tv_status,tv_locknumber;
        ImageView im_usestatus;
        private RecyclerView rc_msg_list;
        public findlockViewHodler( View findlockView) {
            super(findlockView);
            Log.i("TAG14",""+3);
            tv_status = findlockView.findViewById(R.id.tv_status);
            im_usestatus = findlockView.findViewById(R.id.im_usestatus);
            tv_locknumber = findlockView.findViewById(R.id.tv_locknumber);
            tv_lockname = findlockView.findViewById(R.id.tv_lockname);
            Log.i("TAG14",""+4);
            //tv_lockinfo_blueCode = findlockView.findViewById(R.id.tv_lockinfo_blueCode);
        }
    }
    /**
     * 设置item的监听事件的接口
     */

    @Override
    public void onBindViewHolder( findlockViewHodler holder, final int position ) {
        Integer useStatus = (Integer) list.get(position).get("useStatus");
        Integer enableStatus = (Integer) list.get(position).get("enableStatus");
        Integer onlineStatus = (Integer) list.get(position).get("onlineStatus");
        //final String str = StatusUtils.setStatus(useStatus, enableStatus, onlineStatus);
       // holder.tv_status.setText(str);
        switch (useStatus) {
            case 0:
                //未激活
                holder.im_usestatus.setBackgroundResource(R.mipmap.lock_notactivated);
                holder.tv_status.setText("未激活状态");
                break;
            case 1:
                if(enableStatus == 10){
                    //在线状态
                    switch (onlineStatus) {
                        case 100: {
                            holder.im_usestatus.setImageResource(R.mipmap.lock_close);
                            holder.tv_status.setText("关闭状态");
                        }
                        break;
                        case 101:{
                            holder.im_usestatus.setImageResource(R.mipmap.lock_open);
                            holder.tv_status.setText("打开状态");
                        }
                        break;
                        case 102:{
                            holder.im_usestatus.setImageResource(R.mipmap.lock_warn);
                            holder.tv_status.setText("异常打开");
                        }
                        break;
                        case -1:{
                            holder.im_usestatus.setImageResource(R.mipmap.lock_na);
                            holder.tv_status.setText("未知");
                        }
                        break;
                    }
                }else{
                    //离线状态
                    holder.im_usestatus.setImageResource(R.mipmap.lock_miss);
                    holder.tv_status.setText("离线状态");
                }
                break;
            case 2:{
                holder.im_usestatus.setImageResource(R.mipmap.lock_stop);
                holder.tv_status.setText("停用状态");
            }
            break;
            case 3:{
                holder.im_usestatus.setImageResource(R.mipmap.lock_discard);
                holder.tv_status.setText("废弃状态");
            }
            break;
        }
        holder.tv_lockname.setText(list.get(position).get("lockName").toString());
        holder.tv_locknumber.setText(list.get(position).get("lockNumber").toString());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map <String, Object> mlist = list.get(position);
                Intent intent = new Intent(context, OpenLockActivity.class);
                intent.putExtra("lockinfo", (Serializable) mlist);
                // Log.i("TAG4",mlist.toString());
                context.startActivity(intent);
            }
        });

    }
    @Override
    public int getItemCount() {
        return list.size();
    }
    }
