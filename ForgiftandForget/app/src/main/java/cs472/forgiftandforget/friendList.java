package cs472.forgiftandforget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class friendList extends AppCompatActivity
{
    ExpandableListView friendList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        //TextView testView = (TextView) findViewById(R.id.testText);
        //testView.setText(getResources().getStringArray(R.array.Testing_list).toString());
        //Super Basic set up
        friendList = (ExpandableListView) findViewById(R.id.listView);

        HashMap<String,List<String>> ideaList = new HashMap<String,List<String>>();

        String friends[] = getResources().getStringArray(R.array.Testing_list);
        String sl01[] = getResources().getStringArray(R.array.Fred_list);
        String sl02[] = getResources().getStringArray(R.array.Grace_list);
        String sl03[] = getResources().getStringArray(R.array.Bob_list);

        List<String> headings = new ArrayList<String>();
        List<String> subList01 = new ArrayList<String>();
        List<String> subList02 = new ArrayList<String>();
        List<String> subList03 = new ArrayList<String>();
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
        headings.add("TEST");
        subListEp.add("");

        ideaList.put(headings.get(0),subList01);
        ideaList.put(headings.get(1),subList02);
        ideaList.put(headings.get(2),subList03);
        ideaList.put(headings.get(3),subListEp);
        friendsListAdapter myAdapter = new friendsListAdapter(this,headings,ideaList);
        friendList.setAdapter(myAdapter);

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
        getMenuInflater().inflate(R.menu.menu_resource,menu);
        return true;
    }

}
