package cs472.forgiftandforget;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.jar.Attributes;

public class entireCreation extends AppCompatActivity
{
    private Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entire_creation);

    }

    public void onClick(View view)
    {
        Intent returnIntent = new Intent(ctx,friendList.class);

       /*

        EditText contact = (EditText) findViewById(R.id.contactName);
        returnIntent.putExtra(FRIEND_TO_ADD,contact.getText().toString());

        */

        startActivity(returnIntent);
        finish();
    }
}
