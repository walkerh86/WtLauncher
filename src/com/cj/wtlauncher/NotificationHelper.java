package com.cj.wtlauncher;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.util.Log;
import java.util.Date;

public class NotificationHelper{

  public static NotificationData mNotificationData = new NotificationData();
  
  public static int add(NotificationData.Entry paramEntry){
    return mNotificationData.add(paramEntry);
  }
  
  public static void dumpNotification(Notification paramNotification){
  	/*
    Bundle localBundle = NotificationCompat.getExtras(paramNotification);
    Log.d("NotificationHelper", "title: " + getTitle(localBundle));
    Log.d("NotificationHelper", "text: " + getText(localBundle));
    Log.d("NotificationHelper", "subText: " + getSubText(localBundle));
    StringBuilder localStringBuilder = new StringBuilder().append("when: ");
    if (getWhen(paramNotification) != 0L) {}
    int i;
    for (Object localObject = new Date(getWhen(paramNotification));; localObject = Integer.valueOf(0))
    {
      Log.d("NotificationHelper", localObject);
      Log.d("NotificationHelper", "priority: " + getPriority(paramNotification));
      Log.d("NotificationHelper", "showChronometer: " + getShowChronometer(localBundle));
      Log.d("NotificationHelper", "defaults: 0x" + Integer.toHexString(getDefaults(paramNotification)));
      Log.d("NotificationHelper", "flags: 0x" + Integer.toHexString(getFlags(paramNotification)));
      Log.d("NotificationHelper", "has sound" + getSound(paramNotification));
      Log.d("NotificationHelper", "vibrate: " + getVibrate(paramNotification));
      localObject = getLargeIcon(localBundle);
      if (localObject != null) {
        Log.d("NotificationHelper", "large icon: " + ((Bitmap)localObject).getWidth() + "x" + ((Bitmap)localObject).getHeight());
      }
      localObject = getPicture(localBundle);
      if (localObject != null) {
        Log.d("NotificationHelper", "big picture: " + ((Bitmap)localObject).getWidth() + "x" + ((Bitmap)localObject).getHeight());
      }
      localObject = getTextLines(localBundle);
      if (localObject == null) {
        break;
      }
      Log.d("NotificationHelper", "inbox style with " + localObject.length + " lines");
      i = 0;
      while (i < localObject.length)
      {
        Log.d("NotificationHelper", "line: " + localObject[i]);
        i += 1;
      }
    }
    int j = getActionCount(paramNotification);
    Log.d("NotificationHelper", "action count = " + j);
    if (j > 0)
    {
      i = 0;
      while (i < j)
      {
        Log.d("NotificationHelper", "action " + i + ": " + getAction(paramNotification, i));
        i += 1;
      }
    }
    Log.d("NotificationHelper", "content Intent: " + getContentIntent(paramNotification));
    Log.d("NotificationHelper", "local only: " + getLocalOnly(paramNotification));
	*/
  }
  
  public static boolean filterNotification(Notification paramNotification){
    return (getFlags(paramNotification) & (Notification.FLAG_ONGOING_EVENT|Notification.FLAG_NO_CLEAR)/*0x22*/) != 0;
  }
  
  public static int findPositionByKey(String paramString1, String paramString2, int paramInt){  
    return mNotificationData.findPositionByKey(paramString1, paramString2, paramInt);
  }
  
  public static NotificationCompat.Action getAction(Notification paramNotification, int paramInt){  
    return NotificationCompat.getAction(paramNotification, paramInt);
  }
  
  public static int getActionCount(Notification paramNotification){  
    return NotificationCompat.getActionCount(paramNotification);
  }
  
  public static Bitmap getAppIcon(Bundle paramBundle){  
    return (Bitmap)paramBundle.getParcelable("app_icon");
  }
  
  public static PendingIntent getContentIntent(Notification paramNotification){  
    return paramNotification.contentIntent;
  }
  
  public static int getDefaults(Notification paramNotification){  
    return paramNotification.defaults;
  }
  
  public static int getFlags(Notification paramNotification){  
    return paramNotification.flags;
  }
  
  public static Bitmap getLargeIcon(Bundle paramBundle){  
    return (Bitmap)paramBundle.getParcelable("android.largeIcon");
  }
  
  public static boolean getLocalOnly(Notification paramNotification){  
    return NotificationCompat.getLocalOnly(paramNotification);
  }
  
  public static NotificationData getNotificationData(){  
    return mNotificationData;
  }
  
  public static Bitmap getPicture(Bundle paramBundle){  
    return (Bitmap)paramBundle.getParcelable("android.picture");
  }
  
  public static int getPriority(Notification paramNotification){  
    return paramNotification.priority;
  }
  
  public static boolean getShowChronometer(Bundle paramBundle){  
    return paramBundle.getBoolean("android.showChronometer");
  }
  
  public static Uri getSound(Notification paramNotification){  
    return paramNotification.sound;
  }
  
  public static CharSequence getSubText(Bundle paramBundle){  
    return paramBundle.getCharSequence("android.subText");
  }
  
  public static CharSequence getText(Bundle paramBundle){  
    return paramBundle.getCharSequence("android.text");
  }
  
  public static CharSequence[] getTextLines(Bundle paramBundle){  
    return paramBundle.getCharSequenceArray("android.textLines");
  }
  
  public static CharSequence getTitle(Bundle paramBundle){  
    return paramBundle.getCharSequence("android.title");
  }
  
  public static long[] getVibrate(Notification paramNotification){  
    return paramNotification.vibrate;
  }
  
  public static long getWhen(Notification paramNotification){  
    return paramNotification.when;
  }
  
  public static boolean isDefaultVibrate(Notification paramNotification){  
    return (paramNotification.defaults & Notification.DEFAULT_VIBRATE/*0x2*/) != 0;
  }
  
  public static boolean isHighPriorityNotification(Notification paramNotification){  
    return getPriority(paramNotification) > 0;
  }
  
  public static NotificationData.Entry remove(String paramString1, String paramString2, int paramInt){  
    NotificationData.Entry localEntry = mNotificationData.remove(paramString1, paramString2, paramInt);
    if (localEntry == null) {
      Log.w("NotificationHelper", "removeNotificationEntry failed, packageName=" + paramString1 + " mTag=" + paramString2 + " id=" + paramInt);
    }
    return localEntry;
  }
  
  public static int update(NotificationData.Entry paramEntry){  
    return mNotificationData.update(paramEntry);
  }
}