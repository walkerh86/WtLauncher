package com.cj.wtlauncher;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.other.widget.*;
import android.util.Log;

public class VPagerFragment extends Fragment{
	private ArrayList<View> mViews = new ArrayList<View>();
	private VerticalViewPager mViewPager;
	private static final int PAGE_CLOCK_INDEX = 1;
	
	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_vpager, container, false);
		mViewPager = (VerticalViewPager)rootView.findViewById(R.id.vertical_view_pager);
		/*
		View stepsView = inflater.inflate(R.layout.today_steps, container, false);
		mViews.add(stepsView);
		View clockView = inflater.inflate(R.layout.clock_layout_style1, container, false);
		mViews.add(clockView);
		View notifyView = inflater.inflate(R.layout.notification_panel, container, false);
		mViews.add(notifyView);

		VerticalPagerAdapter pagerAdapter = new VerticalPagerAdapter() {
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			@Override
			public int getCount() {
				return mViews.size();
			}
			
			@Override
			public void destroyItem(ViewGroup container, int position,Object object) {
				container.removeView(mViews.get(position));
			}
			
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(mViews.get(position));
				return mViews.get(position);
			}
		};
		mViewPager.setAdapter(pagerAdapter);
		
		*/
		ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
		fragmentList.add(new StepFragment());
		fragmentList.add(new ClockFragment());
		fragmentList.add(new NotificationFragment());
		mViewPager.setAdapter(new MyFragmentPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList));
		mViewPager.setCurrentItem(PAGE_CLOCK_INDEX);
		mViewPager.setOnPageChangeListener(new VerticalViewPager.OnPageChangeListener() {			
			@Override
			public void onPageSelected(int position) {
				if(mOnClockPageSelectListener != null){
					mOnClockPageSelectListener.onClockPageSelect(position == PAGE_CLOCK_INDEX);
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		return rootView;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.i("hcj.VPagerFragment","onDestory");
	}

	public class MyFragmentPagerAdapter extends VerticalFragmentPagerAdapter{
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

	public interface OnClockPageSelectListener{
		void onClockPageSelect(boolean selected);
	}

	private OnClockPageSelectListener mOnClockPageSelectListener;
	public void setOnClockPageSelectListener(OnClockPageSelectListener listener){
		mOnClockPageSelectListener = listener;
	}

	public void showClockPage(){
		if(mViewPager == null) return;
		mViewPager.setCurrentItem(PAGE_CLOCK_INDEX);
	}
}
