package com.cj.clock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.cj.wtlauncher.R;

public class WtClock extends View {
    public static final int CLOCK_STYLE_POINTER = 0;
    public static final int CLOCK_STYLE_DIGIT = 1;
    public static final int CLOCK_STYLE_IMAGES = 2;
    private int mClockStyle;
    private int mValueMin;
    private int mValueMax;
    private int mValue = 0;
    
    private Drawable mPointerDrawable;
    private int mPointerCenterX;
    private int mPointerCenterY;
    private int mClockMinAngle;
    private int mClockMaxAngle;
    private boolean mClockReverse;//non-clockwise
    
    private int mDigitTextSize;
    private int mDigitTextColor;
    private int mDigitTextStrokeSize;
    private int mDigitTextX;
    private int mDigitTextY;
    private Paint mDigitTextPaint;
	private Drawable[] mDigitTextDrawables;
	private int mDigitTextNum;
	private Drawable[] mDigitTextDrawables_1;
	private int mDigitDrawablesIdx;
	
    public WtClock(Context context) {
        this(context, null);
    }

    public WtClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WtClock(Context context, AttributeSet attrs,int defStyle) {                       
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WtClock, defStyle, 0); 
        
        mClockStyle = a.getInt(R.styleable.WtClock_clkstyle, CLOCK_STYLE_POINTER);
        mValueMin = a.getInt(R.styleable.WtClock_value_min, 0);
        mValueMax = a.getInt(R.styleable.WtClock_value_max, 0);
        mValue = mValueMin;
        mClockMinAngle = a.getInt(R.styleable.WtClock_clk_min_angle, 0);
        mClockMaxAngle = a.getInt(R.styleable.WtClock_clk_max_angle, 360);
        mClockReverse = a.getBoolean(R.styleable.WtClock_clk_reverse, false);
        
        if(mClockStyle == CLOCK_STYLE_POINTER){
        	mPointerDrawable = a.getDrawable(R.styleable.WtClock_pointer_drawable);        
        	mPointerCenterX = a.getInt(R.styleable.WtClock_pointer_center_x, -1);
        	mPointerCenterY = a.getInt(R.styleable.WtClock_pointer_center_y, -1);
        	if(mPointerDrawable != null){
        		int dw = mPointerDrawable.getIntrinsicWidth();
        		int dh = mPointerDrawable.getIntrinsicHeight();
        		mPointerDrawable.setBounds(mPointerCenterX-dw/2, mPointerCenterY-dh/2, mPointerCenterX+dw/2, mPointerCenterY+dh/2);
        	}
        }else if(mClockStyle == CLOCK_STYLE_DIGIT){        
	        mDigitTextSize = a.getInt(R.styleable.WtClock_digitext_size, 0);
	        mDigitTextColor = a.getColor(R.styleable.WtClock_digitext_color, 0);
	        mDigitTextStrokeSize = a.getInt(R.styleable.WtClock_digitext_strokesize, 2);
	        mDigitTextX = a.getInt(R.styleable.WtClock_digitext_x, -1);
	        mDigitTextY = a.getInt(R.styleable.WtClock_digitext_y, -1);
	                
	        int digitDrawablesId = a.getResourceId(R.styleable.WtClock_digitext_drawables, -1);
	        if(digitDrawablesId > 0){
	        	Resources res = context.getResources();
	        	TypedArray ar = res.obtainTypedArray(digitDrawablesId);
	        	int len = ar.length();
	        	mDigitTextDrawables = new Drawable[len];
	        	for(int i=0;i<len;i++){
	        		int resId = ar.getResourceId(i, 0);
	        		if(resId > 0){
	        			mDigitTextDrawables[i] = res.getDrawable(resId);
	        		}
	        	}
	        	mDigitTextNum = len;
	        	ar.recycle();
	        }
	        
	        if(isDigitTextStyle()){
	        	mDigitTextPaint = new Paint();
	        	mDigitTextPaint.setAntiAlias(true);
	        	mDigitTextPaint.setTextSize(mDigitTextSize);
	        	mDigitTextPaint.setColor(mDigitTextColor);
	        	mDigitTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	        	mDigitTextPaint.setStrokeWidth(mDigitTextStrokeSize);
	        }
        }else if(mClockStyle == CLOCK_STYLE_IMAGES){ 
        	mDigitTextX = a.getInt(R.styleable.WtClock_digitext_x, -1);
	        mDigitTextY = a.getInt(R.styleable.WtClock_digitext_y, -1);
	        int digitDrawablesId = a.getResourceId(R.styleable.WtClock_digitext_drawables, -1);
	        if(digitDrawablesId > 0){
	        	Resources res = context.getResources();
	        	TypedArray ar = res.obtainTypedArray(digitDrawablesId);
	        	int len = ar.length();
	        	mDigitTextDrawables = new Drawable[len];
	        	for(int i=0;i<len;i++){
	        		int resId = ar.getResourceId(i, 0);
	        		if(resId > 0){
	        			mDigitTextDrawables[i] = res.getDrawable(resId);
	        		}
	        	}
	        	mDigitTextNum = len;
	        	ar.recycle();
	        }
	        digitDrawablesId = a.getResourceId(R.styleable.WtClock_digitext_drawables_1, -1);
	        if(digitDrawablesId > 0){
	        	Resources res = context.getResources();
	        	TypedArray ar = res.obtainTypedArray(digitDrawablesId);
	        	int len = ar.length();
	        	mDigitTextDrawables_1 = new Drawable[len];
	        	for(int i=0;i<len;i++){
	        		int resId = ar.getResourceId(i, 0);
	        		if(resId > 0){
	        			mDigitTextDrawables_1[i] = res.getDrawable(resId);
	        		}
	        	}
	        	//mDigitTextNum = len;
	        	ar.recycle();
	        }
	        mDigitDrawablesIdx = 0;
        }
        
        a.recycle();
    }
        
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if(isPointerStyle()){
        	canvas.save();
        	int fullAngle = (mClockMaxAngle > mClockMinAngle) ? (mClockMaxAngle-mClockMinAngle) : (360-mClockMinAngle+mClockMaxAngle);
        	float deltaAngle = (float)mValue/ (mValueMax-mValueMin+1) * fullAngle;
        	float currAngle = mClockReverse ? (mClockMaxAngle - deltaAngle) : (mClockMinAngle + deltaAngle);
            canvas.rotate(currAngle, mPointerCenterX, mPointerCenterY);
        	mPointerDrawable.draw(canvas);
        	canvas.restore();
        }else if(isDigitTextStyle()){        	
        	canvas.drawText(String.format("%02d", mValue), mDigitTextX, mDigitTextY, mDigitTextPaint);
        }else if(isDigitDrawableStyle()){
        	int offsetX = mDigitTextX;
        	int value1 = mValue/mDigitTextNum;
        	Drawable dr = mDigitTextDrawables[value1];
        	if(dr != null){
        		int w = dr.getIntrinsicWidth();
        		int h = dr.getIntrinsicHeight();
        		dr.setBounds(offsetX, mDigitTextY, offsetX+w, mDigitTextY+h);
        		dr.draw(canvas);
        		offsetX += w;
        	}
        	
        	int value2 = mValue%mDigitTextNum;
        	dr = mDigitTextDrawables[value2];
        	if(dr != null){
        		int w = dr.getIntrinsicWidth();
        		int h = dr.getIntrinsicHeight();
        		dr.setBounds(offsetX, mDigitTextY, offsetX+w, mDigitTextY+h);
        		dr.draw(canvas);
        	}
        }else if(iImaegsDrawableStyle()){
        	int segment = (mValueMax-mValueMin+1)/mDigitTextNum;
        	int index = mValue/segment;
        	if(index >= mDigitTextNum){
        		index = mDigitTextNum-1;
        	}
        	Drawable dr = mDigitDrawablesIdx == 1 ? mDigitTextDrawables_1[index] : mDigitTextDrawables[index];
        	if(dr != null){
        		int w = dr.getIntrinsicWidth();
        		int h = dr.getIntrinsicHeight();
        		dr.setBounds(mDigitTextX, mDigitTextY, mDigitTextX+w, mDigitTextY+h);
        		dr.draw(canvas);
        	}
        }
    }    
    
    public void setValue(int value){
    	mValue = value;
    	invalidate();
    }
    
    public void setDigitDrawableIdx(int index){
    	mDigitDrawablesIdx = index;
    	invalidate();
    }
    
    public boolean isPointerStyle(){
    	return (mClockStyle == CLOCK_STYLE_POINTER && mPointerDrawable != null && mPointerCenterX > -1 && mPointerCenterY > -1);
    }
    
    private boolean isDigitTextStyle(){
    	return (mClockStyle == CLOCK_STYLE_DIGIT && mDigitTextDrawables == null);
    }
    
    private boolean isDigitDrawableStyle(){
    	return (mClockStyle == CLOCK_STYLE_DIGIT && mDigitTextDrawables != null);
    }
    
    private boolean iImaegsDrawableStyle(){
    	return (mClockStyle == CLOCK_STYLE_IMAGES && mDigitTextDrawables != null);
    }
}
