package com.cj.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class HorizontalViewPager extends ViewPager{
	public HorizontalViewPager(Context context) {
        super(context);
    }

    public HorizontalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public HorizontalViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
    }    

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
    	if(!mSwipeEnable){
    		return false;
    	}
    	return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
    	if(!mSwipeEnable){
    		return false;
    	}
    	return super.onTouchEvent(ev);
    }
    
    private boolean mSwipeEnable = true;
    public void setSwipeEnable(boolean enable){
    	mSwipeEnable = enable;
    }
}

