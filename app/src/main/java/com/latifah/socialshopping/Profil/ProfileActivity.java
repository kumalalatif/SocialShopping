package com.latifah.socialshopping.Profil;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.latifah.socialshopping.R;
import com.latifah.socialshopping.Utils.ViewCommentFragment;
import com.latifah.socialshopping.Utils.ViewPostFragment;
import com.latifah.socialshopping.models.Photo;

/**
 * Created by User on 09/12/2017.
 */

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener{
    @Override
    public void onCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG,"onCommentThreadSelectedListener: selected a comment thread");
        ViewCommentFragment fragment=new ViewCommentFragment();
        Bundle args=new Bundle();
        args.putParcelable(getString(R.string.photo),photo);
        fragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }
    private static final String TAG = "ProfileActivity";

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG,"onGridImageSelected: selected an image gridview: "+ photo.toString());
        ViewPostFragment fragment=new ViewPostFragment();
        Bundle args=new Bundle();
        args.putParcelable(getString(R.string.photo),photo);
        args.putInt(getString(R.string.activity_number),activityNumber);
        fragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    private static final int ACTIVITY_NUM=4;
    private static final int NUM_GRID_COLUMNS=3;
    private Context mContext=ProfileActivity.this;

    private ProgressBar mProgressBar;
    private ImageView profilePhoto;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG,"onCreate: started.");

        init();
    }
    private void init(){
        Log.d(TAG,"init: inflating " + getString(R.string.profile_fragment));

        ProfileFragment fragment=new ProfileFragment();
        FragmentTransaction transaction=ProfileActivity.this.getFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.addToBackStack(getString(R.string.profile_fragment));
        transaction.commit();

    }

}
