package com.example.kinny.instagram_clone;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class UserList extends AppCompatActivity {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef;

    ArrayList<String> usersList;
    ArrayAdapter arrayAdapter;
    ListView listview;
    String user = MainActivity.user.getEmail();
    ProgressBar progressBar;
    Button shareImage;
    public static String currentUser;
    String currentUserName;
    DatabaseReference ref;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_list, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);


        mStorageRef = FirebaseStorage.getInstance().getReference();

        listview = (ListView) findViewById(R.id.userlist);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), UserFeed.class);
                i.putExtra("username", usersList.get(position));
                startActivity(i);
            }
        });

        shareImage = (Button) findViewById(R.id.share);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);


        ref = FirebaseDatabase.getInstance().getReference("users");
        ref.addListenerForSingleValueEvent(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    usersList = new ArrayList<String>();

                    for(DataSnapshot data: dataSnapshot.getChildren()){
                        if(!user.equals(String.valueOf((data.child("email")).getValue()))){
                            usersList.add(String.valueOf((data.child("username")).getValue()));
                        }else{
                            //Log.i("Data", String.valueOf(data.child()))
                            currentUser = String.valueOf(data.getKey());
                            currentUserName = String.valueOf((data.child("username")).getValue());
                        }
                    }

                    populatingListview(usersList);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //handle databaseError
                    Log.w("data", "Failed to read value.", databaseError.toException());
                }
            });


    }

    public void populatingListview(ArrayList<String> users){
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, users);
        listview.setAdapter(arrayAdapter);
    }

    public void shareImage(View view){
        // Code for picking a media
        // This piece od code will allow to get a photo using either camera or device store
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.Logout){
            FirebaseAuth.getInstance().signOut();

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }

        if(id == R.id.myfeed){
            Intent i = new Intent(getApplicationContext(), UserFeed.class);
            i.putExtra("username", currentUserName);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            // accessing the image
            Uri selectedImage = data.getData();

            try{

                // getting and converting image into byte array
                Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                Log.i("Imageinfo", "ImageRecieved");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                String path = MainActivity.user.getEmail() + "/" + UUID.randomUUID() + ".png";
                mStorageRef = storage.getReference(path);

                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setCustomMetadata("text", MainActivity.user.getEmail()).build();

                progressBar.setVisibility(View.VISIBLE);
                shareImage.setEnabled(false);
                final UploadTask uploadTask = mStorageRef.putBytes(byteArray, metadata);
                uploadTask.addOnSuccessListener(UserList.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        shareImage.setEnabled(true);

                        ref.addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot data: dataSnapshot.getChildren()){
                                        if(String.valueOf(data.getKey()).equals(currentUser)) {
                                            ref.child(String.valueOf(currentUser)).child("images").push().setValue(String.valueOf(mStorageRef));
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //handle databaseError
                                    Log.w("data", "Failed to read value.", databaseError.toException());
                                }
                            });

                        //MainActivity.database.getReference().child("users").push().setValue("Hello");
                        Log.i("current user", currentUser);
                        Log.i("download link", String.valueOf(mStorageRef));
                        Toast.makeText(getApplicationContext(), "Photo Uploaded!", Toast.LENGTH_LONG).show();
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }
}
