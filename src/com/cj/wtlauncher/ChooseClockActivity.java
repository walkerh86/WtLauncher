package com.cj.wtlauncher;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;

public class ChooseClockActivity extends Activity{
	private Gallery myGallery;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_clock);

		myGallery = (Gallery)findViewById(R.id.myGallery);
		initUI();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}

	public void initUI(){
		int[] iconIds = new int[ClockUtil.mClockList.length];
		int[] titleIds = new int[ClockUtil.mClockList.length];
		for(int i=0;i<ClockUtil.mClockList.length;i++){
			titleIds[i] = ClockUtil.mClockList[i].mTitleId;
			iconIds[i] = ClockUtil.mClockList[i].mThumbImageId;
		}
		HorizontalListViewAdapter adapter = new HorizontalListViewAdapter(this, titleIds, iconIds);
		myGallery.setAdapter(adapter);
		myGallery.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				Intent intent = new Intent();
				intent.putExtra("index", position);
				ChooseClockActivity.this.setResult(50, intent);
				ChooseClockActivity.this.finish();
				ChooseClockActivity.this.overridePendingTransition(0, R.anim.exit_anim);
			}
		});
		myGallery.setSelection(getSharedPreferences("clockview_settings", 0).getInt("clockview_index", 0));
	}
}
