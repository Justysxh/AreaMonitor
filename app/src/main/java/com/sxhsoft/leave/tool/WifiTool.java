package com.sxhsoft.leave.tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.params.Face;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sxhsoft.leave.MonitorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sxh on 2016/9/5.
 */
public class WifiTool
{
    private static WifiTool sInstance = null;

    private WifiTool()
    {
    }

    public static WifiTool getObj()
    {
        synchronized (WifiTool.class)
        {
            if (sInstance == null)
            {
                sInstance = new WifiTool();
            }
        }

        return sInstance;
    }

    private static final String KEY_WIFI_NAME = "wifiName";
    private static final String KEY_SIGNAL_THRESHOLD = "signalThreshold";
    private static final String KEY_SIGNAL_OFFSET = "signalOffset";
    private static final String KEY_CHECK_TIMER = "checkTimer";
    public void saveConfig()
    {
        if(mContext!=null)
        {
            MobileTool.setContext(mContext);
            MobileTool.setShareValue(KEY_WIFI_NAME, mWifiName);
            MobileTool.setShareValue(KEY_SIGNAL_THRESHOLD,mSignalThreshold);
            MobileTool.setShareValue(KEY_SIGNAL_OFFSET, mSignalOffset);
            MobileTool.setShareValue(KEY_CHECK_TIMER, mTimer);
        }
    }

    public void loadConfig()
    {
        if(mContext!=null)
        {
            MobileTool.setContext(mContext);
            MobileTool.getShareValue(KEY_WIFI_NAME, mWifiName);
            MobileTool.getShareValue(KEY_SIGNAL_THRESHOLD,mSignalThreshold);
            MobileTool.getShareValue(KEY_SIGNAL_OFFSET, mSignalOffset);
            MobileTool.getShareValue(KEY_CHECK_TIMER, mTimer);
        }
    }


    public static String getCurWifiName(Context context)
    {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null)
        {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null)
            {
                String ssid = wifiInfo.getSSID();
                ssid = ssid.replaceAll("\"","");
                return  ssid;
            }
        }
        return "";
    }

    public static int getWifiSignalLevel(Context context)
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null)
        {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null)
            {
                return wifiInfo.getRssi();
                //return WifiManager.calculateSignalLevel(wifiInfo.getRssi(),100);
            }
        }
        return 0;
    }

    public static List<ScanResult> getScanResult(Context context)
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null)
        {
            wifiManager.startScan();
            List<ScanResult> wifis = wifiManager.getScanResults();
            if(wifis!=null)
            {
                return wifis;
            }
        }
        return new ArrayList<>();
    }


    public static final int WIFISTATU_DISABLE = 244;  //WIFI被禁用
    public static final int WIFISTATU_NO_CHANGE = 851; //在阈值区间内, 未改变
    public static final int WIFISTATU_GET_IN = 345;  //进入阈值区域
    public static final int WIFISTATU_GO_OUT = 333;  //离开阈值区域
    public static final int WIFISTATU_UNKNOWERROR = 437; //未知错误  比如无权限,不能获取wifi信息

    private static final int SIGNAL_LEVEL_INIT = 9999;




    private Context mContext;
    private  int mTimer =5;
    private  int mSignalThreshold=65;
    private  int mSignalOffset=10;
    private  String mWifiName="default";
    private  Handler mTimerHandler=null;
    private int  mLastStatu=WIFISTATU_UNKNOWERROR;
    private boolean mIsWorking= false;

    public void setContext(Context context)
    {
        mContext = context;
    }

    public void update(int timer, int signalThreshold, int signalOffset, String wifiName)
    {
        mTimer = timer;
        mSignalOffset = signalOffset;
        mSignalThreshold = signalThreshold;
        mWifiName = wifiName;
        saveConfig();
    }

    public void setHandler(Handler handler)
    {
        mTimerHandler = handler;
    }

    public void startCheck()
    {
        stop();
        mIsWorking = true;
        processAreaJudge();
    }

    public void stop()
    {
        mIsWorking = false;
        if(mTimerTask!=null)
        {
            mTimerTask.cancel();
        }
    }

    private void processAreaJudge()
    {
        if(mIsWorking==false)
        {
            return;
        }
        if( isWifiEnable() ==false)
        {
            waitNextUpdate();
        }
        else
        {
            int signal = getCurrentWifiSignal(mWifiName);
            if(signal == SIGNAL_LEVEL_INIT)
            {
                sendScan();
            }
            else
            {
                processSignal(signal);
            }
        }
    }

    private void processSignal(int signal)
    {
        Log.i("sxh", "signal:"+signal);
        if(mTimerHandler!=null)
        {
            Message msg = new Message();
            msg.what = 1111;
            msg.arg1 = signal;
            mTimerHandler.sendMessage(msg);
        }

        signal = Math.abs(signal);
        if(signal<mSignalThreshold-mSignalOffset)
        {
            if(mLastStatu!=WIFISTATU_UNKNOWERROR && mLastStatu!=WIFISTATU_GET_IN && mListener!=null)
            {
                mListener.onChange(true);
            }
            mLastStatu = WIFISTATU_GET_IN;
        }
        else if(signal>mSignalThreshold)
        {
            if(mLastStatu!=WIFISTATU_UNKNOWERROR && mLastStatu!=WIFISTATU_GO_OUT && mListener!=null)
            {
                mListener.onChange(false);
            }
            mLastStatu = WIFISTATU_GO_OUT;
        }
        waitNextUpdate();
    }

    private String processSSID(String ssid)
    {
        if(ssid!=null)
        {
            ssid = ssid.replaceAll("\"","");
        }
        else
        {
            ssid="unknown";
        }
        return ssid;
    }

    private boolean isWifiEnable()
    {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager!=null)
        {
            return wifiManager.getWifiState()==WifiManager.WIFI_STATE_ENABLED;
        }
        return false;
    }


    //尝试获取当前Wifi信息, 如果是指定监视的wifi
    private int getCurrentWifiSignal(String wifiName)
    {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null)
        {
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info != null)
            {
                String ssid = processSSID(info.getSSID());
                if(ssid.equals(wifiName))
                {
                    return info.getRssi();
                }
            }
        }
        return SIGNAL_LEVEL_INIT;
    }


    private void sendScan()
    {
        if(mIsWorking==false)
        {
            return;
        }
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null)
        {
            wifiManager.startScan();
        }
        else
        {
            waitNextUpdate();
        }
    }

    private void parseScanResults()
    {
        if(mIsWorking==false)
        {
            return;
        }
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null)
        {
            List<ScanResult> wifis = wifiManager.getScanResults();
            int signal = SIGNAL_LEVEL_INIT;
            if(wifis!=null && !wifis.isEmpty())
            {
                for(ScanResult item:wifis)
                {
                    String ssid = processSSID(item.SSID);
                    if(ssid.equals(mWifiName))
                    {
                        signal = item.level;
                        break;
                    }
                }
            }
            //如果未搜索到目标wifi.
            if(signal==SIGNAL_LEVEL_INIT && wifis!=null)
            {
                signal = -100;
            }
            processSignal(signal);
        }
        else
        {
            waitNextUpdate();
        }
    }


    private void waitNextUpdate()
    {
        if(mTimerTask!=null)
        {
            mTimerTask.cancel();
        }
        mTimerTask = new MyTimerTask();
        mTimerOpt.schedule(mTimerTask,mTimer*1000);
    }

    Timer mTimerOpt = new Timer();
    TimerTask mTimerTask = null;

    private class MyTimerTask extends TimerTask
    {
        @Override
        public void run()
        {
            processAreaJudge();
        }
    }

    public void registerBrodcast()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(mReceiver, filter);
    }

    public void unregisterBrodcast()
    {
        mContext.unregisterReceiver(mReceiver);
    }


    /**
     * 广播接收，监听网络
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // wifi已成功扫描到可用wifi。
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {
                if( SIGNAL_LEVEL_INIT == getCurrentWifiSignal(mWifiName) )
                {
                    parseScanResults();
                }

            }
        }
    };


    public static abstract class OnAreaChangeListener
    {
        public abstract void onChange(boolean bIn);
    }

    private OnAreaChangeListener mListener;
    public void setOnAreaChangeListener(OnAreaChangeListener listener)
    {
        mListener = listener;
    }




}
