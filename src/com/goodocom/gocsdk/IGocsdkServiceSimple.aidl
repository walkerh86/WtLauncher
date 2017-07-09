package com.goodocom.gocsdk;

import com.goodocom.gocsdk.IGocsdkCallback;

interface IGocsdkServiceSimple {
	void sendCommand(String cmd);
	void registerCallback(IGocsdkCallback callback);
	void unregisterCallback(IGocsdkCallback callback);
	
	void setBtSwitch(boolean open);
	boolean isBtOpen();
	boolean isBtConnected();
	void dial(String number);
	boolean isInCall();
	void endCall();
	void acceptCall();
}