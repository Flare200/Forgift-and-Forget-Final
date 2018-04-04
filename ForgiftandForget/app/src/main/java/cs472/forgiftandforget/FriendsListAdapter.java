package cs472.forgiftandforget;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Aaron on 2/15/2018.
 */

public class FriendsListAdapter extends BaseExpandableListAdapter {
	private List<String> headerList;
	private HashMap<String, List<String>> ideasList;
	private Context ctx;

	FriendsListAdapter(Context ctx, List<String> headerList, HashMap<String, List<String>> ideasList) {
		this.ctx = ctx;
		this.ideasList = ideasList;
		this.headerList = headerList;
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
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		String title = (String) this.getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.parent_layout, null);
		}
		TextView textView = (TextView) convertView.findViewById(R.id.headingItem);
		textView.setTypeface(null, Typeface.BOLD);
		textView.setText(title);

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		String title = (String) this.getChild(groupPosition, childPosition);
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.child_layout, null);
		}
		TextView textView = (TextView) convertView.findViewById(R.id.childItem);
		textView.setText(title);

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
