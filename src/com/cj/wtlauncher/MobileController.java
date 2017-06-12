package com.cj.wtlauncher;

import java.util.List;

import com.android.internal.telephony.TelephonyIntents;
import com.systemui.ext.DataType;
import com.systemui.ext.NetworkType;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MobileController{
	private static final String TAG = "hcj.MobileController";
	//private Context mContext;
	private TelephonyManager mTelephonyManager;
	private SubscriptionManager mSubscriptionManager;
	private SignalStrength mSignalStrength;
	private int mSignalLevel = -1;
	private ServiceState mServiceState;
	private boolean mStateConnected;
	private boolean mNoSims;
	private int mDataNetType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
	private int mDataState = TelephonyManager.DATA_DISCONNECTED;
	private WifiController mWifiController;
	private boolean mWifiConnected;
	private boolean mAirplaneOn;
	
	private static final boolean NETWORK_TYPE_MIN_3G = false;
	public static final int WT_NETWORK_TYPE_NULL = 0;
	public static final int WT_NETWORK_TYPE_2G = 1;
	public static final int WT_NETWORK_TYPE_3G = 2;
	public static final int WT_NETWORK_TYPE_4G = 3;
	
	//private int mNetworkType = WT_NETWORK_TYPE_NULL;
	private int mDataType = WT_NETWORK_TYPE_NULL;
	private boolean mDataEnable;

	PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) { 
			Log.i(TAG,"onSignalStrengthsChanged signalStrength="+signalStrength);
			mSignalStrength = signalStrength;
			updateTelephony();
		}

		@Override
        public void onServiceStateChanged(ServiceState state) {
             	Log.i(TAG,"onServiceStateChanged state="+state);
             	mServiceState = state;             	
             	updateNetworkType();
                updateTelephony();
		}
		
		@Override
        public void onDataConnectionStateChanged(int state, int networkType) {
			Log.i(TAG,"onDataConnectionStateChanged state="+state+",networkType="+networkType);
			mDataState = state;
			mDataNetType = networkType;
			updateNetworkType();
            updateTelephony();
		}
	};
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			Log.i(TAG,"onReceive action="+action);
			if(TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)){
				updateSimState();
			}else if(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED.equals(action)){
				updateSimState();
			}else if(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED.equals(action)){
				getDataConnectionState();
			}
		}
	};
	
	private ContentObserver mMobileStateForSingleCardChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
               getDataConnectionState();
        }
    };
	
	private final OnSubscriptionsChangedListener mSubscriptionListener = new OnSubscriptionsChangedListener() {            
        @Override
        public void onSubscriptionsChanged() {
            Log.i(TAG, "onSubscriptionsChanged");
            updateSimState();
        };
    };
	
	public MobileController(Context context){
		//mContext = context;
		
		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_SERVICE_STATE
				|PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
				|PhoneStateListener.LISTEN_DATA_CONNECTION_STATE); 
		//updateTelephony();

		IntentFilter filter = new IntentFilter();
		filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
		filter.addAction(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED);
		filter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
		context.registerReceiver(mReceiver, filter);

		mSubscriptionManager = SubscriptionManager.from(context);
		mSubscriptionManager.addOnSubscriptionsChangedListener(mSubscriptionListener);
		
		//mContext.getContentResolver().registerContentObserver(
        //        Settings.Secure.getUriFor(Settings.Global.MOBILE_DATA)
        //        , true, mMobileStateForSingleCardChangeObserver);

		DataType.mapDataTypeSets(NETWORK_TYPE_MIN_3G, false,false);
		
		mDataEnable = mTelephonyManager.getDataEnabled();
	}
	
	public void onDestroy(Context context){
		mSubscriptionManager.removeOnSubscriptionsChangedListener(mSubscriptionListener);
		context.unregisterReceiver(mReceiver);
		
		mWifiController.onDestroy(context);
	}
	
	@SuppressLint("NewApi")
	private void updateTelephony(){
		mStateConnected = hasService() && mSignalStrength != null;
		Log.i(TAG,"updateTelephony mStateConnected="+mStateConnected);
		
		//int signalLevel = -1;
		if(mStateConnected){
			mSignalLevel = mSignalStrength.getLevel();
		}
		if(mOnMobileListener != null){
			mOnMobileListener.onSignalStrengthChange(mSignalLevel);
		}
		
		boolean dataConnected = mStateConnected && mDataState == TelephonyManager.DATA_CONNECTED;
		int networkType = WT_NETWORK_TYPE_NULL;
		if(dataConnected){			
			networkType = convertDataNetType(mDataNetType);
		}		
    	if(mOnMobileListener != null){
    		mOnMobileListener.onDataTypeChange(networkType);
    	}
	}
	
	private int convertDataNetType(int originType){
		int networkType = WT_NETWORK_TYPE_NULL;
		switch (originType) {
        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            if (!NETWORK_TYPE_MIN_3G) {
            	networkType = WT_NETWORK_TYPE_2G;
                break;
            }
        case TelephonyManager.NETWORK_TYPE_EDGE:
            if (!NETWORK_TYPE_MIN_3G) {
            	networkType = WT_NETWORK_TYPE_2G;
                break;
            }
        case TelephonyManager.NETWORK_TYPE_UMTS:
        	networkType = WT_NETWORK_TYPE_3G;
            break;
        case TelephonyManager.NETWORK_TYPE_HSDPA:
        case TelephonyManager.NETWORK_TYPE_HSUPA:
        case TelephonyManager.NETWORK_TYPE_HSPA:
        case TelephonyManager.NETWORK_TYPE_HSPAP:
        	networkType = WT_NETWORK_TYPE_3G;
            break;
        case TelephonyManager.NETWORK_TYPE_CDMA:
        case TelephonyManager.NETWORK_TYPE_1xRTT:
        	networkType = WT_NETWORK_TYPE_2G;
            break;
        case TelephonyManager.NETWORK_TYPE_EVDO_0: //fall through
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
        case TelephonyManager.NETWORK_TYPE_EVDO_B:
        case TelephonyManager.NETWORK_TYPE_EHRPD:
        	networkType = WT_NETWORK_TYPE_2G;
            break;
        case TelephonyManager.NETWORK_TYPE_LTE:
        	networkType = WT_NETWORK_TYPE_4G;
            break;
        default:
            if (!NETWORK_TYPE_MIN_3G) {
            	networkType = WT_NETWORK_TYPE_2G;
            } else {
            	networkType = WT_NETWORK_TYPE_3G;
            }
            break;
		}
		return networkType;
	}
	
	private void updateSimState(){
		boolean noSims = true;
		List<SubscriptionInfo> subscriptions = mSubscriptionManager.getActiveSubscriptionInfoList();
		if(subscriptions == null || subscriptions.size() < 1){
			noSims = true;
		}else{
			noSims = false;
		}
		if(noSims != mNoSims){
			mNoSims = noSims;
		}
		Log.i(TAG, "updateSimState mNoSims="+mNoSims);
		updateTelephony();
	}
	
	private final void updateNetworkType() {
		/*
        int tempNetworkType; //Big - switch

        if (mServiceState != null) {
            int networkTypeData = mDataNetType; //Big - Data
            if ((mDataState == TelephonyManager.DATA_UNKNOWN ||
                 mDataState == TelephonyManager.DATA_DISCONNECTED)) {
                Log.d(TAG, "updateNetworkType: DataState= " + mDataState
                       + ", getDataNetworkType= " + mServiceState.getDataNetworkType());
                networkTypeData = mServiceState.getDataNetworkType();
            }
            tempNetworkType = getNWTypeByPriority(mServiceState.getVoiceNetworkType(),
                                                  networkTypeData);
        } else {
            tempNetworkType = mDataNetType;
        }
        
        int networkType = WT_NETWORK_TYPE_NULL;
        switch (tempNetworkType) {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                if (!NETWORK_TYPE_MIN_3G) {
                	networkType = WT_NETWORK_TYPE_2G;
                    break;
                }
            case TelephonyManager.NETWORK_TYPE_EDGE:
                if (!NETWORK_TYPE_MIN_3G) {
                	networkType = WT_NETWORK_TYPE_2G;
                    break;
                }
            case TelephonyManager.NETWORK_TYPE_UMTS:
            	networkType = WT_NETWORK_TYPE_3G;
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            	networkType = WT_NETWORK_TYPE_3G;
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            	networkType = WT_NETWORK_TYPE_2G;
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0: //fall through
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            	networkType = WT_NETWORK_TYPE_2G;
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
            	networkType = WT_NETWORK_TYPE_4G;
                break;
            default:
                if (!NETWORK_TYPE_MIN_3G) {
                	networkType = WT_NETWORK_TYPE_2G;
                } else {
                	networkType = WT_NETWORK_TYPE_3G;
                }
                break;
        }
        if (!hasService()) {
        	networkType = WT_NETWORK_TYPE_NULL;
        }
        
        if(mNetworkType != networkType){
        	mNetworkType = networkType;        	
        }
        */
        /*
        boolean dataEnable = mTelephonyManager.getDataEnabled();
        if(dataEnable != mDataEnable){
        	mDataEnable = dataEnable;
        	if(mOnMobileListener != null){
        		mOnMobileListener.onDataEnable(mDataEnable);
        	}
        }
        
        int tmpType = WT_NETWORK_TYPE_NULL;
        if (mServiceState != null) {
        	int networkTypeData = mDataNetType;
        	if ((mDataState == TelephonyManager.DATA_UNKNOWN ||
                    mDataState == TelephonyManager.DATA_DISCONNECTED)) {
                   Log.d(TAG, "updateNetworkType: DataState= " + mDataState
                          + ", getDataNetworkType= " + mServiceState.getDataNetworkType());
                   networkTypeData = mServiceState.getDataNetworkType();
            }
        	tmpType = getNWTypeByPriority(mServiceState.getVoiceNetworkType(),networkTypeData);            		   
        }else{
        	tmpType = mDataNetType;
        }
        
        switch(tmpType){
        	case TelephonyManager.NETWORK_TYPE_EDGE:
        	case TelephonyManager.NETWORK_TYPE_CDMA:
        	case TelephonyManager.NETWORK_TYPE_1xRTT:
        		tmpType = WT_NETWORK_TYPE_2G;
        		break;
        	case TelephonyManager.NETWORK_TYPE_EVDO_0:
        	case TelephonyManager.NETWORK_TYPE_EVDO_A:
        	case TelephonyManager.NETWORK_TYPE_EVDO_B:
        	case TelephonyManager.NETWORK_TYPE_EHRPD:
        	case TelephonyManager.NETWORK_TYPE_UMTS:
        	case TelephonyManager.NETWORK_TYPE_HSDPA:
        	case TelephonyManager.NETWORK_TYPE_HSUPA:
        	case TelephonyManager.NETWORK_TYPE_HSPA:
        	case TelephonyManager.NETWORK_TYPE_HSPAP:
        		tmpType = WT_NETWORK_TYPE_3G;
        		break;
        	case TelephonyManager.NETWORK_TYPE_LTE:
        		tmpType = WT_NETWORK_TYPE_4G;
        		break;
        	default:
        		if (!NETWORK_TYPE_MIN_3G) {
        			tmpType = WT_NETWORK_TYPE_2G;
                } else {
                	tmpType = WT_NETWORK_TYPE_3G;
                }
        		break;
        }
*/
        /*
        if(!hasService() || mSignalStrength == null 
        	|| mDataState != TelephonyManager.DATA_CONNECTED || !mDataEnable){
        	tmpType = WT_NETWORK_TYPE_NULL;
        }*/
        //if(tmpType != mDataType){
        	//mDataType = tmpType;
        	//if(mOnMobileListener != null){
        		//mOnMobileListener.onDataTypeChange(tmpType);
        	//}
        //}            

        //Log.d(TAG, "updateNetworkType: mNetworkType=" + mNetworkType+",mDataType="+mDataType);
    }
	
	private void getDataConnectionState() {
		boolean dataEnable = mTelephonyManager.getDataEnabled();
		Log.d(TAG, "getDataConnectionState: dataEnable=" + dataEnable);
		//todo , add airplane mode
		if(mOnMobileListener != null){
    		mOnMobileListener.onDataTypeChange(dataEnable ? 2 : 0);
    	}
	}
		
	 /// M: Support "Service Network Type on Statusbar". @{
    private final int getNWTypeByPriority(int cs, int ps) {
        /// By Network Class.
        if (TelephonyManager.getNetworkClass(cs) > TelephonyManager.getNetworkClass(ps)) {
            return cs;
        } else {
            return ps;
        }
    }
/*    
    private int getNetworkTypeIcon(NetworkType networkType) {
        if (networkType == NetworkType.Type_G) {
            return R.drawable.stat_sys_network_type_g;
        } else if (networkType == NetworkType.Type_E) {
            return R.drawable.stat_sys_network_type_e;
        } else if (networkType == NetworkType.Type_3G) {
            return R.drawable.stat_sys_network_type_3g;
        } else if (networkType == NetworkType.Type_4G) {
            return R.drawable.stat_sys_network_type_4g;
        } else if (networkType == NetworkType.Type_1X) {
            return R.drawable.stat_sys_network_type_1x;
        } else if (networkType == NetworkType.Type_1X3G) {
            return R.drawable.stat_sys_network_type_3g;
        } else {
            return -1;
        }
    }
*/
	private boolean hasService(){
		if (mServiceState != null) {
			Log.i(TAG,"hasService state="+mServiceState.getVoiceRegState());
			switch (mServiceState.getVoiceRegState()) {
				case ServiceState.STATE_POWER_OFF:
					return false;
				case ServiceState.STATE_OUT_OF_SERVICE:
				case ServiceState.STATE_EMERGENCY_ONLY:
					return mServiceState.getDataRegState() == ServiceState.STATE_IN_SERVICE;
				default:
					return true;
			}
		} else {
			return false;
		}
	}
	
	public int getSignalStrengthLevel(){
		return mStateConnected ? mSignalLevel : -1;
	}
	
	public int getDateNetType(){
		return mDataType;
	}
	
	public void toggle(){
		setEnable(!isDataEnable());
	}
	
	public boolean isDataEnable(){
		return mTelephonyManager.getDataEnabled();
	}
	
	public void setEnable(boolean enable){
		try{
			if (mTelephonyManager != null) {
				mTelephonyManager.setDataEnabled(enable);
			}
		}catch(Exception e){
			Log.i(TAG,"setEnabled e="+e);
		}
	}
	
	private OnMobileListener mOnMobileListener;
	public void setOnMobileListener(OnMobileListener listener){
		mOnMobileListener = listener;
	}
	
	public interface OnMobileListener{
		void onSignalStrengthChange(int strength);
		//void onNetworkTypeChange(NetworkType networkType);
		void onDataTypeChange(int dataType);
		void onDataEnable(boolean enable);
	}
}