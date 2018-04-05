package cs472.forgiftandforget;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;

import cs472.forgiftandforget.DatabaseClasses.Database;
import cs472.forgiftandforget.DatabaseClasses.Event;
import cs472.forgiftandforget.DatabaseClasses.Friend;

public class FriendList extends AppCompatActivity {
	private Context ctx = this;
	private ExpandableListView friendList;
	private FriendsListAdapter myAdapter;

	Database database;
	CopyOnWriteArrayList<Friend> friends = new CopyOnWriteArrayList<>(); //this is accessed by multiple threads.
	CopyOnWriteArrayList<ArrayList<Event>> friendsEvents = new CopyOnWriteArrayList<>();
	DatabaseReference friendsListReference;
	//StorageReference storageRef;


	static final int ADD_FRIEND_REQUEST = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_list);

		database = new Database();
		friendsListReference = Friend.GetFriendsListsReference().child(Database.GetCurrentUID());
		friendList = (ExpandableListView) findViewById(R.id.listView);
		//storageRef  = FirebaseStorage.getInstance().getReference().child("contactImages");
		final List<String> headerList = new ArrayList<String>();
		final HashMap<String, List<String>> eventList = new HashMap<String, List<String>>();

		// single Event, on create, to populate a list of friends(myList)
		friendsListReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				Iterable<DataSnapshot> children = dataSnapshot.getChildren();

				// add each of the users friends to the list friends
				for (DataSnapshot child : children) {
					Friend newFriend = child.getValue(Friend.class);
					friends.add(newFriend);
				}

				//for each Friend in the list get all events for the Friend, add them to ExpandableList
				for (int i = 0; i < friends.size(); i++) {
					final Friend thisFriend = friends.get(i);
					final int loc = i;
					//getting reference to specific Event list
					DatabaseReference thisRef = FirebaseDatabase.getInstance().getReference("EventLists").child(thisFriend.eventListID);
					thisRef.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(DataSnapshot dataSnapshot) {
							Iterable<DataSnapshot> Children = dataSnapshot.getChildren();
							ArrayList<Event> events = new ArrayList<Event>();
							// getting all events and adding to the list events
							// Except for the null Event which is added on Friend creation(to hold Database spot)
							for (DataSnapshot Child : Children) {
								Event thisEvent = Child.getValue(Event.class);
								if (thisEvent != null && !thisEvent.eventID.equals("null")) {
									events.add(thisEvent);
								}
							}
							// add list of events to the list friendsEvents
							friendsEvents.add(events);

							// insert Friend name into top level expandableList
							Friend insert = friends.get(loc);
							headerList.add(insert.name);


							// create the sublist for the above added Friend
							List<String> subList = new ArrayList<String>();
							// add each Event to the sublist
							if (friendsEvents.get(loc).size() == 0) {
								subList.add("~Click to add an Event~");
							} else {
								for (int j = 0; j < friendsEvents.get(loc).size(); j++) {
									subList.add(friendsEvents.get(loc).get(j).name);
								}
								subList.add("~Click to add an Event~");
							}
							// add sublist into lower level expandableList
							eventList.put(headerList.get(loc), subList);

							// if all friends+events have been loaded, display expandableList
							if (loc == friends.size() - 1) {
								myAdapter = new FriendsListAdapter(ctx, headerList, eventList);
								friendList.setAdapter(myAdapter);
							}


						}

						@Override
						public void onCancelled(DatabaseError databaseError) {

						}
					});
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

		//Handler to add events to the list
		friendList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if (childPosition == myAdapter.getChildrenCount(groupPosition) - 1)
					addEvent(friends.get(groupPosition));
				else {
					// send eventID to IdeaPage
					openIdeaPage(friendsEvents.get(groupPosition).get(childPosition).eventID);
				}

				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.friendslist_menu_resource, menu);
		getMenuInflater().inflate(R.menu.block_menu_resource, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.action_add:
				addFriend();
				return true;
			case R.id.action_Logout:
				userLogOut();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void addFriend() {
		Intent friendIntent = new Intent(ctx, FriendCreation.class);
		finish();
		startActivity(friendIntent);
	}

	private void addEvent(Friend currentFriend) {
		Intent eventIntent = new Intent(ctx, EventCreation.class);
		eventIntent.putExtra("ELID", currentFriend.eventListID);
		eventIntent.putExtra("FID", currentFriend.friendID);
		finish();
		startActivity(eventIntent);
	}

	private void openIdeaPage(String eventID) {
		Intent ideaIntent = new Intent(ctx, IdeaPage.class);
		ideaIntent.putExtra("eventID", eventID);
		finish();
		startActivity(ideaIntent);
	}

	private void userLogOut() {
		Database.GetInstance().signOut();
		finish();
		Intent intent = new Intent(ctx, MainActivity.class);
		startActivity(intent);
	}
}