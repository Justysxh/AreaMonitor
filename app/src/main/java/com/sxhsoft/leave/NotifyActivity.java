package com.sxhsoft.leave;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class NotifyActivity extends AppCompatActivity
{

    MediaPlayer mMediaPlayer;
    Vibrator mVibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        setTitle("通知");

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        mMediaPlayer = MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.vibrate(new long[]{200,300},0);
        //*/
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mMediaPlayer.stop();
        mVibrator.cancel();
    }
}
