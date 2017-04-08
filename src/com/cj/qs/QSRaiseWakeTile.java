package com.cj.qs;

import com.cj.wtlauncher.R;

import android.provider.Settings;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.net.ConnectivityManager;

public class QSRaiseWakeTile extends QSTile{
	private static final String TAG = "hcj.QSAirplaneTile";
	private QSTileView mTileView;
	private Context mContext;
	
	public QSRaiseWakeTile(Context context, QSTileView tileView){
		mContext = context;
		
		mTileView = tileView;
		tileView.setOnClickListener(mClickListener);
		updateView(isEnabled());		
	}
	
	public void onDestroy(Context context){
	}

	private void updateView(boolean isOn){
		mTileView.setImageResource(isOn ? R.drawable.smart_watch_screenon_guesture_on : R.drawable.smart_watch_screenon_guesture_off);
	}
	
	private View.OnClickListener mClickListener = new View.OnClickListener(){
		@Override
		public void onClick(View view){
			setEnabled(!isEnabled());
		}
	};

	public boolean isEnabled(){
		return SystemProperties.getBoolean("persist.sys.raise.wakeup", false);
	}
	
	public void setEnabled(boolean enable){
		SystemProperties.set("persist.sys.raise.wakeup", String.valueOf(enable));
		updateView(enable);
		
		Intent intent = new Intent();
		intent.setAction("com.cj.wtlauncher.ScreenSensorService");
		intent.setPackage("com.cj.wtlauncher");
        mContext.startService(intent);
	}
}
