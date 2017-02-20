package com.cj.qs;

import com.cj.wtlauncher.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

public class QSBluetoothTile extends QSTile{
	private QSTileView mTileView;
	private final BluetoothAdapter mAdapter;
	private Context mContext;
	
	public QSBluetoothTile(Context context, QSTileView tileView){
		mContext = context;
		BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		mAdapter = bluetoothManager.getAdapter();
		
		mTileView = tileView;
		tileView.setOnClickListener(mClickListener);
		tileView.setOnLongClickListener(mLongClickListener);
		updateView(isEnabled());		
		
        final IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        context.registerReceiver(mReceiver, filter);		
	}
	
	private void updateView(boolean isOn){
		mTileView.setImageResource(isOn ? R.drawable.smart_watch_bt_on : R.drawable.smart_watch_bt_off);
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
        public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				boolean isOn = (state == BluetoothAdapter.STATE_ON) || (state == BluetoothAdapter.STATE_TURNING_OFF);
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
        return mAdapter != null && mAdapter.isEnabled();
    }
	
	 public void setEnabled(boolean enabled) {
	        if (mAdapter != null) {
	            if (enabled) {
	                mAdapter.enable();
	            } else {
	                mAdapter.disable();
	            }
	        }
	   	 }
	
	 private void handleLongClick(){
		 Intent intent = new Intent();
		 intent.setComponent(new ComponentName("com.android.settings","com.android.settings.bluetooth.BluetoothSettings"));
		 mContext.startActivity(intent);
	 }
}
