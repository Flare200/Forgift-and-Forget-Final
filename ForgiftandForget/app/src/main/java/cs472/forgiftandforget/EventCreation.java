package cs472.forgiftandforget;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.TimeZone;

import cs472.forgiftandforget.DatabaseClasses.Event;

public class EventCreation extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
	EditText eventField;
	EditText dateField;
	EditText timeField;
	Spinner spinner;
	private String eventListID;
	private String friendID;
	private int option;
	private String eventID;
	final static int TIME_PICK = 1;
	final static int DATE_PICK = 0;
	final static int TEN = 10;
	final static int TWELVE = 12;
	final static int MINUTES_IN_A_DAY = 1440;
	final static int MINUTES_IN_A_WEEK = 10080;
	final static int MY_PERMISSIONS_REQUEST_READ_WRITE_CALENDAR = 88;
	final static String[] reminderStrings = {"No Reminder", "1 Day Before", "2 Days Before",
											"1 Week Before", "2 Weeks Before", "Set Reminder..."};
	int year;
	int month;
	int day;
	int hour;
	int minute;
	int reminderTime;
	Boolean timeSet = false;
	Boolean dateSet = false;
	int calendarID = 1;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_creation);
		option = getIntent().getIntExtra("option", 0);
		eventListID = getIntent().getStringExtra("ELID");
		friendID = getIntent().getStringExtra("FID");
		eventID = getIntent().getStringExtra("eventID");


		eventField = (EditText) findViewById(R.id.event);
		dateField = (EditText) findViewById(R.id.date);
		timeField = (EditText) findViewById(R.id.time);
		spinner = (Spinner) findViewById(R.id.reminderSpinner);

		// set up spinner adapter to allow a hint
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(EventCreation.this, android.R.layout.simple_spinner_dropdown_item) {
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
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter.add(reminderStrings[0]);
		adapter.add(reminderStrings[1]);
		adapter.add(reminderStrings[2]);
		adapter.add(reminderStrings[3]);
		adapter.add(reminderStrings[4]);
		adapter.add(reminderStrings[5]);
		spinner.setAdapter(adapter);
		spinner.setSelection(adapter.getCount());
		spinner.setOnItemSelectedListener(this);

		// set calendar to today
		Calendar cal = Calendar.getInstance();
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DAY_OF_MONTH);

		// disallow any user input to date and time fields
		dateField.setInputType(InputType.TYPE_NULL);
		timeField.setInputType(InputType.TYPE_NULL);

		// listeners to open date and time dialogs
		dateField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_PICK);
			}
		});
		dateField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showDialog(DATE_PICK);
				}
			}
		});
		timeField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(TIME_PICK);
			}
		});
		timeField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showDialog(TIME_PICK);
				}
			}
		});

		if(option == 1){
			setUpUpdateScreen();
		}
	}

	// chooses which dialog to display
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
			dateField.setText(setDate);
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
			timeField.setText(setTime);
			timeSet = true;
		}
	};

	public void addNewEvent(View view) {
		// require event name to be set
		if(eventField.getText().toString().trim().length() == 0){
			Toast.makeText(getApplicationContext(), "Please set Event Name first", Toast.LENGTH_LONG).show();
			return;
		}
		// require date and time to be set
		if (!(dateSet && timeSet)) {
			Toast.makeText(getApplicationContext(), "Please set date and time first", Toast.LENGTH_LONG).show();
			return;
		}

		// add or update event in database
		Event newEvent = new Event(eventField.getText().toString(), dateField.getText().toString(), timeField.getText().toString());
		newEvent.eventID = eventID;
		if(option == 0) {
			Event.AddEvent(eventListID, friendID, newEvent);
		}else{
			Event.UpdateEvent(eventListID, newEvent);
		}
		checkPermissions();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			ReturnToFriendList();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void ReturnToFriendList()
	{
		Intent intent = new Intent(EventCreation.this, FriendList.class);
		finish();
		startActivity(intent);
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
			values.put(CalendarContract.Events.TITLE, eventField.getText().toString());
			values.put(CalendarContract.Events.DESCRIPTION, eventField.getText().toString());
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


		}catch(SecurityException e){
			Toast.makeText(getApplicationContext(), "Added " + eventField.getText().toString() + "to events\nUnable to update Calendar", Toast.LENGTH_LONG).show();
			ReturnToFriendList();
		}
		if(option == 0) { // new event added
			Toast.makeText(getApplicationContext(), "Added " + eventField.getText().toString() + " to events and Calendar", Toast.LENGTH_LONG).show();
		}else{ // old event updated
			Toast.makeText(getApplicationContext(), "Updated " + eventField.getText().toString(), Toast.LENGTH_LONG).show();
		}

		ReturnToFriendList();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_READ_WRITE_CALENDAR: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted
					addCalendarEntry();

				} else {
					// permission denied, send back to friendsList without adding calendar entry
					Toast.makeText(getApplicationContext(), "Added " + eventField.getText().toString() + " to events\nUnable to update Calendar", Toast.LENGTH_LONG).show();
					ReturnToFriendList();
				}
				break;
			}
		}
	}

	public void checkPermissions(){
		boolean write = true;
		boolean read = true;


		// check if permission has already been granted for read and write calendar
		if (ContextCompat.checkSelfPermission(EventCreation.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			write = false;
		}
		if(ContextCompat.checkSelfPermission(EventCreation.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			read = false;
		}

		// check if both are available, if not, request
		if(!(read && write)) {
			ActivityCompat.requestPermissions(EventCreation.this,
					new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
					MY_PERMISSIONS_REQUEST_READ_WRITE_CALENDAR);
		}else{
			//permission was already granted
			addCalendarEntry();
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


	public void deleteEvent(View view){
		// ask user to verify deletion
		AlertDialog.Builder builder = new AlertDialog.Builder(EventCreation.this);
		builder.setMessage("Permanently delete this Event,\nand all of its Gifts?");
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// remove event from database
				Event.RemoveSingleEvent(eventID, eventListID);
				ReturnToFriendList();
			}
		});
		builder.setNegativeButton("Cancel", null);

		AlertDialog alert = builder.create();
		alert.show();
	}

	public void setUpUpdateScreen(){
		// show progress dialog, in case event loads slowly
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setTitle("Loading Event Information");
		progress.setMessage("Loading...");
		progress.setCancelable(false);
		progress.show();

		//update the buttons to update or delete
		Button updateButton = (Button) findViewById(R.id.button2);
		Button deleteButton = (Button) findViewById(R.id.eventDeleteButton);
		String updateEvent = "UPDATE EVENT";
		updateButton.setText(updateEvent);
		deleteButton.setVisibility(View.VISIBLE);

		// get reference to event to update, read in from database
		DatabaseReference eventReference = Event.GetEventListsReference().child(eventListID).child(eventID);
		eventReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				Event thisEvent = dataSnapshot.getValue(Event.class);
				// load event into UI
				loadEvent(thisEvent);
				progress.dismiss();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	public void loadEvent(Event thisEvent){
		eventField.setText(thisEvent.name);
		dateField.setText(thisEvent.date);
		timeField.setText(thisEvent.time);
		timeSet = true;
		dateSet = true;
	}
}
