package com.example.kinny.instagram_clone;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.ViewGroup;
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

    LinearLayout linearLayout;
    private StorageReference storageReference;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);

        Intent i = getIntent();
        final String usernameForFeed = i.getStringExtra("username");

        setTitle(usernameForFeed + "'s Feed");
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        final ImageView imageview = (ImageView) findViewById(R.id.imageView);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.addListenerForSingleValueEvent(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for(DataSnapshot data: dataSnapshot.getChildren()){
                        if(usernameForFeed.equals(String.valueOf(data.child("username").getValue()))){
                            userEmail = String.valueOf(data.child("email").getValue());
                        }
                    }

                    storageReference = FirebaseStorage.getInstance().getReference().child(userEmail + "/3fcca0f2-906c-4042-b895-dfd16d7fd200.png");
                    Log.i("currentStorage", String.valueOf(storageReference));

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(getApplicationContext())
                                    .using(new FirebaseImageLoader())
                                    .load(storageReference)
                                    .into(imageview);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });


                    Log.i("currentStorage", String.valueOf(storageReference));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //handle databaseError
                    Log.w("data", "Failed to read value.", databaseError.toException());
                }
            });

    }
}
