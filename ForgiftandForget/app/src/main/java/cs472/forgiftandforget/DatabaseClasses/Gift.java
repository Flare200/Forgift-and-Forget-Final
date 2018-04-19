package cs472.forgiftandforget.DatabaseClasses;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Tristan on 3/15/2018.
 */

public class Gift
{
	public String name;
	public String description;
	public String imageID;
	public String url;
	public boolean gifted;

	//Constructors
	public Gift(){}

	public Gift(String name)
	{
		this.name = name;
		this.description = "";
		this.url = "";
		this.imageID = "";
		this.gifted = false;
	}

	//Useful Methods
	@Exclude
	public static DatabaseReference GetGiftListsReference() {
		return FirebaseDatabase.getInstance().getReference("GiftLists");
	}

	@Exclude
	public static DatabaseReference GetGiftsReference() {
		return FirebaseDatabase.getInstance().getReference("Gifts");
	}

	@Exclude
	public static void GetAndSetToRunOnce(String friendIdKey, ValueEventListener eventListener) {
		DatabaseReference ref = GetGiftListsReference().child(friendIdKey);
		ref.addListenerForSingleValueEvent(eventListener);
	}

	@Exclude
	public static ValueEventListener GetAndSetToRunOnUpdate(String friendIdKey, ValueEventListener eventListener) {
		DatabaseReference ref = GetGiftListsReference().child(friendIdKey);
		return ref.addValueEventListener(eventListener);
	}

	@Exclude
	public static void AddGift(String friendIdKey, Gift giftToAdd) {
		DatabaseReference giftListFriendRef = GetGiftListsReference().child(friendIdKey);
		DatabaseReference giftsRef = GetGiftsReference();

		//generate GID and save under passed friendIdKey
		DatabaseReference newGiftRef = giftListFriendRef.push();
		newGiftRef.setValue(".");

		//
		final String giftIdKey = newGiftRef.getKey();
		giftsRef.setValue(giftIdKey);
		giftsRef.child(giftIdKey).setValue(giftToAdd);

	}

	@Exclude
	public static void RemoveGift(String giftID) {
		// need a separate new reference for each call, as it is called in a loop from RemoveEvent
		final DatabaseReference giftRef = GetGiftsReference().child(giftID);
		giftRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				String imageID = dataSnapshot.child("imageID").toString();
				// delete from image collection
				giftRef.removeValue();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

}
