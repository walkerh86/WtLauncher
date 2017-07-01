package com.cj.wtlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ListView;

public class StyleSettingActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initGalleryStyle();		
		//initListStyle();
	}	
		
	
	private void initListStyle(){
		setContentView(R.layout.style_setting_activity);
		
		CharSequence[] items = this.getResources().getTextArray(R.array.menu_style_items);
		ListView listView = (ListView)findViewById(R.id.list_view);
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_list_item_single_choice,android.R.id.text1,items);
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setItemChecked(getCheckedPostion(), true);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				saveStyleSetting(position);
			}
		});
	}
	
	private void saveStyleSetting(int styleId){
		SharedPreferences settings = getSharedPreferences("setting", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("menu_style", styleId);
		editor.commit();
	}
	
	public int getCheckedPostion(){
		SharedPreferences settings = getSharedPreferences("setting", 0);		
		int position = 0;
		int styleId = settings.getInt("menu_style", MenuSettings.MENU_STYLES[0].mStyleId);
		for(int i=0;i<MenuSettings.MENU_STYLES.length;i++){
			if(styleId == MenuSettings.MENU_STYLES[i].mStyleId){
				position = i;
				break;
			}
		}
		return position;
	}
	
	private void initGalleryStyle(){
		setContentView(R.layout.activity_choose_clock);
		
		Gallery gallery = (Gallery)findViewById(R.id.myGallery);
		/*
		int[] iconIds = new int[GALLERY_ITEMS.length];
		int[] titleIds = new int[GALLERY_ITEMS.length];
		for(int i=0;i<GALLERY_ITEMS.length;i++){
			titleIds[i] = GALLERY_ITEMS[i].mTitleId;
			iconIds[i] = GALLERY_ITEMS[i].mThumbImageId;
		}
		*/
		int[] iconIds = new int[MenuSettings.MENU_STYLES.length];
		int[] titleIds = new int[MenuSettings.MENU_STYLES.length];
		for(int i=0;i<MenuSettings.MENU_STYLES.length;i++){
			titleIds[i] = 0;
			iconIds[i] = MenuSettings.MENU_STYLES[i].mPreviewId;
		}
		
		HorizontalListViewAdapter adapter = new HorizontalListViewAdapter(this, titleIds, iconIds);
		gallery.setAdapter(adapter);
		gallery.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				MenuSettings.MenuStyle menuStyle= MenuSettings.MENU_STYLES[position];
				saveStyleSetting(menuStyle.mStyleId);
				
				StyleSettingActivity.this.finish();
				StyleSettingActivity.this.overridePendingTransition(0, R.anim.exit_anim);
			}
		});
		gallery.setSelection(getCheckedPostion());
	}
}
