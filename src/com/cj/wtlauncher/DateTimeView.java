package com.cj.wtlauncher;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.System;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeView
  extends TextView
{
  private boolean mAttachedToWindow;
  private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
  {
    public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
    {/*
      if (("android.intent.action.TIME_TICK".equals(paramAnonymousIntent.getAction())) && (System.currentTimeMillis() < DateTimeView.this.mUpdateTimeMillis)) {
        return;
      }*/
      DateTimeView.this.mLastFormat = null;
      DateTimeView.this.update();
    }
  };
  private ContentObserver mContentObserver = new ContentObserver(new Handler())
  {
    public void onChange(boolean paramAnonymousBoolean)
    {
      DateTimeView.this.mLastFormat = null;
      DateTimeView.this.update();
    }
  };
  int mLastDisplay = -1;
  java.text.DateFormat mLastFormat;
  Date mTime;
  long mTimeMillis;
  private long mUpdateTimeMillis;
  
  public DateTimeView(Context paramContext)
  {
    super(paramContext);
  }
  
  public DateTimeView(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
  }
  
  private java.text.DateFormat getDateFormat()
  {
    Object localObject = Settings.System.getString(getContext().getContentResolver(), "date_format");
    if ((localObject == null) || ("".equals(localObject))) {
      return java.text.DateFormat.getDateInstance(3);
    }
    try
    {
      localObject = new SimpleDateFormat((String)localObject);
      return (java.text.DateFormat)localObject;
    }
    catch (IllegalArgumentException localIllegalArgumentException) {}
    return java.text.DateFormat.getDateInstance(3);
  }
  
  private java.text.DateFormat getTimeFormat()
  {
    return android.text.format.DateFormat.getTimeFormat(getContext());
  }
  
  private void registerReceivers()
  {
    Context localContext = getContext();
    Object localObject = new IntentFilter();
    ((IntentFilter)localObject).addAction("android.intent.action.TIME_TICK");
    ((IntentFilter)localObject).addAction("android.intent.action.TIME_SET");
    ((IntentFilter)localObject).addAction("android.intent.action.CONFIGURATION_CHANGED");
    ((IntentFilter)localObject).addAction("android.intent.action.TIMEZONE_CHANGED");
    localContext.registerReceiver(this.mBroadcastReceiver, (IntentFilter)localObject);
    localObject = Settings.System.getUriFor("date_format");
    localContext.getContentResolver().registerContentObserver((Uri)localObject, true, this.mContentObserver);
  }
  
  private void unregisterReceivers()
  {
    Context localContext = getContext();
    localContext.unregisterReceiver(this.mBroadcastReceiver);
    localContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    registerReceivers();
    this.mAttachedToWindow = true;
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    unregisterReceivers();
    this.mAttachedToWindow = false;
  }
  
  public void setTime(long paramLong)
  {
    Time localTime = new Time();
    localTime.set(paramLong);
    localTime.second = 0;
    this.mTimeMillis = localTime.toMillis(false);
    this.mTime = new Date(localTime.year - 1900, localTime.month, localTime.monthDay, localTime.hour, localTime.minute, 0);
    update();
  }
  
  void update()
  	{
  	}
}
