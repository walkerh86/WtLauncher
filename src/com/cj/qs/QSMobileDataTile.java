package com.cj.qs;

import com.cj.aidl.ISettingsService;
import com.cj.util.ReflectUtil;
import com.cj.wtlauncher.R;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;

public class QSMobileDataTile extends QSTile{
	private static final String TAG = "hcj.QSMobileDataTile";
	private QSTileView mTileView;
	private final TelephonyManager mTelephonyManager;
	
	public QSMobileDataTile(Context context, QSTileView tileView){
		mTelephonyManager = TelephonyManager.from(context);
		
		mTileView = tileView;
		tileView.setOnClickListener(mClickListener);
		updateView(isEnabled());
		
		final IntentFilter filter = new IntentFilter();
		//filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		//filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		context.registerReceiver(mReceiver, filter);		
	}
	
	public void onDestroy(Context context){
		context.unregisterReceiver(mReceiver);
	}

	public void setSettingsService(ISettingsService service){
		super.setSettingsService(service);
		updateView(isEnabled());
	}
	
	private void updateView(boolean isOn){
		mTileView.setImageResource(isOn ? R.drawable.smart_watch_mobile_data_on : R.drawable.smart_watch_mobile_data_off);
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
		}
	};
	
	private View.OnClickListener mClickListener = new View.OnClickListener(){
		@Override
		public void onClick(View view){
			setEnabled(!isEnabled());
		}
	};
	
	public boolean isEnabled() {
		//reflection way must put apk in /system/priv-app/
		//if(mTelephonyManager == null){
		//	return false;
		//}
		//Boolean ret = (Boolean)ReflectUtil.reflectCallMethod(mTelephonyManager, "getDataEnabled", null, null);
		//return ret != null && ret.booleanValue();
		boolean enabled = false;
		try{
			if(mSettingsService != null){
				enabled = mSettingsService.getDataEnabled();
			}
		}catch(Exception e){
			Log.i(TAG,"isEnabled e="+e);
		}
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		//if (mTelephonyManager != null) {
		//	mTelephonyManager.setDataEnabled(enabled);
		//}
		try{
			if(mSettingsService != null){
				mSettingsService.setDataEnabled(enabled);
			}
		}catch(Exception e){
			Log.i(TAG,"setEnabled e="+e);
		}
	}
}
