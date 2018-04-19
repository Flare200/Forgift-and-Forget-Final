package cs472.forgiftandforget;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cs472.forgiftandforget.DatabaseClasses.Database;
import cs472.forgiftandforget.DatabaseClasses.Event;
import cs472.forgiftandforget.DatabaseClasses.Friend;

import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FriendCreation extends AppCompatActivity implements View.OnClickListener {
	EditText nameField;
	ImageView friendImage;
	DatabaseReference friendsListReference;
	StorageReference storageReference;
	static final int GALLERY = 1;
	Uri contactImageUri;
	String friendID;
	int option;
	Button deleteButton;
	Button addOrUpdateButton;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setTitle("Add Friend");
		setContentView(R.layout.activity_friend_creation);
		friendImage = (ImageView) findViewById(R.id.contactImage);
		nameField = (EditText) findViewById(R.id.nameField);
		friendImage.setOnClickListener(this);
		deleteButton = (Button) findViewById(R.id.delete);
		addOrUpdateButton = (Button) findViewById(R.id.add);


		option = getIntent().getIntExtra("option", 0);
		if(option == 1) {
			setupUpdateScreen();
		}
	}


	public void deleteFriend(View view)
	{
		Toast.makeText(getApplicationContext(), "Removed Friend", Toast.LENGTH_LONG).show();
		final Intent intent = new Intent(FriendCreation.this, FriendList.class);

		//create a new listener for AddFriend
		DatabaseReference.CompletionListener completionListener = new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(DatabaseError error, DatabaseReference ref) {
				if (error != null) {
					//error, notify user and do nothing.
					Toast.makeText(getApplicationContext(), "Unable to delete Friend. Please try again", Toast.LENGTH_LONG).show();
				} else {
					// completed successfully
					finish();
					startActivity(intent);
				}
			}
		};

		Friend.RemoveFriend(friendID, completionListener);
	}

	public void addOrUpdateFriend(View view)
	{
		switch (option) {
			case 0:
				addFriend();
				break;
			case 1:
				updateFriend();
				break;
		}
	}

	public void addFriend()
	{
		final String newName = nameField.getText().toString().trim();
		final Friend newFriend = new Friend(newName);
		final Intent intent = new Intent(FriendCreation.this, FriendList.class);

		//create a new listener for AddFriend
		DatabaseReference.CompletionListener listener = new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(DatabaseError error, DatabaseReference ref) {
				if (error != null) {
					//error, notify user and do nothing.
					Toast.makeText(getApplicationContext(), "Unable to add Friend. Please try again", Toast.LENGTH_LONG).show();
				} else {
					// completed successfully
					Event.GetEventListsReference().child(newFriend.eventListID).setValue(".");

					//UI things
					Toast.makeText(getApplicationContext(), newName + " Added to Friend's List", Toast.LENGTH_LONG).show();
					finish();
					startActivity(intent);
				}
			}
		};

		Friend.AddFriend(newFriend, contactImageUri, listener);
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

	public void setupUpdateScreen(){
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setTitle("Loading Contact Information");
		progress.setMessage("Loading...");
		progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
		progress.show();

		String update = "Update";
		friendID = getIntent().getStringExtra("friendID");
		addOrUpdateButton.setText(update);
		deleteButton.setVisibility(View.VISIBLE);
		friendsListReference = Friend.GetFriendsListsReference().child(Database.GetCurrentUID()).child(friendID);
		storageReference = FirebaseStorage.getInstance().getReference("contactImages");
		friendsListReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {

				nameField.setText(dataSnapshot.child("name").getValue().toString());
				String imageID = dataSnapshot.child("imageID").getValue().toString();
				if(!imageID.equals("null"))
				try {
					File contactImageFile = File.createTempFile("images", "jpg");
					final Uri contactImageUri = Uri.parse(contactImageFile.getAbsolutePath());
					storageReference.child(imageID).getFile(contactImageFile)
							.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
						@Override
						public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
							friendImage.setImageURI(contactImageUri);
							progress.dismiss();
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							// no image, or image download failed
							progress.dismiss();
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
					progress.dismiss();
				}


			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

	}
}

