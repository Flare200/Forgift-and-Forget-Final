package cs472.forgiftandforget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;

import cs472.forgiftandforget.DatabaseClasses.database;
import cs472.forgiftandforget.DatabaseClasses.friend;

import android.widget.Toast;

public class friendCreation extends AppCompatActivity
{
    DatabaseReference ref;
    EditText nameField;
    EditText dateField;
    EditText emailField;
    EditText addressField;
    database db;
    int ret;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_creation);
        nameField  = (EditText) findViewById(R.id.nameField);
        dateField  = (EditText) findViewById(R.id.dateField);
        emailField  = (EditText) findViewById(R.id.emailField);
        addressField  = (EditText) findViewById(R.id.addressField);
        db = new database();
    }

    public void addNewFriend(View view)
    {
        final String newName = nameField.getText().toString().trim();
        friend newFriend = new friend(newName);
        ret = db.addFriend(newFriend);
        if (ret != 0) {
            Toast.makeText(getApplicationContext(), "Could not add Friend", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), newName + " Added to Friend's List", Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent(friendCreation.this, friendList.class);
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent(friendCreation.this, friendList.class);
            finish();
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}
