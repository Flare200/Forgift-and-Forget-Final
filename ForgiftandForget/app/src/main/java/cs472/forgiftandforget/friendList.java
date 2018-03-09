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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;

public class friendList extends AppCompatActivity
{
    private Context ctx = this;

    ExpandableListView friendList;
    FirebaseAuth mAuth;

    static final int ADD_FRIEND_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        mAuth = FirebaseAuth.getInstance();

        friendList = (ExpandableListView) findViewById(R.id.listView);
        HashMap<String,List<String>> ideaList = new HashMap<String,List<String>>();
        List<String> headings = new ArrayList<String>();


        friendsListAdapter myAdapter = new friendsListAdapter(ctx,headings,ideaList);
        friendList.setAdapter(myAdapter);

        //Super Basic set up
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

        friendList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
            {
                //TextView testView = (TextView) findViewById(R.id.testText);
                //testView.setText("CLICKED");
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.friendslist_menu_resource,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_add:
                addFriend();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addFriend()
    {
        EditText test = (EditText) findViewById(R.id.testText);

        String hold = null;
        friendList = (ExpandableListView) findViewById(R.id.listView);

        HashMap<String,List<String>> ideaList = new HashMap<String,List<String>>();
        List<String> headings = new ArrayList<String>();
        List<String> newEntire = new ArrayList<String>();

        friendsListAdapter myAdapter = new friendsListAdapter(ctx,headings,ideaList);
        friendList.setAdapter(myAdapter);

        //Does not work yet please do not touch
        Intent entireIntent = new Intent(ctx,entireCreation.class);
        finish();
        startActivityForResult(entireIntent,ADD_FRIEND_REQUEST);

    }

    public void userLogOut(View view)
    {
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(ctx, MainActivity.class);
        startActivity(intent);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Check which request we're responding to
        if (requestCode == ADD_FRIEND_REQUEST)
        {
            // Make sure the request was successful
            if (resultCode == RESULT_OK)
            {

            }
        }
    }
}