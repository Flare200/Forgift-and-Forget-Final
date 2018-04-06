package cs472.forgiftandforget;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cs472.forgiftandforget.DatabaseClasses.Database;
import cs472.forgiftandforget.DatabaseClasses.Friend;

import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;

public class FriendCreation extends AppCompatActivity implements View.OnClickListener {
	EditText nameField;
	ImageView friendImage;
	DatabaseReference friendsListReference;
	static final int GALLERY = 1;
	Uri contactImageUri;
	String friendID;
	int option;
	Button deleteButton;
	Button addOrUpdateButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_creation);
		friendImage = (ImageView) findViewById(R.id.contactImage);
		nameField = (EditText) findViewById(R.id.nameField);
		friendImage.setOnClickListener(this);
		deleteButton = (Button) findViewById(R.id.delete);
		addOrUpdateButton = (Button) findViewById(R.id.add);


		option = getIntent().getIntExtra("option", 0);
		if(option == 1) {
			String update = "Update";
			friendID = getIntent().getStringExtra("friendID");
			addOrUpdateButton.setText(update);
			deleteButton.setVisibility(View.VISIBLE);
		}

	}


	public void deleteFriend(View view){
		Friend.RemoveFriend(friendID);
		Toast.makeText(getApplicationContext(), "Removed Friend", Toast.LENGTH_LONG).show();
		Intent intent = new Intent(FriendCreation.this, FriendList.class);
		finish();
		startActivity(intent);
	}

	public void addOrUpdateFriend(View view) {
		switch (option){
			case 0 :
				addFriend();
				break;
			case 1 :
				updateFriend();
				break;
		}
	}

	public void addFriend(){
		final String newName = nameField.getText().toString().trim();
		Friend newFriend = new Friend(newName);

		if (Friend.AddFriend(newFriend, contactImageUri) == 0) {
			// completed successfully
			Toast.makeText(getApplicationContext(), newName + " Added to Friend's List", Toast.LENGTH_LONG).show();
		} else {
			// error adding friend
			Toast.makeText(getApplicationContext(), "Unable to add Friend. Please try again", Toast.LENGTH_LONG).show();
		}
		Intent intent = new Intent(FriendCreation.this, FriendList.class);
		finish();
		startActivity(intent);
	}

	public void updateFriend(){
		final String newName = nameField.getText().toString().trim();
		friendsListReference = Friend.GetFriendsListsReference().child(Database.GetCurrentUID()).child(friendID);
		friendsListReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				Friend thisFriend = dataSnapshot.getValue(Friend.class);
				thisFriend.name = newName;
				thisFriend.updateFriend(contactImageUri);
				Intent intent = new Intent(FriendCreation.this, FriendList.class);
				finish();
				startActivity(intent);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(FriendCreation.this, FriendList.class);
			finish();
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		gallery.putExtra(MediaStore.EXTRA_OUTPUT, contactImageUri);
		startActivityForResult(gallery, GALLERY);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);


		if (requestCode == GALLERY && resultCode == RESULT_OK) {
			contactImageUri = data.getData();
			Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(contactImageUri));
				friendImage.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}

