package com.example.kinny.instagram_clone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    EditText password;
    EditText email;
    TextView changeSingupLoginMode;
    Button signupButton;
    ImageView logo;
    RelativeLayout relativeLayout;
    EditText username;

    DatabaseReference users;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    Boolean signupModeActive;


    FirebaseUser user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Write a message to the database


        signupModeActive = true;
        // Firebase Database reference
        users = FirebaseDatabase.getInstance().getReference();


        // Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    makeToast("Welcome back");
                    showUserList();
                } else {
                    // User is signed out
                    makeToast("Welcome");
                }
            }
        };

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        signupButton = (Button) findViewById(R.id.LoginSignup);
        logo = (ImageView) findViewById(R.id.mainPageLogo);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        changeSingupLoginMode = (TextView) findViewById(R.id.changeButtonText);
        username = (EditText) findViewById(R.id.username);

        changeSingupLoginMode.setOnClickListener(this);
        logo.setOnKeyListener(this);
        relativeLayout.setOnClickListener(this);

        email.setOnKeyListener(this);
        password.setOnKeyListener(this);
        username.setOnClickListener(this);


    }

    public void showUserList() {
        Intent i = new Intent(getApplicationContext(), UserList.class);
        startActivity(i);
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void createAccount(final String accountEmail, final String accountPassword, final String accountUsername){
        mAuth.createUserWithEmailAndPassword(accountEmail, accountPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i("Sign Up", "createUserWithEmail:success");

                            User newUser = new User(accountEmail, accountPassword, accountUsername);
                            myRef.push().setValue(newUser);
                            makeToast("Successfully Signed Up");
                            showUserList();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i("Sign Up", "createUserWithEmail:failure", task.getException());
                            task.getException();
                            Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signupOrLogin(View view) {

        if (TextUtils.isEmpty(String.valueOf(email.getText()))) {
            this.makeToast("Please enter an email id!");
            return;
        }

        if (TextUtils.isEmpty(String.valueOf(password.getText()))) {
            this.makeToast("Please enter a password");
            return;
        }

        if (!isValidEmail(String.valueOf(email.getText()))) {
            this.makeToast("Invalid email address!!");
            return;
        } else {

            if (signupModeActive) {

                if (TextUtils.isEmpty(String.valueOf(username.getText()))) {
                    this.makeToast("Please enter a username");
                    return;
                }

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        boolean usernamTaken = false;

                        for(DataSnapshot data: dataSnapshot.getChildren()){

                            //Log.i("username", String.valueOf(data.child("username").getValue()));
                            if ((data.child("username").getValue()).equals(String.valueOf(username.getText()))) {
                                Log.i("name", String.valueOf(username.getText()));
                                Log.i("name", String.valueOf(dataSnapshot));
                                usernamTaken = true;
                                break;
                            }
                        }

                        if(usernamTaken){
                            makeToast("Username Taken, Try Something else");
                        }else{
                            createAccount(String.valueOf(email.getText()), String.valueOf(password.getText()), String.valueOf(username.getText()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("data", "Failed to read value.", error.toException());
                    }
                });

            } else {
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
                                } else {
                                    makeToast("Successfull Logged in");
                                    showUserList();
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

    public void makeToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.changeButtonText) {
            if (signupModeActive == true) {
                signupModeActive = false;
                changeSingupLoginMode.setText("Sign Up");
                signupButton.setText("Log In");
                username.setVisibility(View.INVISIBLE);
            } else {
                signupModeActive = true;
                changeSingupLoginMode.setText("Log In");
                signupButton.setText("Sign Up");
                username.setVisibility(View.VISIBLE);
            }
        } else if (v.getId() == R.id.mainPageLogo || v.getId() == R.id.relativeLayout) {

            // removing keyboard form the app if clicked somewhere else

            InputMethodManager inm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            signupOrLogin(v);
        }
        return false;
    }
}
