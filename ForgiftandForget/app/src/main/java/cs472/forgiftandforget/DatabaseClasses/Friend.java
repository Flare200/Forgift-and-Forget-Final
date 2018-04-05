package cs472.forgiftandforget.DatabaseClasses;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.net.Uri;

/**
 * Created by mike_ on 3/7/2018.
 */

public class Friend {
	public String name;
	public String friendID;
	public String eventListID;
	public String imageID;
	public boolean hasEvents;

	public Friend() {
		// need public empty constructor for firebase
	}

	// parametrized constructor, setting FID,ELID,imageID to null(will be updated on Database AddFriend method)
	public Friend(String name) {
		this.name = name;
		this.friendID = "null";
		this.imageID = "null";
		this.eventListID = "null";
		this.hasEvents = false;
	}


	@Exclude
	public static DatabaseReference GetFriendsListsReference() {
		return FirebaseDatabase.getInstance().getReference("FriendsLists");
	}

	@Exclude
	public static void RemoveFriend(String friendID) {

		final DatabaseReference friendsListRef = GetFriendsListsReference().child(Database.GetCurrentUID()).child(friendID);
		friendsListRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				String eventListID;
				String imageID;
				java.lang.Object temp;
				temp = dataSnapshot.child("eventListID").getValue();
				if(temp != null){
					eventListID = temp.toString();
					Event.RemoveEventList(eventListID);
				}
				else{
					// no friend to remove
					return;
				}
				temp = dataSnapshot.child("imageID").getValue();
				if(temp != null){
					imageID = temp.toString();
					// ToDo remove image from database
				}


				friendsListRef.removeValue();

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				//do nothing
			}
		});
	}

	@Exclude
	//accepts a friend object, to add to the current users friends list
	public static int AddFriend(Friend newFriend, final Uri contactImageUri) {

		//get database reference to the node of the logged in UID
		DatabaseReference friendsListsRef = GetFriendsListsReference().child(Database.GetCurrentUID());
		final Event blankEvent = new Event("null", "null");

		// get references to images and event list nodes
		final DatabaseReference eventListRef = Event.GetEventListsReference();
		//storageRef = FirebaseStorage.getInstance().getReference().child("contactImages");

		//push new unique new keys, load into newFriend object
		final String friendID = friendsListsRef.push().getKey();
		final String eventListID = eventListRef.push().getKey();
		final String imageID = friendsListsRef.push().getKey();

		newFriend.eventListID = eventListID;
		newFriend.friendID = friendID;
		newFriend.imageID = imageID;

		//add newFriend to users friends list inside database
		friendsListsRef.child(friendID).setValue(newFriend, new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(DatabaseError error, DatabaseReference ref) {
				if (error != null) {
					Database.errorCode = 1;
				} else {
					// completed successfully
					eventListRef.child(eventListID).child("blankEvent").setValue(blankEvent);
		            /*if (contactImageUri != null) {
                        storageRef.child(imageID).putFile(contactImageUri);
                    }*/
					Database.errorCode = 0;
				}
			}
		});

		return Database.errorCode;
	}

}
