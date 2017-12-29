package com.latifah.socialshopping.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.latifah.socialshopping.Home.HomeActivity;
import com.latifah.socialshopping.Profil.AccountSettingsActivity;
import com.latifah.socialshopping.R;
import com.latifah.socialshopping.models.Photo;
import com.latifah.socialshopping.models.UserAccountSettings;
import com.latifah.socialshopping.models.UserSettings;
import com.latifah.socialshopping.models.Users;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by User on 16/12/2017.
 */

public class FirebaseMethods {
    private static final String TAG="FirebaseMethods";
    //FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;

    //var
    private Context mContext;
    private double mPhotoUploadProgress=0;

    public FirebaseMethods(Context mContext) {
        mAuth=FirebaseAuth.getInstance();
        mFirebaseDatabase =FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        mStorageReference= FirebaseStorage.getInstance().getReference();

        this.mContext = mContext;
        if(mAuth.getCurrentUser()!=null){
            userID=mAuth.getCurrentUser().getUid();
        }
    }
    public void uploadNewPhoto(String photoType,final String caption,final int count,String imgURL, Bitmap bm){
        Log.d(TAG,"uploadNewPhoto: attempting to upload new photo.");
        //case 1 new photo
        FilePaths filePaths=new FilePaths();

        if(photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG,"uploadNewPhoto: uploading NEW photo.");
            String user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference=mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE+ "/" + user_id + "/photo" + (count + 1));

            //convert image url to bitmap;
            if(bm == null){
                bm=ImageManager.getBitmap(imgURL);
            }

            byte[] bytes=ImageManager.getBytesFromBitmap(bm,100);


            UploadTask uploadTask=null;
            uploadTask=storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUrl=taskSnapshot.getDownloadUrl();
                    Toast.makeText(mContext,"photo upload succes",Toast.LENGTH_SHORT).show();

                    //add new photo to 'photos' node and 'user_photos' node
                    addPhotoToDatabase(caption,firebaseUrl.toString());

                    //navigate to the main feed so the user can see their photo
                    Intent intent=new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG,"onFailure: Photo upload failed.");
                    Toast.makeText(mContext,"Photo upload failed.",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100*taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(progress-15 > mPhotoUploadProgress){
                        Toast.makeText(mContext,"photo upload progress: " + String.format("%.0f",progress) + "%",Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress=progress;
                    }
                    Log.d(TAG,"onProgress: upload progress: " + progress + "% done");
                }
            });

        }
        //case 2) new profile photo
        else if(photoType.equals(mContext.getString(R.string.profile_photo))){
            Log.d(TAG,"uploadNewPhoto: uploading PROFILE photo.");

            String user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference=mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE+ "/" + user_id + "/profile_photo");

            //convert image url to bitmap;
            if(bm == null){
                bm=ImageManager.getBitmap(imgURL);
            }
            byte[] bytes=ImageManager.getBytesFromBitmap(bm,100);


            UploadTask uploadTask=null;
            uploadTask=storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUrl=taskSnapshot.getDownloadUrl();
                    Toast.makeText(mContext,"photo upload succes",Toast.LENGTH_SHORT).show();

                    //insert into 'users_account settings' node
                    setProfilePhoto(firebaseUrl.toString());

                    ((AccountSettingsActivity)mContext).setViewPager(
                            ((AccountSettingsActivity)mContext).pagerAdapter
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                    );
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG,"onFailure: Photo upload failed.");
                    Toast.makeText(mContext,"Photo upload failed.",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100*taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(progress-15 > mPhotoUploadProgress){
                        Toast.makeText(mContext,"photo upload progress: " + String.format("%.0f",progress) + "%",Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress=progress;
                    }
                    Log.d(TAG,"onProgress: upload progress: " + progress + "% done");
                }
            });
        }

    }
    private void setProfilePhoto(String url){
        Log.d(TAG,"setProfilePhoto: setting new profile image: "+ url);
        myRef.child(mContext.getString(R.string.dbnmae_users_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }
    private String getTimeStamp(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pasific"));
        return sdf.format(new Date());
    }
    private  void addPhotoToDatabase(String caption, String url){
        Log.d(TAG,"addPhotoToDatabase: adding photo to database.");
        String tags= StringManipulation.getTags(caption);
        String newPhotoKey=myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo=new Photo();
        photo.setCaption(caption);
        photo.setDate_create(getTimeStamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);

    }
    public int getImageCount(DataSnapshot dataSnapshot){
        int count=0;
        for(DataSnapshot ds: dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()){
            count++;
        }return count;
    }
    /*
   *update 'user_account_settings' node for current user
   * @param displayName
   * @param website
   * @param description
   * @param phoneNumber
    */
    public void updateUserAccountSettings(String displayName, String website,String description, long phoneNumber){
        Log.d(TAG,"updateUserAccountSettings: updating user account settings.");
        if(displayName!=null){
            myRef.child(mContext.getString(R.string.dbnmae_users_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }
        if(website!=null){
            myRef.child(mContext.getString(R.string.dbnmae_users_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }
        if(description!=null){
            myRef.child(mContext.getString(R.string.dbnmae_users_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }
        if(phoneNumber!=0){
            myRef.child(mContext.getString(R.string.dbnmae_users_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }
    }
    /*
   *update username in 'users' node and 'user_account_settings' node
   * @param username
    */
    public void updateUsername(String username){
        Log.d(TAG,"updateUsername: updating username to "+username);
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
        myRef.child(mContext.getString(R.string.dbnmae_users_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }
    /*
    *update email in 'users' node
    * @param email
     */
    public void updateEmail(String email){
        Log.d(TAG,"updateEmail: updating email to "+email);
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
    }
//    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot){
//        Log.d(TAG,"checkIfUsernameExists: checking if "+ username +" already exists");
//        Users users=new Users();
//        for(DataSnapshot ds:dataSnapshot.child(userID).getChildren()){
//            Log.d(TAG,"checkIfUsernameExists: datasnapshot: "+ ds);
//            users.setUsername(ds.getValue(Users.class).getUsername());
//            Log.d(TAG,"checkIfUsernameExists: username: "+ users.getUsername());
//            if(StringManipulation.expandUsername(users.getUsername()).equals(username)){
//                Log.d(TAG,"checkIfUsernameExists: FOUND A MATCH: "+ users.getUsername());
//                return true;
//            }
//        }
//        return false;
//    }

    /*
        *Register a new email and password to authentication
        * @param email
        * @param password
        * @param username
         */
    public void registerNewEmail(final String email, String password, final String username){
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG,"createUserWithEmail: onComplete: "+task.isSuccessful());
                        ///if, sign in fails, display amessage to the user .If sign in succeeds
                        ///the auth state listener will be notified and logic to handle the sign in user can be handled in the listener
                        if(!task.isSuccessful()){
                            Toast.makeText(mContext, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                        }
                        else if(task.isSuccessful()){
                            sendVerificationEmail();
                            userID=mAuth.getCurrentUser().getUid();
                            Log.d(TAG,"onComplete: Authstate change: "+ userID);
                        }
                    }
                });
    }
    public void sendVerificationEmail(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                            }else {
                                Toast.makeText(mContext,"Couln't send email verification",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    /*
        *Add information to the users node
        * add information to the user_account_settings node
        * @param email
        * @param password
        * @param username
        * @param description
        * @param website
        * @param profile_photo
         */
    public void addNewUser(String email, String username, String description, String website, String profile_photo){
        Users users=new Users(userID ,email, 1, StringManipulation.condenseUsername(username));
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(users);

            UserAccountSettings settings=new UserAccountSettings(
                    description,
                    username,
                    0,
                    0,
                    0,
                    profile_photo,
                    StringManipulation.condenseUsername(username),
                    website,
                    userID);

        myRef.child(mContext.getString(R.string.dbnmae_users_account_settings)).child(userID).setValue(settings);
    }
    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        Log.d(TAG,"getUserAccountSettings: retrieving user account settings from database");
        UserAccountSettings settings=new UserAccountSettings();
        Users users=new Users();
        for (DataSnapshot ds:dataSnapshot.getChildren()){
            //users_account_settings node
            if(ds.getKey().equals(mContext.getString(R.string.dbnmae_users_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: " + ds);

                try {
                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name());
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername());
                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite());
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription());
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo());
                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers());
                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing());
                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts());
                    Log.d(TAG, "getUserAccountSettings: retrieved users_account_settings  information: " + settings.toString());
                } catch (NullPointerException e) {
                    Log.d(TAG, "getUserAccountSettings: NullPointerException " + e.getMessage());
                }
            }
                //users node
                if(ds.getKey().equals(mContext.getString(R.string.dbname_users))){
                    Log.d(TAG,"getUserAccountSettings: datasnapshot: "+ ds);
                    users.setUsername(
                            ds.child(userID)
                            .getValue(Users.class)
                            .getUsername());
                    users.setEmail(
                            ds.child(userID)
                            .getValue(Users.class)
                            .getEmail());
                    users.setPhone_number(
                            ds.child(userID)
                            .getValue(Users.class)
                            .getPhone_number());
                    users.setUser_id(
                            ds.child(userID)
                            .getValue(Users.class)
                            .getUser_id());
                    Log.d(TAG,"getUserAccountSettings: retrieved users  information: "+ users.toString());
            }

        }return new UserSettings(users,settings);
    }
}
