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
import android.graphics.drawable.Drawable;
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

public class NotificationSubFragment extends Fragment implements NotificationScrollView.Callback{
	private static final int[] IMAGE_ID = { 
		R.drawable.notification_background_01, 
		R.drawable.notification_background_02,
		R.drawable.notification_background_03, 
		R.drawable.notification_background_04, 
		R.drawable.notification_background_05 };
	private static final String TAG = "hcj.NotificationSubFragment";
	private Button mAction1;
	private Button mAction2;
	private Button mAction3;
	private View mBackgroundView;
	private boolean mCanDismiss;
	private Chronometer mChronometer;
	private Context mContext;
	private Button mDismiss;
	private ImageView mIconNoteType;
	private ImageView mIconView;
	private int mId;
	private String mKey;
	private Button mOpen;
	private ImageButton mOptions;
	private String mPkgName;
	private ViewGroup mRootView;
	private StatusBarNotification mSbn;
	private NotificationScrollView mScrollView;
	private String mTag;
	private TextView mTextNoteType;
	private TextView mTextView;
	private DateTimeView mTime;
	private TextView mTitleView;
	
	public static NotificationSubFragment create(StatusBarNotification statusBarNotification){
		NotificationSubFragment notificationSubFragment = new NotificationSubFragment();
		notificationSubFragment.setContent(statusBarNotification);
		return notificationSubFragment;
	}

	private boolean isCollapse(){
		return this.mOptions.getVisibility() == 0;
	}

	private void loadAndSetActions(Notification notification){
		if (!NotificationHelper.filterNotification(notification)){
			mCanDismiss = true;
		}
		loadAndSetContentIntent(this.mOpen, notification);
	}

	private void loadAndSetActions(Button paramButton, final NotificationCompat.Action paramAction){
		if (paramButton == null){
			return;
		}

		paramButton.setText(paramAction.title);
		paramButton.setTag("VISIBLE");
		paramButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View paramAnonymousView){
				try{
					Log.d(NotificationSubFragment.TAG, "Send actionIntent");
					if (paramAction.actionIntent != null) {
						paramAction.actionIntent.send();
					}
					return;
				}catch (PendingIntent.CanceledException e){
					Log.w(TAG, "loadAndSetActions e="+e);
				}
			}
		});
	}

	private void loadAndSetBackground(Notification notification, View view, int imgIdx){
		view.setBackgroundResource(IMAGE_ID[Math.abs(imgIdx % IMAGE_ID.length)]);
	}

	private void loadAndSetBigText(Notification notification, TextView textView, boolean paramBoolean){
		if(textView == null){
			return;
		}
		CharSequence[] textLines = NotificationHelper.getTextLines(NotificationCompat.getExtras(notification));
		if(textLines == null){
			return;
		}
		if(paramBoolean){
			textView.setMaxLines(8);
		}
		for(int i=0;i<textLines.length;i++){
			if(textLines[i].length() < 20){
				textView.append(textLines[i] + "\n");
			}else{
				textView.append(textLines[i].subSequence(0, 17) + "...\n");
			}
		}
	}

	private void loadAndSetContentIntent(Button button, final Notification notification){
		if ((button == null) || (notification.contentIntent == null)){
			return;
		}
		button.setTag("VISIBLE");
		button.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				try{
					Log.i(TAG,"loadAndSetContentIntent contentIntent.send");
					notification.contentIntent.send();
					if ((notification.flags & Notification.FLAG_AUTO_CANCEL) != Notification.FLAG_AUTO_CANCEL) {
						Log.i(TAG,"open click, no FLAG_AUTO_CANCEL return");
						return;
					}
					if ((notification.flags & Notification.FLAG_FOREGROUND_SERVICE) == 0){
						NotificationHelper.filterNotification(notification);
						if (NotificationSubFragment.this.mCanDismiss){
							NotificationSubFragment.this.requestDismiss();
						}else{
							
						}
					}else{
						Log.i(TAG,"open click, FLAG_FOREGROUND_SERVICE return");
					}
				}catch (PendingIntent.CanceledException e){
					Log.i(TAG,"loadAndSetContentIntent e="+e);
				}
			}
		});
	}

	private void loadAndSetContentText(Notification notification){
		Bundle localBundle = NotificationCompat.getExtras(notification);
		if (mTitleView != null) {
			mTitleView.setText(NotificationHelper.getTitle(localBundle));
		}
		if (mTextView != null){
			if (NotificationHelper.getText(localBundle) != null) {
				mTextView.setText(NotificationHelper.getText(localBundle));
			}
		}else{
			return;
		}
		loadAndSetBigText(notification, this.mTextView, false);
	}

	private void loadAndSetIcon(Notification notification, ImageView imageView){
		if (imageView == null){
			return;
		}
		Bitmap localBitmap;
		localBitmap = NotificationHelper.getAppIcon(NotificationCompat.getExtras(notification));
		if(localBitmap != null){
			imageView.setImageBitmap(localBitmap);
			return;
		}
		
		try{
			Drawable dr = this.mContext.getPackageManager().getApplicationIcon(this.mPkgName);
			imageView.setImageDrawable(dr);
		}catch(PackageManager.NameNotFoundException e){
		}
	}

	private void loadAndSetKey(){
		if (this.mSbn == null){
			return;
		}
		this.mKey = this.mSbn.getKey();
		this.mPkgName = this.mSbn.getPackageName();
		this.mTag = this.mSbn.getTag();
		this.mId = this.mSbn.getId();
	}

	private void loadAndSetTime(Notification notification){
		long postTime = 0L;
		if(mSbn != null){
			postTime = mSbn.getPostTime();
		}
		Log.i(TAG,"loadAndSetTime mSbn="+mSbn+",postTime="+postTime);
		if(postTime != 0L){
			if (NotificationHelper.getShowChronometer(NotificationCompat.getExtras(notification))) {
				if (this.mChronometer != null){
					mChronometer.setVisibility(0);
					mChronometer.setBase(SystemClock.elapsedRealtime() - System.currentTimeMillis() + postTime);
					mChronometer.start();
				}
			}
			mTime.setVisibility(0);
			mTime.setTime(postTime);
		}else{
			mChronometer.setVisibility(8);
			mTime.setVisibility(4);
		}
	}

	private void onRestoreInstanceState(Bundle paramBundle){
		if (paramBundle != null){
			String str1 = paramBundle.getString("package_name");
			String str2 = paramBundle.getString("tag");
			int i = paramBundle.getInt("id");
			NotificationData data = NotificationHelper.getNotificationData();
			i = data.findPositionByKey(str1, str2, i);
			if (i != -1) {
				setContent(data.get(i).notification);
			}
		}
	}

	private void requestDismiss(){
		mScrollView.dismissChild(mScrollView);
	}

	private void setActionsVisibility(){
		String str1 = (String)this.mAction1.getTag();
		String str2 = (String)this.mAction2.getTag();
		String str3 = (String)this.mAction3.getTag();
		String str4 = (String)this.mOpen.getTag();
		String str5 = (String)this.mDismiss.getTag();
		mAction1.setVisibility(((str1 != null) && (str1.equals("VISIBLE"))) ? 0 : 8);
		mAction2.setVisibility(((str2 != null) && (str2.equals("VISIBLE"))) ? 0 : 8);
		mAction3.setVisibility(((str3 != null) && (str3.equals("VISIBLE"))) ? 0 : 8);
		mOpen.setVisibility(((str4 != null) && (str4.equals("VISIBLE"))) ? 0 : 8);
		mDismiss.setVisibility(((str5 != null) && (str5.equals("VISIBLE"))) ? 0 : 8);
	}

	public void collapseLayout(){
		if (!isCollapse()){
			mOptions.setVisibility(0);
			loadAndSetContentText(this.mSbn.getNotification());
			mAction1.setVisibility(8);
			mAction2.setVisibility(8);
			mAction3.setVisibility(8);
			mOpen.setVisibility(8);
			mDismiss.setVisibility(8);
		}
	}

	public void expandLayout(){
		if (isCollapse()){
			mOptions.setVisibility(8);
			loadAndSetBigText(this.mSbn.getNotification(), this.mTextView, true);
			setActionsVisibility();
		}
	}

	public void handleSwipe(View paramView){
		if (mContext != null) {
			mContext.sendBroadcast(new Intent("com.mediatek.watchapp.NOTIFICATION_LISTENER.CANCEL").putExtra("key", this.mKey));
		}
		try{
			 if (this.mSbn.getNotification().deleteIntent != null) {
				this.mSbn.getNotification().deleteIntent.send();
			}
			this.mRootView.setVisibility(View.GONE);
		}catch(PendingIntent.CanceledException e){
			Log.i(TAG,"handleSwipe e="+e);
		}
	}

	public void onAttach(Activity paramActivity){
		super.onAttach(paramActivity);
		this.mContext = paramActivity;
	}

	public void onCreate(Bundle paramBundle){  
		super.onCreate(paramBundle);
	}

	public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle){  
		this.mRootView = ((ViewGroup)paramLayoutInflater.inflate(R.layout.notification_content_layout, paramViewGroup, false));
		this.mScrollView = ((NotificationScrollView)this.mRootView.findViewById(R.id.card_scroll_view));
		this.mBackgroundView = this.mRootView.findViewById(R.id.card_background);
		this.mIconView = ((ImageView)this.mRootView.findViewById(R.id.icon));
		this.mTitleView = ((TextView)this.mRootView.findViewById(R.id.title));
		this.mTextView = ((TextView)this.mRootView.findViewById(R.id.text));
		this.mOptions = ((ImageButton)this.mRootView.findViewById(R.id.options));
		this.mAction1 = ((Button)this.mRootView.findViewById(R.id.action1));
		this.mAction2 = ((Button)this.mRootView.findViewById(R.id.action2));
		this.mAction3 = ((Button)this.mRootView.findViewById(R.id.action3));
		this.mOpen = ((Button)this.mRootView.findViewById(R.id.open));
		this.mDismiss = ((Button)this.mRootView.findViewById(R.id.close));
		this.mTime = ((DateTimeView)this.mRootView.findViewById(R.id.time));
		this.mChronometer = ((Chronometer)this.mRootView.findViewById(R.id.chronometer));
		this.mIconNoteType = ((ImageView)this.mRootView.findViewById(R.id.type_icon));
		this.mTextNoteType = ((TextView)this.mRootView.findViewById(R.id.type_text));
		this.mScrollView.setCallback(this);
		this.mOptions.setOnClickListener(new View.OnClickListener(){
			public void onClick(View paramAnonymousView){
				NotificationSubFragment.this.expandLayout();
			}
		});
		this.mDismiss.setOnClickListener(new View.OnClickListener(){
			public void onClick(View paramAnonymousView){
				NotificationSubFragment.this.requestDismiss();
			}
		});
		if (this.mSbn == null) {
			onRestoreInstanceState(paramBundle);
		}
		refreshContent();
		return this.mRootView;
	}

	public void onSaveInstanceState(Bundle paramBundle){
		super.onSaveInstanceState(paramBundle);
		paramBundle.putString("package_name", this.mPkgName);
		paramBundle.putString("tag", this.mTag);
		paramBundle.putInt("id", this.mId);
	}

	public void refreshContent(){
		loadAndSetKey();

		Log.i(TAG,"refreshContent");
		if ((this.mRootView == null) || (this.mSbn == null)){
			Log.i(TAG,"refreshContent null");
			return;
		}
		Notification localNotification = this.mSbn.getNotification();
		String str = this.mSbn.getPackageName();
		if (str.equals("com.mediatek.wearable")) {
			this.mIconNoteType.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.type_watch));
			this.mTextNoteType.setText(this.mContext.getResources().getString(R.string.type_watch));
		}else{
			this.mIconNoteType.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.type_phone));
			this.mTextNoteType.setText(this.mContext.getResources().getString(R.string.type_phone));
		}
		
		loadAndSetBackground(localNotification, this.mBackgroundView, this.mId);
		loadAndSetActions(localNotification);
		loadAndSetTime(localNotification);
		loadAndSetIcon(localNotification, this.mIconView);
		loadAndSetContentText(localNotification);
		if (!isCollapse()) {
			setActionsVisibility();
		}		
	}
	
	public void setContent(StatusBarNotification paramStatusBarNotification){  
		this.mSbn = paramStatusBarNotification;
	}
}
