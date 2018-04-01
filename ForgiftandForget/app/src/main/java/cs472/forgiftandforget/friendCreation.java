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
import android.widget.EditText;
import cs472.forgiftandforget.DatabaseClasses.database;
import cs472.forgiftandforget.DatabaseClasses.friend;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.FileNotFoundException;

public class FriendCreation extends AppCompatActivity implements View.OnClickListener
{
    EditText nameField;
    database database;
    ImageView friendImage;
    static final int GALLERY = 1;
    Uri uri;
    int ret;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_creation);
        friendImage = (ImageView) findViewById(R.id.contactImage);
        nameField = (EditText) findViewById(R.id.nameField);
        database = new database();
        friendImage.setOnClickListener(this);
    }

    public void addNewFriend(View view)
    {
        final String newName = nameField.getText().toString().trim();
        friend newFriend = new friend(newName);
        ret = database.addFriend(newFriend, uri);
        if (ret != 0) {
            Toast.makeText(getApplicationContext(), "Could not add Friend", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), newName + " Added to Friend's List", Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent(FriendCreation.this, FriendList.class);
        finish();
        startActivity(intent);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent(FriendCreation.this, FriendList.class);
            finish();
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(gallery, GALLERY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == GALLERY && resultCode == RESULT_OK) {
            uri = data.getData();
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                friendImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

