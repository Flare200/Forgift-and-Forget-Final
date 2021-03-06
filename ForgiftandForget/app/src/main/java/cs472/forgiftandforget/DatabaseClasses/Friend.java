package cs472.forgiftandforget.DatabaseClasses;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
	public Uri contactImage;

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
		this.contactImage = null;
	}


	@Exclude
	public static DatabaseReference GetFriendsListsReference() {
		return FirebaseDatabase.getInstance().getReference("FriendsLists");
	}

	@Exclude
	public static void RemoveFriend(String friendID, final DatabaseReference.CompletionListener completionListener) {

		final StorageReference storageReference = FirebaseStorage.getInstance().getReference("contactImages");
		final DatabaseReference friendsListRef = GetFriendsListsReference().child(Database.GetCurrentUID()).child(friendID);
		friendsListRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				String eventListID;
				String imageID;
				java.lang.Object temp;
				temp = dataSnapshot.child("eventListID").getValue();
				if (temp != null) {
					eventListID = temp.toString();
					Event.RemoveEventList(eventListID);
				} else {
					// no friend to remove
					return;
				}
				temp = dataSnapshot.child("imageID").getValue();
				if (temp != null) {
					imageID = temp.toString();
					storageReference.child(imageID).delete();
				}
				
				if (completionListener == null) {
					friendsListRef.removeValue();
				} else {
					friendsListRef.removeValue(completionListener);
				}

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				//do nothing
			}
		});

	}

	@Exclude
	//accepts a friend object, to add to the current users friends list
	public static void AddFriend(Friend newFriend, final Uri contactImageUri, DatabaseReference.CompletionListener listener) {

		//get database reference to the node of the logged in UID
		DatabaseReference friendsListsRef = GetFriendsListsReference().child(Database.GetCurrentUID());




		// get references to images and event list nodes
		final DatabaseReference eventListRef = Event.GetEventListsReference();
		final StorageReference storageReference = FirebaseStorage.getInstance().getReference("contactImages");


		//push new unique new keys, load into newFriend object
		final String friendID = friendsListsRef.push().getKey();
		final String eventListID = eventListRef.push().getKey();
		final String imageID = friendsListsRef.push().getKey();

		newFriend.eventListID = eventListID;
		newFriend.friendID = friendID;
		newFriend.imageID = imageID;
		newFriend.name = newFriend.name.toUpperCase();
		if(contactImageUri != null) {
			storageReference.child(imageID).putFile(contactImageUri);
		}
		friendsListsRef.child(friendID).setValue(newFriend, listener);
	}

	public int updateFriend(final Uri updatedContactImage){
		DatabaseReference friendsListsRef = GetFriendsListsReference().child(Database.GetCurrentUID());
		final StorageReference storageReference = FirebaseStorage.getInstance().getReference("contactImages");
		friendsListsRef.child(this.friendID).setValue(this, new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(DatabaseError error, DatabaseReference ref) {
				if (error != null) {
					Database.errorCode = 1;
				} else {
					// ToDO deal with update of contact image when storage reference is put back into project
					// completed successfully
		            if (updatedContactImage != null) {
                        storageReference.child(imageID).putFile(updatedContactImage);
                    }
					Database.errorCode = 0;
				}
			}
		});
		return Database.errorCode;
	}

}
