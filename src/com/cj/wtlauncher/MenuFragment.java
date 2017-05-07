package com.cj.wtlauncher;

import java.util.ArrayList;
import java.util.List;

import com.cj.widget.PageIndicator;

import android.app.Activity;
import android.app.ActivityManager;
import android.support.v4.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;

import android.content.pm.LauncherApps;
import android.content.pm.LauncherActivityInfo;
import android.os.UserHandle;

public class MenuFragment extends Fragment {
	private static final String TAG = "hcj";
	private RecyclerView mRecyclerView;
	private TextView mCurrLabelView;
	private MyLinearLayoutManager mMyLinearLayoutManager;
	private PackageManager mPackageManager;
	private int mIconDpi;
	private PageIndicator mPageIndicator;
	private static final int PAGE_INDICATOR_ITEM_NUM = 8;
	private MyAdapter mMyAdapter;
	private Context mContext;
	public static final int MENU_STYLE_H = 0;
	public static final int MENU_STYLE_GRID = 1;
	public static final int MENU_STYLE_V = 2;
	private int mMenuStyle = MENU_STYLE_V;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//initData();
		mContext = getActivity();
		mPackageManager = getActivity().getPackageManager();
		ActivityManager activityManager =
                (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		mIconDpi = 240;//activityManager.getLauncherLargeIconDensity();
		mMenuStyle = getMenuStyle();
		initPackageMonitor(getActivity());
		loadAllApps();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		int menuStyle = getMenuStyle();
		android.util.Log.i("hcj", "menuStyle="+menuStyle);
		if(mMenuStyle != menuStyle){
			mMenuStyle = menuStyle;
			switchMenuStyle();
		}
	}
	
	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
    		Log.i(TAG,"MenuFragment onCreateView");
		View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
		mRecyclerView = (RecyclerView)rootView.findViewById(R.id.list_view);
		mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		
		mCurrLabelView = (TextView)rootView.findViewById(R.id.curr_label_view);
		mPageIndicator = (PageIndicator)rootView.findViewById(R.id.page_indicator);

		if(mMenuStyle == MENU_STYLE_H){
			mMyLinearLayoutManager = new MyLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);//
			mMyLinearLayoutManager.setOnLayoutListener(mOnLayoutListener);
			mMyLinearLayoutManager.setRecyclerView(mRecyclerView);
			mRecyclerView.setLayoutManager(mMyLinearLayoutManager);
			mRecyclerView.addItemDecoration(new MyItemDecoration());
			
			mPageIndicator.setPageNum(PAGE_INDICATOR_ITEM_NUM);
			mPageIndicator.setPageCurr(0);
		}else if(mMenuStyle == MENU_STYLE_V){
			mCurrLabelView.setVisibility(View.GONE);
			mPageIndicator.setVisibility(View.GONE);
		
			mMyLinearLayoutManager = new MyLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);//
			mMyLinearLayoutManager.setOnLayoutListener(mOnLayoutListener);
			mMyLinearLayoutManager.setRecyclerView(mRecyclerView);
			mRecyclerView.setLayoutManager(mMyLinearLayoutManager);	
			mRecyclerView.addItemDecoration(new MyItemDecoration());
			((WtRecyclerView)mRecyclerView).setAdjustDrawingOrder(true);
		}else{
			mCurrLabelView.setVisibility(View.GONE);
			mPageIndicator.setVisibility(View.GONE);

			mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2,LinearLayoutManager.HORIZONTAL,false));
			//mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.HORIZONTAL));
		}
		mMyAdapter = new MyAdapter();
		mMyAdapter.setOnItemClickListener(mOnItemClickListener);
		mRecyclerView.setAdapter(mMyAdapter);
	
		mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
			public void onScrollStateChanged(RecyclerView recyclerView, int newState){
				if(mMenuStyle == MENU_STYLE_H || mMenuStyle == MENU_STYLE_V){
					mMyLinearLayoutManager.onScrollStateChanged(recyclerView,newState);
				}
			}
			
			public void onScrolled(RecyclerView recyclerView, int dx, int dy){
				if(mMenuStyle == MENU_STYLE_H || mMenuStyle == MENU_STYLE_V){
					mMyLinearLayoutManager.onScrolled();
				}
			}
		});
		
		return rootView;
	}
	
	private class AppInfo{
		Drawable mIcon;
		String mLabel;
		Intent mIntent;
		String mPackage;
		public AppInfo(ResolveInfo resolveInfo, PackageManager packageManager){
			if(resolveInfo == null || packageManager == null){
				//mLabel = "";
				return;
			}
			
			//mIcon = getIcon(packageManager,resolveInfo.activityInfo,mIconDpi);
			//mIcon = resolveInfo.loadIcon(packageManager);
			mLabel = resolveInfo.loadLabel(packageManager).toString();
			resolveIntent(new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName, resolveInfo.activityInfo.name));
			mIcon = getFixIcon(resolveInfo.activityInfo.name);
			if(mIcon == null){
				mIcon = getIcon(packageManager,resolveInfo.activityInfo,mIconDpi);
			}
		}

		public AppInfo(LauncherActivityInfo actInfo){
			//mIcon = actInfo.getIcon(mIconDpi);
			mLabel = actInfo.getLabel().toString();
			resolveIntent(actInfo.getComponentName());
			mIcon = getFixIcon(actInfo.getComponentName().getClassName());
			if(mIcon == null){
				mIcon = actInfo.getIcon(mIconDpi);
			}
		}

		private void resolveIntent(ComponentName cn){
			mIntent = new Intent(Intent.ACTION_MAIN);
			mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			mPackage = cn.getPackageName();
			mIntent.setComponent(cn);
			mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		}

		private Drawable getFixIcon(String actName){
			String name = actName.replaceAll("\\.","_").toLowerCase();
			android.util.Log.i("hcj","getIcon name="+name);
			Resources res = mContext.getResources();
			int resId = res.getIdentifier(name, "mipmap", "com.cj.wtlauncher");
			return (resId != 0) ? res.getDrawable(resId) : null;
		}
	}
	
    public Drawable getIcon(PackageManager packageManager, ActivityInfo aInfo, int density) {
        Drawable d = null;
        if (aInfo.getIconResource() != 0) {
            Resources resources;
            try {
                resources = packageManager.getResourcesForApplication(aInfo.packageName);
            } catch (PackageManager.NameNotFoundException e) {
                resources = null;
            }
            if (resources != null) {
                try {
                    d = resources.getDrawableForDensity(aInfo.getIconResource(), density);
                } catch (Resources.NotFoundException e) {
                    // Return default icon below.
                }
            }
        }
        if (d == null) {
            Resources resources = Resources.getSystem();
            d = resources.getDrawableForDensity(android.R.mipmap.sym_def_app_icon, density);
        }
        return d;
    }	
	
	private ArrayList<AppInfo> mAllApps = new ArrayList<AppInfo>();
	private void loadAllApps(){
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        List<ResolveInfo> apps = null;
        apps = mPackageManager.queryIntentActivities(mainIntent, 0);
        if(apps == null){
        	Log.i(TAG, "loadAllApps apps == null");
        	return;
        }
        Log.i(TAG, "loadAllApps start time="+SystemClock.uptimeMillis());
        int count = apps.size();
        for(int i=0;i<count;i++){
        	//Drawable icon = getFullResIcon(apps.get(i));
        	ResolveInfo resolveInfo = apps.get(i);
        	mAllApps.add(new AppInfo(resolveInfo,mPackageManager));
        }
		
	  if(mMenuStyle == MENU_STYLE_H){
	        mAllApps.add(0,new AppInfo(null,null));
		  mAllApps.add(mAllApps.size(),new AppInfo(null,null));
	  }
        Log.i(TAG, "loadAllApps end time="+SystemClock.uptimeMillis());
	}
	
	public class MyItemDecoration extends RecyclerView.ItemDecoration {
		public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
			if(mMenuStyle == MENU_STYLE_H){
				outRect.set(0,26,0,0);
			}else{
				outRect.set(0,0,0,0);
			}
		}
	}

	public interface OnLayoutListener{
		void onCenterItemFixed(View view);
		void onCenterItemChanged(View view);
	}
	
	private class MyLinearLayoutManager extends LinearLayoutManager{
		private int mScrolledX;//avoid overscroll loop
		private RecyclerView mRecyclerView;
		private int mCenterItemPosition = -1;
		private int mMyOrientation;

		public MyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
			super(context, orientation, reverseLayout);
			mScrolledX = 0;
			mMyOrientation = orientation;
		}

		public void setRecyclerView(RecyclerView recyclerView){
			mRecyclerView = recyclerView;
		}
		
		public void onScrolled(){
			updateItemViewsScale();
		}
		
		public void onScrollStateChanged(RecyclerView recyclerView, int newState){
			//Log.i("hcj", "onScrollStateChanged newState="+newState+",mScrolledX="+mScrolledX);
			if(newState == RecyclerView.SCROLL_STATE_IDLE){
				if(mScrolledX != 0){
					adjustItemViewsPosition(recyclerView);
				}
				mScrolledX = 0;
			}
		}
		
		@Override
		public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
			int scrollBy = super.scrollHorizontallyBy(dx, recycler, state);
			mScrolledX += scrollBy;
			return scrollBy;
		}

		@Override
		public int scrollVerticallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
			int scrollBy = super.scrollVerticallyBy(dx, recycler, state);
			mScrolledX += scrollBy;
			return scrollBy;
		}
		
		@Override
		public void onLayoutChildren(Recycler recycler, State state) {
			super.onLayoutChildren(recycler, state);
			mCenterItemPosition = -1;
			adjustItemViewsPosition(mRecyclerView);
			updateItemViewsScale();
		}

		private View getCenterItemView(boolean checkCenter){
			View centerView = null;
			int count = getChildCount();
			int centerX = getCenterXY();
			for(int i=0;i<count;i++){
				View view = getChildAt(i);
				int childRight = getChildRightBottom(view);
				if(childRight >= centerX){
					int childLeft = getChildLeftTop(view);
					if(childLeft <= centerX){
						if(checkCenter){
							int childCenterX = getChildCenterXY(view);
							//in item layout center
							if(childCenterX == centerX){
								centerView = view;
							}
						}else{
							//in item layout range
							centerView = view;
						}
						break;
					}
				}
			}
			return centerView;
		}

		private boolean isItemViewCenterFix(View view){
			int centerX = getCenterXY();
			int childLeft = getChildLeftTop(view);
			int childRight = getChildRightBottom(view);	
			//int childWidth = childRight-childLeft;
			int childCenterX = getChildCenterXY(view);
			return (childCenterX == centerX);
		}
		
		private void updateItemViewsScale(){
			/*
			if(mMyOrientation == VERTICAL){
				int offset = mScrolledX%20;
				int count = getChildCount();
				int childH = 0;
				int prevH = 0;
				for(int i=0;i<count;i++){
					if(i == 0){
						childH = 40-offset*2;
						prevH += childH;
					}else if(i == 1){
						childH = 60-offset*3;
						prevH += childH;
					}else if(i == 2){
						childH = 120-offset*6;	
						prevH += childH;
					}else if(i == 3){
						childH = 60+offset*3;	
						prevH += childH;
					}else if(i == 4){
						childH = 40+offset*2;	
						prevH += childH;
					}else if(i == 5){
						childH = 320-prevH;					
					}
					View view = getChildAt(i);
					View iconView = view.findViewById(R.id.icon_view);
					LayoutParams lp = (LayoutParams)iconView.getLayoutParams();
					lp.height = childH;
					lp.width = childH;
					iconView.setLayoutParams(lp);
				}
				return;
			}
			*/
			int count = getChildCount();
			int centerX = getCenterXY();
			View firstChild = getChildAt(0);
			int childWidth = getChildWH(firstChild);
			for(int i=0;i<count;i++){
				View view = getChildAt(i);
				int childLeft = getChildLeftTop(view);
				int childCenterX = childLeft+childWidth/2;
				int deltaX = Math.abs(childCenterX-centerX);
				float scaleX = 1.0f;
				if(mMyOrientation == VERTICAL){
					scaleX = 1.0f-0.6f*deltaX/centerX;
				}else if(mMyOrientation == HORIZONTAL  && deltaX < childWidth){
					scaleX = 1.0f+0.6f*(childWidth-deltaX)/childWidth;
				}
				//view.setScaleX(scaleX);
				//view.setScaleY(scaleX);
				view.findViewById(R.id.icon_view).setScaleX(scaleX);
				view.findViewById(R.id.icon_view).setScaleY(scaleX);
				view.findViewById(R.id.label_view).setScaleX(scaleX);
				view.findViewById(R.id.label_view).setScaleY(scaleX);
			}
			View centerView = getCenterItemView(false);
			if(centerView == null || mOnLayoutListener == null){
				return;
			}
			int position = mRecyclerView.getChildPosition(centerView);
			if(mCenterItemPosition != position){
				mCenterItemPosition = position;
				mOnLayoutListener.onCenterItemChanged(centerView);
			}
			if(isItemViewCenterFix(centerView)){
				mOnLayoutListener.onCenterItemFixed(centerView);
			}
		}
		
		private void adjustItemViewsPosition(RecyclerView recyclerView){
			int count = getChildCount();
			int centerX = getCenterXY();
			View view;
			int scrollX = 0;
			for(int i=0;i<count;i++){
				view = getChildAt(i);
				int childRight = getChildRightBottom(view);
				if(childRight >= centerX){
					int childLeft = getChildLeftTop(view);
					int decoratedW = childRight-childLeft;
					if(childLeft <= centerX){
						int childCenterX = childLeft+decoratedW/2;
						scrollX = childCenterX-centerX;
						//Log.i("hcj", "adjustItemsPosition centerX="+centerX+",childCenterX="+childCenterX+",scrollX="+scrollX+",i="+i);
						break;
					}
				}
			}
			
			if(scrollX != 0){
				recyclerView.smoothScrollBy(scrollX,0);
			}
		}

		public void adjustItemViewToCenter(RecyclerView recyclerView, View view){
			int scrollX = 0;
			int centerX = getCenterXY();
			/*
			int childRight = getChildRightBottom(view);
			int childLeft = getChildLeftTop(view);
			int decoratedW = childRight-childLeft;
			int childCenterX = childLeft+decoratedW/2;
			*/
			int childCenterX = getChildCenterXY(view);
			scrollX = childCenterX-centerX;
			
			if(scrollX != 0){
				recyclerView.smoothScrollBy(scrollX,0);
			}else{
				if(mOnLayoutListener != null){
					mOnLayoutListener.onCenterItemFixed(view);
				}
			}
		}

		private int getCenterXY(){
			return (mMyOrientation == HORIZONTAL) ? getWidth()/2 : getHeight()/2;
		}

		private int getChildCenterXY(View view){
			int childWH = (mMyOrientation == HORIZONTAL) ?  (getDecoratedRight(view)-getDecoratedLeft(view))
				: (getDecoratedBottom(view)-getDecoratedTop(view));
			return getChildLeftTop(view)+childWH/2;
		}

		private int getChildLeftTop(View view){
			return (mMyOrientation == HORIZONTAL) ? getDecoratedLeft(view) : getDecoratedTop(view);
		}

		private int getChildRightBottom(View view){
			return (mMyOrientation == HORIZONTAL) ? getDecoratedRight(view) : getDecoratedBottom(view);
		}

		private int getChildWH(View view){
			return (mMyOrientation == HORIZONTAL) ?  (getDecoratedRight(view)-getDecoratedLeft(view))
				: (getDecoratedBottom(view)-getDecoratedTop(view));
		}
		
		private OnLayoutListener mOnLayoutListener;
		public void setOnLayoutListener(OnLayoutListener listener){
			mOnLayoutListener = listener;
		}
	}
	
	public interface OnItemClickListener{
		void onItemClick(View view);
	}
	
	private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
		android.util.Log.i("hcj","onCreateViewHolder mMenuStyle="+mMenuStyle);
			int layoutId = (mMenuStyle == MENU_STYLE_V) ? R.layout.list_item_vertical: R.layout.list_item;
			MyViewHolder holder = new MyViewHolder(LayoutInflater.from(MenuFragment.this.getActivity()).inflate(layoutId, parent,false)); 
            return holder;
		}
		
		@Override
		public void onBindViewHolder(MyViewHolder holder, int position){
			AppInfo appInfo = mAllApps.get(position);
			holder.mIconView.setImageDrawable(appInfo.mIcon);
			holder.mLabelView.setText(appInfo.mLabel);
			holder.mRootView.setTag(appInfo);
		}
		
		@Override
		public int getItemCount(){
			return mAllApps.size();
		}
		
		private class MyViewHolder extends ViewHolder{
			View mRootView;
			ImageView mIconView;
			TextView mLabelView;			
			public MyViewHolder(View view){
				super(view);				
				mIconView = (ImageView) view.findViewById(R.id.icon_view);
				mLabelView = (TextView) view.findViewById(R.id.label_view);
				if(mMenuStyle == MENU_STYLE_H){
					mLabelView.setVisibility(View.GONE);
				}
				mRootView = view;
				mRootView.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View view){
						if(mOnItemClickListener != null){
							mOnItemClickListener.onItemClick(view);
						}
					}
				});
			}
		}

		private OnItemClickListener mOnItemClickListener;
		public void setOnItemClickListener(OnItemClickListener listener){
			mOnItemClickListener = listener;
		}
	}

	private boolean mLaunchCenter;
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(View view) {
			AppInfo appInfo = (AppInfo)view.getTag();
			Log.i("hcj", "appInfo.mIntent="+appInfo.mIntent);
			if(appInfo.mIntent == null){
				return;
			}
			if(mMenuStyle == MENU_STYLE_H){
				mMyLinearLayoutManager.adjustItemViewToCenter(mRecyclerView,view);
				mLaunchCenter = true;
			}else{
				if(appInfo.mIntent.getComponent().getClassName().equals("com.cj.wtlauncher.StyleSettingActivity")){
					Intent intent = new Intent(getActivity(),StyleSettingActivity.class);
					getActivity().startActivity(intent);
				}else{
					getActivity().startActivity(appInfo.mIntent);
				}
			}
		}			
	};

	private OnLayoutListener mOnLayoutListener = new OnLayoutListener(){
		@Override
		public void onCenterItemFixed(View view){
			Log.i(TAG,"onCenterItemFixed");
			if(mLaunchCenter){
				mLaunchCenter = false;
				AppInfo appInfo = (AppInfo)view.getTag();
				getActivity().startActivity(appInfo.mIntent);
			}
		}
		
		@Override
		public void onCenterItemChanged(View view){
			AppInfo appInfo = (AppInfo)view.getTag();
			//mCurrLabelView.setText(appInfo.mLabel);
			//Log.i(TAG,"onCenterItemChanged mLabel="+appInfo.mLabel+",mCurrLabelView="+mCurrLabelView);
			//this call maybe before mCurrLabelView layout, so delay to avoid text display invisible
			mPendingLabel = appInfo.mLabel;
			mHandler.post(new Runnable(){
				public void run(){
					mPageIndicator.setPageCurr(mPendingIndex);
					mCurrLabelView.setText(mPendingLabel);
					Log.i(TAG,"onCenterItemFixed mLabel="+mPendingLabel+",mCurrLabelView="+mCurrLabelView);
				}
			});
			
			int position = mRecyclerView.getChildPosition(view);
			int count = mRecyclerView.getAdapter().getItemCount();
			mPendingIndex = PAGE_INDICATOR_ITEM_NUM*position/count;			
		}
	};

	private Handler mHandler = new Handler();
	private String mPendingLabel;
	private int mPendingIndex;

	private LauncherApps mLauncherApps;
	private void initPackageMonitor(Context context){
		mLauncherApps = (LauncherApps) context.getSystemService("launcherapps");
		mLauncherApps.registerCallback(mPkgCallbak);
	}
	
	private LauncherApps.Callback mPkgCallbak = new LauncherApps.Callback(){
		public void onPackageRemoved(String packageName, UserHandle user){
			/*
			List<LauncherActivityInfo> list = mLauncherApps.getActivityList(packageName,user);
			if(list == null || list.size() == 0){
				return;
			}
			for(LauncherActivityInfo actInfo : list){
				for(AppInfo appInfo : mAllApps){
					if(appInfo.mCName.compareTo(actInfo.getComponentName())){
					}
				}
			}
			*/
			for (int i = mAllApps.size() - 1; i >= 0; i--) {
				AppInfo appInfo = mAllApps.get(i);
				if(packageName.equals(appInfo.mPackage)){
					mAllApps.remove(appInfo);
				}
			}
			
			mMyAdapter.notifyDataSetChanged();
		}
		
		public void onPackageAdded(String packageName, UserHandle user){
			List<LauncherActivityInfo> list = mLauncherApps.getActivityList(packageName,user);
			if(list == null || list.size() == 0){
				return;
			}
			for(LauncherActivityInfo actInfo : list){
				mAllApps.add(mAllApps.size()-1,new AppInfo(actInfo));
			}
			
			mMyAdapter.notifyDataSetChanged();
		}
		
		public void onPackageChanged(String packageName, UserHandle user){
		}
		
		public void onPackagesAvailable(String[] packageNames, UserHandle user,boolean replacing){
		}
                
		public void onPackagesUnavailable(String[] packageNames, UserHandle user,boolean replacing){
		}
	};
		
	public int getMenuStyle(){
		SharedPreferences settings = getActivity().getSharedPreferences("setting", 0);
		int style = settings.getInt("menu_style", this.MENU_STYLE_GRID);
		return style;
	}
	
	private void switchMenuStyle(){
		if(mMenuStyle == MENU_STYLE_H){
			mCurrLabelView.setVisibility(View.VISIBLE);
			mPageIndicator.setVisibility(View.VISIBLE);
			
			mMyLinearLayoutManager = new MyLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);//
			mMyLinearLayoutManager.setOnLayoutListener(mOnLayoutListener);
			mMyLinearLayoutManager.setRecyclerView(mRecyclerView);
			mRecyclerView.setLayoutManager(mMyLinearLayoutManager);
			mRecyclerView.addItemDecoration(new MyItemDecoration());
			
			mPageIndicator.setPageNum(PAGE_INDICATOR_ITEM_NUM);
			mPageIndicator.setPageCurr(0);
		}else if(mMenuStyle == MENU_STYLE_V){
			mCurrLabelView.setVisibility(View.GONE);
			mPageIndicator.setVisibility(View.GONE);
		
			mMyLinearLayoutManager = new MyLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);//
			mMyLinearLayoutManager.setOnLayoutListener(mOnLayoutListener);
			mMyLinearLayoutManager.setRecyclerView(mRecyclerView);
			mRecyclerView.setLayoutManager(mMyLinearLayoutManager);			
		}else{
			mCurrLabelView.setVisibility(View.GONE);
			mPageIndicator.setVisibility(View.GONE);

			mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2,LinearLayoutManager.HORIZONTAL,false));
			//mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.HORIZONTAL));
		}
		
		mMyAdapter = new MyAdapter();
		mMyAdapter.setOnItemClickListener(mOnItemClickListener);
		mRecyclerView.setAdapter(mMyAdapter);
	}
}
