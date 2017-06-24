package com.cj.clock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class WtHour extends WtClock{
	private boolean mIsHour24;
	private TextView mAmPmView;

	public WtHour(Context context) {
        this(context, null);
    }

    public WtHour(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WtHour(Context context, AttributeSet attrs,int defStyle) {                       
        super(context, attrs, defStyle);
    }
    
    public void setIsHour24(boolean isHour24){
    	mIsHour24 = isHour24;
    	if(mAmPmView != null){
    		mAmPmView.setVisibility(mIsHour24 ? View.GONE : View.VISIBLE);
    	}
    }
    
    public void setAmPmView(TextView tvAmPm){
    	mAmPmView = tvAmPm;
    }
    
    public View getAmPmView(){
    	return mAmPmView;
    }
    
    public void setValue(int h, int m){
    	int value = h;
    	if(isPointerStyle()){
    		h %= 12;
    		value = h*30+(int)(m*30/60f);
    	}else{
    		if(!mIsHour24){
    			if(mAmPmView != null){
    				mAmPmView.setText(h < 12 ? "AM" : "PM");
    			}
    			value %= 12;
    		}
    	}
    	super.setValue(value);
    }

}
