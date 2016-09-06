package com.sxhsoft.leave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sxhsoft.leave.joanzapata.BaseAdapterHelper;
import com.sxhsoft.leave.joanzapata.QuickAdapter;
import com.sxhsoft.leave.tool.WifiTool;

import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    EditText mWifiName;
    EditText mCheckPeriod;
    ListView mList;
    EditText mSignalThreshold;
    EditText mSignalOffset;
    EditText mCheckTimer;
    TextView mInfo;
    String mSSID;
    int mIntThreholdSignal=60;
    int mIntSignalOffset=10;
    int mIntTimer = 5;
    int mLastStatu= WifiTool.WIFISTATU_UNKNOWERROR;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWifiName = (EditText) findViewById(R.id.wifiName);
        mCheckPeriod = (EditText) findViewById(R.id.checkPeriod);
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.updateWifiName).setOnClickListener(this);
        findViewById(R.id.updateSignal).setOnClickListener(this);
        mList = (ListView) findViewById(R.id.wifiList);
        mSignalThreshold = (EditText) findViewById(R.id.wifiSignal);
        mSignalOffset = (EditText) findViewById(R.id.wifiSignalOffset);
        mCheckTimer = (EditText) findViewById(R.id.checkPeriod);
        mInfo = (TextView) findViewById(R.id.info);

        WifiTool.getObj().setHandler(mHandler);
        WifiTool.getObj().setContext(getApplicationContext());

        WifiTool.getObj().setOnAreaChangeListener(new WifiTool.OnAreaChangeListener()
        {
            @Override
            public void onChange(boolean bIn)
            {
                Toast.makeText(MainActivity.this, bIn?"进入监视区域":"离开监视区域", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnSave:
                onSave();
                break;
            case R.id.updateWifiName:
                onUpdateWifiName();
                break;
            case R.id.updateSignal:
                onUpdateSignal();
                break;
            default:
                break;
        }
    }

    private void onUpdateSignal()
    {
        int signal = WifiTool.getWifiSignalLevel(getApplicationContext());

        mInfo.setText("signal:" + signal);
        //mSignalThreshold.setText(""+signal);
    }

    private void onUpdateWifiName()
    {
        String str = WifiTool.getCurWifiName(getApplicationContext());
        mWifiName.setText(str);


    }


    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what==1111)
            {
                mInfo.setText("signal: "+msg.arg1);
                updateList();
            }
        }
    };
    Runnable mRunnable;

    private void onSave()
    {
        mSSID = mWifiName.getText().toString();
        mIntThreholdSignal = Integer.parseInt( mSignalThreshold.getText().toString() );
        mIntSignalOffset = Integer.parseInt( mSignalOffset.getText().toString() );
        mIntTimer = Integer.parseInt( mCheckTimer.getText().toString());

        WifiTool.getObj().update(mIntTimer,mIntThreholdSignal,mIntSignalOffset,mSSID);
        WifiTool.getObj().stop();
        WifiTool.getObj().startCheck();

    }

    QuickAdapter<ScanResult> mAdapter;
    private void updateList()
    {
        List<ScanResult> wifis = WifiTool.getScanResult(getApplicationContext());
        if (mAdapter == null)
        {
            mAdapter = new QuickAdapter<ScanResult>(this,R.layout.item_wifi,wifis)
            {
                @Override
                protected void convert(BaseAdapterHelper helper, ScanResult item)
                {
                    helper.setText(R.id.wifiName, item.SSID);
                    helper.setText(R.id.signal, "" + item.level);
                }
            };
            mList.setAdapter(mAdapter);
        }
        else
        {
            mAdapter.replaceAll(wifis);
        }
    }




    @Override
    protected void onPause()
    {
        super.onPause();
        WifiTool.getObj().unregisterBrodcast();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        WifiTool.getObj().registerBrodcast();

    }
}
