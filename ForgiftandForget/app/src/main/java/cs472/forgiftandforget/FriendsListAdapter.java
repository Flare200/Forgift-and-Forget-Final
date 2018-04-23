package cs472.forgiftandforget;

import android.content.Context;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import cs472.forgiftandforget.DatabaseClasses.Friend;

/**
 * Created by Aaron on 2/15/2018.
 */

public class FriendsListAdapter extends BaseExpandableListAdapter
{
	private List<String> headerList;
	private HashMap<String, List<String>> ideasList;
	private Context ctx;
	private List<Friend> friends;
	private StorageReference storageReference;

	FriendsListAdapter(Context ctx, List<String> headerList, HashMap<String, List<String>> ideasList, List<Friend> friends)
	{
		this.ctx = ctx;
		this.ideasList = ideasList;
		this.headerList = headerList;
		this.friends = friends;
		storageReference = FirebaseStorage.getInstance().getReference("contactImages");
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
		if(convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.parent_layout, null);
		}
		final View convertView2 = convertView;

		TextView textView = (TextView) convertView.findViewById(R.id.headingItem);
		textView.setTypeface(null, Typeface.BOLD);
		textView.setText(title);
		try {
			File contactImageFile = File.createTempFile("images" + groupPosition, "jpg");
			final Uri contactImageUri = Uri.parse(contactImageFile.getAbsolutePath());
			storageReference.child(friends.get(groupPosition).imageID).getFile(contactImageFile)
					.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
						@Override
						public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
							//ImageView contactImage = (ImageView) convertView2.findViewById(R.id.imageView);
							//contactImage.setImageURI(contactImageUri);
							// ToDo move image loading to once, on adapter creation.
						}
					}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					// no image, or image download failed
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
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
