package cs472.forgiftandforget;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import cs472.forgiftandforget.DatabaseClasses.database;
import cs472.forgiftandforget.DatabaseClasses.event;
import cs472.forgiftandforget.DatabaseClasses.friend;

public class friendList extends AppCompatActivity
{
    private Context ctx = this;
    private ExpandableListView friendList;
    private friendsListAdapter myAdapter;

    FirebaseAuth mAuth;
    database db;
    CopyOnWriteArrayList<friend> friends = new CopyOnWriteArrayList<>(); //this is accessed by multiple threads.
    CopyOnWriteArrayList<ArrayList<event>> friendsEvents = new CopyOnWriteArrayList<>();
    DatabaseReference ref;
    FirebaseUser currentUser;
    StorageReference storageRef;


    static final int ADD_FRIEND_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        mAuth       = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        String uid  = currentUser.getUid();
        ref         = FirebaseDatabase.getInstance().getReference("FriendsLists").child(uid);
        db          = new database();
        friendList  = (ExpandableListView) findViewById(R.id.listView);
        storageRef  = FirebaseStorage.getInstance().getReference().child("contactImages");
        final List<String> headerList = new ArrayList<String>();
        final HashMap<String,List<String>> eventList = new HashMap<String,List<String>>();

        // single event, on create, to populate a list of friends(myList)
        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                // add each of the users friends to the list friends
                for (DataSnapshot child: children)
                {
                    friend newFriend = child.getValue(friend.class);
                    friends.add(newFriend);
                }

                //for each friend in the list get all events for the friend, add them to ExpandableList
                for(int i = 0; i < friends.size(); i++)
                {
                    final friend thisFriend = friends.get(i);
                    final int loc = i;
                    //getting reference to specific event list
                    DatabaseReference thisRef = FirebaseDatabase.getInstance().getReference("EventLists").child(thisFriend.getEventListID());
                    thisRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> Children = dataSnapshot.getChildren();
                            ArrayList<event> events = new ArrayList<event>();
                            // getting all events and adding to the list events
                            // Except for the null event which is added on friend creation(to hold database spot)
                            for (DataSnapshot Child: Children)
                            {
                                event thisEvent = Child.getValue(event.class);
                                if(!thisEvent.getEid().equals("null")){
                                    events.add(thisEvent);
                                }
                            }
                            // add list of events to the list friendsEvents
                            friendsEvents.add(events);

                            // insert friend name into top level expandableList
                            friend insert = friends.get(loc);
                            headerList.add(insert.getName());


                            // create the sublist for the above added friend
                            List<String> subList = new ArrayList<String>();
                            // add each event to the sublist
                            if(friendsEvents.get(loc).size() == 0)
                            {
                                subList.add("~Click to add an event~");
                            }
                            else
                            {
                                for (int j = 0; j < friendsEvents.get(loc).size(); j++)
                                {
                                    subList.add(friendsEvents.get(loc).get(j).getName());
                                }
                                subList.add("~Click to add an event~");
                            }
                            // add sublist into lower level expandableList
                            eventList.put(headerList.get(loc),subList);

                            // if all friends+events have been loaded, display expandableList
                            if(loc == friends.size()-1)
                            {
                                myAdapter = new friendsListAdapter(ctx,headerList,eventList);
                                friendList.setAdapter(myAdapter);
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        //Handler to add events to the list
        friendList.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {
                if(childPosition ==  myAdapter.getChildrenCount(groupPosition)-1)
                    addEvent(friends.get(groupPosition));
                else
                {
                    List<String> events = eventList.get(friends.get(groupPosition).getName());
                    openIdeaPage(events.get(childPosition).toString());//Need to replace with the actual event class
                }

                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.friendslist_menu_resource,menu);
        getMenuInflater().inflate(R.menu.block_menu_resource,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
            case R.id.action_add:
                addFriend();
                return true;
            case R.id.action_Logout:
                userLogOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addFriend()
    {
        Intent friendIntent = new Intent(ctx,friendCreation.class);
        finish();
        startActivity(friendIntent);
    }

    private void addEvent(friend currentFriend)
    {
        Intent eventIntent = new Intent(ctx,eventCreation.class);
        eventIntent.putExtra("ELID",currentFriend.getEventListID());
        eventIntent.putExtra("FID",currentFriend.getFriendID());
        finish();
        startActivity(eventIntent);
    }

    private void openIdeaPage(String eventName)
    {
        Intent ideaIntent = new Intent(ctx,ideaPage.class);
        ideaIntent.putExtra("event",eventName);
        finish();
        startActivity(ideaIntent);
    }

    private void userLogOut()
    {
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(ctx, MainActivity.class);
        startActivity(intent);
    }
}