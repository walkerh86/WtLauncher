package com.cj.wtlauncher;

import java.util.ArrayList;

import com.cj.widget.HorizontalViewPager;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class MainActivity extends FragmentActivity{
	private HorizontalViewPager mViewPager;
	private static final int DEFAULT_PAGE = 1;
	private VPagerFragment mVPagerFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mViewPager = (HorizontalViewPager)findViewById(R.id.view_pager);
		ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
		fragmentList.add(new QuickFragment());
		mVPagerFragment = new VPagerFragment();
		mVPagerFragment.setOnClockPageSelectListener(new VPagerFragment.OnClockPageSelectListener(){
			@Override
			public void onClockPageSelect(boolean selected){
				mViewPager.setSwipeEnable(selected);
			}
		});
		fragmentList.add(mVPagerFragment);
		fragmentList.add(new MenuFragment());
		mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
		mViewPager.setCurrentItem(DEFAULT_PAGE);

		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.SCREEN_OFF");
		registerReceiver(mScreenUpdateReceiver, filter);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mScreenUpdateReceiver);
	}
	
	@Override
	public void onWindowFocusChanged(boolean focused){
		super.onWindowFocusChanged(focused);
		WatchApp.setTopActivityStatus(focused);
	}
	
	public class MyFragmentPagerAdapter extends FragmentPagerAdapter{
		ArrayList<Fragment> list;
		public MyFragmentPagerAdapter(android.support.v4.app.FragmentManager fm,ArrayList<Fragment> list) {
			super(fm);
			this.list = list;
		}
		
		
		@Override
		public int getCount() {
			return list.size();
		}
		
		@Override
		public android.support.v4.app.Fragment getItem(int arg0) {
			return list.get(arg0);
		}
	}

	@Override
	public void onBackPressed() {
	}

	private final BroadcastReceiver mScreenUpdateReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if("android.intent.action.SCREEN_OFF".equals(action)){
				mViewPager.setCurrentItem(DEFAULT_PAGE);
				mVPagerFragment.showClockPage();
			}
		}
	};
}
