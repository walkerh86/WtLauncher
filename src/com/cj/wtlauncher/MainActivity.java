package com.cj.wtlauncher;

import java.util.ArrayList;

import com.cj.widget.HorizontalViewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity{
	private HorizontalViewPager mViewPager;
	private static final int DEFAULT_PAGE = 1;
	private VPagerFragment mVPagerFragment;
	private Handler mHandler = new Handler();
	
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
		filter.addAction("android.intent.action.BATTERY_CHANGED");
		registerReceiver(mScreenUpdateReceiver, filter);

		startService(new Intent(this, NotificationService.class));
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
			}else if("android.intent.action.BATTERY_CHANGED".equals(action)){
				final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,BatteryManager.BATTERY_STATUS_UNKNOWN);
				boolean charging = status == BatteryManager.BATTERY_STATUS_FULL || status == BatteryManager.BATTERY_STATUS_CHARGING;
				if(charging){
					if(!mBattDialogShowed){
						mBatteryLevel = (int)(100f* intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)/ intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
						showBatteryDialog();
					}
				}else{
					mBattDialogShowed = false;
				}
			}
		}
	};

	public void backToNoteView(boolean paramBoolean){
	}

	private static final int[] BATT_IMGS = new int[]{
		R.drawable.battery_anim_0,
		R.drawable.battery_anim_1,
		R.drawable.battery_anim_2,
		R.drawable.battery_anim_3,
		R.drawable.battery_anim_4,
		R.drawable.battery_anim_5,
	};
	private boolean mBattDialogShowed;
	private AlertDialog mBatteryDialog;
	private int mBatteryLevel;
	private ImageView mIvBattery;
	private TextView mTvPercent;
	private void showBatteryDialog(){
		if(mBatteryDialog == null){
			View view = LayoutInflater.from(this).inflate(R.layout.battery_dialog, null);
			mIvBattery = (ImageView)view.findViewById(R.id.iv_battery);
			mTvPercent = (TextView)view.findViewById(R.id.tv_percent);
			AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
			builder.setView(view );
			mBatteryDialog = builder.create();
		}
		if(mBatteryDialog.isShowing()){
			return;
		}
		int battDrawable = R.drawable.battery_anim_6;
		if(mBatteryLevel < 100){
			int segment = 100/BATT_IMGS.length;
			int index = mBatteryLevel/segment;
			if(index >= BATT_IMGS.length){
				index = BATT_IMGS.length-1;
			}
			battDrawable = BATT_IMGS[index];
		}
		mIvBattery.setImageResource(battDrawable);
		mBatteryDialog.show();
		mHandler.postDelayed(hideBatteryDialogRunnable, 2000L);
		
		mBattDialogShowed = true;
	}

	private Runnable hideBatteryDialogRunnable = new Runnable(){
		@Override
		public void run(){
			if(mBatteryDialog != null && mBatteryDialog.isShowing()){
				mBatteryDialog.dismiss();
			}
		}
	};
}
