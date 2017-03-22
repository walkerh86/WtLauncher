package com.cj.qs;

import com.cj.wtlauncher.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.ImageView;

public class QSBluetoothTile extends QSTile{
	private QSTileView mTileView;
	private ImageView mStatusView;
	private final BluetoothAdapter mAdapter;
	private Context mContext;
	private SignalState mSignalState;
	
	public QSBluetoothTile(Context context, QSTileView tileView, ImageView statusView){
		mContext = context;
		BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		mAdapter = bluetoothManager.getAdapter();

		mSignalState = new SignalState();
		mSignalState.enabled = isEnabled();
		
		mStatusView= statusView;
		
		mTileView = tileView;
		tileView.setOnClickListener(mClickListener);
		tileView.setOnLongClickListener(mLongClickListener);
		updateView();		
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		context.registerReceiver(mReceiver, filter);		
	}

	public void onDestroy(Context context){
		context.unregisterReceiver(mReceiver);
	}
	
	private void updateView(){
		mSignalState.connected = isConnected();
		android.util.Log.i("hcj","Bt, updateView mSignalState.connected="+mSignalState.connected);
		mTileView.setImageResource(mSignalState.enabled ? R.drawable.smart_watch_bt_on : R.drawable.smart_watch_bt_off);
		if(mSignalState.enabled){
			//mStatusView.setImageResource(R.drawable.stat_sys_bluetooth);
			mStatusView.setVisibility(View.VISIBLE);
		}else{
			mStatusView.setVisibility(View.GONE);
		}
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			android.util.Log.i("hcj","Bt, action="+action);
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				mSignalState.enabled = (state == BluetoothAdapter.STATE_ON) || (state == BluetoothAdapter.STATE_TURNING_OFF);
				
			}else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,BluetoothAdapter.ERROR);
				mSignalState.connecting = state == BluetoothAdapter.STATE_CONNECTING;                        
			}else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				
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

    public boolean isConnected() {
        return mAdapter != null
                && mAdapter.getConnectionState() == BluetoothAdapter.STATE_CONNECTED;
    }

    public boolean isConnecting() {
        return mAdapter != null
                && mAdapter.getConnectionState() == BluetoothAdapter.STATE_CONNECTING;
    }	 
}
