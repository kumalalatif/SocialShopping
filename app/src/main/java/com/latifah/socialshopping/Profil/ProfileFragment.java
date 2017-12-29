package com.latifah.socialshopping.Profil;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.latifah.socialshopping.R;
import com.latifah.socialshopping.Utils.BottomNavigationViewHelper;
import com.latifah.socialshopping.Utils.FirebaseMethods;
import com.latifah.socialshopping.Utils.GridImageAdapter;
import com.latifah.socialshopping.Utils.UniversalImageLoader;
import com.latifah.socialshopping.models.Comment;
import com.latifah.socialshopping.models.Like;
import com.latifah.socialshopping.models.Photo;
import com.latifah.socialshopping.models.UserAccountSettings;
import com.latifah.socialshopping.models.UserSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by User on 17/12/2017.
 */

public class ProfileFragment extends Fragment {
    private static final String TAG="ProfileFragment";

    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListener mOnGridImageSelectedListener;
    private static final int ACTIVITY_NUM=4;
    private static final int NUM_GRID_COLUMNS=3;
    //firebase authentification
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private TextView mPosts,mFollowers,mFollowing,mDisplayName,mUsername,mDiscription,mWebsite;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private android.support.v7.widget.Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container,false);
        mDisplayName=(TextView)view.findViewById(R.id.display_name);
        mUsername=(TextView)view.findViewById(R.id.username);
        mWebsite=(TextView)view.findViewById(R.id.website);
        mDiscription=(TextView)view.findViewById(R.id.description);
        mFollowers=(TextView)view.findViewById(R.id.tvFollowers);
        mFollowing=(TextView)view.findViewById(R.id.tvFollowers);
        mPosts=(TextView)view.findViewById(R.id.tvPosts);
        mProfilePhoto=(CircleImageView)view.findViewById(R.id.profile_photo);
        mProgressBar=(ProgressBar)view.findViewById(R.id.profileProgressBar);
        gridView=(GridView)view.findViewById(R.id.gridView);
        toolbar= (Toolbar) view.findViewById(R.id.profileToolbar);
        profileMenu=(ImageView)view.findViewById(R.id.profileMenu);
        bottomNavigationView=(BottomNavigationViewEx)view.findViewById(R.id.bottomNavViewBar);
        mContext=getActivity();
        mFirebaseMethods=new FirebaseMethods(getActivity());
        Log.d(TAG,"onCreateView: stared");


        setupBottomNavigationView();
        setupToolbar();
        setupFirebaseAuth();
        setupGridView();

        TextView editProfile=(TextView)view.findViewById(R.id.textEditProfil);
        editProfile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick navigating to "+ mContext.getString(R.string.edit_profile_fragment));
                Intent intent=new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

        return view;

    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnGridImageSelectedListener=(OnGridImageSelectedListener)getActivity();
        }catch (ClassCastException e){
            Log.e(TAG,"onAttach: ClassCastException: "+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void setupGridView(){
        Log.d(TAG,"setupGridView: Setting up image grid.");
        final ArrayList<Photo> photos=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        Query query=reference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    //photos.add(singleSnapshot.getValue(Photo.class));
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    //photo.setDate_create(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    ArrayList<Comment> comments=new ArrayList<Comment>();
                    for(DataSnapshot dSnapshot :singleSnapshot.child(getString(R.string.field_comments)).getChildren()){
                        Comment comment=new Comment();
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                        comments.add(comment);
                    }
                    photo.setComments(comments);

                    List<Like> likesList = new ArrayList<Like>();
                    for (DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_likes)).getChildren()) {
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }
                    photo.setLikes(likesList);
                    photos.add(photo);
                }
                //setup our image grid
                int gridWidth=getResources().getDisplayMetrics().widthPixels;
                int imageWidth=gridWidth/NUM_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls=new ArrayList<String>();
                for(int i=0;i<photos.size();i++){
                    imgUrls.add(photos.get(i).getImage_path());
                }
                GridImageAdapter adapter=new GridImageAdapter(getActivity(),R.layout.layout_grid_image_view,"",imgUrls);
                gridView.setAdapter(adapter);
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mOnGridImageSelectedListener.onGridImageSelected(photos.get(position),ACTIVITY_NUM);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"onCancelled: query cancelled.");
            }
        });
    }
    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG,"setProfileWidgets: setting widget width data retrieving from firebase database: "+ userSettings.toString());
        Log.d(TAG,"setProfileWidgets: setting widget width data retrieving from firebase database: "+ userSettings.getSettings().getUsername());

        //Users users=userSettings.getUsers();
        UserAccountSettings settings=userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null,"");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDiscription.setText(settings.getDescription());
        mPosts.setText(String.valueOf(settings.getPosts()));
        mFollowing.setText(String.valueOf(settings.getFollowing()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mProgressBar.setVisibility(View.GONE);
    }
    private void setupToolbar(){
        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: navigating to account settings.");
                Intent intent=new Intent(mContext,AccountSettingsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });
    }
    /**
     //     *BottomNavigationView setup
     //     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext,getActivity(),bottomNavigationView);
        Menu menu=bottomNavigationView.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
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
