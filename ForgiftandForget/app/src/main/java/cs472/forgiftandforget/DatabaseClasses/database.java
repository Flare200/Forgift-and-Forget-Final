package cs472.forgiftandforget.DatabaseClasses;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import cs472.forgiftandforget.entireCreation;
import cs472.forgiftandforget.friendList;

/**
 * Created by mike_ on 3/7/2018.
 */

public class database extends AppCompatActivity {

    private static final String TAG = "this";
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase dataBase;
    DatabaseReference ref;
    DatabaseReference imagesRef;
    DatabaseReference eventListRef;
    DatabaseReference secondaryRef;
    String uid;
    int code;       // return value, 0 success, non zero specific error codes

    public database(){
        // empty constructor
    }

    //accepts a friend object, to add to the current users friends list
    public int addFriend(friend newFriend){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();

        //get database reference to the node of the logged in UID
        ref = FirebaseDatabase.getInstance().getReference("FriendsLists").child(uid);
        // get references to images and event list nodes
        eventListRef = FirebaseDatabase.getInstance().getReference("EventLists");

        //push new unique new keys, load into newFriend object
        final String FID = ref.push().getKey();
        final String ELID = eventListRef.push().getKey();
        final String imageID = ref.push().getKey();

        newFriend.setEventListID(ELID);
        newFriend.setFriendID(FID);
        newFriend.setImageID(imageID);

        //add newFriend to users friends list inside database
        ref.child(FID).setValue(newFriend, new DatabaseReference.CompletionListener(){
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error != null) {
                    code = 1;
                } else {
                    // completed successfully
                    eventListRef.child(ELID).child("eventCount").setValue(0);
                    code = 0;
                }
            }
        });
        return code;
    }

    // accepts the event list id, and an event object to add to the corresponding event list
    public int addEvent(String ELID, String FID, event newEvent){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();


        //get reference to specific event list
        ref = FirebaseDatabase.getInstance().getReference("EventLists").child(ELID);
        secondaryRef = FirebaseDatabase.getInstance().getReference("FriendsLists").child(uid).child(FID);
        final String EID = ref.push().getKey();
        newEvent.setEid(EID);
        //add new value
        ref.child(EID).setValue(newEvent, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference databaseReference) {
                if(error != null){
                    //failed
                    code = 1;
                }else{
                    //success
                    secondaryRef.child("hasEvents").setValue(true);
                    code = 0;
                }
            }
        });


        return code;
    }
}