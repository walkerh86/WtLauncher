package com.cj.clock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cj.wtlauncher.R;

public class WtClockRoot extends FrameLayout {
	private Time mCalendar;
	private Date mDate;
	private final Handler mHandler = new Handler();
	private boolean mAttached;
	private WtClock mClockHour;
	private WtClock mClockMin;
	private WtClock mClockSec;
	private TextView mClockDate;
	private WtClock mClockBatt;
	private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd  E");
	
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
    	
    	View clockItem = findViewById(R.id.clk_hour);
    	if(clockItem != null){
    		mClockHour = (WtClock)clockItem;
    	}
    	clockItem = findViewById(R.id.clk_min);
    	if(clockItem != null){
    		mClockMin = (WtClock)clockItem;
    	}
    	clockItem = findViewById(R.id.clk_sec);
    	if(clockItem != null){
    		mClockSec = (WtClock)clockItem;
    	}
    	clockItem = findViewById(R.id.clk_date);
    	if(clockItem != null){
    		mClockDate = (TextView)clockItem;
    	}
    	clockItem = findViewById(R.id.clk_batt);
    	if(clockItem != null){
    		mClockBatt = (WtClock)clockItem;
    	}
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
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);

            getContext().registerReceiverAsUser(mIntentReceiver,
                    android.os.Process.myUserHandle(), filter, null, mHandler);
            
            mHandler.postDelayed(mSecRunnable, 1000);
        }
        
        mCalendar = new Time();
        mDate = new Date();
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
        if(mClockDate != null){
        	mDate.setTime(System.currentTimeMillis());
        	mClockDate.setText(mDateFormat.format(mDate));
        }
    }
    
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	if(action.equals(Intent.ACTION_BATTERY_CHANGED)){
        		if(mClockBatt != null){
        			int level = (int)(100f
                    		* intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    		/ intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
        			mClockBatt.setValue(level);
        		}
        		return;
        	}
        	
            if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
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
