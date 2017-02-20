package com.cj.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class PageIndicator extends View{
	private int mPageNum;
	private static final int INDICATOR_ITEM_GAP = 4;
	private int mItemWidth;
	private int mItemGap;
	private Paint mPaint;
	private int mPageCurr;
	
	public PageIndicator(Context context) {
        this(context,null);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    
    public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mPaint = new Paint();
        mPaint.setColor(0xFFFFFFFF);
        
        //mPageNum = 8;
        //mPageCurr =3;
    }
    
    public void setPageNum(int num){
    	mPageNum = num;
    }
    
    public void setPageCurr(int index){
    	mPageCurr = index;
    	invalidate();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	Log.i("hcj", "onMeasure mPageNum="+mPageNum);
    	if(mPageNum <= 0){
    		return;
    	}
    	int width = getMeasuredWidth();
    	int rest = width%mPageNum;
    	int itemW = width/mPageNum;
    	mItemWidth = itemW - 8;
    	mItemGap = (8*mPageNum+rest)/(mPageNum-1);
    	//mItemWidth = (width-INDICATOR_ITEM_GAP*(mPageNum-1))/mPageNum;
    	//Log.i("hcj", "width="+width+",mItemWidth="+mItemWidth);
    	Log.i("hcj", "onMeasure width="+width);
    }
    
    @Override
    public void onDraw(Canvas canvas){
    	Log.i("hcj", "onDraw");
    	super.onDraw(canvas);  	
    	
    	int left = 0;
    	int height = getHeight();
    	for(int i=0;i<mPageNum;i++){
    		mPaint.setColor((i == mPageCurr) ? 0xFF982f98 : 0xFF134671);
    		canvas.drawRect(left, 0, left+mItemWidth, height, mPaint);
    		left += mItemWidth+mItemGap;
    	}
    }
}
