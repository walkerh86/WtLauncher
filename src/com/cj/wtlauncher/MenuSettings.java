package com.cj.wtlauncher;

public class MenuSettings {
	public static final int MENU_STYLE_GRID = 0;
	public static final int MENU_STYLE_V = 1;
	public static final int MENU_STYLE_H = 2;
	
	public static class MenuStyle{
		public int mStyleId;
		public int mPreviewId;
		public int mWallpaperId;
		public MenuStyle(int styleId, int previewId, int wallpaperId){
			mStyleId = styleId;
			mPreviewId = previewId;
			mWallpaperId = wallpaperId;
		}
	}	
	
	public static final MenuStyle STYLE_GRID = new MenuStyle(MENU_STYLE_GRID,R.drawable.menu_style_grid,R.drawable.bg_app);
	public static final MenuStyle STYLE_V = new MenuStyle(MENU_STYLE_V,R.drawable.menu_style_list,R.drawable.bg_app);
	public static final MenuStyle STYLE_H = new MenuStyle(MENU_STYLE_H,R.drawable.menu_style_list_h,R.drawable.wallpaper_00);
	public static final MenuStyle[] MENU_STYLES = new MenuStyle[]{STYLE_H,STYLE_GRID};

	public static int getMenuWallpaperId(int styleId){
		int wallpaperId = 0;
		for(int i=0;i<MENU_STYLES.length;i++){
			if(MENU_STYLES[i].mStyleId == styleId){
				wallpaperId = MENU_STYLES[i].mWallpaperId;
				break;
			}
		}
		return wallpaperId;
	}
}
