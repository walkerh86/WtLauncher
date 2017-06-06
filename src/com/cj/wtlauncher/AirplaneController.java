package com.cj.wtlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.util.Log;

public class AirplaneController {
	private Context mContext;
	private boolean mEnabled;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
				boolean enable = isEnabled();
				if(enable != mEnabled){
					mEnabled = enable;
					if(mOnAirplaneChangeListener != null){
						mOnAirplaneChangeListener.onAirplaneChange(enable);
					}
				}
			}
		}
	};
	
	public AirplaneController(Context context){
		mContext = context;
		
		mEnabled = isEnabled();
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		context.registerReceiver(mReceiver, filter);
	}
	
	public void onDestroy(Context context){
		context.unregisterReceiver(mReceiver);
	}
	
	public void toggle(){
		setEnable(!isEnabled());
	}
	
	public boolean isEnabled() {
		return (Settings.Global.getInt(mContext.getContentResolver(),Settings.Global.AIRPLANE_MODE_ON, 0) == 1);
	}
	
	 public void setEnable(boolean enabled) {
	 	//Log.i(TAG,"setEnabled enabled="+enabled);
	 	try{
			//Settings.Global.putInt(mContext.getContentResolver(),Settings.Global.AIRPLANE_MODE_ON, enabled ? 1: 0);
			final ConnectivityManager mgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			mgr.setAirplaneMode(enabled);
		}catch(Exception e){
			//Log.i(TAG,"setEnabled e="+e);
		}
	 }
	 
	 private OnAirplaneChangeListener mOnAirplaneChangeListener;
	 public void setOnAirplaneChangeListener(OnAirplaneChangeListener listener){
		 mOnAirplaneChangeListener = listener;
	 }
	 
	 public interface OnAirplaneChangeListener{
		 void onAirplaneChange(boolean enable);
	 }
}
