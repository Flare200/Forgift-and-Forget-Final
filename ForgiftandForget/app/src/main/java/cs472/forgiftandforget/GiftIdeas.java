package cs472.forgiftandforget;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;

import cs472.forgiftandforget.DatabaseClasses.Gift;

public class GiftIdeas extends AppCompatActivity {

	ImageView photo[] = new ImageView[3];
	Button photoButton;
	Bitmap defaultBitmap = null;
	TextView nameField;
	EditText notesField;
	Uri imageUri;
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
		nameField = (TextView) findViewById(R.id.giftIdeaName);
		notesField = (EditText) findViewById(R.id.giftIdeaNotes);

		photoButton = (Button) findViewById(R.id.giftPhotoButtonGallery);
		photo[0] = (ImageView) findViewById(R.id.giftPhoto1);
		photo[1] = (ImageView) findViewById(R.id.giftPhoto2);
		photo[2] = (ImageView) findViewById(R.id.giftPhoto3);
		photo[0].setImageBitmap(null);
		photo[1].setImageBitmap(null);
		photo[2].setImageBitmap(null);
		photoButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, 0);
			}
		});

		giftReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				thisGift = dataSnapshot.getValue(Gift.class);
				loadGift();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});


	}

	private void openGallery() {
		Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		startActivityForResult(gallery, PICK_IMAGE);
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
				if (defaultBitmap == ((BitmapDrawable) photo[0].getDrawable()).getBitmap()) {
					photo[0].setImageBitmap(bitmap);
				} else if (defaultBitmap == ((BitmapDrawable) photo[1].getDrawable()).getBitmap()) {
					photo[1].setImageBitmap(bitmap);
				} else if (defaultBitmap == ((BitmapDrawable) photo[2].getDrawable()).getBitmap()) {
					photo[2].setImageBitmap(bitmap);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void loadGift(){
		nameField.setText(thisGift.name);
		notesField.setText(thisGift.description);
	}
}
