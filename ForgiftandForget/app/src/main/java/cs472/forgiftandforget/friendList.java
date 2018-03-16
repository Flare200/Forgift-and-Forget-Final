package cs472.forgiftandforget;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

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

import cs472.forgiftandforget.DatabaseClasses.Gift;
import cs472.forgiftandforget.DatabaseClasses.database;
import cs472.forgiftandforget.DatabaseClasses.friend;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class friendList extends AppCompatActivity
{
    private Context ctx = this;
    private ExpandableListView friendList;

    FirebaseAuth mAuth;
    database db;
    CopyOnWriteArrayList<friend> friends = new CopyOnWriteArrayList<>(); //this is accessed by multiple threads.
    DatabaseReference ref;
    FirebaseUser currentUser;


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

        // single event, on create, to populate a list of friends(myList)
        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for (DataSnapshot child: children)
                {
                    friend newFriend = child.getValue(friend.class);
                    friends.add(newFriend);
                }

                //Fills the list with you friends
                friendList = (ExpandableListView) findViewById(R.id.listView);

                List<String> headerList = new ArrayList<String>();
                HashMap<String,List<String>> eventList = new HashMap<String,List<String>>();

                for(int i = 0; i < friends.size(); i++)
                {
                    friend insert = friends.get(i);
                    headerList.add(insert.getName());
                }

                //Add the events in here, Addes empty events for now.
                List<String> subListEp = new ArrayList<String>();
                subListEp.add("");

                for(int i = 0; i < headerList.size(); i++)
                {
                    eventList.put(headerList.get(i),subListEp);
                }

                friendsListAdapter myAdapter = new friendsListAdapter(ctx,headerList,eventList);
                friendList.setAdapter(myAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }

        });

        //Super Basic set up Do not remove Aaron will remove at a later date
        //==========================================================================================
        /*
        friendList = (ExpandableListView) findViewById(R.id.listView);

        HashMap<String,List<String>> ideaList = new HashMap<String,List<String>>();

        String friends[] = getResources().getStringArray(R.array.Testing_list);
        String sl01[] = getResources().getStringArray(R.array.Fred_list);
        String sl02[] = getResources().getStringArray(R.array.Grace_list);
        String sl03[] = getResources().getStringArray(R.array.Bob_list);
        String sl04[] = getResources().getStringArray(R.array.Grace_list);
        String sl05[] = getResources().getStringArray(R.array.Grace_list);
        String sl06[] = getResources().getStringArray(R.array.Grace_list);
        String sl07[] = getResources().getStringArray(R.array.Grace_list);

        List<String> headings = new ArrayList<String>();
        List<String> subList01 = new ArrayList<String>();
        List<String> subList02 = new ArrayList<String>();
        List<String> subList03 = new ArrayList<String>();
        List<String> subList04 = new ArrayList<String>();
        List<String> subList05 = new ArrayList<String>();
        List<String> subList06 = new ArrayList<String>();
        List<String> subList07 = new ArrayList<String>();
        List<String> subListEp = new ArrayList<String>();



        for(String title : friends)
        {
            headings.add(title);
        }
        for(String title : sl01)
        {
            subList01.add(title);
        }
        for(String title : sl02)
        {
            subList02.add(title);
        }
        for(String title : sl03)
        {
            subList03.add(title);
        }
        for(String title : sl04)
        {
            subList04.add(title);
        }
        for(String title : sl05)
        {
            subList05.add(title);
        }
        for(String title : sl06)
        {
            subList06.add(title);
        }
        for(String title : sl07)
        {
            subList07.add(title);
        }
        headings.add("TEST");
        subListEp.add("");

        ideaList.put(headings.get(0),subList01);
        ideaList.put(headings.get(1),subList02);
        ideaList.put(headings.get(2),subList03);
        ideaList.put(headings.get(3),subList02);
        ideaList.put(headings.get(4),subList02);
        ideaList.put(headings.get(5),subList02);
        ideaList.put(headings.get(6),subList02);
        ideaList.put(headings.get(7),subListEp);
        friendsListAdapter myAdapter = new friendsListAdapter(this,headings,ideaList);
        friendList.setAdapter(myAdapter);
        */
        //==========================================================================================

        /*friendList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
            {
                //TextView testView = (TextView) findViewById(R.id.testText);
                //testView.setText("CLICKED");
                return true;
            }
        });*/
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
        Intent entireIntent = new Intent(ctx,entireCreation.class);
        finish();
        startActivity(entireIntent);
    }

    public void userLogOut()
    {
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(ctx, MainActivity.class);
        startActivity(intent);
    }
}