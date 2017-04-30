package com.cj.clock;

import com.cj.wtlauncher.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

public class WtDate extends TextView {
	private String mDateFormat;

	public WtDate(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}
	
	public WtDate(Context context, AttributeSet attrs,int defStyle) {                       
        super(context, attrs, defStyle);

		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WtDate, defStyle, 0); 
		mDateFormat = a.getString(R.styleable.WtDate_date_format);
		if(mDateFormat == null){
			mDateFormat = "yyyy/MM/dd  E";
		}
		a.recycle();
	}

	public String getDateFormat(){
		return mDateFormat;
	}
}
