package com.goodocom.gocsdk;

interface IGocsdkExt {
	void setBtSwitch(boolean open);
	boolean isBtOpen();
	boolean isBtConnected();
	void dial(String number);
	boolean isInCall();
	void endCall();
	void acceptCall();
}