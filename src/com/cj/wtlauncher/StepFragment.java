package com.cj.wtlauncher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StepFragment extends Fragment{
	private static final String TAG = "hcj.StepFragment";
	private AppWidgetHost  mAppWidgetHost;  
	private AppWidgetManager mAppWidgetManager;
	private static final int HOST_ID = 1024;
	private ViewGroup mRootView;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		mAppWidgetHost = new AppWidgetHost(getActivity(), HOST_ID);
		mAppWidgetHost.startListening();
		
		mAppWidgetManager = AppWidgetManager.getInstance(getActivity());
	}
	
	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.today_steps, container, false);
		mRootView = (ViewGroup)rootView;
		initAppWidget();
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
		mAppWidgetHost.stopListening();
	}
	
	private void initAppWidget(){
		AppWidgetProviderInfo stepProvider = getStepWidgetProvider(getActivity());
		Log.i(TAG, "initAppWidget stepProvider="+stepProvider);
        if (stepProvider == null) {
            return;
        }
        Context context = getActivity();
        Bundle opts = new Bundle();
        SharedPreferences sp = context.getSharedPreferences("com.wt.health.prefs", Context.MODE_PRIVATE);
        int widgetId = sp.getInt("step_widget_id", -1);
        AppWidgetProviderInfo widgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetId);
        Log.i(TAG, "initAppWidget stepProvider.provider="+stepProvider.provider+",widgetInfo="+widgetInfo+",widgetId="+widgetId);
        if (!stepProvider.provider.flattenToString().equals(
                sp.getString("step_widget_provider", null))
                || (widgetInfo == null)
                || !widgetInfo.provider.equals(stepProvider.provider)) {
            // A valid widget is not already bound.
            if (widgetId > -1) {
                mAppWidgetHost.deleteAppWidgetId(widgetId);
                widgetId = -1;
            }

            // Try to bind a new widget
            widgetId = mAppWidgetHost.allocateAppWidgetId();

            if (!mAppWidgetManager.bindAppWidgetIdIfAllowed(widgetId, stepProvider.getProfile(), stepProvider.provider,opts)) {
                mAppWidgetHost.deleteAppWidgetId(widgetId);
                widgetId = -1;
            }

            sp.edit()
                .putInt("step_widget_id", widgetId)
                .putString("step_widget_provider", stepProvider.provider.flattenToString())
                .commit();
        }

        Log.i(TAG, "initAppWidget widgetId="+widgetId);
        if (widgetId != -1) {
        	AppWidgetHostView hostView = mAppWidgetHost.createView(context, widgetId, stepProvider);
            ///M.ALPS2044024, The view id is "View.NO_ID",will same with other view id.
            if (hostView.getId() == View.NO_ID) {
            	hostView.setId(View.generateViewId());
            }
            Log.d(TAG, "initAppWidget hostView id=" + hostView.getId());

            hostView.updateAppWidgetOptions(opts);
            hostView.setPadding(0, 0, 0, 0);
            mRootView.addView(hostView);
        }
        
        notifyStepChanged();
	}
	
    private static AppWidgetProviderInfo getStepWidgetProvider(Context context) {
        String providerPkg = "com.wt.health";

        AppWidgetProviderInfo appWidgetProviderInfo = null;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        for (AppWidgetProviderInfo info : appWidgetManager.getInstalledProviders()) {
            if (info.provider.getPackageName().equals(providerPkg)) {
            	appWidgetProviderInfo = info;
            	break;
            }
        }
        return appWidgetProviderInfo;
    }
    
    private void notifyStepChanged(){
    	getActivity().sendBroadcast(new Intent("com.wt.health.APPWIDGET_UPDATE"));
    }
}
