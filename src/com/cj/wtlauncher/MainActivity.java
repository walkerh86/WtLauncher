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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity{
	private static final String TAG = "hcj.MainActivity";
	private HorizontalViewPager mViewPager;
	private static final int DEFAULT_PAGE = 1;
	private VPagerFragment mVPagerFragment;
	private Handler mHandler = new Handler();
	private Fragment mClockFragment;
	private boolean mPendingShowClock;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG,"onCreate");
		setContentView(R.layout.activity_main);
		
		mViewPager = (HorizontalViewPager)findViewById(R.id.view_pager);
		ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
		fragmentList.add(new QuickFragment());
		fragmentList.add(new VPagerFragment());
		//Log.i(TAG, "showCenter mVPagerFragment="+mVPagerFragment);
		fragmentList.add(new MenuFragment());
		mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
		mViewPager.setCurrentItem(DEFAULT_PAGE);

		mClockFragment = getSupportFragmentManager().findFragmentById(R.id.wt_clock_fragment);
		setClockFragmentVisible(false);
                    
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.SCREEN_OFF");
		filter.addAction("android.intent.action.SCREEN_ON");
		//filter.addAction("android.intent.action.BATTERY_CHANGED");//disable battery dialog
		registerReceiver(mScreenUpdateReceiver, filter);

		startService(new Intent(this, NotificationService.class));
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mScreenUpdateReceiver);
	}
	
	@Override
	public void onAttachFragment(Fragment fragment){
		if(fragment instanceof VPagerFragment){			
			mVPagerFragment = (VPagerFragment)fragment;
			mVPagerFragment.setOnClockPageSelectListener(new VPagerFragment.OnClockPageSelectListener(){
				@Override
				public void onClockPageSelect(boolean selected){
					mViewPager.setSwipeEnable(selected);
				}
			});
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean focused){
		Log.i(TAG,"onWindowFocusChanged focused="+focused);
		super.onWindowFocusChanged(focused);
		WatchApp.setTopActivityStatus(focused);
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String action = intent.getAction();
		Log.i(TAG,"onNewIntent action="+action);
		if (Intent.ACTION_MAIN.equals(action)) {			
			showCenterPage();
		}
	}
	
	@Override
	public void onResume(){
		Log.i(TAG,"onResume");
		super.onResume();
		if(mPendingShowClock){
			mPendingShowClock = false;
			setClockFragmentVisible(true);
		}
	}
	
	@Override
	public void onPause(){
		Log.i(TAG,"onPause");
		super.onPause();
	}
	
	@Override
	public void onStop(){
		Log.i(TAG,"onStop");
		super.onStop();
	}
	
	@Override
	public void onStart(){
		Log.i(TAG,"onStart");
		super.onStart();
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
				Log.i(TAG, "action.SCREEN_OFF");
				//mViewPager.setCurrentItem(DEFAULT_PAGE);
				//mVPagerFragment.showClockPage();
				showCenterPage();
				if(MainActivity.this.isResumed()){
					setClockFragmentVisible(true);
				}else{
					mPendingShowClock = true;
				}
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
			}else if("android.intent.action.SCREEN_ON".equals(action)){
				Log.i(TAG, "action.SCREEN_ON");
				//setClockFragmentVisible(true);
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
				try{
				mBatteryDialog.dismiss();
				}catch(Exception e){
					Log.i(TAG, "dismiss battery dialog e="+e);
				}
			}
		}
	};

	public void setClockFragmentVisible(boolean visible){
		if(mClockFragment == null/* || !mWindowFocused*/){
			return;
		}
		Log.i(TAG, "setClockFragmentVisible visible="+visible+",isVisible="+mClockFragment.isVisible());
		if(visible){
			if(!mClockFragment.isVisible()){
				this.getSupportFragmentManager().beginTransaction().show(mClockFragment).commit();
			}
		}else{
			if(mClockFragment.isVisible()){
				this.getSupportFragmentManager().beginTransaction().hide(mClockFragment).commit();
			}
		}
	}
	
	private void showCenterPage(){
		if(mViewPager != null){
			mViewPager.setCurrentItem(DEFAULT_PAGE,false);
		}
		Log.i(TAG, "showCenterPage mVPagerFragment="+mVPagerFragment);
		if(mVPagerFragment != null){
			mVPagerFragment.showCenterPage();
		}
	}
}
