package com.cj.wtlauncher;

import java.util.ArrayList;

import com.cj.widget.HorizontalViewPager;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class MainActivity extends FragmentActivity{
	private HorizontalViewPager mViewPager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mViewPager = (HorizontalViewPager)findViewById(R.id.view_pager);
		ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
		fragmentList.add(new QuickFragment());
		VPagerFragment vPagerFragment = new VPagerFragment();
		vPagerFragment.setOnClockPageSelectListener(new VPagerFragment.OnClockPageSelectListener(){
			@Override
			public void onClockPageSelect(boolean selected){
				mViewPager.setSwipeEnable(selected);
			}
		});
		fragmentList.add(vPagerFragment);
		fragmentList.add(new MenuFragment());
		mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
		mViewPager.setCurrentItem(1);
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
}
