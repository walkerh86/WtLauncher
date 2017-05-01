package com.cj.qs;


public class QSTile{
	public boolean isEnabled(){
		return false;
	}

	public void setEnabled(boolean enabled) {
	}

	public class SignalState{
		public boolean enabled;
		public boolean connected;
		public boolean connecting;
		public int level;
		public int rssi;
	}
}
