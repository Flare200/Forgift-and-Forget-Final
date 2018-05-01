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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import android.view.LayoutInflater;
import android.widget.Toast;

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
	EditText ideaName;
	String goToNewGift;
	String eventListID;


	ListView ideaListView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_idea_page);

		friendID = getIntent().getStringExtra("friendID");
		eventID = getIntent().getStringExtra("eventID");
		eventName = getIntent().getStringExtra("eventName");
		goToNewGift = getIntent().getStringExtra("intentFrom");
		eventListID = getIntent().getStringExtra("eventListID");
		giftListReference = Gift.GetGiftListsReference().child(eventID);
		ideaListView = (ListView) findViewById(R.id.ideaList);
		setTitle(eventName);
		ideaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent giftIntent = new Intent(IdeaPage.this, GiftIdeas.class);
				giftIntent.putExtra("giftID", giftIDS.get(position));
				giftIntent.putExtra("friendID", friendID);
				giftIntent.putExtra("eventID", eventID);
				giftIntent.putExtra("eventListID", eventListID);
				startActivity(giftIntent);
			}
		});
		ideaListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				editGiftName(position);
				return true;
			}
		});

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
				if(giftIDS.size() == 0){
					// no gifts, open dialog to add a gift
					addNewGift();
				}

				//Takes you to the idea page if a new idea was just added to the list
				if(goToNewGift.equals("Idea"))
				{
					Intent giftIntent = new Intent(IdeaPage.this, GiftIdeas.class);
					int test = giftIDS.size() - 1;
					giftIntent.putExtra("giftID", giftIDS.get(test));
					giftIntent.putExtra("friendID", friendID);
					startActivity(giftIntent);
				}

				for (int i = 0; i < giftIDS.size(); i++)
				{
					final int loc = i;
					DatabaseReference giftReference = Gift.GetGiftsReference().child(giftIDS.get(i));
					giftReference.addListenerForSingleValueEvent(new ValueEventListener()
					{
						@Override
						public void onDataChange(DataSnapshot dataSnapshot)
						{
							Gift newGift = new Gift();
							newGift = dataSnapshot.getValue(Gift.class);
							gifts.add(newGift);

							if(loc == giftIDS.size()-1)
							{
								// list of gifts available here
								for(int i = 0; i < gifts.size(); i++)
								{
									headerList.add(gifts.get(i).name);
								}
								// gifts loaded, set adapter
								ArrayAdapter<String> ideaPageAdapter = new ArrayAdapter<String>(IdeaPage.this, R.layout.idea_layout,headerList);
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

/*	// create a new dialog, set the layout
		AlertDialog.Builder addIdeaDialog = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		final View dialogView = inflater.inflate(R.layout.add_idea_dialogue, null);

		// get reference to the dialogue edit text
		ideaName = (EditText) dialogView.findViewById(R.id.dialogEditText);

		//Adds idea to database
		addIdeaDialog.setTitle("Add Idea");
		addIdeaDialog.setPositiveButton("Add", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// check length of name, if 0 toast and re-dialogue
				final String newIdea = ideaName.getText().toString();
				if(newIdea.length() == 0)
				{
					Toast.makeText(getApplicationContext(), "Please add an idea first", Toast.LENGTH_LONG).show();
					addNewGift();
				}
				else
				{
					Gift newGift = new Gift(newIdea);
					Gift.AddGift(eventID,newGift);

					//UI stuff
					Intent reloadIntent = new Intent(IdeaPage.this, IdeaPage.class);
					reloadIntent.putExtra("friendID",friendID);
					reloadIntent.putExtra("eventID",eventID);
					reloadIntent.putExtra("eventName",eventName);
					reloadIntent.putExtra("intentFrom","Idea");
					finish();
					startActivity(reloadIntent);
				}
			}
		});
		addIdeaDialog.setNegativeButton("Cancel", null);
		addIdeaDialog.setView(dialogView);
		addIdeaDialog.show(); */

		AlertDialog.Builder addGiftAlert = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		final View dialogView = inflater.inflate(R.layout.dialog_single_edit_text, null);
		addGiftAlert.setTitle("Add New Gift");
		final EditText giftName = (EditText) dialogView.findViewById(R.id.editText);
		giftName.setHint("Gift Name");
		addGiftAlert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newGiftName = giftName.getText().toString().trim();
				if(newGiftName.length() > 0) {
					Gift newGift = new Gift(newGiftName);
					Gift.AddGift(eventID,newGift);
					reloadPage();
				}

			}
		}).setNegativeButton("Cancel", null);
		addGiftAlert.setView(dialogView);
		addGiftAlert.show();
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

	public void editGiftName(final int position){
		AlertDialog.Builder giftEditDialog = new AlertDialog.Builder(IdeaPage.this);
		LayoutInflater inflater = LayoutInflater.from(IdeaPage.this);
		final View dialogView = inflater.inflate(R.layout.dialog_single_edit_text, null);
		final EditText giftName = (EditText) dialogView.findViewById(R.id.editText);
		giftName.setText(gifts.get(position).name);
		giftEditDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = giftName.getText().toString().trim();
				if(name.length() == 0){
					Toast.makeText(getApplicationContext(), "Please Enter a name first", Toast.LENGTH_LONG).show();
					editGiftName(position);
					return;
				}
				Gift thisGift = gifts.get(position);
				thisGift.name = name;
				thisGift.updateGift(giftIDS.get(position));
				reloadPage();
			}
		}).setNegativeButton("Cancel", null).setTitle("Update Gift Name");
		giftEditDialog.setView(dialogView);
		giftEditDialog.show();
	}

	public void reloadPage(){
		Intent ideaIntent = new Intent(IdeaPage.this, IdeaPage.class);
		ideaIntent.putExtra("eventID", eventID);
		ideaIntent.putExtra("eventName",eventName);
		ideaIntent.putExtra("friendID", friendID);
		ideaIntent.putExtra("eventListID", eventListID);
		finish();
		startActivity(ideaIntent);
	}

}
