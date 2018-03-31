package cs472.forgiftandforget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import cs472.forgiftandforget.DatabaseClasses.database;
import cs472.forgiftandforget.DatabaseClasses.event;

public class eventCreation extends AppCompatActivity
{
    EditText eventField;
    EditText dateField;
    EditText timeField;
    database db;
    int ret;
    private String ELID;
    private String FID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        eventField = (EditText) findViewById(R.id.event);
        dateField = (EditText) findViewById(R.id.date);
        timeField = (EditText) findViewById(R.id.time);
        db = new database();

        ELID = getIntent().getStringExtra("ELID");
        FID = getIntent().getStringExtra("FID");
    }

    public void addNewEvent(View view)
    {
        event newEvent = new event(eventField.getText().toString(),dateField.getText().toString());

        ret = db.addEvent(ELID,FID,newEvent);
        Intent intent = new Intent(eventCreation.this, friendList.class);
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent(eventCreation.this, friendList.class);
            finish();
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}
