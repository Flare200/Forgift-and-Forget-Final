package cs472.forgiftandforget.DatabaseClasses;


import android.net.Uri;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by mike_ on 3/7/2018.
 */

public class database extends AppCompatActivity {

    private static final String TAG = "this";
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference ref;
    DatabaseReference eventListRef;
    StorageReference storageRef;
    String uid;
    int code;       // return value, 0 success, non zero specific error codes

    public database(){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
    }

    //accepts a friend object, to add to the current users friends list
    public int addFriend(friend newFriend, final Uri uri){

        //get database reference to the node of the logged in UID
        ref = FirebaseDatabase.getInstance().getReference("FriendsLists").child(uid);
        final event blankEvent = new event("blank", "blank");
        // get references to images and event list nodes
        eventListRef = FirebaseDatabase.getInstance().getReference("EventLists");
        storageRef = FirebaseStorage.getInstance().getReference().child("contactImages");

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
                    eventListRef.child(ELID).child("blankEvent").setValue(blankEvent);
                    if (uri != null) {
                        storageRef.child(imageID).putFile(uri);
                    }
                    code = 0;
                }
            }
        });
        return code;
    }

    // accepts the event list id, and an event object to add to the corresponding event list
    public int addEvent(String ELID, String FID, event newEvent){


        //get reference to specific event list
        eventListRef = FirebaseDatabase.getInstance().getReference("EventLists").child(ELID);
        ref = FirebaseDatabase.getInstance().getReference("FriendsLists").child(uid).child(FID);
        final String EID = ref.push().getKey();
        newEvent.setEid(EID);
        //add new value
        eventListRef.child(EID).setValue(newEvent, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference databaseReference) {
                if(error != null){
                    //failed
                    code = 1;
                }else{
                    //success
                    ref.child("hasEvents").setValue(true);
                    code = 0;
                }
            }
        });


        return code;
    }

    public void removeFriend(String FID){

        ref = FirebaseDatabase.getInstance().getReference("FriendsLists").child(uid).child(FID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String ELID;
                String imageID;
                ELID = dataSnapshot.child("eventListID").getValue().toString();
                imageID = dataSnapshot.child("imageID").getValue().toString();
                ref.removeValue();
                removeEventList(ELID);
                // something to remove image possibly
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeEventList(String ELID){
        ref = FirebaseDatabase.getInstance().getReference("EventLists").child(ELID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot Child: children)
                {
                    String EID = Child.child("eid").getValue().toString();
                    removeEvent(EID);
                }

                ref.removeValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void removeEvent(String EID){
        // need a separate new reference for each call, as it is called in a loop from removeEventList
        final DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("GiftLists").child(EID);
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot Child: children)
                {
                    String GID = Child.child("gid").getValue().toString();
                    removeGift(GID);
                }
                eventRef.removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeGift(String GID){
        // need a separate new reference for each call, as it is called in a loop from removeEvent
        final DatabaseReference giftRef = FirebaseDatabase.getInstance().getReference("Gifts").child(GID);
        giftRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String imageID = dataSnapshot.child("imageId").toString();
                // delete from image collection
                giftRef.removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}