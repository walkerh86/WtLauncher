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
import com.cj.util.TimeUtil;
import com.cj.wtlauncher.MobileController;
import com.cj.wtlauncher.NetworkController;
import com.cj.wtlauncher.R;
import com.systemui.ext.DataType;

public class WtClockRoot extends FrameLayout {
	private static final String TAG = "hcj.WtClockRoot";
	private Time mCalendar;
	private Date mDate;	
	private boolean mAttached;
	private WtHour mClockHour;
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
	private ImageView mClockWifiConnect;
	private QSBluetoothTile mQSBluetoothTile;
	private ImageView mClockBtConnect;
	//
	private NetworkController mNetworkController;
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
	
	private ContentObserver mTimeFormatObserver = new ContentObserver(mHandler){
		@Override
		public void onChange(boolean selfChange) {
			if(mClockHour != null){
				mClockHour.setIsHour24(TimeUtil.is24HourFormat(mContext));
			}
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
    		mClockHour = (WtHour)clockItem;
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
    	
    	clockItem = findViewById(R.id.tx_clk_ampm);
    	if(clockItem != null){
    		TextView tvAmPm = (TextView)clockItem;
    		if(mClockHour != null){
    			mClockHour.setAmPmView(tvAmPm);
    			mClockHour.setIsHour24(TimeUtil.is24HourFormat(mContext));
    		}
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
    	clockItem = findViewById(R.id.clk_wifi_connect);
    	if(clockItem != null){
    		mClockWifiConnect = (ImageView)clockItem;
    	}
    	clockItem = findViewById(R.id.clk_bt_connect);
    	if(clockItem != null){
    		mClockBtConnect = (ImageView)clockItem;
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
			
			if(mClockHour != null){				
				getContext().getContentResolver().registerContentObserver(
						Settings.System.getUriFor(Settings.System.TIME_12_24), true, 
						mTimeFormatObserver, 
						UserHandle.USER_ALL);
            }
		
			if(mClockBt != null || mClockBtConnect != null){
				mQSBluetoothTile = new QSBluetoothTile(getContext(),null,mClockBtConnect);
				mQSBluetoothTile.setOnStateChangedListener(new QSBluetoothTile.OnStateChangedListener(){
					@Override
					public void onStateChanged(boolean enabled){
						if(mClockBt != null){
							mClockBt.setValue(enabled ? 1 : 0);
						}
					}
				});
				if(mClockBt != null){
					mClockBt.setValue(mQSBluetoothTile.isEnabled() ? 1 : 0);
				}
			}
			
			if(mClockStep != null){
				getContext().getContentResolver().registerContentObserver(
						Settings.System.getUriFor("today_steps"), true, 
						mStepsObserver, 
						UserHandle.USER_ALL);
			}
			
			if(isNetworkControllerNeeded()){
				mNetworkController = NetworkController.getInstance(getContext());
				mNetworkController.addOnNetworkListener(mOnNetworkListener);		
				
				if(mClockWifi != null){
					mClockWifi.setValue(mNetworkController.isWifiEnabled() ? 1 : 0);
				}
				if(mClockWifiConnect != null){
					mClockWifiConnect.setVisibility(mNetworkController.isWifiConnected() ? View.VISIBLE : View.GONE);
				}
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
            if(mClockHour != null){
				getContext().getContentResolver().unregisterContentObserver(mTimeFormatObserver);
            }
        	if(mQSBluetoothTile != null){
				mQSBluetoothTile.onDestroy(getContext());				
			}
        	if(mClockStep != null){
        		getContext().getContentResolver().unregisterContentObserver(mStepsObserver);
			}            
            if(mNetworkController != null){
            	mNetworkController.removeOnNetworkListener(mOnNetworkListener);
			}
            mHandler.removeCallbacks(mSecRunnable);
            mAttached = false;
        }
    }
    
    private boolean isNetworkControllerNeeded(){
    	return mMobileSignalView != null || mMobileDataView != null || mClockWifiConnect != null || mClockWifi != null;
    }
    
    private boolean mAirplaneOn;
    private NetworkController.OnNetworkListener mOnNetworkListener = new NetworkController.OnNetworkListener() {					
		@Override
		public void onSignalStrengthChange(int level) {
			if(mAirplaneOn){
				return;
			}
			if(level < 0){
				level = 0;
			}
			if(mMobileSignalView != null){
				mMobileSignalView.setImageLevel(level);
			}
		}
		
		@Override
		public void onDataTypeChange(int dataType){
			int level = 0;
			if(dataType == MobileController.WT_NETWORK_TYPE_2G){
				level = 1;
			}else if(dataType == MobileController.WT_NETWORK_TYPE_3G){
				level = 2;
			}
			if(mMobileDataView != null){
				mMobileDataView.setImageLevel(level);
			}
		}
		
		@Override
		public void onDataEnable(boolean enable){
			
		}
		
		@Override
		public void onWifiEnable(boolean enable){
			if(mClockWifi != null){
				mClockWifi.setValue(enable ? 1 : 0);
			}
		}
		
		@Override
		public void onWifiConnect(boolean connected, int level){
			if(mClockWifiConnect != null){
				mClockWifiConnect.setVisibility(connected ? View.VISIBLE : View.GONE);
				if(connected){
					mMobileDataView.setImageLevel(0);
				}
			}
		}
		
		@Override
		public void onAirplaneEnable(boolean enable){
			mAirplaneOn = enable;
			if(enable){
				mMobileSignalView.setImageResource(R.drawable.stat_sys_mobile_airplane);
			}else{
				mMobileSignalView.setImageResource(R.drawable.mobile_signal_level_drawable);
				mMobileSignalView.setImageLevel(0);
			}
		}
	};
    
    private void onTimeChanged() {
        mCalendar.setToNow();

        int hour = mCalendar.hour;
        int minute = mCalendar.minute;
        int second = mCalendar.second;

        int m = (int)(minute + second / 60.0f);
        int h = (int)(hour + m / 60.0f);
        h %= 24;//hour is 0-23
        if(mClockHour != null){     	
        	mClockHour.setValue(h,m);
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
        	int value = mClockHour.isPointerStyle() ? (h*30+(int)(m*30/60f)) : h;
        	mClockHour2.setValue(value);
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
