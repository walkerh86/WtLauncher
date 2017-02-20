package com.cj.qs;

import com.cj.wtlauncher.R;

import android.net.wifi.WifiManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

public class QSWifiTile extends QSTile{
	private QSTileView mTileView;
	private final WifiManager mWifiManager;
	private Context mContext;
	
	public QSWifiTile(Context context, QSTileView tileView){
		mContext = context;
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		mTileView = tileView;
		tileView.setOnClickListener(mClickListener);
		tileView.setOnLongClickListener(mLongClickListener);
		updateView(isEnabled());
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		context.registerReceiver(mReceiver, filter);		
	}

	private void updateView(boolean isOn){
		mTileView.setImageResource(isOn ? R.drawable.smart_watch_wifi_on : R.drawable.smart_watch_wifi_off);
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN);
				boolean isOn = (state == WifiManager.WIFI_STATE_ENABLED);
				updateView(isOn);
			}
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
