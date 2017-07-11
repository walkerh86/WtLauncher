package com.cj.wtlauncher;

import com.android.internal.telephony.TelephonyIntents;
import com.cj.qs.QSAirplaneTile;
import com.cj.qs.QSBluetoothTile;
import com.cj.qs.QSMobileDataTile;
import com.cj.qs.QSRaiseWakeTile;
import com.cj.qs.QSTileView;
import com.cj.qs.QSWifiTile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.view.View;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.UserHandle;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.SignalStrength;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;

import java.util.List;

public class StatusActivity extends Activity{
	private static final String TAG = "hcj.StatusActivity";
	private ImageView mBatteryView;
	private TextView mBatteryPercentView;
	private int mBattLvlDrawableId;
	private int mBatteryLevel;

	private View mBrightnessLowView;
	private View mBrightnessMediumView;
	private View mBrightnessHighView;
	private static final int BRIGHTNESS_VALUE_LOW = 30;
	private static final int BRIGHTNESS_VALUE_MEDIUM = 128;
	private static final int BRIGHTNESS_VALUE_HIGH = 255;
	private static final int BRIGHTNESS_RANGE_LOW = BRIGHTNESS_VALUE_MEDIUM-(BRIGHTNESS_VALUE_MEDIUM-BRIGHTNESS_VALUE_LOW)/2;
	private static final int BRIGHTNESS_RANGE_HIGH = BRIGHTNESS_VALUE_MEDIUM+(BRIGHTNESS_VALUE_HIGH-BRIGHTNESS_VALUE_MEDIUM)/2;;
	//private int mMaximumBacklight;

	private QSBluetoothTile mBluetoothTile;
	//private QSWifiTile mWifiTile;
	//private QSMobileDataTile mMobileDataTile;
	//private QSAirplaneTile mAirplaneTile;
	private QSRaiseWakeTile mQSRaiseWakeTile;
	
	ImageView mWifiEnableView;
	ImageView mWifiConnectView;
	ImageView mMobileDataEnableView;
	ImageView mAirplaneEnableView;
	private boolean mAirplaneOn;
	private boolean mWifiConnected;

	//signal
	private ImageView mSignalView;
	private ImageView mMobileDataTypeView;
	private TextView mOperatorView;

	private SubscriptionManager mSubscriptionManager;

	//notification mode
	private ImageView mNotfiyModeNormalBtn;
	private ImageView mNotfiyModeVibrateBtn;
	
	private ImageView mBtStatusView;
	
	private NetworkController mNetworkController;
	static final int[] WIFI_SIGNAL_STRENGTH_FULL = {
			R.drawable.stat_sys_wifi_strength_0,
			R.drawable.stat_sys_wifi_strength_1,
			R.drawable.stat_sys_wifi_strength_2,
			R.drawable.stat_sys_wifi_strength_3,
			R.drawable.stat_sys_wifi_strength_4,
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status);

		mBatteryView = (ImageView)findViewById(R.id.img_battery);
		mBatteryPercentView = (TextView)findViewById(R.id.tv_battery);

		mBrightnessLowView = findViewById(R.id.brightness_low);
		mBrightnessLowView.setOnClickListener(mOnClickListener);
		mBrightnessMediumView = findViewById(R.id.brightness_mid);
		mBrightnessMediumView.setOnClickListener(mOnClickListener);
		mBrightnessHighView = findViewById(R.id.brightness_hig);
		mBrightnessHighView.setOnClickListener(mOnClickListener);
		//PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		//mMinimumBacklight = pm.getMinimumScreenBrightnessSetting();
		//mMaximumBacklight = pm.getMaximumScreenBrightnessSetting();
		updateBrightnessViews();

		//qs bar
		QSTileView bluetoothTileView = (QSTileView)findViewById(R.id.bt_settings);
		mBtStatusView = (ImageView)findViewById(R.id.img_bt);
		mBluetoothTile = new QSBluetoothTile(this,bluetoothTileView,null);
		mBluetoothTile.setOnStateChangedListener(new QSBluetoothTile.OnStateChangedListener(){
			@Override
			public void onStateChanged(int state) {
				updateBtView(state);
			}
		});
		updateBtView(QSBluetoothTile.BT_STATE_UNKOWN);
		
		mWifiEnableView = (ImageView)findViewById(R.id.wifi_settings);
		mWifiConnectView = (ImageView)findViewById(R.id.img_wifi);
		mWifiEnableView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				mNetworkController.toggleWifi();
			}
		});
		mWifiEnableView.setOnLongClickListener(new View.OnLongClickListener() {			
			@Override
			public boolean onLongClick(View arg0) {
				startWifiActivity();
				return true;
			}
		});
		//mWifiTile = new QSWifiTile(this,wifiTileView,wifiStatusView);
		
		mMobileDataEnableView = (ImageView)findViewById(R.id.mobile_data_settings);
		mMobileDataEnableView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				mNetworkController.toggleMobileData();
			}
		});
		//mMobileDataTile = new QSMobileDataTile(this,mobileDataView);

		mAirplaneEnableView = (ImageView)findViewById(R.id.system_airplane_mode);
		mAirplaneEnableView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				mNetworkController.toggleAirplane();
			}
		});
		//mAirplaneTile = new QSAirplaneTile(this,airplaneView);
		
		QSTileView raiseWakeView = (QSTileView)findViewById(R.id.system_screenon_guesture);
		mQSRaiseWakeTile = new QSRaiseWakeTile(this,raiseWakeView);

		//status bar
		mSignalView = (ImageView)findViewById(R.id.img_signal);
		mMobileDataTypeView = (ImageView)findViewById(R.id.img_type);
		mOperatorView = (TextView)findViewById(R.id.tv_operator);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
		filter.addAction(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED);
		registerReceiver(mReceiver, filter);

		mSubscriptionManager = SubscriptionManager.from(this);
		mSubscriptionManager.addOnSubscriptionsChangedListener(mSubscriptionListener);

		//notification mode
		mNotfiyModeNormalBtn = (ImageView)findViewById(R.id.notify_send_style_1);
		mNotfiyModeNormalBtn.setOnClickListener(mOnClickListener);
		mNotfiyModeVibrateBtn = (ImageView)findViewById(R.id.notify_send_style_2);
		mNotfiyModeVibrateBtn.setOnClickListener(mOnClickListener);
		updateNotificationModeView(getNotificationMode());
		
		mNetworkController = NetworkController.getInstance(this);
		mNetworkController.addOnNetworkListener(mOnNetworkListener);
		
		mAirplaneOn = mNetworkController.isAirplaneOn();
		updateAirplaneView(mAirplaneOn);
		updateWifiView(mNetworkController.isWifiEnabled());
		updateMobileDataView(mNetworkController.isMobileDataEnable());
		updateMobileSignalStrengthView(mNetworkController.getMobileSignalStrengthLevel());
		updateMobileDataTypeView(mNetworkController.getMobileDataNetType());
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		mSubscriptionManager.removeOnSubscriptionsChangedListener(mSubscriptionListener);
		unregisterReceiver(mReceiver);
		mBluetoothTile.onDestroy(this);
		//mWifiTile.onDestroy(this);
		//mAirplaneTile.onDestroy(this);
		//mMobileDataTile.onDestroy(this);
		mNetworkController.removeOnNetworkListener(mOnNetworkListener);
	}
	
	private void updateBtView(int state){
    	if(mBtStatusView == null){
    		return;
    	}
    	if(state == QSBluetoothTile.BT_STATE_UNKOWN){
    		if(mBluetoothTile.isConnected()){
    			state = QSBluetoothTile.BT_STATE_CONNECTED;
    		}else if(mBluetoothTile.isEnabled()){
    			state = QSBluetoothTile.BT_STATE_ON;
    		}else{
    			state = QSBluetoothTile.BT_STATE_OFF;
    		}
    	}
    	if(state == QSBluetoothTile.BT_STATE_CONNECTED){
    		mBtStatusView.setImageResource(R.drawable.stat_sys_bt_connected);
    		mBtStatusView.setVisibility(View.VISIBLE);
		}else if(state == QSBluetoothTile.BT_STATE_ON){
			mBtStatusView.setImageResource(R.drawable.stat_sys_bluetooth);
			mBtStatusView.setVisibility(View.VISIBLE);
		}else{
			mBtStatusView.setVisibility(View.GONE);
		}
    }
	
	private NetworkController.OnNetworkListener mOnNetworkListener = new NetworkController.OnNetworkListener() {					
		@Override
		public void onSignalStrengthChange(int level) {			
			updateMobileSignalStrengthView(level);
		}
		
		@Override
		public void onDataTypeChange(int dataType){
			updateMobileDataTypeView(dataType);
		}
		
		@Override
		public void onDataEnable(boolean enable){
			updateMobileDataView(enable);
		}
		
		@Override
		public void onWifiEnable(boolean enable){
			updateWifiView(enable);
		}
		
		@Override
		public void onWifiConnect(boolean connected, int level){
			mWifiConnected = connected;
			if(connected){
				mWifiConnectView.setImageResource(WIFI_SIGNAL_STRENGTH_FULL[level]);
				mWifiConnectView.setVisibility(View.VISIBLE);
				updateMobileDataTypeView(MobileController.WT_NETWORK_TYPE_NULL);
			}else{
				mWifiConnectView.setVisibility(View.GONE);				
			}
		}
		
		@Override
		public void onAirplaneEnable(boolean enable){
			Log.i(AirplaneController.TAG,"onAirplaneEnable enable="+enable);
			mAirplaneOn = enable;
			updateAirplaneView(enable);
		}
	};
	
	private void updateMobileDataTypeView(int dataType){
		int level = 0;
		if(dataType == MobileController.WT_NETWORK_TYPE_2G){
			level = R.drawable.stat_sys_mobile_2g;
		}else if(dataType == MobileController.WT_NETWORK_TYPE_3G){
			level = R.drawable.stat_sys_mobile_3g;
		}else if(dataType == MobileController.WT_NETWORK_TYPE_4G){
			level = R.drawable.stat_sys_mobile_4g;
		}
		
		if(level == 0){
			mMobileDataTypeView.setImageDrawable(null);
		}else{
			mMobileDataTypeView.setImageResource(level);
		}
	}
	
	private void updateMobileSignalStrengthView(int level){
		if(mAirplaneOn){
			return;
		}
		if(level < 0){
			mSignalView.setImageResource(R.drawable.stat_sys_mobile_strength_null);
		}else{
			mSignalView.setImageResource(SIGNAL_STRENGTH_ICONS[level]);
		}
	}
	
	private void updateAirplaneView(boolean airplaneOn){
		mAirplaneEnableView.setImageResource(airplaneOn ? R.drawable.smart_watch_airmode_on : R.drawable.smart_watch_airmode_off);
		if(airplaneOn){
			mSignalView.setImageResource(R.drawable.stat_sys_mobile_airplane);
			mOperatorView.setVisibility(View.GONE);
		}else{
			mSignalView.setImageResource(R.drawable.stat_sys_mobile_strength_null);
			mOperatorView.setVisibility(View.VISIBLE);
		}
	}
	
	private void updateWifiView(boolean enable){
		mWifiEnableView.setImageResource(enable ? R.drawable.smart_watch_wifi_on : R.drawable.smart_watch_wifi_off);
	}
	
	private void startWifiActivity(){
		 Intent intent = new Intent();
		 intent.setComponent(new ComponentName("com.android.settings","com.android.settings.wifi.WifiSettings"));
		 startActivity(intent);
	}
	
	private void updateMobileDataView(boolean enable){
		mMobileDataEnableView.setImageResource(enable ? R.drawable.smart_watch_mobile_data_on : R.drawable.smart_watch_mobile_data_off);
	}

	private void updateBrightnessViews(){
		int value = 0;
		try{
			value = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			Log.i(TAG, "updateBrightnessViews e="+e);
			return;
		}
		boolean isLow = false;
		boolean isHigh = false;
		if(value < BRIGHTNESS_RANGE_LOW){
			isLow = true;
		}else if(value > BRIGHTNESS_RANGE_HIGH){
			isHigh = true;
		}
		if(isLow){
			mBrightnessLowView.setBackgroundResource(R.drawable.brightness_level_background);
			mBrightnessHighView.setBackground(null);
			mBrightnessMediumView.setBackground(null);
		}else if(isHigh){
			mBrightnessHighView.setBackgroundResource(R.drawable.brightness_level_background);
			mBrightnessLowView.setBackground(null);
			mBrightnessMediumView.setBackground(null);
		}else{
			mBrightnessMediumView.setBackgroundResource(R.drawable.brightness_level_background);
			mBrightnessLowView.setBackground(null);
			mBrightnessHighView.setBackground(null);
		}
	}

	private void setBrightnessValue(int value){
		Log.i(TAG, "setBrightnessValue 0="+value);
		try{
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,value);
			updateBrightnessViews();
		} catch (Exception e) {
			Log.i(TAG, "setBrightnessValue e="+e);
		}
	}

	private static final int[] BATT_LVL_DRAWABLES = new int[]{
		R.drawable.stat_sys_battery_0,
		R.drawable.stat_sys_battery_20,
		R.drawable.stat_sys_battery_40,
		R.drawable.stat_sys_battery_60,
		R.drawable.stat_sys_battery_80,
		R.drawable.stat_sys_battery_100,
	};
	private static final int[] BATT_LVL_CHARGE_DRAWABLES = new int[]{
		R.drawable.stat_sys_charging_0,
		R.drawable.stat_sys_charging_20,
		R.drawable.stat_sys_charging_40,
		R.drawable.stat_sys_charging_60,
		R.drawable.stat_sys_charging_80,
		R.drawable.stat_sys_charging_100,
	};
	private void updateBatteryViews(){
		mBatteryPercentView.setText(String.valueOf(mBatteryLevel));
		mBatteryView.setImageResource(mBattLvlDrawableId);
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			//Log.i(TAG,"onReceive action="+action);
			if(Intent.ACTION_BATTERY_CHANGED.equals(action)){
				mBatteryLevel = (int)(100f
                    		* intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    		/ intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
				final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    		BatteryManager.BATTERY_STATUS_UNKNOWN);
				boolean charging = status == BatteryManager.BATTERY_STATUS_FULL || status == BatteryManager.BATTERY_STATUS_CHARGING;
				int imgLevel = mBatteryLevel/20;
				mBattLvlDrawableId = charging ? BATT_LVL_CHARGE_DRAWABLES[imgLevel] : BATT_LVL_DRAWABLES[imgLevel];
				updateBatteryViews();
			}else if(TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)){
				updateSimState();
			}else if(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED.equals(action)){
				updateSimState();
			}
		}
	};

	private View.OnClickListener mOnClickListener = new View.OnClickListener(){
		@Override
		public void onClick(View view){
			int id = view.getId();
			if(id == R.id.brightness_low){
				setBrightnessValue(BRIGHTNESS_VALUE_LOW);
			}else if(id == R.id.brightness_mid){
				setBrightnessValue(BRIGHTNESS_VALUE_MEDIUM);
			}else if(id == R.id.brightness_hig){
				setBrightnessValue(BRIGHTNESS_VALUE_HIGH);
			}else if(id == R.id.notify_send_style_1){
				setNotificationMode(NotificationFragment.MODE_NORMAL);
			}else if(id == R.id.notify_send_style_2){
				setNotificationMode(NotificationFragment.MODE_VIBRATE);
			}
		}
	};

	//signal
	private static final int[] SIGNAL_STRENGTH_ICONS = new int[]{
		R.drawable.stat_sys_mobile_strength_0,
		R.drawable.stat_sys_mobile_strength_1,
		R.drawable.stat_sys_mobile_strength_2,
		R.drawable.stat_sys_mobile_strength_3,
		R.drawable.stat_sys_mobile_strength_4,
	};
	private SignalStrength mSignalStrength;
	private ServiceState mServiceState;
	private boolean mStateConnected;
	PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) { 
			Log.i("hcj","onSignalStrengthsChanged signalStrength="+signalStrength);
			mSignalStrength = signalStrength;
			updateTelephony();
		}

		@Override
             public void onServiceStateChanged(ServiceState state) {
             	Log.i("hcj","onServiceStateChanged state="+state);
             	mServiceState = state;
		}
	};

	private void updateTelephony(){
		mStateConnected = hasService() && mSignalStrength != null;
		Log.i("hcj","updateTelephony mStateConnected="+mStateConnected);
		int signalIconId = getSimStateIconId();
		//mSignalView.setImageResource(signalIconId);
		if(mNoSims){
			mOperatorView.setText(R.string.no_sim_card);
		}else{
			mOperatorView.setText(null);
		}
		
	}

	private int getSimStateIconId() {
		if(mStateConnected){
			int signalLevel = mSignalStrength.getLevel();
			Log.i("hcj","getSimStateIconId signalLevel="+signalLevel);
			return SIGNAL_STRENGTH_ICONS[signalLevel];
		}else{
			return mNoSims ? R.drawable.stat_sys_mobile_strength_null : R.drawable.stat_sys_mobile_strength_0;
		}
	}

	private boolean hasService(){
		if (mServiceState != null) {
			Log.i("hcj","hasService state="+mServiceState.getVoiceRegState());
			switch (mServiceState.getVoiceRegState()) {
				case ServiceState.STATE_POWER_OFF:
					return false;
				case ServiceState.STATE_OUT_OF_SERVICE:
				case ServiceState.STATE_EMERGENCY_ONLY:
					return mServiceState.getDataRegState() == ServiceState.STATE_IN_SERVICE;
				default:
					return true;
			}
		} else {
			return false;
		}
	}

	private boolean mNoSims;
	private void updateSimState(){
		List<SubscriptionInfo> subscriptions = mSubscriptionManager.getActiveSubscriptionInfoList();
		if(subscriptions == null || subscriptions.size() < 1){
			mNoSims = true;
		}else{
			mNoSims = false;
		}
		Log.i(TAG, "updateSimState mNoSims="+mNoSims);
		updateTelephony();
	}
	
    private final OnSubscriptionsChangedListener mSubscriptionListener =
            new OnSubscriptionsChangedListener() {
        @Override
        public void onSubscriptionsChanged() {
            Log.i(TAG, "onSubscriptionsChanged");
            updateSimState();
        };
    };

	private void setNotificationMode(int mode){
		SystemProperties.set("persist.sys.notify.coming", String.valueOf(mode));
		updateNotificationModeView(mode);
	}

	private int getNotificationMode(){
		return SystemProperties.getInt("persist.sys.notify.coming", NotificationFragment.MODE_NORMAL);
	}

	private void updateNotificationModeView(int mode){
		if(mode == NotificationFragment.MODE_VIBRATE){
			mNotfiyModeNormalBtn.setImageResource(R.drawable.smart_watch_notify_send_style1);
			mNotfiyModeVibrateBtn.setImageResource(R.drawable.smart_watch_notify_send_style2_selected);
		}else{
			mNotfiyModeNormalBtn.setImageResource(R.drawable.smart_watch_notify_send_style1_selected);
			mNotfiyModeVibrateBtn.setImageResource(R.drawable.smart_watch_notify_send_style2);
		}
	}
}
