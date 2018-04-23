package cs472.forgiftandforget;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
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
	static final int GALLERY = 1;
	Uri contactImageUri;
	ImageView contactImage;
	EditText contactName;
	View dialogView;

	Database database;
	private static final Object friendLock = new Object();
	final ArrayList<Friend> friends = new ArrayList<>();
	final ArrayList<ArrayList<Event>> friendsEvents = new ArrayList<>();
	private static int iterationCount;
	DatabaseReference friendsListReference;


	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
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
					int eventPosition = ExpandableListView.getPackedPositionChild(position);

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
							if(eventPosition < friendsEvents.get(friendPosition).size()){
								Intent eventEditIntent = new Intent(ctx, EventCreation.class);
								// set option so the activity knows this is an edit, instead of create
								eventEditIntent.putExtra("option", 1);
								// send event to edit
								eventEditIntent.putExtra("eventID", friendsEvents.get(friendPosition).get(eventPosition).eventID);
								eventEditIntent.putExtra("ELID", friends.get(friendPosition).eventListID);
								eventEditIntent.putExtra("FID", friends.get(friendPosition).friendID);
								finish();
								startActivity(eventEditIntent);
							}
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
						for (final Friend thisFriend : friends)
						{
							//getting reference to specific Event list
							DatabaseReference thisRef = FirebaseDatabase.getInstance().getReference("EventLists").child(thisFriend.eventListID);
							thisRef.addListenerForSingleValueEvent(new ValueEventListener()
							{
								@Override
								public void onDataChange(DataSnapshot dataSnapshot)
								{
									synchronized (friendLock)
									{
										Iterable<DataSnapshot> Children = dataSnapshot.getChildren();

										// Get all events and adding to the list events,
										// Except for the null Event (which is added on Friend creation to hold Database spot)
										ArrayList<Event> events = new ArrayList<Event>();
										for (DataSnapshot Child : Children)
										{
											Event thisEvent = Child.getValue(Event.class);
											if (thisEvent != null)
											{
												events.add(thisEvent);
											}
										}
										// add list of events to the list friendsEvents
										friendsEvents.add(events);

										// insert Friend name into top level expandableList
										headerList.add(thisFriend.name);

										// create the sublist for the above added Friend
										List<String> subList = new ArrayList<String>();
										for (Event subEvent : events)
										{
											subList.add(subEvent.name);
										}
										subList.add("Add Event");
										subList.add("Gifted List");

										// add sublist into lower level expandableList
										eventList.put(thisFriend.name, subList);

										//If each event list for each friend is populated (aka, this is

										if (iterationCount == goalIterations) {
											// done loading friends
											myAdapter = new FriendsListAdapter(ctx, headerList, eventList, friends);
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
							openIdeaPage(friendsEvents.get(groupPosition).get(childPosition).eventID,friendsEvents.get(groupPosition).get(childPosition).name,friends.get(groupPosition));
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

	// add friend now done completely through a dialoge
	private void addFriend()
	{
		// create a new dialog, set the layout
		AlertDialog.Builder addFriendDialog = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		final View dialogView = inflater.inflate(R.layout.add_friend_dialogue, null);

		// get reference to the dialoge image and edit text
		contactImage = (ImageView) dialogView.findViewById(R.id.dialog_imageview);
		contactName = (EditText) dialogView.findViewById(R.id.dialog_edittext);

		// if image was previously loaded here, reload it
		// (if user entered no name, and function was called again)
		if(contactImageUri != null){
			Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(contactImageUri));
				contactImage.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		// set listener for imageView, to open gallery intent
		dialogView.findViewById(R.id.dialog_imageview).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
				gallery.putExtra(MediaStore.EXTRA_OUTPUT, contactImageUri);
				startActivityForResult(gallery, GALLERY);
			}
		});

		addFriendDialog.setTitle("Add Friend");

		// add friend to database
		addFriendDialog.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int which) {
						// check length of name, if 0 toast and re-dialogue
						final String newName = contactName.getText().toString().trim();
						if(newName.length() == 0){
							Toast.makeText(getApplicationContext(), "Please add a name first", Toast.LENGTH_LONG).show();
							addFriend();
						}else {
							final Friend newFriend = new Friend(newName);
							// listener for database completion
							DatabaseReference.CompletionListener listener = new DatabaseReference.CompletionListener() {
								@Override
								public void onComplete(DatabaseError error, DatabaseReference ref) {
									if (error != null) {
										//error, notify user and do nothing.
										Toast.makeText(getApplicationContext(), "Unable to add Friend. Please try again.", Toast.LENGTH_LONG).show();
									} else {
										// completed successfully
										Event.GetEventListsReference().child(newFriend.eventListID).setValue(".");

										// UI things
										Toast.makeText(getApplicationContext(), newName.toUpperCase() + " Added to Friends List.", Toast.LENGTH_LONG).show();
										Intent reloadIntent = new Intent(ctx, FriendList.class);
										finish();
										startActivity(reloadIntent);
									}
								}
							};
							// add friend to database, wait for completion
							Friend.AddFriend(newFriend, contactImageUri, listener);
						}
					}
				});

		addFriendDialog.setNegativeButton("Cancel", null);
		addFriendDialog.setView(dialogView);
		addFriendDialog.show();
	}

	private void addEvent(Friend currentFriend)
	{
		Intent eventIntent = new Intent(ctx, EventCreation.class);
		eventIntent.putExtra("ELID", currentFriend.eventListID);
		eventIntent.putExtra("FID", currentFriend.friendID);
		eventIntent.putExtra("option", 0);
		finish();
		startActivity(eventIntent);
	}

	private void openIdeaPage(String eventID, String eventName, Friend currentFriend)
	{
		Intent ideaIntent = new Intent(ctx, IdeaPage.class);
		ideaIntent.putExtra("eventID", eventID);
		ideaIntent.putExtra("eventName",eventName);
		ideaIntent.putExtra("friendID", currentFriend.friendID);
		finish();
		startActivity(ideaIntent);
	}

	private void userLogOut() {
		Database.GetInstance().signOut();
		finish();
		Intent intent = new Intent(ctx, MainActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);


		if (requestCode == GALLERY && resultCode == RESULT_OK) {
			contactImageUri = data.getData();
			Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(contactImageUri));
				contactImage.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}


}