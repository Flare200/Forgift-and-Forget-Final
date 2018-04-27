package cs472.forgiftandforget;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import cs472.forgiftandforget.DatabaseClasses.Database;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	static final int PASSWORD_MIN_LENGTH = 6;
	static final String INVALID_PASSWORD = "The password is invalid or the user does not have a password.";
	static final String INVALID_EMAIL = "There is no user record corresponding to this identifier. The user may have been deleted.";
	EditText emailField;
	EditText passwordField;
	ProgressBar progressBar;
	EditText dialogEmailField;
	EditText dialogPasswordField;
	String email;
	boolean badEmail = false;
	boolean noPassword = false;
	boolean passwordLength = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
				newUserDialog();
				break;
			case R.id.cardView:
				loginClicked();
				break;
			case R.id.forgotPasswordButton:
				forgotPasswordDialog();
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
					String error = task.getException().getMessage();
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

	public void newUserDialog(){
		final AlertDialog.Builder newUser = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		final View dialogView = inflater.inflate(R.layout.new_user_dialogue, null);
		// load previous values, if this is a re-dialog
		if(dialogEmailField != null){
			String oldEmail = dialogEmailField.getText().toString().trim();
			dialogEmailField = (EditText) dialogView.findViewById(R.id.dialog_email);
			dialogEmailField.setText(oldEmail);
		}else{
			dialogEmailField = (EditText) dialogView.findViewById(R.id.dialog_email);
		}
		dialogPasswordField = (EditText) dialogView.findViewById(R.id.dialog_password);

		if(badEmail){
			dialogEmailField.setError("Invalid Email");
			dialogEmailField.requestFocus();
			badEmail = false;
		}else if(noPassword){
			dialogPasswordField.setError("Password Required");
			dialogPasswordField.requestFocus();
			noPassword = false;
		}else if(passwordLength){
			dialogPasswordField.setError("Password must exceed 6 characters");
			dialogPasswordField.requestFocus();
			passwordLength = false;
		}

		newUser.setTitle("Register New User");
		newUser.setPositiveButton("Register", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
				email = dialogEmailField.getText().toString().trim();
				String password = dialogPasswordField.getText().toString().trim();
				//Validate email
				if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
					badEmail = true;
					newUserDialog();
					return;
				}

				//Validate password
				if (password.isEmpty()) {
					noPassword = true;
					newUserDialog();
					return;
				}
				if (password.length() < PASSWORD_MIN_LENGTH) {
					passwordLength = true;
					newUserDialog();
					return;
				}

				progressBar.setVisibility(View.VISIBLE);
				Database.GetInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						progressBar.setVisibility(View.GONE);
						if (task.isSuccessful()) {
							Database.GetInstance().signOut();
							Toast.makeText(getApplicationContext(), "Thank you for registering. Please Sign in", Toast.LENGTH_LONG).show();
							emailField.setText(email);
						} else {
							if (task.getException() instanceof FirebaseAuthUserCollisionException) {
								Toast.makeText(getApplicationContext(), "You are already registered. Please Sign in", Toast.LENGTH_LONG).show();
								emailField.setText(email);
							}
						}
						dialogEmailField.setText("");
						dialogPasswordField.setText("");
					}
				});
			}
		});
		newUser.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialogEmailField.setText("");
				dialogPasswordField.setText("");
			}
		});
		newUser.setView(dialogView);
		newUser.show();
	}

	public void forgotPasswordDialog(){
		final AlertDialog.Builder forgotPassword = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		final View dialogView = inflater.inflate(R.layout.forgot_password_dialogue, null);
		final EditText forgotEmailField = (EditText) dialogView.findViewById(R.id.dialog_email);
		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
		forgotPassword.setTitle("Password Recovery");
		forgotPassword.setPositiveButton("Reset Password", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				progressBar.setVisibility(View.VISIBLE);
				String email = forgotEmailField.getText().toString().trim();
				Database.GetInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						progressBar.setVisibility(View.GONE);
						if(task.isSuccessful()){
							Toast.makeText(getApplicationContext(), "Password Reset email has been sent\n" +
									"Follow Instructions to reset password", Toast.LENGTH_LONG).show();
						}else{
							String error = task.getException().getMessage();
							Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
						}
					}
				});

			}
		});
		forgotPassword.setNegativeButton("Cancel", null);
		forgotPassword.setView(dialogView);
		forgotPassword.show();
	}
}

