package com.example.kinny.instagram_clone;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserFeed extends AppCompatActivity {

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    LinearLayout linearLayout;
    String userEmail;
    String currentUser;
    ArrayList<String> imageLinks;
    HashMap<String, String> imagesInFirebase;
    String activeUser = UserList.currentUserName;
    boolean checkingHisOwnList = false;
    boolean userHasImages = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);

        Intent i = getIntent();
        final String usernameForFeed = i.getStringExtra("username");

        if(usernameForFeed.equals(activeUser)){
            checkingHisOwnList = true;
        }

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
                    imagesInFirebase = new HashMap<String, String>();

                    currentUserData.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot data: dataSnapshot.getChildren()){
                                    imageLinks.add(String.valueOf(data.getValue()));
                                    imagesInFirebase.put(String.valueOf(data.getKey()), String.valueOf(data.getValue()));
                                }

                                Iterator it = imagesInFirebase.entrySet().iterator();

                                while(it.hasNext()){
                                    final Map.Entry pair = (Map.Entry)it.next();

                                    final String currentImage = String.valueOf(pair.getValue());
                                    String exactStorageLocation = currentImage.replace("%40", "@");
                                    StorageReference current = storage.getReferenceFromUrl(exactStorageLocation);

                                    final ImageView image = new ImageView(getApplicationContext());
                                    image.setLayoutParams(new ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            500
                                    ));
                                    image.getLayoutParams().height = 1000;
                                    image.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                                    image.setId(View.generateViewId());
                                    image.setTag(String.valueOf(pair.getValue()));

                                    linearLayout.addView(image);

                                    current.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            // Got the download URL for 'users/me/profile.png'
                                            // Pass it to Picasso to download, show in ImageView and caching
                                            Picasso.with(getApplicationContext()).load(uri.toString()).into(image);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle any errors
                                        }
                                    });

                                    if(checkingHisOwnList){
                                        image.setOnLongClickListener(new View.OnLongClickListener() {
                                            @Override
                                            public boolean onLongClick(View v) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(UserFeed.this, R.style.myDialog));
                                                builder
                                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                                        .setTitle("Delete Image")
                                                        .setMessage("Are you sure you want to delete this image")
                                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener(){

                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                String storageLocationTag = (String) image.getTag();
                                                                storageLocationTag = storageLocationTag.replace("%40", "@");
                                                                Log.i("Storage Location", storageLocationTag);
                                                                StorageReference desertRef = storage.getReferenceFromUrl(storageLocationTag);
                                                                imagesInFirebase.remove(pair.getKey());
                                                                currentUserData.child(String.valueOf(pair.getKey())).removeValue();
                                                                linearLayout.removeView(linearLayout.findViewById(image.getId()));
                                                                Log.i("Array size", String.valueOf(imageLinks.size()));
                                                                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        Toast.makeText(getApplicationContext(), "Photo Deleted!", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception exception) {
                                                                        // Uh-oh, an error occurred!
                                                                        Toast.makeText(getApplicationContext(), "Something Went Wrong!", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                                Toast.makeText(getApplicationContext(), "Yes", Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .setNegativeButton("No", null)
                                                        .show();
                                                return true;
                                            }
                                        });
                                    }
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
