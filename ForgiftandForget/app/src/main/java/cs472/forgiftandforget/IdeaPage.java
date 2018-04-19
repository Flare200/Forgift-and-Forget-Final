package cs472.forgiftandforget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import cs472.forgiftandforget.DatabaseClasses.Database;
import cs472.forgiftandforget.DatabaseClasses.Gift;


public class IdeaPage extends AppCompatActivity
{

	DatabaseReference giftListReference;
	CopyOnWriteArrayList<Gift> gifts = new CopyOnWriteArrayList<>();
	CopyOnWriteArrayList<String> giftIDS = new CopyOnWriteArrayList<>();
	String friendID;
	String eventID;
	String eventName;

	ListView ideaListView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_idea_page);

		friendID = getIntent().getStringExtra("friendID");
		eventID = getIntent().getStringExtra("eventID");
		eventName = getIntent().getStringExtra("eventName");
		giftListReference = Gift.GetGiftListsReference().child(eventID);
		ideaListView = (ListView) findViewById(R.id.ideaList);
		//ideaListView = new ListView(IdeaPage.this);
		setTitle(eventName);

		final ArrayList<String> headerList = new ArrayList<String>();


		giftListReference.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				Iterable<DataSnapshot> children = dataSnapshot.getChildren();

				// for each entry in gift list, add to gifts, except for blank gift
				for (DataSnapshot Child : children)
				{
					String giftID = Child.getKey();
					giftIDS.add(giftID);
				}

				for (int i = 0; i < giftIDS.size(); i++)
				{
					final int loc = i;
					DatabaseReference giftReference = Gift.GetGiftsReference();
					giftReference.addListenerForSingleValueEvent(new ValueEventListener()
					{
						@Override
						public void onDataChange(DataSnapshot dataSnapshot)
						{
							Gift newGift = new Gift();
							newGift.name = dataSnapshot.child(giftIDS.get(loc)).child("name").getValue(String.class);
							newGift.description = dataSnapshot.child(giftIDS.get(loc)).child("description").getValue(String.class);
							newGift.url = dataSnapshot.child(giftIDS.get(loc)).child("url").getValue(String.class);
							newGift.imageID = dataSnapshot.child(giftIDS.get(loc)).child("imageID").getValue(String.class);
							gifts.add(newGift);

							if(loc == giftIDS.size()-1)
							{
								// list of gifts available here
								for(int i = 0; i < gifts.size(); i++)
								{
									headerList.add(gifts.get(i).name);
								}
								// gifts loaded, set adapter
								ArrayAdapter<String> ideaPageAdapter = new ArrayAdapter<String>(IdeaPage.this, android.R.layout.simple_list_item_1,headerList);
								ideaListView.setAdapter(ideaPageAdapter);
							}

						}

						@Override
						public void onCancelled(DatabaseError databaseError)
						{

						}
					});
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.idea_page_resource, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
			case R.id.action_Logout:
				userLogOut();
				return true;
			case R.id.action_add_gift:
				addNewGift();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void addNewGift()
	{
		// ToDo need an activity for adding gifts and a button to do so, similar to adding friends
		Intent intentIdeaCreation = new Intent(IdeaPage.this, IdeaCreation.class);
		intentIdeaCreation.putExtra("friendID",friendID);
		intentIdeaCreation.putExtra("eventID",eventID);
		intentIdeaCreation.putExtra("eventName",eventName);
		finish();
		startActivity(intentIdeaCreation);
	}

	private void userLogOut()
	{
		Database.GetInstance().signOut();
		finish();
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			Intent intent = new Intent(IdeaPage.this, FriendList.class);
			finish();
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}

}
