package com.cj.wtlauncher;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class NetworkController {
	private static final String TAG = "hcj.NetworkController";
	private static NetworkController mNetworkController;
	private MobileController mMobileController;
	private WifiController mWifiController;
	private AirplaneController mAirplaneController;
	private boolean mWifiConnected;
	private boolean mAirplaneOn;
	private boolean mDataEnable;
	private int mDataType = MobileController.WT_NETWORK_TYPE_NULL;
	
	private NetworkController(Context context){
		mMobileController = new MobileController(context);
		mMobileController.setOnMobileListener(new MobileController.OnMobileListener() {					
			@Override
			public void onSignalStrengthChange(int level) {
				notifySignalStrengthChange(level);
			}
			
			@Override
			public void onDataTypeChange(int dataType){
				if(mWifiConnected || mAirplaneOn){
					dataType = MobileController.WT_NETWORK_TYPE_NULL;
				}
				if(mDataType != dataType){
					mDataType = dataType;
					notifyDataTypeChange(dataType);
				}				
			}
						
			@Override
			public void onDataEnable(boolean enable){
				mDataEnable = mAirplaneOn ? false : enable;
				notifyDataEnable(enable);
			}
		});
		mDataEnable = mMobileController.isDataEnable();
		
		mWifiController = new WifiController(context);
		mWifiController.setOnWifiChangeListener(new WifiController.OnWifiChangeListener() {			
			@Override
			public void onWifiEnable(boolean enable) {
				notifyWifiEnable(enable);
			}
			
			@Override
			public void onWifiConnect(boolean connected) {
				mWifiConnected = connected;
				notifyDataTypeChange(mWifiConnected ? 
							MobileController.WT_NETWORK_TYPE_NULL : mMobileController.getDateNetType());
			}
		});
		
		mAirplaneController = new AirplaneController(context);
		mAirplaneController.setOnAirplaneChangeListener(new AirplaneController.OnAirplaneChangeListener() {			
			@Override
			public void onAirplaneChange(boolean enable) {
				mAirplaneOn = enable;
				notifyAirplaneEnable(enable);
				
				boolean dataEnable = mAirplaneOn ? false : mMobileController.isDataEnable();
				if(dataEnable != mDataEnable){
					notifyDataEnable(mDataEnable);
				}
			}
		});
		mAirplaneOn = mAirplaneController.isEnabled();
		Log.i(TAG, "mAirplaneOn="+mAirplaneOn);
	}
	
	public static NetworkController getInstance(Context context){
		if(mNetworkController == null){
			mNetworkController = new NetworkController(context);
		}
		return mNetworkController;
	}
	
	public void onDestroy(Context context){
		mMobileController.onDestroy(context);
		mWifiController.onDestroy(context);
		mAirplaneController.onDestroy(context);
	}
	
	public boolean isAirplaneOn(){
		return mAirplaneOn;
	}
	
	private void notifySignalStrengthChange(int strength){
		for(int i=0;i<mOnNetworkListeners.size();i++){
			mOnNetworkListeners.get(i).onSignalStrengthChange(strength);
		}
	}
	
	private void notifyDataTypeChange(int dataType){
		for(int i=0;i<mOnNetworkListeners.size();i++){
			mOnNetworkListeners.get(i).onDataTypeChange(dataType);
		}
	}
	
	private void notifyDataEnable(boolean enable){
		for(int i=0;i<mOnNetworkListeners.size();i++){
			mOnNetworkListeners.get(i).onDataEnable(enable);
		}
	}
	
	private void notifyWifiEnable(boolean enable){
		for(int i=0;i<mOnNetworkListeners.size();i++){
			mOnNetworkListeners.get(i).onWifiEnable(enable);
		}
	}
	
	private void notifyAirplaneEnable(boolean enable){
		for(int i=0;i<mOnNetworkListeners.size();i++){
			mOnNetworkListeners.get(i).onAirplaneEnable(enable);
		}
	}
	
	//private OnNetworkListener mOnNetworkListener;
	private ArrayList<OnNetworkListener> mOnNetworkListeners = new ArrayList<OnNetworkListener>();
	public void addOnNetworkListener(OnNetworkListener listener){
		//mOnNetworkListener = listener;
		mOnNetworkListeners.add(listener);
	}
	
	public void removeOnNetworkListener(OnNetworkListener listener){
		mOnNetworkListeners.remove(listener);
	}
	
	public interface OnNetworkListener{
		void onSignalStrengthChange(int strength);
		//void onNetworkTypeChange(NetworkType networkType);
		void onDataTypeChange(int dataType);
		void onDataEnable(boolean enable);
		void onWifiEnable(boolean enable);
		void onAirplaneEnable(boolean enable);
	}
}
