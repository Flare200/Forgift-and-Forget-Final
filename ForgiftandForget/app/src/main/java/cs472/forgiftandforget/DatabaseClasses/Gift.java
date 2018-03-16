package cs472.forgiftandforget.DatabaseClasses;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Tristan on 3/15/2018.
 */

public class Gift {
    private String name;
    private String description;
    private String imageId;
    private String url;
    private boolean gifted;

    //Constructors
    public Gift() {
    }

    public Gift(String name) {
        this.name = name;
        this.description = "";
        this.url = "";
        this.imageId = "";
        this.gifted = false;
    }

    //Getters and setters. Do not use the getters. Do not use the setters.
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setGifted(boolean gifted) {
        this.gifted = gifted;
    }

    public boolean isGifted() {
        return gifted;
    }


    //Useful Methods
    @Exclude
    private static DatabaseReference GetGiftListsReference() {
        return FirebaseDatabase.getInstance().getReference("GiftLists");
    }

    @Exclude
    private static DatabaseReference GetGiftsReference() {
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
}
