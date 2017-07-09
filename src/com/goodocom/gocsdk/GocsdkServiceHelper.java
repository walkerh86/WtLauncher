package com.goodocom.gocsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class GocsdkServiceHelper {
	private static final String TAG = "hcj.GocsdkExtService";
	private IGocsdkServiceSimple mGocsdkService;
	private GocsdkConnection mConnection;
	
	private class GocsdkConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mGocsdkService = IGocsdkServiceSimple.Stub.asInterface(service);
			Log.i(TAG, "onServiceConnected mGocsdkService="+mGocsdkService);
			if(mOnServiceConnectListener != null){
				mOnServiceConnectListener.onServiceConnected(mGocsdkService);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mGocsdkService = null;
			Log.i(TAG, "onServiceDisconnected mGocsdkService="+mGocsdkService);
			if(mOnServiceConnectListener != null){
				mOnServiceConnectListener.onServiceDisconnected();
			}
		}
	}
	
	public GocsdkServiceHelper(OnServiceConnectListener listener){
		mOnServiceConnectListener = listener;
	}
	
	public void bindService(Context context){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.goodocom.gocsdkfinal","com.goodocom.gocsdkfinal.service.GocsdkService"));
		if(mConnection == null){
			mConnection = new GocsdkConnection();
		}
		context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void unbindService(Context context){
		context.unbindService(mConnection);
	}
	
	public interface OnServiceConnectListener{
		void onServiceConnected(IGocsdkServiceSimple service);
		void onServiceDisconnected();
	} 
	
	private OnServiceConnectListener mOnServiceConnectListener;

	public void  setBtSwitch(boolean open){
		try{
			mGocsdkService.setBtSwitch(open);
		}catch(Exception e){
			
		}
	}
	
	public boolean isBtOpen(){
		boolean isOpen = false;
		try{
			isOpen = mGocsdkService.isBtOpen();
		}catch(Exception e){
			
		}
		return isOpen;
	}
	
	public boolean isBtConnected(){
		boolean isConnected = false;
		try{
			isConnected = mGocsdkService.isBtConnected();
		}catch(Exception e){
			
		}
		return isConnected;
	}
	
	public void dial(String number){
		try{
			mGocsdkService.dial(number);
		}catch(Exception e){
			
		}
	}
	
	public boolean isInCall(){
		boolean isInCall = false;
		try{
			isInCall = mGocsdkService.isInCall();
		}catch(Exception e){
			
		}
		return isInCall;
	}
	
	public void endCall(){
		try{
			mGocsdkService.endCall();
		}catch(Exception e){
			
		}
	}
	
	public void acceptCall(){
		try{
			mGocsdkService.acceptCall();
		}catch(Exception e){
			
		}
	}
	

}
