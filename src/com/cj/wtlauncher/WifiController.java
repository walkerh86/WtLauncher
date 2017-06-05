package com.cj.wtlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;


public class WifiController {
	private WifiManager mWifiManager;
	private boolean mEnabled;
	private boolean mConnected;
	private int mRssi;
	private int mLevel;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN);
				boolean enable = (state == WifiManager.WIFI_STATE_ENABLED);
				if(enable != mEnabled){
					mEnabled = enable;
					if(mOnWifiChangeListener != null){
						mOnWifiChangeListener.onWifiEnable(mEnabled);
					}
				}				
			}else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
				final NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				boolean connected = networkInfo != null && networkInfo.isConnected();
				if(connected != mConnected){
					mConnected = connected;
					if(mOnWifiChangeListener != null){
						mOnWifiChangeListener.onWifiConnect(mConnected);
					}
				}
			}else if(action.equals(WifiManager.RSSI_CHANGED_ACTION)){
				mRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
				mLevel = WifiManager.calculateSignalLevel(mRssi, 5);
			}			
		}
	};
	
	public WifiController(Context context){
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mEnabled = isEnabled();
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		context.registerReceiver(mReceiver, filter);	
	}
	
	public void onDestroy(Context context){
		context.unregisterReceiver(mReceiver);
	}
	
	public boolean isEnabled() {
		return mWifiManager != null && mWifiManager.isWifiEnabled();
	}
	
	public void setEnabled(boolean enabled) {
	 	if(mWifiManager == null){
			return;
	 	}
	 	mWifiManager.setWifiEnabled(enabled);
	}
	
	private OnWifiChangeListener mOnWifiChangeListener;
	public void setOnWifiChangeListener(OnWifiChangeListener listener){
		mOnWifiChangeListener = listener;
	}
	
	public interface OnWifiChangeListener{
		void onWifiEnable(boolean enable);
		void onWifiConnect(boolean connected);
	}
}
