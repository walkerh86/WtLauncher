package com.cj.wtlauncher;

import com.android.internal.telephony.TelephonyIntents;
import com.cj.aidl.ISettingsService;
import com.cj.qs.QSAirplaneTile;
import com.cj.qs.QSBluetoothTile;
import com.cj.qs.QSMobileDataTile;
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
	private QSWifiTile mWifiTile;
	private QSMobileDataTile mMobileDataTile;
	private QSAirplaneTile mAirplaneTile;

	//signal
	private ImageView mSignalView;
	private TextView mOperatorView;

	private SubscriptionManager mSubscriptionManager;

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
		ImageView btStatusView = (ImageView)findViewById(R.id.img_bt);
		mBluetoothTile = new QSBluetoothTile(this,bluetoothTileView,btStatusView);
		
		QSTileView wifiTileView = (QSTileView)findViewById(R.id.wifi_settings);
		ImageView wifiStatusView = (ImageView)findViewById(R.id.img_wifi);
		mWifiTile = new QSWifiTile(this,wifiTileView,wifiStatusView);
		
		QSTileView mobileDataView = (QSTileView)findViewById(R.id.mobile_data_settings);
		mMobileDataTile = new QSMobileDataTile(this,mobileDataView);

		QSTileView airplaneView = (QSTileView)findViewById(R.id.system_airplane_mode);
		mAirplaneTile = new QSAirplaneTile(this,airplaneView);

		//status bar
		mSignalView = (ImageView)findViewById(R.id.img_signal);
		mOperatorView = (TextView)findViewById(R.id.tv_operator);
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(mPhoneStateListener,PhoneStateListener.LISTEN_SERVICE_STATE|PhoneStateListener.LISTEN_SIGNAL_STRENGTHS); 
		updateTelephony();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
		filter.addAction(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED);
		registerReceiver(mReceiver, filter);

		mSubscriptionManager = SubscriptionManager.from(this);
		mSubscriptionManager.addOnSubscriptionsChangedListener(mSubscriptionListener);

		setupSettingsService(this);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		mSubscriptionManager.removeOnSubscriptionsChangedListener(mSubscriptionListener);
		unregisterReceiver(mReceiver);
		mBluetoothTile.onDestroy(this);
		mWifiTile.onDestroy(this);
		mAirplaneTile.onDestroy(this);
		mMobileDataTile.onDestroy(this);
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
		Log.i(TAG, "setBrightnessValue value="+value);
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
			if(view == mBrightnessLowView){
				setBrightnessValue(BRIGHTNESS_VALUE_LOW);
			}else if(view == mBrightnessMediumView){
				setBrightnessValue(BRIGHTNESS_VALUE_MEDIUM);
			}else if(view == mBrightnessHighView){
				setBrightnessValue(BRIGHTNESS_VALUE_HIGH);
			}
		}
	};

	private void setupSettingsService(Context context) {
		Log.i(TAG,"setupSettingsService");
		final Intent serviceIntent = new Intent(ISettingsService.class.getName());
		serviceIntent.setComponent(new ComponentName("com.android.settings","com.cj.settings.SettingsService"));
		if (mConnection == null || mSettingsService == null) {
			if(mConnection == null) {
				mConnection = new SettingsServiceConnection();
			}
		}
		if (!context.bindService(serviceIntent, mConnection,Context.BIND_AUTO_CREATE)) {
			Log.i(TAG,"can not bind SettingsService");
		}
	}

	private class SettingsServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected (ComponentName className, IBinder service){
			Log.i(TAG,"SettingsServiceConnection onServiceConnected service="+service);
			mSettingsService = ISettingsService.Stub.asInterface(service);

			mBluetoothTile.setSettingsService(mSettingsService);
			mWifiTile.setSettingsService(mSettingsService);
			mMobileDataTile.setSettingsService(mSettingsService);
			mAirplaneTile.setSettingsService(mSettingsService);
		}
		@Override
		public void onServiceDisconnected (ComponentName className){
			mSettingsService = null;
		}
	}

	private ServiceConnection mConnection = null;
	private ISettingsService mSettingsService;

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
		mSignalView.setImageResource(signalIconId);
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
}
