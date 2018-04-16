package cs472.forgiftandforget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private static final Object friendLock = new Object();
	final ArrayList<Friend> friends = new ArrayList<>();
	final ArrayList<ArrayList<Event>> friendsEvents = new ArrayList<>();
	private static int iterationCount;
	DatabaseReference friendsListReference;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_list);
		this.setTitle("Friends List");

		database = new Database();
		friendsListReference = Friend.GetFriendsListsReference().child(Database.GetCurrentUID());

		friendList = (ExpandableListView) findViewById(R.id.listView);
		friendList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int totalPosition, long position) {
				synchronized (friendList) {
					ExpandableListView.getPackedPositionType(position);
					int type = ExpandableListView.getPackedPositionType(position);
					int friendPosition = ExpandableListView.getPackedPositionGroup(position);

					switch (type) {
						case ExpandableListView.PACKED_POSITION_TYPE_GROUP: {
							//if friend long clicked
							// ToDo send to edit friend
							Intent friendEditIntent = new Intent(ctx, FriendCreation.class);
							// set option so the activity knows this is an edit, instead of create
							friendEditIntent.putExtra("option", 1);
							// send friendID to edit
							friendEditIntent.putExtra("friendID", friends.get(friendPosition).friendID);
							finish();
							startActivity(friendEditIntent);

							return true;
						}
						case ExpandableListView.PACKED_POSITION_TYPE_CHILD: {
							// if event item clicked
							// ToDo have an edit event option maybe? will need to verify they didnt click ~add event~

							return true;
						}
					}
					return false;
				}
			}
		});

		final List<String> headerList = new ArrayList<String>();
		final HashMap<String, List<String>> eventList = new HashMap<String, List<String>>();

		synchronized (friendLock) {
			// single Event, on create, to populate a list of friends(myList)
			friendsListReference.addListenerForSingleValueEvent(new ValueEventListener() {

				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					synchronized (friendLock) {
						Iterable<DataSnapshot> children = dataSnapshot.getChildren();

						// add each of the users friends to the list friends
						friends.clear();
						for (DataSnapshot child : children) {
							friends.add(child.getValue(Friend.class));
						}

						//set the iterationCount to 0. This will be increased each time one Friend's EventList is populated.
						iterationCount = 0;
						final int goalIterations = friends.size() - 1;

						//for each Friend in the list get all events for the Friend, add them to ExpandableList
						for (final Friend thisFriend : friends) {
							//getting reference to specific Event list
							DatabaseReference thisRef = FirebaseDatabase.getInstance().getReference("EventLists").child(thisFriend.eventListID);
							thisRef.addListenerForSingleValueEvent(new ValueEventListener() {
								@Override
								public void onDataChange(DataSnapshot dataSnapshot) {
									synchronized (friendLock) {
										Iterable<DataSnapshot> Children = dataSnapshot.getChildren();

										// Get all events and adding to the list events,
										// Except for the null Event (which is added on Friend creation to hold Database spot)
										ArrayList<Event> events = new ArrayList<Event>();
										for (DataSnapshot Child : Children) {
											Event thisEvent = Child.getValue(Event.class);
											if (thisEvent != null) {
												events.add(thisEvent);
											}
										}
										// add list of events to the list friendsEvents
										friendsEvents.add(events);

										// insert Friend name into top level expandableList
										headerList.add(thisFriend.name);

										// create the sublist for the above added Friend
										List<String> subList = new ArrayList<String>();
										for (Event subEvent : events) {
											subList.add(subEvent.name);
										}
										subList.add("~Click to add an Event~");
										subList.add("~View Gifted List~");

										// add sublist into lower level expandableList
										eventList.put(thisFriend.name, subList);

										//If each event list for each friend is populated (aka, this is
										if (iterationCount == goalIterations) {
											// done loading friends
											myAdapter = new FriendsListAdapter(ctx, headerList, eventList);
											friendList.setAdapter(myAdapter);
										}

										//increment the added EventList count
										iterationCount++;
									}
								}

								@Override
								public void onCancelled(DatabaseError databaseError) { }
							});
						}
						if(friends.size() == 0){
							// no friends, force user to add first friend
							Toast.makeText(getApplicationContext(), "No Friends found, Add a friend to continue", Toast.LENGTH_LONG).show();
							addFriend();
						}
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError) { }

			});

			//Handler to add events to the list
			friendList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
					synchronized (friendLock) {
						if (childPosition == myAdapter.getChildrenCount(groupPosition) - 2) {
							addEvent(friends.get(groupPosition));
						}else if(childPosition == myAdapter.getChildrenCount(groupPosition) - 1) {
							// ToDo send to gifted page (pass friend ID through)
						}else{
							// send eventID to IdeaPage
							openIdeaPage(friendsEvents.get(groupPosition).get(childPosition).eventID);
						}
					}

					return false;
				}
			});
		}
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
		// send option so activity knows this is an add, and not edit
		friendIntent.putExtra("option", 0);
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