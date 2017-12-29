package com.latifah.socialshopping.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.latifah.socialshopping.Profil.AccountSettingsActivity;
import com.latifah.socialshopping.R;
import com.latifah.socialshopping.Utils.FilePaths;
import com.latifah.socialshopping.Utils.FileSearch;
import com.latifah.socialshopping.Utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by User on 10/12/2017.
 */

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    //constant
    private static final int NUM_GRID_COLUMNS=3;
    //widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;

    //vars
    private ArrayList<String> directoies;
    private String mAppend="file:/";
    private String mSelectedImage;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView=(GridView)view.findViewById(R.id.gridView);
        galleryImage=(ImageView)view.findViewById(R.id.galleryImageView);
        directorySpinner=(Spinner)view.findViewById(R.id.spinnerDirectory);
        mProgressBar=(ProgressBar)view.findViewById(R.id.progressBar);
        directoies=new ArrayList<>();
        Log.d(TAG,"onCreate:started.");

        mProgressBar.setVisibility(view.GONE);

        ImageView shareClose=(ImageView)view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: closing the gallery fragmment.");
                getActivity().finish();
            }
        });

        TextView nextScreen=(TextView)view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: navigating to final share screen");
                if(isRootTask()){
                    Intent intent=new Intent(getActivity(),NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image),mSelectedImage);
                    startActivity(intent);
                }else {
                    Intent intent=new Intent(getActivity(),AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_image),mSelectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment),getString(R.string.edit_profile_fragment));

                    startActivity(intent);
                    getActivity().finish();
                }

            }
        });
        init();
        return view;
    }
    private boolean isRootTask(){
        if(((ShareActivity)getActivity()).getTask()==0){
            return true;
        }
        else {
            return false;
        }
    }
    private void init(){
        FilePaths filePaths=new FilePaths();

        //chevk for another folder indede "/storage/emulated/0/pictures"
        if(FileSearch.getDirectoryPaths(filePaths.PICTURES)!=null){
            directoies=FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }
        ArrayList<String> directoryNames=new ArrayList<>();
        for (int i=0;i<directoies.size();i++){
            int index=directoies.get(i).lastIndexOf("/");
            String string=directoies.get(i).substring(index).replace("/","");
            directoryNames.add(string);
        }

        directoies.add(filePaths.CAMERA);

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);
        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"onItemClick: selected" + directoies.get(position));
                //setup our image grid for directory file cosen
                setupGridView(directoies.get(position));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void setupGridView(String selectedDirectory){
        Log.d(TAG,"setupGridView: directory chosen "+ selectedDirectory);
        final ArrayList<String> imgURLs=FileSearch.getFilePaths(selectedDirectory);

        int gridWidth=getResources().getDisplayMetrics().widthPixels;
        int imageWidth=gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        //use the grid adapter to adapter the images to gridView
        GridImageAdapter adapter=new GridImageAdapter(getActivity(),R.layout.layout_grid_image_view, mAppend,imgURLs);
        gridView.setAdapter(adapter);

        //set the first image to be displayed when the activity fragment view is inflated
        try{
            setImage(imgURLs.get(0),galleryImage,mAppend);
            mSelectedImage=imgURLs.get(0);
        }catch (ArrayIndexOutOfBoundsException e){
            Log.e(TAG,"setupGridView: ArrayIndexOutOfBoundsException: "+ e.getMessage());
        }


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"onClick: selected an image: "+ imgURLs.get(position));
                setImage(imgURLs.get(position),galleryImage,mAppend);
                mSelectedImage=imgURLs.get(position);
            }
        });

    }
    private void setImage(String imgURL,ImageView image, String append){
        Log.d(TAG,"setImage: setting image");

        ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.displayImage(append +imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(view.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(view.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(view.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(view.INVISIBLE);
            }
        });
    }
}
