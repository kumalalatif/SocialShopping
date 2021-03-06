package com.latifah.socialshopping.Login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.latifah.socialshopping.R;
import com.latifah.socialshopping.Utils.FirebaseMethods;
import com.latifah.socialshopping.models.Users;

/**
 * Created by User on 15/12/2017.
 */

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG="RegisterActivity";
    private Context mContext;
    private String email,username,password;
    private EditText mEmail,mUsername,mPassword;
    private TextView loadingPleaseWait;
    private Button btnRegister;
    private ProgressBar mProgressBar;


    ///firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference myRef;
    private String append=" ";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext=RegisterActivity.this;
        firebaseMethods=new FirebaseMethods(mContext);
        Log.d(TAG,"onCreate: started.");

        initWidget();
        setupFirebaseAuth();
        init();

    }
    private void init(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email=mEmail.getText().toString();
                username=mUsername.getText().toString();
                password=mPassword.getText().toString();
                if(checkInputs(email,username,password)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);
                    firebaseMethods.registerNewEmail(email,password,username);
                }
            }
        });
    }
    private boolean checkInputs(String email,String username,String password){
        Log.d(TAG,"checkInputs: checking input for null values.");
        if(email.equals("")||username.equals("")||password.equals("")){
            Toast.makeText(mContext, "All field must be filled out" ,Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }
    /**
     * Initializing activity widgets
     */
    private void initWidget(){
        Log.d(TAG,"initWidget: Initializing Widgets");
        mProgressBar=(ProgressBar)findViewById(R.id.ProgressbarRegister);
        loadingPleaseWait=(TextView)findViewById(R.id.pleaseWaitRegister);
        mEmail=(EditText)findViewById(R.id.input_email);
        mPassword=(EditText)findViewById(R.id.input_password);
        mUsername=(EditText) findViewById(R.id.input_username);
        btnRegister=(Button)findViewById(R.id.btn_register);
        mContext=RegisterActivity.this;
        mProgressBar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);

    }
    private boolean IsStringNull(String string){
        Log.d(TAG, "IsStringNull: checking string if null.");
        if(string.equals("")){
            return true;
        }
        else {
            return false;
        }
    }
    /*
    * ******************************** firebase **********************************************
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
                for(DataSnapshot singleSnapshot:dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG,"checkIfUsemaneExists: FOUND A MATCH: "+singleSnapshot.getValue(Users.class).getUsername());
                        append=myRef.push().getKey().substring(3,10);
                        Log.d(TAG,"onDataChange: username already exists. Appending random string to name:"+ append);

                    }
                }
                String mUsername="";
                mUsername=username + append;
                //add new user to database
                firebaseMethods.addNewUser(email,mUsername,"","", "");
                Toast.makeText(mContext, "Sign up successfull . Sending verification email.", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    /*
    * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG,"setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebasedatabase=FirebaseDatabase.getInstance();
        myRef=mFirebasedatabase.getReference();
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();
                if(user!=null){
                    //user is sign in
                    Log.d(TAG,"onAuthStateChanged:signed_in:"+user.getUid());
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            checkIfUsernameExists(username);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    finish();
                }
                else {
                    //User is sign out
                    Log.d(TAG,"onAuthStateChanged:signed_out");
                }
                //....
            }
        };
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
