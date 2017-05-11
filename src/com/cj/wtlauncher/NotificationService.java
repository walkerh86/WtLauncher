package com.cj.wtlauncher;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.Notification.InboxStyle;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

public class NotificationService extends NotificationListenerService{
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent){
			if ((intent != null) && ("com.mediatek.watchapp.NOTIFICATION_LISTENER.CANCEL".equals(intent.getAction()))){
				Log.i("NotificationListenerService", "**********  ACTION_NOTIFICATION_CANCEL_BY_USER");
				NotificationService.this.removeNotification(intent.getExtras());
			}
			if((intent == null) || (!"com.mediatek.watchapp.DEMO_MODE_POST".equals(intent.getAction()))) {
				return;
			}
			NotificationService.this.dumpData();
			NotificationManager nm = (NotificationManager)NotificationService.this.getSystemService("notification");
			Notification.Builder builder = new Notification.Builder(context);
			builder.setContentTitle("+928005506My Notification");
			builder.setContentText("Notification Listener Service Example");
			builder.setTicker("Notification Listener Service Example");
			builder.setSmallIcon(17301668);
			builder.setAutoCancel(true);
			builder.setPriority(1);
			builder.setWhen(System.currentTimeMillis());
			builder.setShowWhen(true);
			Notification notification = new Notification.InboxStyle(builder).addLine("line11111111111111111111111111111111111111111111111")
				.addLine("line22222222222222222222222222222222222222222222222")
				.addLine("line33333333333333333333333333333333333333333333333")
				.addLine("line44444444444444444444444444444444444444444444444")
				.addLine("line55555555555555555555555555555555555555555555555")
				.addLine("line66666666666666666666666666666666666666666666666")
				.addLine("line77777777777777777777777777777777777777777777777")
				.addLine("line88888888888888888888888888888888888888888888888")
				.addLine("line99999999999999999999999999999999999999999999999")
				.setSummaryText("+9 more").setBigContentTitle("+0928005506Bigg").build();
			nm.notify((int)System.currentTimeMillis(), notification);
		}
	};

	public NotificationService(){
		Log.d("NotificationListenerService", "NotificationService start!");
	}
  
	private void dumpData(){
		
  
	NotificationData localNotificationData = NotificationHelper.getNotificationData();
		int count = localNotificationData.size();
		Log.d("NotificationListenerService", "  notification icons: " + count);
		for (int i=0;i<count;i++){
			NotificationData.Entry localEntry = localNotificationData.get(i);
			Log.d("NotificationListenerService", "    [" + i + "]");
			dumpData(localEntry);
		}
	}
  
	private void dumpData(NotificationData.Entry paramEntry){
		StatusBarNotification notification = paramEntry.notification;
		Log.d("NotificationListenerService", "notification=" + notification.getNotification());
		int i = notification.getId();
		String str = notification.getPackageName();
		long l = notification.getPostTime();
		boolean bool1 = notification.isClearable();
		boolean bool2 = notification.isOngoing();
		CharSequence localCharSequence = notification.getNotification().tickerText;
		Log.d("NotificationListenerService", "id:" + i + " name:" + str + " time:" + l);
		Log.d("NotificationListenerService", "isClearable:" + bool1 + " isOngoing:" + bool2 + " tickerText:" + localCharSequence);
		NotificationHelper.dumpNotification(notification.getNotification());
	}
  
	private void ensureEnabled(Context paramContext){
		String str1 = new ComponentName(paramContext, NotificationService.class).flattenToString();
		String str2 = Settings.Secure.getString(paramContext.getContentResolver(), "enabled_notification_listeners");
		Log.i("NotificationListenerService", "ensureEnabled");
		if (!TextUtils.isEmpty(str2)){
			if (str2.contains(str1)) {
				return;
			}
			str1 = str2 + ":" + str1;
		}
		
		Settings.Secure.putString(paramContext.getContentResolver(), "enabled_notification_listeners", str1);
	}
  
	private void removeNotification(Bundle paramBundle){
		String key = paramBundle.getString("key");
		Log.d("NotificationListenerService", "removeNotification Key=" + key);
		try{
			cancelNotification(key);
		}catch (NullPointerException e){
			Log.e("NotificationListenerService", "removeNotification NullPointerException.");
		}
	}
  
	public void onCreate(){
		super.onCreate();
		Log.i("NotificationListenerService", "**********  onCreate");
		ensureEnabled(getApplicationContext());
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction("com.mediatek.watchapp.NOTIFICATION_LISTENER.CANCEL");
		localIntentFilter.addAction("com.mediatek.watchapp.DEMO_MODE_POST");
		registerReceiver(this.mReceiver, localIntentFilter);
	}
  
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(this.mReceiver);
	}

	public void onNotificationPosted(StatusBarNotification paramStatusBarNotification) {}

	public void onNotificationRemoved(StatusBarNotification paramStatusBarNotification) {}
}
