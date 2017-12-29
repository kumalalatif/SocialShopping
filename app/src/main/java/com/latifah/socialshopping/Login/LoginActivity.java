package com.latifah.socialshopping.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.EventLogTags;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.latifah.socialshopping.Home.HomeActivity;
import com.latifah.socialshopping.R;

/**
 * Created by User on 15/12/2017.
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG="LoginActivity";
    ///firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mProgressBar=(ProgressBar)findViewById(R.id.Progressbar);
        mPleaseWait=(TextView)findViewById(R.id.pleaseWait);
        mEmail=(EditText)findViewById(R.id.input_email);
        mPassword=(EditText)findViewById(R.id.input_password);
        mContext=LoginActivity.this;

        Log.d(TAG,"onCreate: started.");
        mPleaseWait.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        setupFirebaseAuth();
        init();
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
    private void init(){
        final Button btnLogin=(Button)findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: attempting to login.");
                String Email=mEmail.getText().toString();
                String Password=mPassword.getText().toString();
                if(IsStringNull(Email)&&IsStringNull(Password)){
                    Toast.makeText(mContext,"You must fill out all of the field", Toast.LENGTH_SHORT).show();
                }
                else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mPleaseWait.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(Email,Password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG,"signInWithEmail:onComplete: "+task.isSuccessful());
                            FirebaseUser user=mAuth.getCurrentUser();

                            /// //if, sign in fails, display amessage to the user .If sign in succeeds
                            ///the auth state listener will be notified and logic to handle the sign in user can be handled in the listener
                            if(!task.isSuccessful()){
                                Log.w(TAG,"signInWithEmail: failed", task.getException());
                                Toast.makeText(mContext,getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.GONE);
                                mPleaseWait.setVisibility(View.GONE);
                            }
                            else {
                                try{
                                    if(user.isEmailVerified()){
                                        Log.d(TAG,"onComplete; success. Email is verified.");
                                        Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
                                        startActivity(intent);
                                    }else {
                                        Toast.makeText(mContext,"Email is not verified \n Check your email inbox.",Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        mPleaseWait.setVisibility(View.GONE);
                                        mAuth.signOut();
                                    }
                                }catch (NullPointerException e){
                                    Log.d(TAG,"onComplete: NullPointerException "+ e.getMessage());

                                }
                            }
                        }
                    });
                }
            }
        });
        TextView linkSignUp= (TextView)findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: navigating to register screen");
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
        /*If user is logged in then navigated to home activity and finish*/
        if(mAuth.getCurrentUser()!=null){
            Intent intent=new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
    /*
    * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG,"setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener!=null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
