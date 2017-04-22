/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cj.clock;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews.RemoteView;

import java.util.TimeZone;

import com.cj.wtlauncher.R;

/**
 * This widget display an analogic clock with two hands for hours and
 * minutes.
 *
 * @attr ref android.R.styleable#AnalogClock_dial
 * @attr ref android.R.styleable#AnalogClock_hand_hour
 * @attr ref android.R.styleable#AnalogClock_hand_minute
 */
//@RemoteView
public class WtClock extends View {
	/*
    private Time mCalendar;

    private Drawable mHourHand;
    private Drawable mMinuteHand;
    private Drawable mDial;
	private Drawable mCenterDrawable;
	private Drawable mWeekDrawable;
	private int mWeekPointCenterX;
	private int mWeekPointCenterY;

    private int mDialWidth;
    private int mDialHeight;

    private boolean mAttached;

    private final Handler mHandler = new Handler();
    private float mMinutes;
    private float mHour;
    private int mDayOfMonth;
    private int mWeek;
    private boolean mChanged;
    
    private Paint mTextPaint;
    */
    public static final int CLOCK_STYLE_POINTER = 0;
    public static final int CLOCK_STYLE_DIGIT = 1;
    private int mClockStyle;
    private int mValueMin;
    private int mValueMax;
    private int mValue = 0;
    
    private Drawable mPointerDrawable;
    private int mPointerCenterX;
    private int mPointerCenterY;

    public WtClock(Context context) {
        this(context, null);
    }

    public WtClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WtClock(Context context, AttributeSet attrs,int defStyle) {                       
        super(context, attrs, defStyle);
        Resources r = context.getResources();
        TypedArray a =
                context.obtainStyledAttributes(
                        attrs, R.styleable.WtClock, defStyle, 0);
        
        mClockStyle = a.getInt(R.styleable.WtClock_clkstyle, CLOCK_STYLE_POINTER);
        mValueMin = a.getInt(R.styleable.WtClock_value_min, 0);
        mValueMax = a.getInt(R.styleable.WtClock_value_max, 0);
        mValue = mValueMin;
        
        mPointerDrawable = a.getDrawable(R.styleable.WtClock_pointer_drawable);
        mPointerCenterX = a.getInt(R.styleable.WtClock_pointer_center_x, -1);
        mPointerCenterY = a.getInt(R.styleable.WtClock_pointer_center_y, -1);
        
/*
        mDial = a.getDrawable(R.styleable.AnalogClock_dial);
        mHourHand = a.getDrawable(R.styleable.AnalogClock_hand_hour);
        mMinuteHand = a.getDrawable(R.styleable.AnalogClock_hand_minute);
	  mCenterDrawable = a.getDrawable(R.styleable.AnalogClock_center_point);
	  mWeekDrawable = a.getDrawable(R.styleable.AnalogClock_week_point_drawable);
	  mWeekPointCenterX = a.getInteger(R.styleable.AnalogClock_week_point_center_x, 0);
	  mWeekPointCenterY = a.getInteger(R.styleable.AnalogClock_week_point_center_y, 0);
	  a.recycle();

        mCalendar = new Time();

        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();
        
        mTextPaint = new Paint();
        mTextPaint.setColor(0xFFFFFFFF);
        mTextPaint.setTextSize(16);
        */
        a.recycle();
    }
    
    /*
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float )heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

	  setMeasuredDimension(mDialWidth,mDialHeight);
    }
*/   
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        if(isPointerStyleValid()){
        	int dw = mPointerDrawable.getIntrinsicWidth();
        	int dh = mPointerDrawable.getIntrinsicHeight();
        	mPointerDrawable.setBounds(mPointerCenterX-dw/2, mPointerCenterY-dh/2, mPointerCenterX+dw/2, mPointerCenterY+dh/2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if(isPointerStyleValid()){
        	canvas.save();
            canvas.rotate((float)mValue/ (mValueMax-mValueMin+1) * 360.0f, mPointerCenterX, mPointerCenterY);
        	mPointerDrawable.draw(canvas);
        	canvas.restore();
        }
/*
        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = getWidth();
        int availableHeight = getHeight();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        final Drawable dial = mDial;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();

        boolean scaled = false;
        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w,
                                   (float) availableHeight / (float) h);
            canvas.save();
            canvas.scale(scale, scale, x, y);
        }

        if (changed) {
            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        dial.draw(canvas);
        
        canvas.drawText(Integer.toString(mDayOfMonth), 245+32/2, 148+20, mTextPaint);

        canvas.save();
        canvas.rotate(mHour / 12.0f * 360.0f, x, y);
        final Drawable hourHand = mHourHand;
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        hourHand.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);

        final Drawable minuteHand = mMinuteHand;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        minuteHand.draw(canvas);
        canvas.restore();

	  if(mCenterDrawable != null){
            w = mCenterDrawable.getIntrinsicWidth();
            h = mCenterDrawable.getIntrinsicHeight();
	  	mCenterDrawable.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
		mCenterDrawable.draw(canvas);
	  }

	  if(mWeekDrawable != null){
            w = mWeekDrawable.getIntrinsicWidth();
            h = mWeekDrawable.getIntrinsicHeight();
		int weekCenterX = mWeekPointCenterX;
		int weekCenterY = mWeekPointCenterY;
	  	mWeekDrawable.setBounds(weekCenterX - (w / 2), weekCenterY - (h / 2), weekCenterX + (w / 2), weekCenterY + (h / 2));

		canvas.save();
	        canvas.rotate(mWeek / 7.0f * 360.0f, weekCenterX, weekCenterY);
		mWeekDrawable.draw(canvas);
		canvas.restore();
	  }
	  
        if (scaled) {
            canvas.restore();
        }
        */
    }    
    
    public void setValue(int value){
    	mValue = value;
    	invalidate();
    }
    
    private boolean isPointerStyleValid(){
    	return (mClockStyle == CLOCK_STYLE_POINTER && mPointerDrawable != null && mPointerCenterX > -1 && mPointerCenterY > -1);
    }
}
