package com.william.findMyfamily;

import com.william.fmfCommon.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import android.widget.ImageView;
import android.widget.TextView;

/*
 * This is an Adapter to list user
 */
public class UserItemListAdapter extends ArrayAdapter<FMFUserData> implements ListAdapter
{
    Context context;
    int layoutResourceId;
    String serverTime = null;

    public UserItemListAdapter(Context context)//, int layoutResourceId, ArrayList<FMPUserData> data) {
    {
    	super(context, R.layout.user_dialoglistrow, Tools.mapUserList); 
    	this.context = context;
    }
    
   	ViewHolder holder;
	Drawable icon;

	class ViewHolder {
		ImageView icon;
		TextView title;
	}

	public View getView(int position, View convertView,	ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) context
		.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.user_dialoglistrow, null);

			holder = new ViewHolder();
			
			FMCLocationData userData = Tools.mapUserList.get(position).getFmpLocationData();
			Bitmap bmap = Tools.getContactPhoto(context, userData.getPhoneNumber());
			System.out.println("UserItemListAdapter: at phone:"+userData.getPhoneNumber());
			Bitmap smallerBitmap = Bitmap.createScaledBitmap(bmap, 200, 200, true);	
			holder.icon  = (ImageView)convertView
			.findViewById(R.id.icon);
			if (smallerBitmap != null)
			{
				System.out.println("UserItemListAdapter: set bitmap at phone:"+userData.getPhoneNumber());
				holder.icon.setImageBitmap(smallerBitmap);
			}

			holder.title = (TextView) convertView
			.findViewById(R.id.title);
			convertView.setTag(holder);
		} else {
			// view already defined, retrieve view holder
			holder = (ViewHolder) convertView.getTag();
		}       

		//Drawable drawable = context.getResources().getDrawable(R.drawable.icon); //this is an image from the drawables folder

		holder.title.setText(Tools.mapUserList.get(position).getUserName()+
				"\n"+Tools.mapUserList.get(position).getFmpLocationData().getPhoneNumber());

		return convertView;
	}
}
    	