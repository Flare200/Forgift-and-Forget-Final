package cs472.forgiftandforget.DatabaseClasses;

import android.net.Uri;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by Tristan on 3/15/2018.
 */

public class Gift
{
	public String name;
	public String description;
	public String imageID1;
	public String imageID2;
	public String imageID3;
	public String url;
	public boolean gifted;

	//Constructors
	public Gift(){}

	public Gift(String name)
	{
		this.name = name;
		this.description = "";
		this.url = "";
		this.imageID1 = "";
		this.imageID2 = "";
		this.imageID3 = "";
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
	public static DatabaseReference GetGiftedListReference() {
		return FirebaseDatabase.getInstance().getReference("GiftedList");
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
	public static void AddGift(String eventID, Gift giftToAdd) {
		DatabaseReference giftListRef = GetGiftListsReference().child(eventID);
		DatabaseReference giftsRef = GetGiftsReference();

		//generate GID and save under passed friendIdKey
		DatabaseReference newGiftRef = giftListRef.push();
		newGiftRef.setValue(".");
		final String giftIdKey = newGiftRef.getKey();
		giftToAdd.imageID1 = newGiftRef.push().getKey();
		giftToAdd.imageID2 = newGiftRef.push().getKey();
		giftToAdd.imageID3 = newGiftRef.push().getKey();

		// this was overwriting the first gift every time
		// giftsRef.setValue(giftIdKey);

		giftsRef.child(giftIdKey).setValue(giftToAdd);
	}

	@Exclude
	public static void RemoveGift(String giftID) {
		// need a separate new reference for each call, as it is called in a loop from RemoveEvent
		final DatabaseReference giftRef = GetGiftsReference().child(giftID);
		giftRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				// ToDo delete images from collection
				giftRef.removeValue();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	@Exclude
	public void addImages(Uri[] images) {
		final StorageReference storageReference = FirebaseStorage.getInstance().getReference("giftImages");
		if(images[0] != null) {
			storageReference.child(imageID1).putFile(images[0]);
		}
		if(images[1] != null){
			storageReference.child(imageID2).putFile(images[1]);
		}
		if(images[2] != null){
			storageReference.child(imageID3).putFile(images[2]);
		}
	}

	@Exclude
	public void updateGift(String giftID) {
		DatabaseReference giftsRef = GetGiftsReference().child(giftID);
		giftsRef.setValue(this);
	}

	@Exclude
	public void moveToGifted (String giftID, String eventID, String friendID) {

		DatabaseReference giftsRef = GetGiftedListReference().child(Database.GetCurrentUID()).child(friendID).child(giftID);
		DatabaseReference giftListReference = GetGiftListsReference().child(eventID);
		giftsRef.setValue(this);
		giftListReference.child(giftID).removeValue();
	}

}
