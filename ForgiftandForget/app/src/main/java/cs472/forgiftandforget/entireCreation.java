package cs472.forgiftandforget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import cs472.forgiftandforget.DatabaseClasses.database;
import cs472.forgiftandforget.DatabaseClasses.friend;

import android.widget.Toast;

public class entireCreation extends AppCompatActivity
{
    DatabaseReference ref;
    EditText nameField;
    EditText dateField;
    EditText emailField;
    EditText addressField;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entire_creation);
        nameField  = (EditText) findViewById(R.id.nameField);
        dateField  = (EditText) findViewById(R.id.dateField);
        emailField  = (EditText) findViewById(R.id.emailField);
        addressField  = (EditText) findViewById(R.id.addressField);
    }

    public void addNewFriend(View view)
    {
        //get currently logged in users UID
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        //get database reference to the node of the logged in UID
        ref = FirebaseDatabase.getInstance().getReference("FriendsLists").child(uid);

        //load in user input, add to friend object
        final String newName = nameField.getText().toString().trim();
        String newDate = dateField.getText().toString().trim();
        String newAddress = addressField.getText().toString().trim();
        String newEmail = emailField.getText().toString().trim();
        friend newFriend = new friend(newName, newAddress, newEmail, newDate);

        //push new unique key, save the value
        String key = ref.push().getKey();

        //load unique FID into object, add entire object to database
        newFriend.setFID(key);
        ref.child(key).setValue(newFriend, new DatabaseReference.CompletionListener(){
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref){
                if (error != null) {
                    Toast.makeText(getApplicationContext(), "Could not add Friend", Toast.LENGTH_LONG).show();
                }else{
                    // completed successfully
                    Toast.makeText(getApplicationContext(), newName + " Added to Friend's List", Toast.LENGTH_LONG).show();
                    finish();
                    Intent intent = new Intent(entireCreation.this, friendList.class);
                    startActivity(intent);
                }
            }
        });

        return;
    }
}
