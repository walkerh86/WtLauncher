package com.cj.clock;

import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.cj.wtlauncher.R;

public class WtClockRoot extends FrameLayout {
	private Time mCalendar;
	private final Handler mHandler = new Handler();
	private boolean mAttached;
	private WtClock mClockHour;
	private WtClock mClockMin;
	private WtClock mClockSec;
	
	public WtClockRoot(Context context) {
        this(context, null);
    }

    public WtClockRoot(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WtClockRoot(Context context, AttributeSet attrs,int defStyle) {                       
        super(context, attrs, defStyle);
    }
    
    protected void onFinishInflate() {
    	super.onFinishInflate();
    	
    	mClockHour = (WtClock)findViewById(R.id.clk_hour);
    	mClockMin = (WtClock)findViewById(R.id.clk_min);
    	mClockSec = (WtClock)findViewById(R.id.clk_sec);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            getContext().registerReceiverAsUser(mIntentReceiver,
                    android.os.Process.myUserHandle(), filter, null, mHandler);
            
            mHandler.postDelayed(mSecRunnable, 1000);
        }
        
        mCalendar = new Time();
        onTimeChanged();   
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mHandler.removeCallbacks(mSecRunnable);
            mAttached = false;
        }
    }
    
    private void onTimeChanged() {
        mCalendar.setToNow();

        int hour = mCalendar.hour;
        int minute = mCalendar.minute;
        int second = mCalendar.second;

        int m = (int)(minute + second / 60.0f);
        int h = (int)(hour + m / 60.0f);
        if(mClockHour != null){
        	mClockHour.setValue(h);
        }
        if(mClockMin != null){
        	mClockMin.setValue(m);
        }
        if(mClockSec != null){
        	mClockSec.setValue(second);
        }
    }
    
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }

            onTimeChanged();
        }
    };
    
    private Runnable mSecRunnable = new Runnable(){
    	@Override
    	public void run(){
    		mCalendar.setToNow();
    		if(mClockSec != null){
            	mClockSec.setValue(mCalendar.second);
            }
    		
    		mHandler.postDelayed(this, 1000);
    	}
    };
}
