package com.latifah.socialshopping.Profil;


import android.animation.IntArrayEvaluator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.latifah.socialshopping.Login.LoginActivity;
import com.latifah.socialshopping.R;

import org.w3c.dom.Text;

/**
 * Created by User on 10/12/2017.
 */

public class SignOutFragment extends Fragment {
    private static final String TAG = "SignOutFragment";
    //firebase
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressBar mProgressBar;
    private TextView tvSignOut, tvSigningOut;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_signout, container, false);
        tvSignOut=(TextView)view.findViewById(R.id.tvConfirmSignOut);
        mProgressBar=(ProgressBar)view.findViewById(R.id.progressBar);
        Button btnConfirmSignOut=(Button) view.findViewById(R.id.btnConfirmSignOut);
        tvSigningOut=(TextView)view.findViewById(R.id.tvSigningOut);

        mProgressBar.setVisibility(View.GONE);
        tvSigningOut.setVisibility(View.GONE);

        setupFirebaseAuth();

        btnConfirmSignOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: attempting to sign out");
                mProgressBar.setVisibility(View.VISIBLE);
                tvSigningOut.setVisibility(View.VISIBLE);
                mAuth.signOut();
                getActivity().finish();
            }
        });
        return view;
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
                    Log.d(TAG,"onAuthStateChanged:navigating back to log in screen.");
                    Intent intent=new Intent(getActivity(),LoginActivity.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                //....
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseUser user= (mAuth.getCurrentUser());

    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener!=null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
