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
	private String name;
	private String date;
	private String eventID;

	public Event() {
		// empty constructor required for firebase
	}

	//parametrized constructor setting Event id to null(will be updated on Database AddEvent method)
	public Event(String name, String date) {
		this.name = name;
		this.date = date;
		this.eventID = "null";
	}

	public String GetName() {
		return name;
	}

	public void SetName(String name) {
		this.name = name;
	}

	public String GetDate() {
		return date;
	}

	public void SetDate(String date) {
		this.date = date;
	}

	public String GetEventID() {
		return eventID;
	}

	public void SetEventID(String eventID) {
		this.eventID = eventID;
	}


	@Exclude
	public static DatabaseReference GetEventListsReference() {
		return FirebaseDatabase.getInstance().getReference("EventLists");
	}

	@Exclude
	public static void RemoveEvent(String EID) {
		final DatabaseReference giftListsRef = Gift.GetGiftListsReference().child(EID);

		// need a separate new reference for each call, as it is called in a loop from RemoveEventList
		giftListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				Iterable<DataSnapshot> children = dataSnapshot.getChildren();
				for (DataSnapshot Child : children) {
					String GID = Child.child("gid").getValue().toString();
					Gift.RemoveGift(GID);
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
	public static void RemoveEventList(String ELID) {
		final DatabaseReference eventListsRef = GetEventListsReference().child(ELID);

		eventListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				Iterable<DataSnapshot> children = dataSnapshot.getChildren();
				for (DataSnapshot Child : children) {
					String EID = Child.child("eid").getValue().toString();
					Event.RemoveEvent(EID);
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
	public static int AddEvent(String ELID, String FID, Event newEvent) {
		//get reference to specific Event list
		final DatabaseReference eventListRef = GetEventListsReference().child(ELID);
		final DatabaseReference friendsListsRef = Friend.GetFriendsListsReference().child(Database.GetCurrentUID()).child(FID);
		final String EID = friendsListsRef.push().getKey();
		newEvent.SetEventID(EID);

		//add new value
		eventListRef.child(EID).setValue(newEvent, new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(DatabaseError error, DatabaseReference databaseReference) {
				if (error != null) {
					//failed
					Database.errorCode = 1;
				} else {
					//success
					friendsListsRef.child("hasEvents").setValue(true);
					Database.errorCode = 0;
				}
			}
		});

		return Database.errorCode;
	}

}
