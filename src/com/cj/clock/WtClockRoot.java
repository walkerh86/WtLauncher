package com.cj.clock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cj.wtlauncher.R;

public class WtClockRoot extends FrameLayout {
	private static final String TAG = "hcj.WtClockRoot";
	private Time mCalendar;
	private Date mDate;
	private final Handler mHandler = new Handler();
	private boolean mAttached;
	private WtClock mClockHour;
	private WtClock mClockMin;
	private WtClock mClockSec;
	private WtDate mClockDate;
	private WtClock mClockBatt;
	private SimpleDateFormat mDateFormat;
	private TextView mClockStep;
	private WtClock mClockHour2;
	private WtClock mClockMin2;
	private View mClockDial;
	
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
    		mClockDate = (WtDate)clockItem;
    		mDateFormat = new SimpleDateFormat(mClockDate.getDateFormat());
    	}
    	clockItem = findViewById(R.id.clk_batt);
    	if(clockItem != null){
    		mClockBatt = (WtClock)clockItem;
    	}
    	clockItem = findViewById(R.id.clk_step);
    	if(clockItem != null){
    		mClockStep = (TextView)clockItem;    
    		mClockStep.setText(String.valueOf(getCurrentSteps()));
    	}
    	
    	clockItem = findViewById(R.id.clk_hour2);
    	if(clockItem != null){
    		mClockHour2 = (WtClock)clockItem;
    	}
    	clockItem = findViewById(R.id.clk_min2);
    	if(clockItem != null){
    		mClockMin2 = (WtClock)clockItem;
    	}
    	
    	clockItem = findViewById(R.id.clk_dial);
    	if(clockItem != null){
    		mClockDial = clockItem;
    		mClockDial.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					startDialActivity();
				}
			});
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
        
        if(mClockHour2 != null){
        	mClockHour2.setValue(h);
        }
        if(mClockMin2 != null){
        	mClockMin2.setValue(m);
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
    
    private int getCurrentSteps(){
    	return Settings.System.getInt(getContext().getContentResolver(), "today_steps", 0);
    }
    
    private void startDialActivity(){
    	Intent intent = new Intent();
    	intent.setComponent(new ComponentName("com.android.dialer","com.wt.WtDialtactsActivity"));
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	getContext().startActivity(intent);
    }
}
