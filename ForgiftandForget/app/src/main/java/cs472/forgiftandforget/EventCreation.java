package cs472.forgiftandforget;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import cs472.forgiftandforget.DatabaseClasses.Event;

public class EventCreation extends AppCompatActivity {
	EditText eventField;
	EditText dateField;
	EditText timeField;
	private String eventListID;
	private String friendID;
	int year;
	int month;
	int day;
	int hour;
	int minute;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_creation);

		eventListID = getIntent().getStringExtra("ELID");
		friendID = getIntent().getStringExtra("FID");

		eventField = (EditText) findViewById(R.id.event);
		dateField = (EditText) findViewById(R.id.date);
		timeField = (EditText) findViewById(R.id.time);
		Calendar cal = Calendar.getInstance();
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DAY_OF_MONTH);


		dateField.setInputType(InputType.TYPE_NULL);
		timeField.setInputType(InputType.TYPE_NULL);
		dateField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(0);
			}
		});
		dateField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showDialog(0);
				}
			}
		});
		timeField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(1);
			}
		});
		timeField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showDialog(1);
				}
			}
		});


	}

	@Override
	protected Dialog onCreateDialog(int id){
		if(id == 0){
			return new DatePickerDialog(this, datePickerListener, year, month, day);
		}else if(id == 1) {
			return new TimePickerDialog(this, timePickerListener, hour, minute, false);
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int chosenYear, int chosenMonth, int chosenDay) {
			year = chosenYear;
			month = chosenMonth+1;
			day = chosenDay;
			String setDate = month + "/" + day + "/" + year;
			dateField.setText(setDate);
		}
	};

	private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
			hour = hourOfDay;
			minute = minuteOfHour;
			String amOrPm = "am";
			String setTime;
			if(hour > 12){
				hour = hour-12;
				amOrPm = "pm";
			}
			if(minute < 10){
				setTime = hour + ":0" + minute + amOrPm;
			}else {
				setTime = hour + ":" + minute + amOrPm;
			}
			timeField.setText(setTime);
		}
	};

	public void addNewEvent(View view) {
		Event newEvent = new Event(eventField.getText().toString(), dateField.getText().toString());
		if(Event.AddEvent(eventListID, friendID, newEvent) == 0){
			Toast.makeText(getApplicationContext(), newEvent.name +" Added to events", Toast.LENGTH_LONG).show();
		}else{
			// error adding event
			Toast.makeText(getApplicationContext(), "Unable to add Event. Please try again", Toast.LENGTH_LONG).show();
		}
		Intent intent = new Intent(EventCreation.this, FriendList.class);
		finish();
		startActivity(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(EventCreation.this, FriendList.class);
			finish();
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}
}
