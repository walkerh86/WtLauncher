package com.cj.wtlauncher;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mediatek.audioprofile.AudioProfileManager;

public class QuickFragment extends Fragment{
	private static final String TAG = "QuickFragment";
	private AudioProfileManager mProfileManager;
	private ImageView mProfileView;
	private IntentFilter mIntentFilter;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		mProfileManager = (AudioProfileManager) getActivity().getSystemService(Context.AUDIO_PROFILE_SERVICE);
		
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction("com.cj.action.audio_profile_change");
		getActivity().registerReceiver(mReceiver, mIntentFilter);
	}
	
	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_quick, container, false);
		
		mProfileView = (ImageView)rootView.findViewById(R.id.quick_profile);
		mProfileView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				try{
					String profileKey = mProfileManager.getActiveProfileKey();
					if("mtk_audioprofile_silent".equals(profileKey) || "mtk_audioprofile_meeting".equals(profileKey)){
						profileKey = "mtk_audioprofile_general";
					}else{
						profileKey = "mtk_audioprofile_silent";
					}
					mProfileManager.setActiveProfile(profileKey);
					updateProfileView();
				}catch(Exception e){
					Log.i(TAG, "e="+e);
				}
			}
		});		
		updateProfileView();
		
		View quickSetting = rootView.findViewById(R.id.quick_settings);
		quickSetting.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				startStatusActivity();
			}
		});		

		View quickMusic = rootView.findViewById(R.id.quick_music);
		quickMusic.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				startMusicActivity();
			}
		});		

		View quickWeather = rootView.findViewById(R.id.quick_weather);
		quickWeather.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				startWeatherActivity();
			}
		});		
		
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
		getActivity().unregisterReceiver(mReceiver);
	}
	
	private void startStatusActivity(){
		Intent intent = new Intent();
		intent.setClass(getActivity(), StatusActivity.class);
		this.startActivity(intent);
	}

	private void startMusicActivity(){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.setComponent(new ComponentName("com.android.music","com.android.music.TrackBrowserActivity"));
		this.startActivity(intent);
	}
	
	private void startWeatherActivity(){
		try{
			Intent intent = new Intent();
			intent.setComponent(new ComponentName("com.android.watchweather","com.android.watchweather.WeatherActivity"));
			this.startActivity(intent);
		}catch(Exception e){
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, "onReceive "+action);
			if("com.cj.action.audio_profile_change".equals(action)){
				updateProfileView();
			}
		}
		
	};
	
	private void updateProfileView(){
		if(mProfileView == null){
			return;
		}
		String profileKey = mProfileManager.getActiveProfileKey();
		Log.i(TAG, "profileKey="+profileKey);
		if("mtk_audioprofile_silent".equals(profileKey) || "mtk_audioprofile_meeting".equals(profileKey)){
			mProfileView.setImageResource(R.drawable.quick_grid_profile_slient);
		}else{
			mProfileView.setImageResource(R.drawable.quick_grid_profile_normal);
		}
	}
}
