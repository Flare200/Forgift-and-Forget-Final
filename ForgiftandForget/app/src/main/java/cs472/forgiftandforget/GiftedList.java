package cs472.forgiftandforget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import cs472.forgiftandforget.DatabaseClasses.Database;
import cs472.forgiftandforget.DatabaseClasses.Friend;
import cs472.forgiftandforget.DatabaseClasses.Gift;

public class GiftedList extends AppCompatActivity {

	String friendID;
	ListView giftedListView;
	CopyOnWriteArrayList<Gift> gifts = new CopyOnWriteArrayList<>();
	CopyOnWriteArrayList<String> giftIDS = new CopyOnWriteArrayList<>();
	DatabaseReference giftedListReference;
	DatabaseReference friendReference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gifted_list);

		friendID = getIntent().getStringExtra("friendID");
		final ArrayList<String> headerList = new ArrayList<String>();

		giftedListView = (ListView) findViewById(R.id.giftedList);
		//setTitle(eventName);
		giftedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent giftIntent = new Intent(GiftedList.this, GiftedIdeas.class);
				giftIntent.putExtra("giftID", giftIDS.get(position));
				giftIntent.putExtra("friendID", friendID);
				startActivity(giftIntent);
			}
		});
		giftedListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				// ToDo ask to delete from gifted list dialog
				return true;
			}
		});

		friendReference = Friend.GetFriendsListsReference().child(Database.GetCurrentUID())
				.child(friendID).child("name");
		friendReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				setTitle(dataSnapshot.getValue().toString().trim() + "'s Gifted List");
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
		giftedListReference = Gift.GetGiftedListReference().child(Database.GetCurrentUID())
				.child(friendID);
		giftedListReference.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				Iterable<DataSnapshot> children = dataSnapshot.getChildren();

				// for each entry in gift list, add to gifts, except for blank gift
				for (DataSnapshot Child : children)
				{
					String giftID = Child.getKey();
					giftIDS.add(giftID);
					headerList.add(Child.child("name").getValue().toString().trim());
				}
				ArrayAdapter<String> giftedListAdapter = new ArrayAdapter<String>(GiftedList.this, R.layout.idea_layout,headerList);
				giftedListView.setAdapter(giftedListAdapter);
			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{

			}
		});

	}
}
