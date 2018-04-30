package cs472.forgiftandforget;

import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.util.Linkify;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.io.IOException;

import cs472.forgiftandforget.DatabaseClasses.Database;
import cs472.forgiftandforget.DatabaseClasses.Gift;

public class GiftedIdeas extends AppCompatActivity {
	String giftID;
	String friendID;
	Gift thisGift;
	DatabaseReference giftedReference;
	TextView notesField;
	EditText urlField;

	Uri images[];
	ImageView photo[] = new ImageView[3];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gift_ideas);

		//remove most functionality
		LinearLayout invisibleButtons = (LinearLayout) findViewById(R.id.linear4);
		invisibleButtons.setVisibility(View.GONE);
		notesField = (TextView) findViewById(R.id.giftIdeaNotes);
		urlField = (EditText) findViewById(R.id.url);
		urlField.setInputType(InputType.TYPE_NULL);

		photo[0] = (ImageView) findViewById(R.id.giftPhoto1);
		photo[1] = (ImageView) findViewById(R.id.giftPhoto2);
		photo[2] = (ImageView) findViewById(R.id.giftPhoto3);
		images = new Uri[3];

		giftID = getIntent().getStringExtra("giftID");
		friendID = getIntent().getStringExtra("friendID");

		//set up clickable urls
		urlField = (EditText) findViewById(R.id.url);
		urlField.setLinksClickable(true);
		urlField.setAutoLinkMask(Linkify.WEB_URLS);
		urlField.setLinkTextColor(Color.parseColor("#0e69f9"));
		Linkify.addLinks(urlField, Linkify.WEB_URLS);

		giftedReference = Gift.GetGiftedListReference().child(Database.GetCurrentUID())
				.child(friendID).child(giftID);
		giftedReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				thisGift = dataSnapshot.getValue(Gift.class);
				loadGift();
				StorageReference storageReference = FirebaseStorage.getInstance().getReference("giftImages");
				for (int i = 0; i < photo.length; i++) {
					String currentImageID = "";
					final int loc = i;
					switch(loc){
						case 0:currentImageID = thisGift.imageID1;break;
						case 1:currentImageID = thisGift.imageID2;break;
						case 2:currentImageID = thisGift.imageID3;break;
					}
					try {
						File giftImageFile = File.createTempFile("images", "jpg");
						final Uri contactImageUri = Uri.parse(giftImageFile.getAbsolutePath());
						storageReference.child(currentImageID).getFile(giftImageFile)
								.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
									@Override
									public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
										photo[loc].setImageURI(contactImageUri);
									}
								}).addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {

							}
						});
					} catch (IOException e) {
						e.printStackTrace();

					}
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

	}
	public void loadGift(){
		setTitle(thisGift.name);
		notesField.setText(thisGift.description);
		urlField.setText(thisGift.url);
	}
}
