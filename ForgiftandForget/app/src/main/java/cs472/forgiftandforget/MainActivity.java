package cs472.forgiftandforget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import cs472.forgiftandforget.DatabaseClasses.Database;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	static final int PASSWORD_MIN_LENGTH = 6;
	static final String INVALID_PASSWORD = "The password is invalid or the user does not have a password.";
	static final String INVALID_EMAIL = "There is no user record corresponding to this identifier. The user may have been deleted.";
	EditText emailField;
	EditText passwordField;
	ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		progressBar = (ProgressBar) findViewById(R.id.progress);
		emailField = (EditText) findViewById(R.id.emailField);
		passwordField = (EditText) findViewById(R.id.passwordField);

		findViewById(R.id.newUserButton).setOnClickListener(this);
		findViewById(R.id.cardView).setOnClickListener(this);
		findViewById(R.id.forgotPasswordButton).setOnClickListener(this);
	}


	// switch statement to handle all button clicks by id
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.newUserButton:
				startActivity(new Intent(MainActivity.this, AccountCreation.class));
				break;
			case R.id.cardView:
				loginClicked();
				break;
			case R.id.forgotPasswordButton:
				startActivity(new Intent(MainActivity.this, PasswordReset.class));
				break;
		}
	}

	private void loginClicked() {
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


		//Validations complete, initiate sign in
		progressBar.setVisibility(View.VISIBLE);
		Database.GetInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				progressBar.setVisibility(View.GONE);
				if (task.isSuccessful()) {
					finish();
					Intent intent = new Intent(MainActivity.this, FriendList.class);
					startActivity(intent);
				} else {
					String error = task.getException().getMessage().toString();
					switch (error) {
						case INVALID_PASSWORD:
							passwordField.requestFocus();
							passwordField.setError(error);
							break;
						case INVALID_EMAIL:
							emailField.requestFocus();
							emailField.setError(error);
							break;
						default:
							Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
							break;
					}
				}
			}
		});


	}

	@Override
	protected void onStart() {
		super.onStart();

		// check if user is logged in already
		if (Database.GetInstance().getCurrentUser() != null) {
			finish();
			Intent intent = new Intent(MainActivity.this, FriendList.class);
			startActivity(intent);
		}

	}
}

