package cs472.forgiftandforget;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import cs472.forgiftandforget.DatabaseClasses.Gift;

public class GiftIdeas extends AppCompatActivity implements View.OnClickListener{

	ImageView photo[] = new ImageView[3];
	TextView urlField;
	EditText notesField;
	CardView saveGift;
	CardView giveGift;
	CardView visitURL;
	Uri images[];
	String friendID;  // will need this later for sending to gifted list
	String giftID;
	Gift thisGift;
	DatabaseReference giftReference;
	private static final int PICK_IMAGE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gift_ideas);

		friendID = getIntent().getStringExtra("friendID");
		giftID = getIntent().getStringExtra("giftID");
		giftReference = Gift.GetGiftsReference().child(giftID);
		notesField = (EditText) findViewById(R.id.giftIdeaNotes);
		urlField = (TextView) findViewById(R.id.url);
		saveGift = (CardView) findViewById(R.id.saveGift);
		giveGift = (CardView) findViewById(R.id.giveGift);
		visitURL = (CardView) findViewById(R.id.visitURL);
		urlField = (TextView) findViewById(R.id.url);
		saveGift.setOnClickListener(this);
		images = new Uri[3];

		photo[0] = (ImageView) findViewById(R.id.giftPhoto1);
		photo[1] = (ImageView) findViewById(R.id.giftPhoto2);
		photo[2] = (ImageView) findViewById(R.id.giftPhoto3);
		// set listeners for each image view
		for(int i = 0; i < photo.length; i++ ) {
			final int loc = i;
			photo[loc].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(intent, loc);
				}
			});
		}

		giftReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				thisGift = dataSnapshot.getValue(Gift.class);
				loadGift();
				// ToDo get images downloaded. im having trouble with this part.
			}
			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			Uri targetUri = data.getData();
			Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
				if (requestCode == 0) {
					photo[0].setImageBitmap(bitmap);
					images[0] = targetUri;
				} else if (requestCode == 1) {
					photo[1].setImageBitmap(bitmap);
					images[1] = targetUri;
				} else if (requestCode == 2) {
					photo[2].setImageBitmap(bitmap);
					images[2] = targetUri;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void loadGift(){
		setTitle(thisGift.name);
		notesField.setText(thisGift.description);
	}

	// switch statement to handle all button clicks by id
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.url:
				// do url edit, maybe dialog
				// ToDO edit url dialog
				break;
			case R.id.saveGift:
				//update gift in db
				updateGift();
				break;
			case R.id.giveGift:
				// send to gifted list
				// ToDO send to gifted list
				break;
			case R.id.visitURL:
				// open url
				// ToDo open url externally
				break;
		}
	}


	public void updateGift(){
		thisGift.addImages(images);
		thisGift.description = notesField.getText().toString().trim();
		thisGift.url = urlField.getText().toString().trim();
		thisGift.updateGift(giftID);
	}

	public void loadImages(){

	}



}
