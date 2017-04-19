package com.cj.wtlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class StyleSettingActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
	
	private void saveStyleSetting(int position){
		int style = MenuFragment.MENU_STYLE_GRID;
		if(position == 1){
			style = MenuFragment.MENU_STYLE_V;
		}
		SharedPreferences settings = getSharedPreferences("setting", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("menu_style", style);
		editor.commit();
	}
	
	public int getCheckedPostion(){
		SharedPreferences settings = getSharedPreferences("setting", 0);
		int style = settings.getInt("menu_style", MenuFragment.MENU_STYLE_GRID);
		int position = 0;
		if(style == MenuFragment.MENU_STYLE_V){
			position = 1;
		}
		return position;
	}
}
