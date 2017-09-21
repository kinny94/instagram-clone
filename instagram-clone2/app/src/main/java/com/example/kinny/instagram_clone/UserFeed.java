package com.example.kinny.instagram_clone;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserFeed extends AppCompatActivity {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = storage.getReference();

    LinearLayout linearLayout;
    String userEmail;
    String currentUser;
    ArrayList<String> imageLinks;
    boolean userHasImages = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);

        Intent i = getIntent();
        final String usernameForFeed = i.getStringExtra("username");

        setTitle(usernameForFeed + "'s Feed");
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

        ref.addListenerForSingleValueEvent(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for(DataSnapshot data: dataSnapshot.getChildren()){
                        if(usernameForFeed.equals(String.valueOf(data.child("username").getValue()))){
                            userEmail = String.valueOf(data.child("email").getValue());
                            currentUser = String.valueOf(data.getKey());
                            if(data.hasChild("images")){
                                userHasImages = true;
                            }
                        }
                    }

                    if(!userHasImages){
                        Toast.makeText(getApplicationContext(), "User hasn't posted anything yet!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    final DatabaseReference currentUserData = ref.child(currentUser).child("images");
                    Log.i("ImagesLinks", String.valueOf(currentUserData));
                    imageLinks = new ArrayList<String>();

                    currentUserData.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot data: dataSnapshot.getChildren()){
                                    imageLinks.add(String.valueOf(data.getValue()));
                                }

                                for(int i=0;i<=imageLinks.size()-1;i++)
                                {
                                    String exactStorageLocation = imageLinks.get(i).replace("%40", "@");
                                    StorageReference current = storage.getReferenceFromUrl(exactStorageLocation);

                                    ImageView image = new ImageView(getApplicationContext());
                                    image.setLayoutParams(new ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            500
                                    ));
                                    image.getLayoutParams().height = 1000;
                                    image.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                                    image.setId(View.generateViewId());

                                    // Adds the view to the layout
                                    linearLayout.addView(image);

                                    Glide.with(getApplicationContext())
                                            .using(new FirebaseImageLoader())
                                            .load(current)
                                            .into(image);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //handle databaseError
                                Log.w("data", "Failed to read value.", databaseError.toException());
                            }
                        });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //handle databaseError
                    Log.w("data", "Failed to read value.", databaseError.toException());
                }
            });


    }
}
