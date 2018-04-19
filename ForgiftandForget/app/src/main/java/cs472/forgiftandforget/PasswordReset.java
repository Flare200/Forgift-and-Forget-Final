package cs472.forgiftandforget;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class PasswordReset extends AppCompatActivity {



	FirebaseAuth firebaseAuth;
	EditText emailField;
	static final String INVALID_EMAIL = "There is no user record corresponding to this identifier. The user may have been deleted.";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password_reset);

	}

	public void resetPassword(View view){
		firebaseAuth = FirebaseAuth.getInstance();
		emailField = (EditText) findViewById(R.id.emailField);
		final String email = emailField.getText().toString().trim();

		//Validate email
		if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			emailField.setError("Invalid Email");
			emailField.requestFocus();
			return;
		}
		firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if(task.isSuccessful()){
					Toast.makeText(getApplicationContext(), "Password Reset email has been sent\nFollow Instructions to reset password", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(PasswordReset.this, MainActivity.class);
					finish();
					startActivity(intent);

				}else{
					String error = task.getException().getMessage();
					if(error.equals(INVALID_EMAIL)) {
						emailField.requestFocus();
						emailField.setError(error);
					}else {
						Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
					}
				}
			}
		});
	}
}
