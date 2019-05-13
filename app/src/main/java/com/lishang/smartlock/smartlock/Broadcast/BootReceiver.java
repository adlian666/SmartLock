package com.lishang.smartlock.smartlock.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lishang.smartlock.smartlock.service.msgService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive( Context context, Intent intent ) {
        Intent intent1 = new Intent(context,msgService.class);
        //启动servicce服务
        context.startService(intent1);
    }
}
