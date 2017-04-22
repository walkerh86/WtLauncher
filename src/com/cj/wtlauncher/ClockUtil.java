package com.cj.wtlauncher;

public class ClockUtil{
	public static final ClockSet mWtClock1 = new ClockSet(R.layout.wt_clock_1_layout, R.string.clock_title_3, R.drawable.wt_clock_1_preview);
	public static final ClockSet mWtClock2 = new ClockSet(R.layout.wt_clock_2_layout, R.string.clock_title_5, R.drawable.wt_clock_2_preview);
	public static final ClockSet mWtClock3 = new ClockSet(R.layout.wt_clock_3_layout, R.string.clock_title_5, R.drawable.wt_clock_3_preview);
	public static final ClockSet mWtClock4 = new ClockSet(R.layout.wt_clock_4_layout, R.string.clock_title_5, R.drawable.wt_clock_4_preview);
	public static final ClockSet[] mClockList = {mWtClock1,mWtClock2,mWtClock3,mWtClock4};
	
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

