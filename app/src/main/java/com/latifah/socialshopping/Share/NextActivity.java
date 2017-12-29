package com.latifah.socialshopping.Share;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.latifah.socialshopping.R;
import com.latifah.socialshopping.Utils.FirebaseMethods;
import com.latifah.socialshopping.Utils.UniversalImageLoader;

/**
 * Created by User on 20/12/2017.
 */

public class NextActivity extends AppCompatActivity {
    private static final String TAG="NextActivity";

    //firebase authentification
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    //widgets
    EditText mCaption;
    //vars
    private String mAppend="file:/";
    private int imageCount=0;
    private String imgUrl;
    private Bitmap bitmap;
    private Intent intent;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        Log.d(TAG,"onCreate: got the chosen image: "+ getIntent().getStringExtra(getString(R.string.selected_image)));

        mFirebaseMethods=new FirebaseMethods(NextActivity.this);
        mCaption=(EditText)findViewById(R.id.caption);

        setupFirebaseAuth();

        ImageView backArrow=(ImageView)findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: closing the activity.");
                finish();
            }
        });

        TextView share=(TextView)findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: navigating to final share screen");
                //upload the image to firebase
                Toast.makeText(NextActivity.this, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();
                String caption=mCaption.getText().toString();
                if(intent.hasExtra(getString(R.string.selected_image))){
                    imgUrl=intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,imgUrl,null);
                }
                else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                    bitmap=(Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,null,bitmap);
                }

            }
        });
        setImage();
    }
    private void someMethod(){
        /*
        *Step 1)
        * create a data model for photos
        *
        * step 2)
        * Add properties to the photo object (caption, date, imageURL, photo_id, tags, user_id)
        * Step 3)
        * Count the numberof photos that the user already has
        * step 4)
        * a)upload photo to firebase storage
        * b)insert into 'photos' node
        * insert into 'user_photos' node
         */
    }
    /**
     * gets the image url from the incoming intent and displays the chosen image
     */
    private void setImage(){
        intent=getIntent();
        ImageView image=(ImageView)findViewById(R.id.imageShare);
        if(intent.hasExtra(getString(R.string.selected_image))){
            imgUrl=intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG,"setImage: got new image url: "+ imgUrl);
            UniversalImageLoader.setImage(imgUrl,image,null,mAppend);
        }
        else if(intent.hasExtra(getString(R.string.selected_bitmap))){
            bitmap=(Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG,"setImage: got new bitmap");
            image.setImageBitmap(bitmap);
        }
    }


    /*
    * ******************************** firebase **********************************************
     */
    /*
    * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG,"setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        Log.d(TAG,"onDataChange: image count: "+ imageCount);

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();

                //check if the current user is logged in
                if(user!=null){
                    //user is sign in
                    Log.d(TAG,"onAuthStateChanged:signed_in:"+user.getUid());
                }
                else {
                    //User is sign out
                    Log.d(TAG,"onAuthStateChanged:signed_out");
                }
                //....
            }
        };
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG,"onDataChange: image count: "+ imageCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener!=null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
