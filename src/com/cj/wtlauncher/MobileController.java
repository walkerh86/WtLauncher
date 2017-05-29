package com.cj.wtlauncher;

import java.util.List;

import com.android.internal.telephony.TelephonyIntents;
import com.systemui.ext.DataType;
import com.systemui.ext.NetworkType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
	private Context mContext;
	private SubscriptionManager mSubscriptionManager;
	private SignalStrength mSignalStrength;
	private ServiceState mServiceState;
	private boolean mStateConnected;
	private boolean mNoSims;
	private int mDataNetType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
	private int mDataState = TelephonyManager.DATA_DISCONNECTED;
	private NetworkType mNetworkType = null;
	private DataType mDataType = null;
	private static final boolean NETWORK_TYPE_MIN_3G = false;

	PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) { 
			Log.i("hcj","onSignalStrengthsChanged signalStrength="+signalStrength);
			mSignalStrength = signalStrength;
			updateTelephony();
		}

		@Override
        public void onServiceStateChanged(ServiceState state) {
             	Log.i("hcj","onServiceStateChanged state="+state);
             	mServiceState = state;             	
             	updateNetworkType();
                updateTelephony();
		}
		
		@Override
        public void onDataConnectionStateChanged(int state, int networkType) {
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
			//Log.i(TAG,"onReceive action="+action);
			if(TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)){
				updateSimState();
			}else if(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED.equals(action)){
				updateSimState();
			}
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
		mContext = context;
		
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_SERVICE_STATE
				|PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
				|PhoneStateListener.LISTEN_DATA_CONNECTION_STATE); 
		//updateTelephony();

		IntentFilter filter = new IntentFilter();
		filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
		filter.addAction(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED);
		context.registerReceiver(mReceiver, filter);

		mSubscriptionManager = SubscriptionManager.from(context);
		mSubscriptionManager.addOnSubscriptionsChangedListener(mSubscriptionListener);

		DataType.mapDataTypeSets(NETWORK_TYPE_MIN_3G, false,false);                
	}
	
	public void destroy(){
		mSubscriptionManager.removeOnSubscriptionsChangedListener(mSubscriptionListener);
		mContext.unregisterReceiver(mReceiver);
	}
	
	private void updateTelephony(){
		mStateConnected = hasService() && mSignalStrength != null;
		Log.i(TAG,"updateTelephony mStateConnected="+mStateConnected);
		
		int signalLevel = 0;
		if(mStateConnected){
			signalLevel = mSignalStrength.getLevel();
		}
		if(mOnMobileListener != null){
			mOnMobileListener.onSignalStrengthChange(signalLevel);
		}
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
        
        NetworkType networkType = null;
        switch (tempNetworkType) {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                if (!NETWORK_TYPE_MIN_3G) {
                	networkType = NetworkType.Type_G;
                    break;
                }
            case TelephonyManager.NETWORK_TYPE_EDGE:
                if (!NETWORK_TYPE_MIN_3G) {
                	networkType = NetworkType.Type_E;
                    break;
                }
            case TelephonyManager.NETWORK_TYPE_UMTS:
            	networkType = NetworkType.Type_3G;
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            	networkType = NetworkType.Type_3G;
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            	networkType = NetworkType.Type_1X;
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0: //fall through
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            	networkType = NetworkType.Type_1X3G;
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
            	networkType = NetworkType.Type_4G;
                break;
            default:
                if (!NETWORK_TYPE_MIN_3G) {
                	networkType = NetworkType.Type_G;
                } else {
                	networkType = NetworkType.Type_3G;
                }
                break;
        }
        if (!hasService()) {
        	networkType = null;
        }
        
        if(mNetworkType != networkType){
        	mNetworkType = networkType;        	
        }
        
        DataType tmpType = null;
        if(hasService() && mSignalStrength != null){
        	tmpType = DataType.get(mDataNetType);
        }
        if(tmpType != mDataType){
        	mDataType = tmpType;
        	if(mOnMobileListener != null){
        		mOnMobileListener.onDataTypeChange(mDataType);
        	}
        }

        Log.d(TAG, "updateNetworkType: mNetworkType=" + mNetworkType+",mDataType="+mDataType);
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
	
	private OnMobileListener mOnMobileListener;
	public void setOnMobileListener(OnMobileListener listener){
		mOnMobileListener = listener;
	}
	
	public interface OnMobileListener{
		void onSignalStrengthChange(int strength);
		//void onNetworkTypeChange(NetworkType networkType);
		void onDataTypeChange(DataType dataType);
	}
}