package com.cj.wtlauncher;

import android.app.Application;
import android.util.Log;

public class WatchApp extends Application { 
	private static final String TAG = "hcj.WatchApp";
  private static boolean can_slide_in_viewpager = false;
  public static boolean mBatterySaver = false;
  public static boolean mBatterySaverEnable = true;
  private static boolean mIsClockView;
  private static boolean mIsMainMenu = false;
  private static boolean mIsTopActivity;
  private static WatchApp sWatchApp;
  private NetworkController mNetworkController;
  
  static{  
    mIsClockView = false;
    mIsTopActivity = false;
  }
  
  public static boolean getIsCanSlide(){  
    return can_slide_in_viewpager;
  }
  
  public static boolean getMainMenuStatus(){  
    return mIsMainMenu;
  }
  
  public static boolean getTopActivityStatus(){  
    return mIsTopActivity;
  }
  
  public static void setClockViewStatus(boolean paramBoolean){  
    mIsClockView = paramBoolean;
  }
  
  public static void setIsCanSlide(boolean paramBoolean){  
    can_slide_in_viewpager = paramBoolean;
  }
  
  public static void setMainMenuStatus(boolean paramBoolean){  
    mIsMainMenu = paramBoolean;
  }
  
  public static void setTopActivityStatus(boolean paramBoolean){  
    mIsTopActivity = paramBoolean;
  }
  
  public void onCreate(){  
	  Log.d(TAG, "onCreate");
    sWatchApp = this;
    super.onCreate();


    mNetworkController = NetworkController.getInstance(this);
  }
  
  @Override
  public void onTerminate() {
      Log.d(TAG, "onTerminate");
      super.onTerminate();
      mNetworkController.onDestroy(this);
  }
}