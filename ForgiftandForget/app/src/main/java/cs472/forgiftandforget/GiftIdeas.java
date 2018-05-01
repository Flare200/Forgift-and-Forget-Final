package cs472.forgiftandforget;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import cs472.forgiftandforget.DatabaseClasses.Event;
import cs472.forgiftandforget.DatabaseClasses.Gift;

public class GiftIdeas extends AppCompatActivity implements View.OnClickListener{

	ImageView photo[] = new ImageView[3];
	EditText urlField;
	EditText notesField;
	CardView saveGift;
	CardView giveGift;
	Uri images[];
	String friendID;  // will need this later for sending to gifted list
	String giftID;
	String eventID;
	String eventListID;
	Gift thisGift;
	DatabaseReference giftReference;
	Spannable spannable;
	private static final int PICK_IMAGE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gift_ideas);

		friendID = getIntent().getStringExtra("friendID");
		giftID = getIntent().getStringExtra("giftID");
		eventID = getIntent().getStringExtra("eventID");
		eventListID = getIntent().getStringExtra("eventListID");
		giftReference = Gift.GetGiftsReference().child(giftID);
		notesField = (EditText) findViewById(R.id.giftIdeaNotes);
		saveGift = (CardView) findViewById(R.id.saveGift);
		giveGift = (CardView) findViewById(R.id.giveGift);
		urlField = (EditText) findViewById(R.id.url);
		saveGift.setOnClickListener(this);
		giveGift.setOnClickListener(this);
		images = new Uri[3];
		// got rid of auto complete. made alot of websites .come for me
		urlField.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		urlField.setLinksClickable(true);
		urlField.setAutoLinkMask(Linkify.WEB_URLS);
		urlField.setLinkTextColor(Color.parseColor("#0e69f9"));
		urlField.setMovementMethod(LinkMovementMethod.getInstance());
		urlField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// if url field loses focus, add a single space, so it remains editable
				// while still allowing the link to be clicked
				if(!hasFocus){
					spannable = new SpannableString(urlField.getText().toString().trim());
					CharSequence text = TextUtils.concat(spannable, " ");
					urlField.setText(text);
					Linkify.addLinks(urlField, Linkify.WEB_URLS);
				}
			}
		});
		urlField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {


			}

			@Override
			public void afterTextChanged(Editable s) {
				Linkify.addLinks(urlField, Linkify.WEB_URLS);
			}
		});

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
		urlField.setText(thisGift.url);
	}

	// switch statement to handle all button clicks by id
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.saveGift:
				//update gift in db
				updateGift();
				Toast.makeText(getApplicationContext(), "Saved Gift Details", Toast.LENGTH_LONG).show();
				finish();
				break;
			case R.id.giveGift:
				// send to gifted list
				verificationDialog();
				break;
		}
	}


	public void updateGift(){
		thisGift.addImages(images);
		thisGift.description = notesField.getText().toString().trim();
		thisGift.url = urlField.getText().toString().trim();
		thisGift.updateGift(giftID);
	}

	public void verificationDialog(){
		AlertDialog.Builder verify = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		verify.setMessage("Give this Gift, and move to the Gifted List?");
		verify.setPositiveButton("Gift", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				updateGift();
				AlertDialog.Builder ask = new AlertDialog.Builder(GiftIdeas.this);
				LayoutInflater inflater2 = LayoutInflater.from(GiftIdeas.this);
				ask.setMessage("Would you like to remove the event");
				ask.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Event.RemoveSingleEvent(eventID, eventListID);
						openGiftedList();
					}
				});
				ask.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						openGiftedList();
					}
				});
				ask.create();
				ask.show();

				thisGift.moveToGifted(giftID, eventID, friendID);
				Gift.RemoveGift(giftID);
			}
		});
		verify.setNegativeButton("Cancel", null);
		verify.create();
		verify.show();


	}

	public void openGiftedList(){
		Intent giftIntent = new Intent(GiftIdeas.this, GiftedList.class);
		giftIntent.putExtra("giftID", giftID);
		giftIntent.putExtra("friendID", friendID);
		finish();
		startActivity(giftIntent);
	}

}
