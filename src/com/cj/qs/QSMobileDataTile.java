package com.cj.qs;

import com.cj.util.ReflectUtil;
import com.cj.wtlauncher.MobileController;
import com.cj.wtlauncher.NetworkController;
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
	private NetworkController mNetworkController;
	private Context mContext;
	private boolean mAirplaneOn;
	
	public QSMobileDataTile(Context context, QSTileView tileView){
		mContext = context;
		mTelephonyManager = TelephonyManager.from(context);
				
		mNetworkController = NetworkController.getInstance(context);
		mNetworkController.addOnNetworkListener(new NetworkController.OnNetworkListener() {					
			@Override
			public void onSignalStrengthChange(int level) {
				//mMobileSignalView.setImageLevel(level);
			}
			
			@Override
			public void onDataTypeChange(int dataType){
				int level = 0;
				if(dataType == MobileController.WT_NETWORK_TYPE_2G){
					level = 1;
				}else{
					level = 2;
				}
				//mMobileDataView.setImageLevel(level);
			}
			
			@Override
			public void onDataEnable(boolean enable){
				updateView(isEnabled());
			}
			
			@Override
			public void onWifiEnable(boolean enable){
				
			}
			
			@Override
			public void onAirplaneEnable(boolean enable){
				mAirplaneOn = enable;
				updateView(isEnabled());
			}
		});
		mAirplaneOn = mNetworkController.isAirplaneOn();
		
		mTileView = tileView;
		tileView.setOnClickListener(mClickListener);
		//tileView.setOnLongClickListener(mLongClickListener);
		updateView(isEnabled());
	}
	
	public void onDestroy(Context context){
		//mNetworkController.destroy();
	}

	private void updateView(boolean isOn){
		Log.i(TAG, "updateView isOn="+isOn);
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
	
	private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener(){
		@Override
		public boolean onLongClick(View arg0) {
			handleLongClick();
			return true;
		}		
	};
	
	private void handleLongClick(){
		 Intent intent = new Intent();
		 intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$DataUsageSummaryActivity"));
		 mContext.startActivity(intent);
	 }
	
	public boolean isEnabled() {
		boolean enabled = false;
		try{
			enabled = mTelephonyManager.getDataEnabled(0);
			Log.i(TAG, "mAirplaneOn="+mNetworkController.isAirplaneOn());
			if(mAirplaneOn){
				enabled = false;
			}
		}catch(Exception e){
			Log.i(TAG,"isEnabled e="+e);
		}
		Log.i(TAG, "isEnabled enabled="+enabled);
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		Log.i(TAG, "setEnabled enabled="+enabled+",mTelephonyManager="+mTelephonyManager);
		try{
			if (mTelephonyManager != null) {
				mTelephonyManager.setDataEnabled(0,enabled);
			}
		}catch(Exception e){
			Log.i(TAG,"setEnabled e="+e);
		}
	}
}
