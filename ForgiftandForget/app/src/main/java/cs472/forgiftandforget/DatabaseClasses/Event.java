package cs472.forgiftandforget.DatabaseClasses;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by mike_ on 3/15/2018.
 */

public class Event {
	public String name;
	public String date;
	public String eventID;

	public Event() {
		// empty constructor required for firebase
	}

	//parametrized constructor setting Event id to null(will be updated on Database AddEvent method)
	public Event(String name, String date) {
		this.name = name;
		this.date = date;
		this.eventID = "null";
	}

	@Exclude
	public static DatabaseReference GetEventListsReference() {
		return FirebaseDatabase.getInstance().getReference("EventLists");
	}

	@Exclude
	public static void RemoveEvent(String eventID) {
		final DatabaseReference giftListsRef = Gift.GetGiftListsReference().child(eventID);

		// need a separate new reference for each call, as it is called in a loop from RemoveEventList
		giftListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				Iterable<DataSnapshot> children = dataSnapshot.getChildren();
				for (DataSnapshot Child : children) {
					// get each giftID in the giftList, and remove them
					java.lang.Object temp = Child.getKey();
					if(temp != null) {
						String giftID = temp.toString();
						Gift.RemoveGift(giftID);
					}
				}
				giftListsRef.removeValue();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				//do nothing
			}
		});
	}

	@Exclude
	public static void RemoveEventList(String eventListID) {
		final DatabaseReference eventListsRef = GetEventListsReference().child(eventListID);

		eventListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				Iterable<DataSnapshot> children = dataSnapshot.getChildren();
				for (DataSnapshot Child : children) {
					java.lang.Object temp = Child.child("eventID").getValue();
					if(temp != null) {
						String eventID = temp.toString();
						Event.RemoveEvent(eventID);
					}

				}
				eventListsRef.removeValue();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				//do nothing
			}
		});
	}

	// accepts the Event list id, and an Event object to add to the corresponding Event list
	@Exclude
	public static int AddEvent(String eventListID, String friendID, Event newEvent) {
		//get reference to specific Event list
		final DatabaseReference eventListRef = GetEventListsReference().child(eventListID);
		final DatabaseReference friendsListsRef = Friend.GetFriendsListsReference().child(Database.GetCurrentUID()).child(friendID);
		final DatabaseReference giftListReference = Gift.GetGiftListsReference();
		final String eventID = friendsListsRef.push().getKey();
		final Gift blankGift = new Gift("null");
		newEvent.eventID = eventID;

		//add new value
		eventListRef.child(eventID).setValue(newEvent, new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(DatabaseError error, DatabaseReference databaseReference) {
				if (error != null) {
					//failed
					Database.errorCode = 1;
				} else {
					//success
					friendsListsRef.child("hasEvents").setValue(true);
					giftListReference.child(eventID).setValue(".");
					Database.errorCode = 0;
				}
			}
		});

		return Database.errorCode;
	}

}
