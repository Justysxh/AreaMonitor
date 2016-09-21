package com.sxhsoft.leave;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.sxhsoft.leave.tool.MobileTool;
import com.sxhsoft.leave.tool.WifiTool;

public class MonitorService extends Service
{
    public MonitorService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        WifiTool.getObj().setContext(getApplicationContext());
        WifiTool.getObj().loadConfig();
        MobileTool.setContext(getApplicationContext());

        WifiTool.getObj().setOnAreaChangeListener(new WifiTool.OnAreaChangeListener()
        {
            @Override
            public void onChange(boolean bIn)
            {
                notifyAreaChange();
            }
        });
        WifiTool.getObj().startCheck();

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        WifiTool.getObj().stop();
        WifiTool.getObj().setOnAreaChangeListener(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);

    }

    private void notifyAreaChange()
    {
        Intent intent = new Intent(MonitorService.this,NotifyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
    }


}
