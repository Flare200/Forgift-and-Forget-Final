package cs472.forgiftandforget.DatabaseClasses;

/**
 * Created by mike_ on 3/7/2018.
 */

public class friend {
    private String name;
    private String friendID;
    private String eventListID;
    private String imageID;
    private boolean hasEvents;

    public friend(){
        // need public empty constructor for firebase
    }

    // parametrized constructor, setting FID,ELID,imageID to null(will be updated on database addFriend method)
    public friend(String name){
        this.name = name;
        this.friendID = "null";
        this.imageID = "null";
        this.eventListID = "null";
        this.hasEvents = false;
    }


    // need public setters and getters for firebase
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getFriendID() { return friendID; }

    public void setFriendID(String friendID) { this.friendID = friendID; }

    public String getEventListID() { return eventListID; }

    public void setEventListID(String eventListID) { this.eventListID = eventListID; }

    public String getImageID() { return imageID; }

    public void setImageID(String imageID) { this.imageID = imageID;}

    public void setHasEvents(boolean hasEvents) { this.hasEvents = hasEvents; }

    public boolean isHasEvents() { return hasEvents; }



}
