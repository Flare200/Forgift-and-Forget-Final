package cs472.forgiftandforget;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    static final int PASSWORD_MIN_LENGTH = 6;
    FirebaseAuth mAuth;
    EditText emailField;
    EditText passwordField;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        progressBar = (ProgressBar) findViewById(R.id.progress);
        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);

        findViewById(R.id.newUserButton).setOnClickListener(this);
        findViewById(R.id.loginButton).setOnClickListener(this);
        findViewById(R.id.debugButton).setOnClickListener(this);
    }



    // switch statement to handle all button clicks by id
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.newUserButton:
                finish();
                startActivity(new Intent(MainActivity.this, accountCreation.class));
                break;

            case R.id.loginButton:
                loginClicked();
                break;

            case R.id.debugButton:
                sendMessage(view);
                break;
        }
    }

    private void loginClicked(){
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        //Validate email
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailField.setError("Invalid Email");
            emailField.requestFocus();
            return;
        }

        //Validate password
        if(password.isEmpty()){
            passwordField.setError("Password Required");
            passwordField.requestFocus();
            return;
        }
        if(password.length() < PASSWORD_MIN_LENGTH){
            passwordField.setError("Password must exceed 6 characters");
            passwordField.requestFocus();
            return;
        }


        //Validations complete, initiate sign in
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    finish();
                    Intent intent = new Intent(MainActivity.this, friendList.class);
                    startActivity(intent);
                    //ToDo: send UID or email to next activity (Not sure of exact DB structure yet)
                } else {
                    //ToDo: handle exceptions here via task.getException()
                }
            }
        });


    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // check if user is logged in already
        if (mAuth.getCurrentUser() != null)
        {
            finish();
            Intent intent = new Intent(MainActivity.this, friendList.class);
            startActivity(intent);
            //ToDo: send UID or email to next activity (Not sure of exact DB structure yet)
        }

    }

    // BELOW IS FOR DEBUG ONLY DELETE WHEN DONE
    public void sendMessage(View view)
    {
        Context ctx = this;
        EditText input = (EditText) findViewById(R.id.emailField);
        String mode = input.getText().toString();
        Intent intent = null;
        switch (mode)
        {
            case "giftIdeas":   intent = new Intent(ctx, GiftIdeas.class); break;
            case "friend":  intent = new Intent(ctx, friendList.class); break;
            default: intent = new Intent(ctx, friendList.class); break;
        }
        startActivity(intent);
    }
}

