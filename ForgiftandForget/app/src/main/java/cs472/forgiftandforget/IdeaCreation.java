package cs472.forgiftandforget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import cs472.forgiftandforget.DatabaseClasses.Gift;

public class IdeaCreation extends AppCompatActivity
{
    String friendID;
    String eventID;
    String eventName;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idea_creation);

        EditText test = (EditText) findViewById(R.id.giftIdea);
        friendID = getIntent().getStringExtra("friendID");
        eventID = getIntent().getStringExtra("eventID");
        eventName = getIntent().getStringExtra("eventName");
    }

    public void addIdea(View view)
    {
        EditText ideaNameET = (EditText) findViewById(R.id.giftIdea);
        EditText URLET = (EditText) findViewById(R.id.URLLink);
        EditText descriptionET = (EditText) findViewById(R.id.giftDescription);

        String ideaName = ideaNameET.getText().toString();
        String URL = URLET.getText().toString();
        String description = descriptionET.getText().toString();

        if(!(ideaName.isEmpty()))
        {
            Gift newIdea = new Gift(ideaName);
            newIdea.url = URL;
            newIdea.description = description;

            Gift.AddGift(friendID,newIdea);

            endIntent();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Please give your idea a name.",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            endIntent();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void endIntent()
    {
        Intent ideaPageIntent = new Intent(IdeaCreation.this, IdeaPage.class);

        ideaPageIntent.putExtra("friendID",friendID);
        ideaPageIntent.putExtra("eventID",eventID);
        ideaPageIntent.putExtra("eventName",eventName);

        finish();
        startActivity(ideaPageIntent);
    }

}
