package com.cj.qs;

import com.cj.aidl.ISettingsService;

public class QSTile{
	protected ISettingsService mSettingsService;
	
	public void setSettingsService(ISettingsService service){
		mSettingsService = service;
	}

	public boolean isEnabled(){
		return false;
	}

	public void setEnabled(boolean enabled) {
	}
}
