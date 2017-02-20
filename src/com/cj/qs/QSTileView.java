package com.cj.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class QSTileView extends ImageView{
	public QSTileView(Context context) {
        this(context,null);
    }

    public QSTileView(Context context, AttributeSet attrs) {
        this(context, attrs,-1);
    }
    
    public QSTileView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    } 
}
