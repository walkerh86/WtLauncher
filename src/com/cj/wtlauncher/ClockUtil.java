package com.cj.wtlauncher;

public class ClockUtil{
	public static final ClockSet mWtClock1 = new ClockSet(R.layout.wt_clock_1_layout, R.string.clock_title_3, R.drawable.wt_clock_1_preview);
	public static final ClockSet mWtClock2 = new ClockSet(R.layout.wt_clock_2_layout, R.string.clock_title_5, R.drawable.wt_clock_2_preview);
	public static final ClockSet mWtClock3 = new ClockSet(R.layout.wt_clock_3_layout, R.string.clock_title_5, R.drawable.wt_clock_3_preview);
	public static final ClockSet mWtClock4 = new ClockSet(R.layout.wt_clock_4_layout, R.string.clock_title_5, R.drawable.wt_clock_4_preview);
	public static final ClockSet mWtClock5 = new ClockSet(R.layout.wt_clock_5_layout, R.string.clock_title_5, R.drawable.wt_clock_5_preview);
	public static final ClockSet mWtClock6 = new ClockSet(R.layout.wt_clock_6_layout, R.string.clock_title_5, R.drawable.wt_clock_6_preview);
	public static final ClockSet mWtClock8 = new ClockSet(R.layout.wt_clock_8_layout, R.string.clock_title_5, R.drawable.wt_clock_8_preview);
	public static final ClockSet mWtClock9 = new ClockSet(R.layout.wt_clock_9_layout, R.string.clock_title_5, R.drawable.wt_clock_9_preview);
	public static final ClockSet mWtClock10 = new ClockSet(R.layout.wt_clock_10_layout, R.string.clock_title_5, R.drawable.wt_clock_10_preview);
	public static final ClockSet mWtClock11 = new ClockSet(R.layout.wt_clock_11_layout, R.string.clock_title_5, R.drawable.wt_clock_11_preview);
	public static final ClockSet[] mClockList = {
			mWtClock1,mWtClock2,mWtClock3,mWtClock4,mWtClock5,
			mWtClock6,mWtClock8,mWtClock9,mWtClock10};
	
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

