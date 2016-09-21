package com.sxhsoft.leave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver
{
    public BootBroadcastReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent intent1 = new Intent(context,MonitorService.class);
        context.startService(intent1);
        Log.i("sxh","Boot");
    }
}
