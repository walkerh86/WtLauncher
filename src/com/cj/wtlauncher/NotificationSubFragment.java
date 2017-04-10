package com.cj.wtlauncher;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Date;

public class NotificationSubFragment extends Fragment{
  public static NotificationSubFragment create(StatusBarNotification paramStatusBarNotification){
  	NotificationSubFragment localNotificationSubFragment = new NotificationSubFragment();
	localNotificationSubFragment.setContent(paramStatusBarNotification);
	return localNotificationSubFragment;
  }

  public void onCreate(Bundle paramBundle){  
    super.onCreate(paramBundle);
  }
  
  public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle){  
    View mRootView = ((ViewGroup)paramLayoutInflater.inflate(R.layout.notification_content_layout, paramViewGroup, false));
    return mRootView;
  }
  
  public void collapseLayout(){
  }
  
  public void refreshContent(){
  }
  
  public void setContent(StatusBarNotification paramStatusBarNotification){  
    //this.mSbn = paramStatusBarNotification;
  }
}
