package com.cj.wtlauncher;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class BitmapUtil{
	public static Bitmap drawableToBitmap(Drawable paramDrawable){
		int i = paramDrawable.getIntrinsicWidth();
		int j = paramDrawable.getIntrinsicHeight();
		Bitmap.Config config;
		if (paramDrawable.getOpacity() != -1) {
			config = Bitmap.Config.ARGB_8888;
		}else{
			config = Bitmap.Config.RGB_565;
		}
		Bitmap bmp = Bitmap.createBitmap(i, j, config);
		Canvas localCanvas = new Canvas(bmp);
		paramDrawable.setBounds(0, 0, i, j);
		paramDrawable.draw(localCanvas);
		return bmp;
	}
}