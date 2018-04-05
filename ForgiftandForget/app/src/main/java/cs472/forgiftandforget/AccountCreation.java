package cs472.forgiftandforget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import cs472.forgiftandforget.DatabaseClasses.Database;


public class AccountCreation extends AppCompatActivity implements View.OnClickListener {

	static final int PASSWORD_MIN_LENGTH = 6;
	ProgressBar progressBar;
	EditText emailField;
	EditText passwordField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_creation);

		progressBar = (ProgressBar) findViewById(R.id.progress);
		emailField = (EditText) findViewById(R.id.emailField);
		passwordField = (EditText) findViewById(R.id.passwordField);

		findViewById(R.id.buttonRegister).setOnClickListener(this);
		findViewById(R.id.buttonAlreadyRegistered).setOnClickListener(this);
	}

	private void registerUser() {
		String email = emailField.getText().toString().trim();
		String password = passwordField.getText().toString().trim();

		//Validate email
		if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			emailField.setError("Invalid Email");
			emailField.requestFocus();
			return;
		}

		//Validate password
		if (password.isEmpty()) {
			passwordField.setError("Password Required");
			passwordField.requestFocus();
			return;
		}
		if (password.length() < PASSWORD_MIN_LENGTH) {
			passwordField.setError("Password must exceed 6 characters");
			passwordField.requestFocus();
			return;
		}

		//Validations complete, initiate registration
		progressBar.setVisibility(View.VISIBLE);
		Database.GetInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				progressBar.setVisibility(View.GONE);
				if (task.isSuccessful()) {
					Database.GetInstance().signOut();
					finish();
					Toast.makeText(getApplicationContext(), "Thank you for registering. Please Sign in", Toast.LENGTH_LONG).show();
					startActivity(new Intent(AccountCreation.this, MainActivity.class));
				} else {

					if (task.getException() instanceof FirebaseAuthUserCollisionException) {
						Toast.makeText(getApplicationContext(), "You are already registered. Please Sign in", Toast.LENGTH_LONG).show();
						startActivity(new Intent(AccountCreation.this, MainActivity.class));

					} else {
						//ToDo: add in additional error handling via task.getException()
					}
				}
			}
		});


	}

	//switch statement to handle all button clicks
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.buttonRegister:
				registerUser();
				break;

			case R.id.buttonAlreadyRegistered:
				finish();
				startActivity(new Intent(AccountCreation.this, MainActivity.class));
				break;
		}
	}
}
