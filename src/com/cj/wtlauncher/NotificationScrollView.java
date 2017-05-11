package com.cj.wtlauncher;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ScrollView;

public class NotificationScrollView extends ScrollView implements SwipeHelper.Callback{
  private Callback mCallback;
  private GestureDetector mGestureDetector;
  protected int mLastScrollPosition;
  private FrameLayout mLayout;
  private SwipeHelper mSwipeHelper;
  
  public NotificationScrollView(Context paramContext, AttributeSet paramAttributeSet){  
    super(paramContext, paramAttributeSet, 0);
    this.mSwipeHelper = new SwipeHelper(0, this, getResources().getDisplayMetrics().density, ViewConfiguration.get(paramContext).getScaledPagingTouchSlop());
    this.mGestureDetector = new GestureDetector(paramContext, new YScrollDetector());
  }
  
  public boolean canChildBeDismissed(View paramView)
  {
    return true;
  }
  
  public void dismissChild(View paramView)
  {
    this.mSwipeHelper.dismissChild(paramView, 0.0F);
  }
  
  public View getChildAtPosition(MotionEvent paramMotionEvent)
  {
    return this;
  }
  
  public View getChildContentView(View paramView)
  {
    return paramView.findViewById(R.id.card_scroll_content);
  }
  
  public void onBeginDrag(View paramView)
  {
    requestDisallowInterceptTouchEvent(true);
  }
  
  public void onChildDismissed(View paramView)
  {
    View localView = getChildContentView(paramView);
    localView.setAlpha(1.0F);
    localView.setTranslationX(0.0F);
    if (this.mCallback != null) {
      this.mCallback.handleSwipe(paramView);
    }
  }
  
  protected void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    float f = getResources().getDisplayMetrics().density;
    this.mSwipeHelper.setDensityScale(f);
    f = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
    this.mSwipeHelper.setPagingTouchSlop(f);
  }
  
  public void onDragCancelled(View paramView) {}
  
  protected void onFinishInflate()
  {
    super.onFinishInflate();
    setScrollbarFadingEnabled(true);
    this.mLayout = ((FrameLayout)findViewById(R.id.card_background));
	Log.i("hcj.NotificationScrollView","onFinishInflate mLayout="+mLayout);
  }
  
  public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent)
  {
    return ((this.mSwipeHelper.onInterceptTouchEvent(paramMotionEvent)) || (super.onInterceptTouchEvent(paramMotionEvent))) && (this.mGestureDetector.onTouchEvent(paramMotionEvent));
  }
  
  protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
	Log.i("hcj.NotificationScrollView","onSizeChanged mLayout="+mLayout);
	if(mLayout == null){
		return;
	}
    LayoutTransition localLayoutTransition = this.mLayout.getLayoutTransition();
    if ((localLayoutTransition != null) && (localLayoutTransition.isRunning())) {
      return;
    }
    this.mLastScrollPosition = 0;
    post(new Runnable()
    {
      public void run()
      {
        LayoutTransition localLayoutTransition = NotificationScrollView.this.mLayout.getLayoutTransition();
        if ((localLayoutTransition == null) || (!localLayoutTransition.isRunning())) {
          NotificationScrollView.this.scrollTo(0, NotificationScrollView.this.mLastScrollPosition);
        }
      }
    });
  }
  
  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    return (this.mSwipeHelper.onTouchEvent(paramMotionEvent)) || (super.onTouchEvent(paramMotionEvent));
  }
  
  public void removeViewInLayout(View paramView)
  {
    dismissChild(paramView);
  }
  
  public void setCallback(Callback paramCallback)
  {
    this.mCallback = paramCallback;
  }
  
  public void setLayoutTransition(LayoutTransition paramLayoutTransition)
  {
    this.mLayout.setLayoutTransition(paramLayoutTransition);
  }
  
  public static abstract interface Callback
  {
    public abstract void handleSwipe(View paramView);
  }
  
  class YScrollDetector
    extends GestureDetector.SimpleOnGestureListener
  {
    YScrollDetector() {}
    
    public boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2)
    {
      return Math.abs(paramFloat2) > Math.abs(paramFloat1);
    }
  }
}
