package cs472.forgiftandforget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CopyOnWriteArrayList;

import cs472.forgiftandforget.DatabaseClasses.Database;
import cs472.forgiftandforget.DatabaseClasses.Friend;
import cs472.forgiftandforget.DatabaseClasses.Gift;


public class IdeaPage extends AppCompatActivity {

	DatabaseReference giftListReference;
	CopyOnWriteArrayList<Gift> gifts = new CopyOnWriteArrayList<>();



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_idea_page);
		String eventID = getIntent().getStringExtra("eventID");
		giftListReference = Gift.GetGiftListsReference().child(eventID);



		giftListReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				Iterable<DataSnapshot> children = dataSnapshot.getChildren();

				// for each entry in gift list, add to gifts, except for blank gift
				for (DataSnapshot child : children) {
					Gift newGift = child.getValue(Gift.class);
					if(newGift != null && !newGift.name.equals("null")) {
						gifts.add(newGift);
					}
				}

				// list of Gifts gifts usable here.
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(IdeaPage.this, FriendList.class);
			finish();
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}


	void addNewGift(){
		// ToDo need an activity for adding gifts and a button to do so, similar to adding friends
	}
}
