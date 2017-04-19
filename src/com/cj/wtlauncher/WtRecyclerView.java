package com.cj.wtlauncher;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class WtRecyclerView extends RecyclerView{
	private int mTouchSlop;
	
	public WtRecyclerView(Context context) {
		this(context,null);
	}

	public WtRecyclerView(Context context, AttributeSet attrs) {
		this(context, attrs,-1);
	}

	public WtRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setChildrenDrawingOrderEnabled(true);
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = (int)(vc.getScaledTouchSlop()*0.8f);
	}
	
	
	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
	       if(!mAdjustDrawingOrder){
		   	return super.getChildDrawingOrder(childCount, i);
	       }
		int count = getChildCount();
		
		int centerX = getWidth()/2;
		View firstChild = getChildAt(0);
		int childWidth = firstChild.getWidth();
		int childStart = this.getLayoutManager().getDecoratedLeft(firstChild);
		//Log.i("hcj", "onScrollChanged count="+count+",childStart="+childStart);
		int centerIdx = count/2;
		int minDeltaX = centerX;
		for(int j=0;j<count;j++){
			View view = getChildAt(j);
			int childCenterX = this.getLayoutManager().getDecoratedLeft(view)+childWidth/2;
			int deltaX = Math.abs(childCenterX-centerX);
			if(deltaX < minDeltaX){
				minDeltaX = deltaX;
				centerIdx = j;
			}
		}
		//Log.i("hcj", "getChildDrawingOrder centerIdx ="+centerIdx);
		int drawIndex = i;
		if(i == centerIdx){
			drawIndex++;
		}else if(i == centerIdx+1){
			drawIndex--;
		}
		//Log.i("hcj", "getChildDrawingOrder centerIdx ="+centerIdx+",i="+i+",drawIndex="+drawIndex);
        return drawIndex;
    }

	private boolean mAdjustDrawingOrder;
	public void setAdjustDrawingOrder(boolean adjust){
		mAdjustDrawingOrder = adjust;
	}
	/*
	private int mInitTouchX;
	@Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
		boolean skipTouch = false;
		final int action = MotionEventCompat.getActionMasked(e);
		switch (action) {
        	case MotionEvent.ACTION_DOWN:
        		mInitTouchX = (int)e.getX();
        		break;
        	case MotionEvent.ACTION_MOVE:
        		int touchX = (int)e.getX();
        		if(touchX > mInitTouchX){
        			skipTouch = true;
        		}
        		break;
		}
		if(skipTouch){
			return false;
		}
		return super.onInterceptHoverEvent(e);
	}*/
}
