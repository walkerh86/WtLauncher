package com.cj.qs;

import com.cj.wtlauncher.R;

import android.net.wifi.WifiManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.ImageView;
import android.net.NetworkInfo;

public class QSWifiTile extends QSTile{
	private QSTileView mTileView;
	private ImageView mStatusView;
	private final WifiManager mWifiManager;
	private Context mContext;
	private SignalState mSignalState;
	static final int[] WIFI_SIGNAL_STRENGTH_FULL = {
          R.drawable.stat_sys_wifi_strength_0,
          R.drawable.stat_sys_wifi_strength_1,
          R.drawable.stat_sys_wifi_strength_2,
          R.drawable.stat_sys_wifi_strength_3,
          R.drawable.stat_sys_wifi_strength_4,
	};
	
	public QSWifiTile(Context context, QSTileView tileView, ImageView statusView){
		mContext = context;
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		mSignalState = new SignalState();
		mSignalState.enabled = isEnabled();
		
		mStatusView = statusView;
		
		mTileView = tileView;
		tileView.setOnClickListener(mClickListener);
		tileView.setOnLongClickListener(mLongClickListener);
		updateView();

		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		context.registerReceiver(mReceiver, filter);		
	}
	
	public void onDestroy(Context context){
		context.unregisterReceiver(mReceiver);
	}

	private void updateView(){
		mTileView.setImageResource(mSignalState.enabled ? R.drawable.smart_watch_wifi_on : R.drawable.smart_watch_wifi_off);
		if(mSignalState.connected){
			mStatusView.setImageResource(WIFI_SIGNAL_STRENGTH_FULL[mSignalState.level]);
			mStatusView.setVisibility(View.VISIBLE);
		}else{
			mStatusView.setVisibility(View.GONE);
		}
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN);
				mSignalState.enabled = (state == WifiManager.WIFI_STATE_ENABLED);
			}else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
				final NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				mSignalState.connected = networkInfo != null && networkInfo.isConnected();
			}else if(action.equals(WifiManager.RSSI_CHANGED_ACTION)){
				mSignalState.rssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
				mSignalState.level = WifiManager.calculateSignalLevel(mSignalState.rssi, WIFI_SIGNAL_STRENGTH_FULL.length);
			}
			updateView();
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
	
	public boolean isEnabled() {
		return mWifiManager != null && mWifiManager.isWifiEnabled();
	}
	
	 public void setEnabled(boolean enabled) {
	 	if(mWifiManager == null){
			return;
	 	}
	 	mWifiManager.setWifiEnabled(enabled);
	 }
	 
	 private void handleLongClick(){
		 Intent intent = new Intent();
		 intent.setComponent(new ComponentName("com.android.settings","com.android.settings.wifi.WifiSettings"));
		 mContext.startActivity(intent);
	 }
}
