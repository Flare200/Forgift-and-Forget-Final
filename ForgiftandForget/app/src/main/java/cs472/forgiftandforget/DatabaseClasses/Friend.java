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
	private String name;
	private String friendID;
	private String eventListID;
	private String imageID;
	private boolean hasEvents;

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


	// need public setters and getters for firebase
	public String GetName() { return name; }

	public void SetName(String name) { this.name = name; }

	public String GetFriendID() { return friendID; }

	public void SetFriendID(String friendID) { this.friendID = friendID; }

	public String GetEventListID() { return eventListID; }

	public void SetEventListID(String eventListID) { this.eventListID = eventListID; }

	public String GetImageID() { return imageID; }

	public void SetImageID(String imageID) { this.imageID = imageID;}

	public void SetHasEvents(boolean hasEvents) { this.hasEvents = hasEvents; }

	public boolean HasEvents() { return hasEvents; }


	@Exclude
	public static DatabaseReference GetFriendsListsReference() {
		return FirebaseDatabase.getInstance().getReference("FriendsLists");
	}

	@Exclude
	public static void RemoveFriend(String FID) {

		final DatabaseReference friendsListRef = GetFriendsListsReference().child(Database.GetCurrentUID()).child(FID);
		friendsListRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				String ELID;
				String imageID;
				ELID = dataSnapshot.child("eventListID").getValue().toString();
				imageID = dataSnapshot.child("imageID").getValue().toString();
				friendsListRef.removeValue();
				Event.RemoveEventList(ELID);
				// something to remove image possibly
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				//do nothing
			}
		});
	}

	@Exclude
	//accepts a friend object, to add to the current users friends list
	public static int AddFriend(Friend newFriend, final Uri uri) {

		//get database reference to the node of the logged in UID
		DatabaseReference friendsListsRef = GetFriendsListsReference().child(Database.GetCurrentUID());
		final Event blankEvent = new Event("blank", "blank");

		// get references to images and event list nodes
		final DatabaseReference eventListRef = Event.GetEventListsReference();
		//storageRef = FirebaseStorage.getInstance().getReference().child("contactImages");

		//push new unique new keys, load into newFriend object
		final String FID = friendsListsRef.push().getKey();
		final String ELID = eventListRef.push().getKey();
		final String imageID = friendsListsRef.push().getKey();

		newFriend.SetEventListID(ELID);
		newFriend.SetFriendID(FID);
		newFriend.SetImageID(imageID);

		//add newFriend to users friends list inside database
		friendsListsRef.child(FID).setValue(newFriend, new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(DatabaseError error, DatabaseReference ref) {
				if (error != null) {
					Database.errorCode = 1;
				} else {
					// completed successfully
					eventListRef.child(ELID).child("blankEvent").setValue(blankEvent);
	                /*if (uri != null) {
                        storageRef.child(imageID).putFile(uri);
                    }*/
					Database.errorCode = 0;
				}
			}
		});

		return Database.errorCode;
	}

}
