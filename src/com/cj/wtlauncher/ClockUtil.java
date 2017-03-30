package com.cj.wtlauncher;

public class ClockUtil{
	public static final ClockSet mMyClock0 = new ClockSet(R.layout.clock_layout_style3, R.string.clock_title_3, R.drawable.my_clock_3_0preview);
	public static final ClockSet mMyClock5 = new ClockSet(R.layout.clock_layout_style5, R.string.clock_title_5, R.drawable.my_clock_5_0preview);
	public static final ClockSet[] mClockList = {mMyClock0,mMyClock5};
	
	public static class ClockSet{
		public int mThumbImageId;
		public int mTitleId;
		public int mViewId;

		public ClockSet(int paramInt1, int paramInt2, int paramInt3){
			this.mViewId = paramInt1;
			this.mTitleId = paramInt2;
			this.mThumbImageId = paramInt3;
		}
	}
}

