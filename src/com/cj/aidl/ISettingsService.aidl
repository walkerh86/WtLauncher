package com.cj.aidl;

interface ISettingsService {
	void setActiveProfile(String profileKey);
	boolean getDataEnabled();
	void setDataEnabled(boolean enable);
	void setAirplaneModeEnabled(boolean enable);
}