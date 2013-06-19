/**
 * Adapter for List of Items
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp.models;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudapp.api.model.CloudAppItem;
import com.tomasvitek.android.cloudapp.R;
//import com.tomasvitek.android.cloudapp.threads.ImageLoader;

public class Adapter extends ArrayAdapter<ListItem> {

	private final Context context;
	private final ArrayList<ListItem> values;
	//public ImageLoader imageLoader;

	public Adapter(Context context, ArrayList<ListItem> items) {
		super(context, R.layout.list_row, items);
		this.context = context;
		this.values = items;
		//imageLoader = new ImageLoader(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.list_row, null);
		}

		TextView name = (TextView) v.findViewById(R.id.name);
		TextView count = (TextView) v.findViewById(R.id.count);
		ImageView thumbnail = (ImageView) v.findViewById(R.id.thumbnail);

		Log.d(values.get(position).name, String.valueOf(values.get(position).viewCounter) + " ~ "
				+ values.get(position).thumbnailUrl);
		if (name != null)
			name.setText(values.get(position).name);
		if (count != null)
			count.setText(String.valueOf(values.get(position).viewCounter));
		// if (thumbnail != null && values.get(position).thumbnailUrl != null) {
		// imageLoader.DisplayImage(values.get(position).thumbnailUrl,
		// thumbnail);
		// }
		if (values.get(position).getItemType() == CloudAppItem.Type.AUDIO) {
			thumbnail.setImageDrawable(getContext().getResources().getDrawable(R.drawable.audio));
		} else if (values.get(position).getItemType() == CloudAppItem.Type.BOOKMARK) {
			thumbnail
					.setImageDrawable(getContext().getResources().getDrawable(R.drawable.bookmark));
		} else if (values.get(position).getItemType() == CloudAppItem.Type.IMAGE) {
			thumbnail
					.setImageDrawable(getContext().getResources().getDrawable(R.drawable.photo));
		} else if (values.get(position).getItemType() == CloudAppItem.Type.VIDEO) {
			thumbnail.setImageDrawable(getContext().getResources().getDrawable(R.drawable.video));
		} else {
			thumbnail.setImageDrawable(getContext().getResources().getDrawable(R.drawable.unknown));
		}

		return v;
	}

}