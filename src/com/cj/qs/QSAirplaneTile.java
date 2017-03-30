package com.cj.qs;

import com.cj.wtlauncher.R;

import android.provider.Settings;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

public class QSAirplaneTile extends QSTile{
	private static final String TAG = "hcj.QSAirplaneTile";
	private QSTileView mTileView;
	private Context mContext;
	
	public QSAirplaneTile(Context context, QSTileView tileView){
		mContext = context;
		
		mTileView = tileView;
		tileView.setOnClickListener(mClickListener);
		updateView(isEnabled());
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		context.registerReceiver(mReceiver, filter);		
	}
	
	public void onDestroy(Context context){
		context.unregisterReceiver(mReceiver);
	}

	private void updateView(boolean isOn){
		mTileView.setImageResource(isOn ? R.drawable.smart_watch_airmode_on : R.drawable.smart_watch_airmode_off);
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
				updateView(isEnabled());
			}
		}
	};
	
	private View.OnClickListener mClickListener = new View.OnClickListener(){
		@Override
		public void onClick(View view){
			setEnabled(!isEnabled());
		}
	};
	
	public boolean isEnabled() {
		return (Settings.Global.getInt(mContext.getContentResolver(),Settings.Global.AIRPLANE_MODE_ON, 0) == 1);
	}
	
	 public void setEnabled(boolean enabled) {
	 	Log.i("SettingsService","setEnabled enabled="+enabled);
	 	//Settings.Global.putInt(mContext.getContentResolver(),Settings.Global.AIRPLANE_MODE_ON, enabled ? 1: 0);
	 	try{
			if(mSettingsService != null){
				mSettingsService.setAirplaneModeEnabled(enabled);
			}
		}catch(Exception e){
			Log.i(TAG,"setEnabled e="+e);
		}
	 }
}
