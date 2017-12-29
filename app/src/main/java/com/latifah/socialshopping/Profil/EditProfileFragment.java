package com.latifah.socialshopping.Profil;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.latifah.socialshopping.R;
import com.latifah.socialshopping.Share.ShareActivity;
import com.latifah.socialshopping.Utils.FirebaseMethods;
import com.latifah.socialshopping.Utils.UniversalImageLoader;
import com.latifah.socialshopping.dialogs.ConfirmPasswordDialog;
import com.latifah.socialshopping.models.UserAccountSettings;
import com.latifah.socialshopping.models.UserSettings;
import com.latifah.socialshopping.models.Users;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by User on 10/12/2017.
 */

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener{

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG,"onConfirmPassword: got the password "+ password);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        //////////////////// Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");
                            ///////////////////////check to see if the emailis not alreadypresent in database
                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if(task.isSuccessful()){
                                        try{
                                            if(task.getResult().getProviders().size()==1){
                                                Log.d(TAG,"onComplete: that emailis already in use.");
                                                Toast.makeText(getActivity(),"That email is already in use", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Log.d(TAG,"onComplete: That email is available.");
                                                /////the email is available so we must to update it
                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User email address updated.");
                                                                    Toast.makeText(getActivity(),"Email updated", Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });
                                            }
                                        }catch (NullPointerException e){
                                            Log.d(TAG,"onComplete: NullPointerException: "+e.getMessage());
                                        }
                                    }
                                }
                            });
                        }else {
                            Log.d(TAG,"onComplete: re-authentication failed.");

                        }
                    }
                });
    }

    private static final String TAG = "EditProfileFragment";

    //firebase authentification
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    //EditProfile Fragment Widget
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;

    //var
    private UserSettings mUserSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto=(CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName=(EditText)view.findViewById(R.id.display_name);
        mUsername=(EditText)view.findViewById(R.id.username);
        mWebsite=(EditText)view.findViewById(R.id.website);
        mDescription=(EditText)view.findViewById(R.id.descriptions);
        mEmail=(EditText)view.findViewById(R.id.email);
        mPhoneNumber=(EditText)view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto=(TextView)view.findViewById(R.id.changeProfilePhoto);
        mFirebaseMethods=new FirebaseMethods(getActivity());


        setupFirebaseAuth();
        //setProfileImage();

        //backarrow for navigating back to "ActivityProfile"
        ImageView backArrow=(ImageView)view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: navigating back to ProfileActivity");
                getActivity().finish();

            }
        });
        ImageView checkMark=(ImageView)view.findViewById(R.id.saveChanges);
        checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: attempting to save changes.");
                saveProfileSettings();
            }
        });
        return view;
    }
    private void saveProfileSettings() {
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        //final String profile_photo=mChangeProfilePhoto.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());

        //case 1: the user made a change username
        if (!mUserSettings.getUsers().getUsername().equals(username)) {
            checkIfUsernameExists(username);
        }
        //case 2: user made a change their email
        if (!mUserSettings.getUsers().getEmail().equals(email)) {
            ///step 1: Reauthenticate
            ///     -Confirm the password and email
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(),getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this,1);
            ///step 2: check if the email is already registered
            ///     -fetchProviderForEmail(String email)

            ///step 3: Change email
            ///     -submit the email to the data base and suthentication
        }
        /**
         * *change the rest of the settingsthat do not require uniqueness
         */
        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
            //update displayName
            mFirebaseMethods.updateUserAccountSettings(displayName,null,null,0);
        }
        if(!mUserSettings.getSettings().getWebsite().equals(website)){
            //update website
            mFirebaseMethods.updateUserAccountSettings(null,website,null,0);
        }
        if(!mUserSettings.getSettings().getDescription().equals(description)){
            //update decription
            mFirebaseMethods.updateUserAccountSettings(null,null,description,0);
        }
//        if(!mUserSettings.getSettings().getPhone_number()){
//            //update phone number
//            mFirebaseMethods.updateUserAccountSettings(null,null,null,phoneNumber);
//        }

    }
    /*
    *check is @param usernamealready exist
    * @param username
     */
    private void checkIfUsernameExists(final  String username) {
        Log.d(TAG,"checkIfUsernameExists : Checking if "+ username +"alreday exist");
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        Query query=reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    //add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(),"That uername saved.",Toast.LENGTH_SHORT).show();
                }
                for(DataSnapshot singleSnapshot:dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG,"checkIfUsemaneExists: FOUND A MATCH: "+singleSnapshot.getValue(Users.class).getUsername());
                        Toast.makeText(getActivity(),"That uername already exist.",Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG,"setProfileWidgets: setting widget width data retrieving from firebase database: "+ userSettings.toString());
        Log.d(TAG,"setProfileWidgets: setting widget width data retrieving from firebase database: "+ userSettings.getUsers().getEmail());
        Log.d(TAG,"setProfileWidgets: setting widget width data retrieving from firebase database: "+ userSettings.getUsers().getPhone_number());

        mUserSettings=userSettings;
        //Users users=userSettings.getUsers();
        UserAccountSettings settings=userSettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null,"");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUsers().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUsers().getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: changing profile photo");
                Intent intent =new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //268435456
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

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
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        userID=mAuth.getCurrentUser().getUid();

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();

                //check if the current user is logged in
                if(user!=null){
                    //user is sign in
                    //Log.d(TAG,"onAuthStateChanged:signed_in:"+user.getUid());
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
                //retrieve user informations from database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
                //retrieve image for the user in question
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
