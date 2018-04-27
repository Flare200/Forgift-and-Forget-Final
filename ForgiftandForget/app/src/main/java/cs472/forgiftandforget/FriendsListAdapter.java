package cs472.forgiftandforget;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cs472.forgiftandforget.DatabaseClasses.Friend;


/**
 * Created by Aaron on 2/15/2018.
 */

public class FriendsListAdapter extends BaseExpandableListAdapter
{
	private List<String> headerList;
	private HashMap<String, List<String>> ideasList;
	private Context ctx;
	private ArrayList<Friend> friends = new ArrayList<>();

	FriendsListAdapter(Context ctx, List<String> headerList, HashMap<String, List<String>> ideasList, ArrayList<Friend> friends)
	{
		this.ctx = ctx;
		this.ideasList = ideasList;
		this.headerList = headerList;
		this.friends = friends;
	}

	@Override
	public int getGroupCount() {
		return headerList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return ideasList.get(headerList.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return headerList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return ideasList.get(headerList.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
			String title = (String) this.getGroup(groupPosition);
			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.parent_layout, null);
			}
			TextView textView = (TextView) convertView.findViewById(R.id.headingItem);
			textView.setTypeface(null, Typeface.BOLD);
			textView.setText(title);
			ImageView contactImage = (ImageView) convertView.findViewById(R.id.imageView);
			contactImage.setImageURI(friends.get(groupPosition).contactImage);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		String title = (String) this.getChild(groupPosition, childPosition);
		TextView textView;
		LayoutInflater layoutInflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if(childPosition < getChildrenCount(groupPosition) - 2){
			convertView = layoutInflater.inflate(R.layout.child_layout, null);
			textView = (TextView) convertView.findViewById(R.id.childItem);
			textView.setText(title);
		}else {
			convertView = layoutInflater.inflate(R.layout.child_button_layout, null);
			textView = (TextView) convertView.findViewById(R.id.listButtonText);
			textView.setText(title);
			if(childPosition == getChildrenCount(groupPosition) - 2){
				textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_event_icon, 0, 0, 0);
			}
			else{
				textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gifted_icon, 0, 0, 0);
			}
		}


		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
