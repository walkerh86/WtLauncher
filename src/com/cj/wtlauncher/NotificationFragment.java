package com.cj.wtlauncher;

import java.util.HashMap;
import java.util.Map;

import com.cj.widget.HorizontalViewPager;

import android.animation.LayoutTransition;
import android.app.INotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NotificationFragment extends Fragment{
	private static final String TAG = NotificationFragment.class.getSimpleName();
	public static final int MODE_NORMAL = 0;
	public static final int MODE_VIBRATE = 1;

	private static NotificationFragment sInstance;
	private Context mContext;
	private NotificationListenerService mListener = new NotificationListenerService(){
		public void onNotificationPosted(final StatusBarNotification statusBarNotification){
			NotificationFragment.this.getActivity().runOnUiThread(new Runnable(){
				@Override
				public void run(){
					String pkgName = statusBarNotification.getPackageName();
					//String tag = paramAnonymousStatusBarNotification.getTag();
					if (NotificationFragment.this.mMapWhiteList.containsKey(pkgName)){
						NotificationFragment.this.addNotification(statusBarNotification);
						return;
					}
					if (NotificationFragment.this.mMapBlackList.containsKey(pkgName)){
						NotificationHelper.dumpNotification(statusBarNotification.getNotification());
						return;
					}
					if (NotificationHelper.filterNotification(statusBarNotification.getNotification())){
						NotificationHelper.dumpNotification(statusBarNotification.getNotification());
						return;
					}
					NotificationFragment.this.addNotification(statusBarNotification);
				}
			});
		}

		public void onNotificationRemoved(final StatusBarNotification statusBarNotification){
			NotificationFragment.this.getActivity().runOnUiThread(new Runnable(){
				@Override
				public void run(){
					NotificationFragment.this.removeNotification(statusBarNotification);
				}
			});
		}
	};
	private Map mMapBlackList = new HashMap();
	private Map mMapWhiteList = new HashMap();
	private View mNoItems;
	private INotificationManager mNoMan;
	private int mPageCount = 0;
	private HorizontalViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private PowerManager mPowerManager;
	private LayoutTransition mRealLayoutTransition;
	private Vibrator mVibrator;

	public NotificationFragment(){
		sInstance = this;
	}

	private void addNotification(StatusBarNotification statusBarNotification){
		int m = NotificationHelper.findPositionByKey(statusBarNotification.getPackageName(), statusBarNotification.getTag(), statusBarNotification.getId());
		if (NotificationHelper.isHighPriorityNotification(statusBarNotification.getNotification())) {
			mPowerManager.wakeUp(SystemClock.uptimeMillis());
		}
		String pkgName = statusBarNotification.getPackageName();
		String tag = statusBarNotification.getTag();
		
		int motifyMode = SystemProperties.getInt("persist.sys.notify.coming", MODE_NORMAL);
		
		if (((!NotificationHelper.isDefaultVibrate(statusBarNotification.getNotification())) 
			&& (NotificationHelper.getVibrate(statusBarNotification.getNotification()) == null)) /*|| ((j != 0) && (i != 0))*/){
			mVibrator.vibrate(300L);
		}

		if(m == -1){
			NotificationData.Entry entry = new NotificationData.Entry(statusBarNotification, NotificationSubFragment.create(statusBarNotification));
			NotificationHelper.add((NotificationData.Entry)entry);
			refreshPager();
			mPager.setCurrentItem(0);
			dumpData((NotificationData.Entry)entry);
			
			notifyDataChangedPosted(statusBarNotification);
			if(motifyMode == MODE_NORMAL){
				MainActivity mainActivity = (MainActivity)getActivity();
				mainActivity.backToNoteView(true);
			}
			return;
		}

		NotificationData.Entry data = NotificationHelper.getNotificationData().get(m);
		NotificationHelper.update(data);
		NotificationSubFragment fragment = getNotificationSubFragment(m);
		if(fragment != null){
			fragment.setContent(statusBarNotification);
			fragment.refreshContent();
		}
		dumpData(data);
	}

	private void dumpData(NotificationData.Entry paramEntry){
	}

	public static NotificationFragment getInstance(){
		if (sInstance == null) {
			sInstance = new NotificationFragment();
		}
		return sInstance;
	}

	private NotificationSubFragment getNotificationSubFragment(int paramInt){
		if ((paramInt >= 0) && (paramInt < NotificationHelper.getNotificationData().size())){
			NotificationData.Entry localEntry = NotificationHelper.getNotificationData().get(paramInt);
			if(localEntry != null){
				return localEntry.mNotificationSubFragment;
			}
		}
		return null;
	}

	private void notifyDataChangedPosted(StatusBarNotification paramStatusBarNotification){
		mContext.sendBroadcast(new Intent("com.mediatek.watchapp.NOTIFICATION_LISTENER.POSTED"));
	}

	private void notifyDataChangedRemoved(StatusBarNotification paramStatusBarNotification){
		mContext.sendBroadcast(new Intent("com.mediatek.watchapp.NOTIFICATION_LISTENER.REMOVED"));
	}

	private void refreshPager(){
		if (this.mPager == null){
			return;
		}
		mPageCount = NotificationHelper.getNotificationData().size();
		mPager.getAdapter().notifyDataSetChanged();
		setNoItemsVisibility(mPageCount == 0);
	}

	private void removeNotification(StatusBarNotification statusBarNotification){
		if (NotificationHelper.remove(statusBarNotification.getPackageName(), statusBarNotification.getTag(), statusBarNotification.getId()) != null){
			refreshPager();
			notifyDataChangedRemoved(statusBarNotification);
		}
	}

	private void setNoItemsVisibility(boolean visible){
		if (mNoItems != null){
			mNoItems.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	public int getPageCount(){
		return this.mPageCount;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		mContext = getActivity();
		mPowerManager = ((PowerManager)mContext.getSystemService("power"));
		mVibrator = ((Vibrator)mContext.getSystemService("vibrator"));
		mNoMan = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));

		try{
			mListener.registerAsSystemService(mContext, new ComponentName(mContext.getPackageName(), getClass().getCanonicalName()), -1);
			//init white black list
			String[] whiteList = mContext.getResources().getStringArray(R.array.notification_whitelist);
			for(int i=0;i<whiteList.length;i++){
				mMapWhiteList.put(whiteList[i],"1");
			}
			//init white black list
			String[] blackList = mContext.getResources().getStringArray(R.array.notification_blacklist);
			for(int i=0;i<blackList.length;i++){
				mMapBlackList.put(blackList[i],"1");
			}
		}catch(Exception e){
		}
	}
	
	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_notification, container, false);
		mPageCount = NotificationHelper.getNotificationData().size();
		mPager = ((HorizontalViewPager)rootView.findViewById(R.id.vpager));
		mNoItems = rootView.findViewById(R.id.notification_no_items);

		setNoItemsVisibility(mPageCount == 0);

		mPagerAdapter = new NotificationPagerAdapter(getChildFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setClipToPadding(false);
		mPager.setPageMargin(10);
		mPager.setCurrentItem(0);
		mPager.setOffscreenPageLimit(2);
		mPager.setPageTransformer(false, new ViewPager.PageTransformer(){
			public void transformPage(View paramAnonymousView, float paramAnonymousFloat){
				paramAnonymousView.setAlpha(1.0F - Math.abs(paramAnonymousFloat));
			}
		});
		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
			public void onPageSelected(int paramAnonymousInt){
				NotificationSubFragment localNotificationSubFragment = NotificationFragment.this.getNotificationSubFragment(paramAnonymousInt - 1);
				if (localNotificationSubFragment != null) {
					localNotificationSubFragment.collapseLayout();
				}
				localNotificationSubFragment = NotificationFragment.this.getNotificationSubFragment(paramAnonymousInt + 1);
				if (localNotificationSubFragment != null) {
					localNotificationSubFragment.collapseLayout();
				}
			}
		});

		mRealLayoutTransition = new LayoutTransition();
		mRealLayoutTransition.setAnimateParentHierarchy(true);
		
		return rootView;
	}
	
	@Override
	public void onResume(){
		super.onResume();	
		mPager.setLayoutTransition(null);
	}
	
	@Override
	public void onPause(){
		super.onPause();		
	}
	
	@Override
	public void onDestroy(){
		sInstance = null;
		super.onDestroy();
	}
	
	@Override
	public void setUserVisibleHint(boolean paramBoolean){
		super.setUserVisibleHint(paramBoolean);
		SystemProperties.set("persist.sys.clock.idle", String.valueOf(false));
	}

	private class NotificationPagerAdapter extends FragmentStatePagerAdapter{
		public NotificationPagerAdapter(FragmentManager paramFragmentManager){
			super(paramFragmentManager);
		}

		public int getCount(){
			return NotificationFragment.this.mPageCount;
		}

		public Fragment getItem(int paramInt){
			return NotificationFragment.this.getNotificationSubFragment(paramInt);
		}

		public int getItemPosition(Object paramObject){
			return -2;
		}
	}
}
