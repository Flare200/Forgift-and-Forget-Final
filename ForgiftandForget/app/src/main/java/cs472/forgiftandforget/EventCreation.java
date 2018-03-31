package cs472.forgiftandforget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import cs472.forgiftandforget.DatabaseClasses.Event;

public class EventCreation extends AppCompatActivity {
	EditText eventField;
	EditText dateField;
	EditText timeField;
	int ret;
	private String ELID;
	private String FID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_creation);

		eventField = (EditText) findViewById(R.id.event);
		dateField = (EditText) findViewById(R.id.date);
		timeField = (EditText) findViewById(R.id.time);

		ELID = getIntent().getStringExtra("ELID");
		FID = getIntent().getStringExtra("FID");
	}

	public void addNewEvent(View view) {
		Event newEvent = new Event(eventField.getText().toString(), dateField.getText().toString());
		ret = Event.AddEvent(ELID, FID, newEvent);
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
