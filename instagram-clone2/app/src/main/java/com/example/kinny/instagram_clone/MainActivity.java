package com.example.kinny.instagram_clone;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

  class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText password;
    EditText email;
    TextView changeSingupLoginMode;
      Button signupButton;
    DatabaseReference users;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    Boolean signupModeActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signupModeActive = true;
        // Firebase Database reference
        users = FirebaseDatabase.getInstance().getReference();

        // Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Signed in", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("Signed Out", "onAuthStateChanged:signed_out");
                }
            }
        };

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        signupButton = (Button) findViewById(R.id.LoginSignup);

        changeSingupLoginMode = (TextView) findViewById(R.id.changeButtonText);
        changeSingupLoginMode.setOnClickListener(this);

    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public void signupOrLogin(View view){
        if(TextUtils.isEmpty(String.valueOf(email.getText()))){
            this.makeToast("Please enter an email id!");
            return;
        }

        if(TextUtils.isEmpty(String.valueOf(password.getText()))){
            this.makeToast("Please enter a password");
            return;
        }

        if(!isValidEmail(String.valueOf(email.getText()))){
            this.makeToast("Invalid email address!!");
            return;
        }else{

            if(signupModeActive){
                mAuth.createUserWithEmailAndPassword(String.valueOf(email.getText()), String.valueOf(email.getText()))
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.i("Sign Up", "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                makeToast("Successfully Signed Up");
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.i("Sign Up", "createUserWithEmail:failure", task.getException());
                                task.getException();
                                Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }else{
                mAuth.signInWithEmailAndPassword(String.valueOf(email.getText()), String.valueOf(password.getText()))
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d("Login", "LogIn:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w("Login", "signInWithEmail:failed", task.getException());
                                    Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    makeToast("Successfull Logged in");
                                }
                            }
                        });
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public  void makeToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.changeButtonText){
            if(signupModeActive == true){
                signupModeActive = false;
                changeSingupLoginMode.setText("Sign Up");
                signupButton.setText("Log In");
            }else{
                signupModeActive = true;
                changeSingupLoginMode.setText("Log In");
                signupButton.setText("Sign Up");
            }
        }
    }
}
