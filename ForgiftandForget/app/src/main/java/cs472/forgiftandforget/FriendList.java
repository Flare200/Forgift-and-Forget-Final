package cs472.forgiftandforget;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import cs472.forgiftandforget.DatabaseClasses.Database;
import cs472.forgiftandforget.DatabaseClasses.Event;
import cs472.forgiftandforget.DatabaseClasses.Friend;

public class FriendList extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
	private Context ctx = this;
	private ExpandableListView friendList;
	private FriendsListAdapter myAdapter;
	static final int GALLERY = 1;
	Uri contactImageUri;
	EditText contactName;
	ImageView contactImage;
	EditText eventName;
	EditText eventDate;
	EditText eventTime;
	Spinner reminder;
	final static String[] reminderStrings = {"No Reminder", "1 Day Before", "2 Days Before",
			"1 Week Before", "2 Weeks Before", "Set Reminder..."};
	final static int TIME_PICK = 1;
	final static int DATE_PICK = 0;
	final static int TEN = 10;
	final static int TWELVE = 12;
	final static int MINUTES_IN_A_DAY = 1440;
	final static int MINUTES_IN_A_WEEK = 10080;
	final static int MY_PERMISSIONS_REQUEST_READ_WRITE_CALENDAR = 88;
	Boolean timeSet = false;
	Boolean dateSet = false;
	int reminderTime;
	int year;
	int month;
	int day;
	int hour;
	int minute;
	int calendarID = 1;

	Database database;
	private static final Object friendLock = new Object();
	final ArrayList<Friend> friends = new ArrayList<>();
	final ArrayList<ArrayList<Event>> friendsEvents = new ArrayList<>();
	final CopyOnWriteArrayList<Uri> contactImages = new CopyOnWriteArrayList<>();
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

	    // set calendar to today
	    Calendar cal = Calendar.getInstance();
	    year = cal.get(Calendar.YEAR);
	    month = cal.get(Calendar.MONTH);
	    day = cal.get(Calendar.DAY_OF_MONTH);

	    final ProgressDialog progress = new ProgressDialog(this);
	    progress.setTitle("Loading Friends");
	    progress.setMessage("Loading...");
	    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
	    progress.show();

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
							editFriend(friendPosition);
							return true;
						}
						case ExpandableListView.PACKED_POSITION_TYPE_CHILD: {
							// if event item clicked
							// ToDo have an edit event option maybe? will need to verify they didnt click ~add event~
							if(eventPosition < friendsEvents.get(friendPosition).size()){
								editEvent(friendPosition, eventPosition);
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

										StorageReference storageReference = FirebaseStorage.getInstance().getReference("contactImages");
										try {
											synchronized(friendLock) {
												File contactImageFile = File.createTempFile("images" + thisFriend.imageID, "jpg");
												final Uri contactImageUri = Uri.parse(contactImageFile.getAbsolutePath());
												storageReference.child(thisFriend.imageID).getFile(contactImageFile)
														.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
															@Override
															public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {

																if (iterationCount == goalIterations) {
																	// done loading friends
																	myAdapter = new FriendsListAdapter(ctx, headerList, eventList, friends);
																	friendList.setAdapter(myAdapter);
																	progress.dismiss();
																}
																//increment the added EventList count
																iterationCount++;
															}
														}).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
													@Override
													public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
														thisFriend.contactImage = contactImageUri;
													}
												}).addOnFailureListener(new OnFailureListener() {
													@Override
													public void onFailure(@NonNull Exception e) {
														// no image, or image download failed
													}
												});
											}
										} catch (IOException e) {
											e.printStackTrace();
										}
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

	// add friend now done completely through a dialog
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
							// set friends contact image
							newFriend.contactImage = contactImageUri;
						}
					}
				});

		addFriendDialog.setNegativeButton("Cancel", null);
		addFriendDialog.setView(dialogView);
		addFriendDialog.show();
	}

	// add event now done completely through a dialog
	private void addEvent(final Friend currentFriend)
	{
		String oldName = null;
		String oldDate = null;
		String oldTime = null;
		long oldReminder = -1;

		// create a new dialog, set the layout
		AlertDialog.Builder addEventDialog = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		final View dialogView = inflater.inflate(R.layout.add_event_dialogue, null);

		// check if old values exist, and reload them
		if(eventName != null){
			oldName = eventName.getText().toString().trim();
			eventName = (EditText) dialogView.findViewById(R.id.event);
			eventName.setText(oldName);
		}else{
			eventName = (EditText) dialogView.findViewById(R.id.event);
		}
		if(eventDate != null){
			oldDate = eventDate.getText().toString().trim();
			eventDate = (EditText) dialogView.findViewById(R.id.date);
			eventDate.setText(oldDate);
		}else{
			eventDate = (EditText) dialogView.findViewById(R.id.date);
		}
		if(eventTime != null){
			oldTime = eventTime.getText().toString().trim();
			eventTime = (EditText) dialogView.findViewById(R.id.time);
			eventTime.setText(oldTime);
		}else{
			eventTime = (EditText) dialogView.findViewById(R.id.time);
		}
		if(reminder != null){
			oldReminder = reminder.getSelectedItemId();
			reminder = (Spinner) dialogView.findViewById(R.id.reminderSpinner);
		}else {
			reminder = (Spinner) dialogView.findViewById(R.id.reminderSpinner);
		}
		setUpSpinner();

		// update reminder AFTER adapter is set, if this is a re-dialogue
		if(oldReminder != -1){
			reminder.setSelection((int) oldReminder);
		}


		addEventDialog.setTitle("Add Event For " + currentFriend.name);

		// user clicked add
		addEventDialog.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int which) {
				final String thisEventDate = eventDate.getText().toString().trim();
				final String thisEventTime = eventTime.getText().toString().trim();
				final String thisEventname = eventName.getText().toString().trim();

				// disallow any user input to date and time fields

				// add the event after error checking fields
				if(eventName.getText().toString().trim().length() == 0){
					Toast.makeText(getApplicationContext(), "Please set Event Name first", Toast.LENGTH_LONG).show();
					addEvent(currentFriend);
					return;
				}
				// require date and time to be set
				if (!(dateSet && timeSet)) {
					Toast.makeText(getApplicationContext(), "Please set date and time first", Toast.LENGTH_LONG).show();
					addEvent(currentFriend);
					return;
				}

				// all required fields are set, add the event
				Event newEvent = new Event(eventName.getText().toString().trim(), eventDate.getText().toString().trim(), eventTime.getText().toString().trim());
				Event.AddEvent(currentFriend.eventListID, currentFriend.friendID, newEvent);
				checkPermissions();
			}
		});

		setDateTimeListeners();

		// cancel was pressed, reset all fields
		addEventDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				eventName.setText("");
				eventDate.setText("");
				eventTime.setText("");
				setUpSpinner();

			}
		});

		addEventDialog.setView(dialogView);
		addEventDialog.show();
	}

	private void openIdeaPage(String eventID, String eventName, Friend currentFriend)
	{
		Intent ideaIntent = new Intent(ctx, IdeaPage.class);
		ideaIntent.putExtra("eventID", eventID);
		ideaIntent.putExtra("eventName",eventName);
		ideaIntent.putExtra("friendID", currentFriend.friendID);
		//This is used to for adding new gift ideas to the idea page
		ideaIntent.putExtra("intentFrom","Friend");
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
		// gallery image selected
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

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// spinner selection for reminder
		switch(position){
			case 1 : // 1 day before
				reminderTime = MINUTES_IN_A_DAY;
				break;
			case 2 : // 2 days before
				reminderTime = MINUTES_IN_A_DAY * 2;
				break;
			case 3 : // 1 week before
				reminderTime = MINUTES_IN_A_WEEK;
				break;
			case 4 : // 2 weeks before
				reminderTime = MINUTES_IN_A_WEEK * 2;
				break;
			default : // set No reminder
				reminderTime = 0;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DATE_PICK) {
			return new DatePickerDialog(this, datePickerListener, year, month, day);
		} else if (id == TIME_PICK) {
			return new TimePickerDialog(this, timePickerListener, hour, minute, false);
		}
		return null;
	}

	// date was chosen
	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int chosenYear, int chosenMonth, int chosenDay) {
			year = chosenYear;
			month = chosenMonth + 1;
			day = chosenDay;
			String setDate = month + "/" + day + "/" + year;
			eventDate.setText(setDate);
			dateSet = true;
		}
	};

	// time was chosen
	private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
			hour = hourOfDay;
			int hourFixed = hour;
			minute = minuteOfHour;
			String amOrPm = "am";
			String setTime;

			// 12 - 1 am, set up to not show 0 o'clock
			if (hourFixed == 0) {
				hourFixed = TWELVE;
				// military time, set up for standard time
			} else if (hourFixed > TWELVE) {
				hourFixed -= TWELVE;
				amOrPm = "pm";
			}
			// set a leading 0 if minutes are less than 10
			if (minute < TEN) {
				setTime = hourFixed + ":0" + minute + amOrPm;
			} else {
				setTime = hourFixed + ":" + minute + amOrPm;
			}
			eventTime.setText(setTime);
			timeSet = true;
		}
	};

	public void checkPermissions(){
		boolean write = true;
		boolean read = true;

		// check if permission has already been granted for read and write calendar
		if (ContextCompat.checkSelfPermission(FriendList.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			write = false;
		}
		if(ContextCompat.checkSelfPermission(FriendList.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			read = false;
		}

		// check if both are available, if not, request
		if(!(read && write)) {
			ActivityCompat.requestPermissions(FriendList.this,
					new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
					MY_PERMISSIONS_REQUEST_READ_WRITE_CALENDAR);
		}else{
			//permission was already granted
			addCalendarEntry();
		}
	}

	public void addCalendarEntry(){
		Calendar startTime = Calendar.getInstance();
		startTime.set(year, month - 1, day, hour, minute);
		Calendar endTime = Calendar.getInstance();
		endTime.set(year, month - 1, day, hour, minute);
		Uri uri = CalendarContract.Calendars.CONTENT_URI;
		int indexPrimary;
		int indexID;
		// check calendar table for the calendar ID of the devices primary calendar, if fails set to 1
		try {
			Cursor calendarCursor = managedQuery(uri, null, null, null, null);
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				indexPrimary = calendarCursor.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY);
				indexID = calendarCursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID);
				while (calendarCursor.moveToNext()) {
					if (calendarCursor.getInt(indexPrimary) == 1) {
						calendarID = calendarCursor.getInt(indexID);
					}
				}
			}
		}catch(Exception e) {
			calendarID = 1;
		}

		try {
			// create and insert calendar entry
			ContentResolver cr = getContentResolver();
			ContentValues values = new ContentValues();
			TimeZone timeZone = TimeZone.getDefault();
			values.put(CalendarContract.Events.DTSTART, startTime.getTimeInMillis());
			values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
			values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
			values.put(CalendarContract.Events.TITLE, eventName.getText().toString());
			values.put(CalendarContract.Events.DESCRIPTION, eventName.getText().toString());
			values.put(CalendarContract.Events.CALENDAR_ID, calendarID);
			values.put(CalendarContract.Events.HAS_ALARM, 0);
			Uri eventUri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

			// set up alarm if requested
			if(reminderTime != 0) {
				values.put(CalendarContract.Events.HAS_ALARM, 1);
				String calendarEventID;
				if (eventUri != null) {
					calendarEventID = eventUri.getLastPathSegment();
					Uri REMINDERS_URI = Uri.parse("content://com.android.calendar/reminders");
					values = new ContentValues();
					values.put("event_id", calendarEventID);
					values.put("method", 1);
					values.put("minutes", reminderTime);
					getContentResolver().insert(REMINDERS_URI, values);
				}
			}
		// failed to add to calendar
		}catch(SecurityException e){
			Toast.makeText(getApplicationContext(), "Added " + eventName.getText().toString() + "to events\nUnable to update Calendar", Toast.LENGTH_LONG).show();
			reloadFriendsList();
		}
		// new event added
		Toast.makeText(getApplicationContext(), "Added " + eventName.getText().toString() + " to events and Calendar", Toast.LENGTH_LONG).show();
		reloadFriendsList();
	}

	public void reloadFriendsList(){
		Intent intent = new Intent(FriendList.this, FriendList.class);
		finish();
		startActivity(intent);
	}

	public void editFriend(final int friendPosition){
		AlertDialog.Builder editFriendAlert = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		final View dialogView = inflater.inflate(R.layout.add_friend_dialogue, null);
		contactName = (EditText) dialogView.findViewById(R.id.dialog_edittext);
		contactImage = (ImageView) dialogView.findViewById(R.id.dialog_imageview);
		contactName.setText(friends.get(friendPosition).name);
		contactImage.setImageURI(friends.get(friendPosition).contactImage);
		dialogView.findViewById(R.id.dialog_imageview).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
				gallery.putExtra(MediaStore.EXTRA_OUTPUT, contactImageUri);
				startActivityForResult(gallery, GALLERY);
			}
		});
		editFriendAlert.setNegativeButton("Cancel", null);
		editFriendAlert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(contactName.getText().toString().trim().length() == 0){
					Toast.makeText(getApplicationContext(), "Please add a name first", Toast.LENGTH_LONG).show();
					editFriend(friendPosition);
					return;
				}
				final ProgressBar progressBar = (ProgressBar) dialogView.findViewById(R.id.progress);
				progressBar.setVisibility(View.VISIBLE);
				friendsListReference = Friend.GetFriendsListsReference().child(Database.GetCurrentUID()).child(friends.get(friendPosition).friendID);
				friendsListReference.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						Friend thisFriend = dataSnapshot.getValue(Friend.class);
						thisFriend.name = contactName.getText().toString().trim();
						thisFriend.updateFriend(contactImageUri);
						progressBar.setVisibility(View.GONE);
						reloadFriendsList();
					}
					@Override
					public void onCancelled(DatabaseError databaseError) {

					}
				});
			}
		});
		editFriendAlert.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteFriend(dialogView, friendPosition);
			}
		});


		editFriendAlert.setTitle("Update Friend");
		editFriendAlert.setView(dialogView);
		editFriendAlert.show();
	}

	public void deleteFriend(View view, final int friendPosition)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(FriendList.this);
		builder.setMessage("Permanently delete " + contactName.getText() + "?");
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeFriend(friendPosition);
			}
		});
		builder.setNegativeButton("Cancel", null);

		AlertDialog alert = builder.create();
		alert.show();
	}

	public void removeFriend(int friendPosition){
		Toast.makeText(getApplicationContext(), "Removed " + contactName.getText() + " from Friends List.", Toast.LENGTH_LONG).show();

		//create a new listener for AddFriend
		DatabaseReference.CompletionListener completionListener = new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(DatabaseError error, DatabaseReference ref) {
				if (error != null) {
					//error, notify user and do nothing.
					Toast.makeText(getApplicationContext(), "Unable to delete Friend. Please try again.", Toast.LENGTH_LONG).show();
				} else {
					// completed successfully
					finish();
					reloadFriendsList();
				}
			}
		};
		Friend.RemoveFriend(friends.get(friendPosition).friendID, completionListener);
	}


	public void editEvent(final int friendPosition, int eventPosition){
		AlertDialog.Builder editEventAlert = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		final View dialogView = inflater.inflate(R.layout.add_event_dialogue, null);
		editEventAlert.setMessage("Update Event");
		eventName = (EditText) dialogView.findViewById(R.id.event);
		eventDate = (EditText) dialogView.findViewById(R.id.date);
		eventTime = (EditText) dialogView.findViewById(R.id.time);
		reminder = (Spinner) dialogView.findViewById(R.id.reminderSpinner);
		final Event thisEvent = friendsEvents.get(friendPosition).get(eventPosition);
		eventName.setText(thisEvent.name);
		eventDate.setText(thisEvent.date);
		eventTime.setText(thisEvent.time);
		timeSet = true;
		dateSet = true;
		setDateTimeListeners();
		setUpSpinner();
		editEventAlert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				thisEvent.setName(eventName.getText().toString().trim());
				thisEvent.setDate(eventDate.getText().toString().trim());
				thisEvent.setTime(eventTime.getText().toString().trim());
				Event.UpdateEvent(friends.get(friendPosition).eventListID, thisEvent);
				checkPermissions();
			}
		});
		editEventAlert.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteEvent(thisEvent.getEventID(), friends.get(friendPosition).eventListID);

			}
		});
		editEventAlert.setNegativeButton("Cancel", null);
		editEventAlert.setView(dialogView);
		editEventAlert.show();


	}

	public void deleteEvent(final String eventID, final String eventListID){
		// ask user to verify deletion
		AlertDialog.Builder builder = new AlertDialog.Builder(FriendList.this);
		builder.setMessage("Permanently delete this Event,\nand all of its Gifts?");
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// remove event from database
				Event.RemoveSingleEvent(eventID, eventListID);
				reloadFriendsList();
			}
		});
		builder.setNegativeButton("Cancel", null);

		AlertDialog alert = builder.create();
		alert.show();
	}

	public void setDateTimeListeners(){
		// listeners to open date and time dialogs
		eventDate.setInputType(InputType.TYPE_NULL);
		eventTime.setInputType(InputType.TYPE_NULL);
		eventDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_PICK);
			}
		});
		eventDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showDialog(DATE_PICK);
				}
			}
		});
		eventTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(TIME_PICK);
			}
		});
		eventTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showDialog(TIME_PICK);
				}
			}
		});
	}

	public void setUpSpinner(){
		// set up spinner adapter to allow a hint
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(FriendList.this, android.R.layout.simple_spinner_dropdown_item) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				View v = super.getView(position, convertView, parent);
				if (position == getCount()) {
					((TextView)v.findViewById(android.R.id.text1)).setText("");
					((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); // Hint to be displayed
				}
				return v;
			}
			@Override
			public int getCount() {
				return super.getCount()-1; // wont display last item. It is used as hint.
			}
		};
		// populate spinner choices
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter.add(reminderStrings[0]);
		adapter.add(reminderStrings[1]);
		adapter.add(reminderStrings[2]);
		adapter.add(reminderStrings[3]);
		adapter.add(reminderStrings[4]);
		adapter.add(reminderStrings[5]);
		reminder.setAdapter(adapter);
		reminder.setSelection(adapter.getCount());
		reminder.setOnItemSelectedListener(FriendList.this);
	}

}