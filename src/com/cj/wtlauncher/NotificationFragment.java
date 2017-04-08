package com.cj.wtlauncher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NotificationFragment extends Fragment{
	public static final int MODE_NORMAL = 0;
	public static final int MODE_VIBRATE = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.notification_panel, container, false);
		return rootView;
	}
	
	@Override
	public void onResume(){
		super.onResume();		
	}
	
	@Override
	public void onPause(){
		super.onPause();		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
}
