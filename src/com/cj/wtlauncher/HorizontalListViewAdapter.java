package com.cj.wtlauncher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HorizontalListViewAdapter extends BaseAdapter{  
	Bitmap iconBitmap;
	private Context mContext;
	private int[] mIconIDs;
	private LayoutInflater mInflater;
	private int[] mTitleIds;
	private int selectIndex = -1;
  
	public HorizontalListViewAdapter(Context paramContext, int[] paramArrayOfInt1, int[] paramArrayOfInt2){
		this.mContext = paramContext;
		this.mIconIDs = paramArrayOfInt2;
		this.mTitleIds = paramArrayOfInt1;
		this.mInflater = ((LayoutInflater)this.mContext.getSystemService("layout_inflater"));
	}
  
	private Bitmap getPropThumnail(int paramInt){
		return ThumbnailUtils.extractThumbnail(BitmapUtil.drawableToBitmap(this.mContext.getResources().getDrawable(paramInt)), 
			this.mContext.getResources().getDimensionPixelOffset(R.dimen.thumnail_default_width), 
			this.mContext.getResources().getDimensionPixelSize(R.dimen.thumnail_default_height));
	}
  
	public int getCount(){
		return this.mIconIDs.length;
	}

	public Object getItem(int paramInt){
		return Integer.valueOf(paramInt);
	}

	public long getItemId(int paramInt){
		return paramInt;
	}
  
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder;
		
		if (convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.choose_clock_horizontal_list_item, null);
			holder.mImage = (ImageView)convertView.findViewById(R.id.img_list_item);
			holder.mTitle = (TextView)convertView.findViewById(R.id.text_list_item);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		android.util.Log.i("hcj","getView iconId="+mIconIDs[position]+",position="+position);

		holder.mTitle.setText(this.mContext.getResources().getString(this.mTitleIds[position]));
		this.iconBitmap = getPropThumnail(this.mIconIDs[position]);
		holder.mImage.setImageBitmap(this.iconBitmap);
		
		convertView.setSelected(position == this.selectIndex);
		
		return convertView;
	}
  
	private static class ViewHolder{
		private ImageView mImage;
		private TextView mTitle;
	}
}

