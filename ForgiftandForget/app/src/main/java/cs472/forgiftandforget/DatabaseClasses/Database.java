package cs472.forgiftandforget.DatabaseClasses;

import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by mike_ on 3/7/2018.
 */

public class Database extends AppCompatActivity {

	private static FirebaseAuth firebaseAuthInstance = null;
	private static FirebaseUser currentUser = null;
	private static String currentUID = null;
	public static int errorCode;       // return value, 0 success, non zero specific error codes

	public static FirebaseAuth GetInstance() {
		return FirebaseAuth.getInstance();
	}

	public static FirebaseUser GetCurrentUser() {
		return GetInstance().getCurrentUser();
	}

	public static String GetCurrentUID() {
		return GetCurrentUser().getUid();
	}

}