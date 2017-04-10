package com.cj.wtlauncher;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class NotificationData{
  private final ArrayList<Entry> mEntries = new ArrayList();
  private final Comparator<Entry> mEntryCmp = new Comparator(){
	  public int compare(Object arg0, Object arg1) {
		NotificationData.Entry entry1 = (NotificationData.Entry)arg0;
		NotificationData.Entry entry2 = (NotificationData.Entry)arg1;
      StatusBarNotification statusBarNotification1 = entry1.notification;
      StatusBarNotification statusBarNotification2 = entry2.notification;
      if (entry1.interruption != entry2.interruption){      
        if (entry1.interruption) {
          return 1;
        }
        return -1;
      }
      return (int)(statusBarNotification1.getNotification().when - statusBarNotification2.getNotification().when);
    }
  };
  private final Comparator<Entry> mEntryPostTimeCmp = new Comparator(){  
    public int compare(Object arg0, Object arg1) {
    	NotificationData.Entry entry1 = (NotificationData.Entry)arg0;
		NotificationData.Entry entry2 = (NotificationData.Entry)arg1;
      StatusBarNotification statusBarNotification1 = entry1.notification;
      StatusBarNotification statusBarNotification2 = entry2.notification;
      return (int)(statusBarNotification1.getPostTime() - statusBarNotification2.getPostTime());
    }
  };
  
  public int add(Entry paramEntry){  
    int size = this.mEntries.size();
    boolean added = false;
    int i=0;
    for (;i<size;i++){    
      if (this.mEntryPostTimeCmp.compare(this.mEntries.get(i), paramEntry) < 0){      
        this.mEntries.add(i, paramEntry);
        added = true;
        break;
      }
    }
    if(!added){
	mEntries.add(paramEntry);
    }
    return i;
  }
  
  public Entry findByKey(String paramString1, String paramString2, int paramInt){  
    Iterator iterator = this.mEntries.iterator();
    while (iterator.hasNext()){    
      Entry localEntry = (Entry)iterator.next();
      if (((paramString2 == null) && (localEntry.notification.getTag() == null)) 
	  	|| ((paramString2 != null) && (paramString2.equals(localEntry.notification.getTag())) 
	  	&& (paramString1.equals(localEntry.notification.getPackageName())) && (paramInt == localEntry.notification.getId()))) {
        return localEntry;
      }
    }
    return null;
  }
  
  public int findPositionByKey(String paramString1, String paramString2, int paramInt){  
    int i = 0;
    while (i < this.mEntries.size()){    
      Entry localEntry = (Entry)this.mEntries.get(i);
      if (((paramString2 == null) && (localEntry.notification.getTag() == null)) 
	  	|| ((paramString2 != null) && (paramString2.equals(localEntry.notification.getTag())) 
	  	&& (paramString1.equals(localEntry.notification.getPackageName())) && (paramInt == localEntry.notification.getId()))) {
        return i;
      }
      i += 1;
    }
    return -1;
  }
  
  public Entry get(int paramInt){  
    return (Entry)this.mEntries.get(paramInt);
  }
  
  public Entry remove(String paramString1, String paramString2, int paramInt){  
    Entry entry = findByKey(paramString1, paramString2, paramInt);
    if (entry != null) {
      this.mEntries.remove(entry);
    }
    return entry;
  }
  
  public int size(){  
    return this.mEntries.size();
  }
  
  public int update(Entry paramEntry){  
    StatusBarNotification localStatusBarNotification = paramEntry.notification;
    remove(localStatusBarNotification.getPackageName(), localStatusBarNotification.getTag(), localStatusBarNotification.getId());
    return add(paramEntry);
  }
  
  public static final class Entry{  
    private boolean interruption;
    private boolean mIsRead = false;
    public NotificationSubFragment mNotificationSubFragment;
    public StatusBarNotification notification;
    
    public Entry() {}
    
    public Entry(StatusBarNotification statusBarNotification, NotificationSubFragment notificationSubFragment){    
      this.notification = statusBarNotification;
      this.mNotificationSubFragment = notificationSubFragment;
    }
  }
}