package com.cj.wtlauncher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ClockFragment  extends Fragment{
	private static final String TAG = "hcj.ClockFragment";
	private FrameLayout mClockHost;
	private int mClockIdx = -1;
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//mClockIdx = getActivity().getSharedPreferences("clockview_settings", 0).getInt("clockview_index", 0);
	}
	
	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_clock, container, false);
		mClockHost = (FrameLayout)rootView.findViewById(R.id.idle_clock);
		mClockHost.setOnLongClickListener(new View.OnLongClickListener(){
			@Override
			public boolean onLongClick(View view){
				startChooseClock();
				return true;
			}
		});
		if(!MainActivity.CLOCKS_IN_MAIN){
		mClockHost.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				MainActivity activity = (MainActivity)ClockFragment.this.getActivity();
				activity.setClockFragmentVisible(false);
			}
		});
		}
		return rootView;
	}
	
	@Override
	public void onResume(){
		super.onResume();		
		setClockStyle(getActivity().getSharedPreferences("clockview_settings", 0).getInt("clockview_index", 0));
	}
	
	@Override
	public void onPause(){
		super.onPause();		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		android.util.Log.i(TAG,"onActivityResult requestCode="+requestCode+",resultCode="+resultCode);
		if ((100 == requestCode) && (50 == resultCode)){
			int style = data.getIntExtra("index", 0);
			saveClockStyle(style);
			setClockStyle(style);
		}
	}

	private void saveClockStyle(int style){
		SharedPreferences.Editor localEditor = getActivity().getSharedPreferences("clockview_settings", 0).edit();
		localEditor.putInt("clockview_index", style);
		localEditor.commit();
	}

	private void startChooseClock(){
		Intent intent = new Intent(getActivity(),ChooseClockActivity.class);
		intent.putExtra("index", mClockIdx);
		startActivityForResult(intent, 100);
		getActivity().overridePendingTransition(R.anim.enter_anim, 0);
	}

	public void setClockStyle(int index){
		android.util.Log.i(TAG,"setClockStyle index="+index+",mClockIdx="+mClockIdx+",clockNum="+ClockUtil.mClockList.length);
		if(mClockIdx == index || mClockHost == null){
			return;
		}
		mClockHost.removeAllViews();
		View clockView = LayoutInflater.from(getActivity()).inflate(ClockUtil.mClockList[index].mViewId, null);
		FrameLayout.LayoutParams localLayoutParams = new FrameLayout.LayoutParams(-1, -1);
		mClockHost.addView(clockView, localLayoutParams);
		mClockIdx = index;
	}
}
