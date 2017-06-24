package com.cj.util;

import android.content.Context;
import android.provider.Settings;

public class TimeUtil {
	public static boolean is24HourFormat(Context context) {
		String value = Settings.System.getString(context.getContentResolver(),Settings.System.TIME_12_24);                
		return (value == null) ? true : value.equals("24");
	}
}
