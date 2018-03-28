package cs472.forgiftandforget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

public class ideaPage extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idea_page);
        setTitle(getIntent().getStringExtra("event"));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent(ideaPage.this, friendList.class);
            finish();
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}
