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
import android.database.ContentObserver;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.cj.qs.QSBluetoothTile;
import com.cj.qs.QSWifiTile;
import com.cj.wtlauncher.MobileController;
import com.cj.wtlauncher.R;
import com.systemui.ext.DataType;

public class WtClockRoot extends FrameLayout {
	private static final String TAG = "hcj.WtClockRoot";
	private Time mCalendar;
	private Date mDate;	
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
	private View mClockMessage;
	private WtClock mClockBt;
	private WtClock mClockWifi;
	private ImageView mClockWifiOn;
	private QSBluetoothTile mQSBluetoothTile;
	private QSWifiTile mQSWifiTile;
	//
	private MobileController mMobileController;
	private ImageView mMobileSignalView;
	private ImageView mMobileDataView;
	
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case MSG_UPDATE_STEPS:
					int steps = Settings.System.getInt(getContext().getContentResolver(), "today_steps",0);
					//Log.i("hcjhcj", "handleMessage step="+steps);
					mClockStep.setText(String.valueOf(steps));
					break;
				default:
					break;
			}
		}
	};
	private static final int MSG_UPDATE_STEPS = 0;
	
	private ContentObserver mStepsObserver = new ContentObserver(mHandler){
		@Override
		public void onChange(boolean selfChange) {
			mHandler.sendEmptyMessage(MSG_UPDATE_STEPS);
		}
	};
	
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
    	clockItem = findViewById(R.id.clk_message);
    	if(clockItem != null){
    		mClockMessage = clockItem;
    		mClockMessage.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					startMessageActivity();
				}
			});
    	}
    	clockItem = findViewById(R.id.clk_bt);
    	if(clockItem != null){
    		mClockBt = (WtClock)clockItem;
    	}
    	clockItem = findViewById(R.id.clk_wifi);
    	if(clockItem != null){
    		mClockWifi = (WtClock)clockItem;
    	}
    	clockItem = findViewById(R.id.clk_wifi_on);
    	if(clockItem != null){
    		mClockWifiOn = (ImageView)clockItem;
    	}
    	
    	clockItem = findViewById(R.id.clk_mobile_signal);
    	if(clockItem != null){
    		mMobileSignalView = (ImageView)clockItem;
    	}
    	clockItem = findViewById(R.id.clk_mobile_data);
    	if(clockItem != null){
    		mMobileDataView = (ImageView)clockItem;
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
            if(mClockBatt != null){
            	filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            }
			getContext().registerReceiverAsUser(mIntentReceiver,android.os.Process.myUserHandle(), filter, null, mHandler);                
		
			if(mClockBt != null){
				mQSBluetoothTile = new QSBluetoothTile(getContext(),null,null);
				mQSBluetoothTile.setOnStateChangedListener(new QSBluetoothTile.OnStateChangedListener(){
					@Override
					public void onStateChanged(boolean enabled){
						mClockBt.setValue(enabled ? 1 : 0);
					}
				});
				mClockBt.setValue(mQSBluetoothTile.isEnabled() ? 1 : 0);
			}
			if(mClockWifi != null || mClockWifiOn != null){
				mQSWifiTile = new QSWifiTile(getContext(),null,null);
				mQSWifiTile.setOnStateChangedListener(new QSWifiTile.OnStateChangedListener(){
					@Override
					public void onStateChanged(boolean enabled){
						if(mClockWifi != null){
							mClockWifi.setValue(enabled ? 1 : 0);
						}
						if(mClockWifiOn != null){
							mClockWifiOn.setVisibility(enabled ? View.VISIBLE : View.GONE);
						}
					}
				});
				if(mClockWifi != null){
					mClockWifi.setValue(mQSWifiTile.isEnabled() ? 1 : 0);
				}
				if(mClockWifiOn != null){
					mClockWifiOn.setVisibility(mQSWifiTile.isEnabled() ? View.VISIBLE : View.GONE);
				}
			}
			
			if(mClockStep != null){
				getContext().getContentResolver().registerContentObserver(
						Settings.System.getUriFor("today_steps"), true, 
						mStepsObserver, 
						UserHandle.USER_ALL);
			}
			
			if(mMobileSignalView != null || mMobileDataView != null){
				mMobileController = new MobileController(getContext());
				mMobileController.setOnMobileListener(new MobileController.OnMobileListener() {					
					@Override
					public void onSignalStrengthChange(int level) {
						mMobileSignalView.setImageLevel(level);
					}
					
					@Override
					public void onDataTypeChange(DataType dataType){
						mMobileDataView.setImageLevel(dataType == null ? 0 : 1);
					}
				});
			}
            
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
        	if(mClockBt != null && mQSBluetoothTile != null){
				mQSBluetoothTile.onDestroy(getContext());				
			}
			if(mClockWifi != null && mQSWifiTile != null){
				mQSWifiTile.onDestroy(getContext());
			}
        	if(mClockStep != null){
        		getContext().getContentResolver().unregisterContentObserver(mStepsObserver);
			}            
            if(mMobileSignalView != null || mMobileDataView != null){
				mMobileController.destroy();
			}
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
    
    private void startMessageActivity(){
    	Intent intent = new Intent();
    	intent.setComponent(new ComponentName("com.android.mms","com.android.mms.ui.BootActivity"));
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	getContext().startActivity(intent);
    }
}
