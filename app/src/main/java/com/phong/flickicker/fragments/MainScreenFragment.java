package com.phong.flickicker.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.phong.flickicker.FlickrjActivity;
import com.phong.flickicker.R;
import com.phong.flickicker.images.ImageUtils;
import com.phong.flickicker.images.LazyAdapter;
import com.phong.flickicker.images.TouchImageView;
import com.phong.flickicker.tasks.ImageDownloadTask;
import com.phong.flickicker.tasks.LoadPhotostreamTask;
import com.phong.flickicker.tasks.LoadUserTask;
import com.phong.flickicker.tasks.UploadImageTask;
import com.phong.flickicker.utils.RecyclerItemClickListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Phong on 9/13/2015.
 */

public class MainScreenFragment extends Fragment {
    private static final int SELECT_PHOTO = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 200;
    OAuth mAuth;
    PopupWindow popUp;
    TouchImageView mDetailImage;
    ImageView mResultImage;
    View rootView;
    View mDetailView;
    View mResultImageView;
    RecyclerView mListview;
    LazyAdapter mAdapter;
    InputStream mImageStream = null;

    public void setAuth(OAuth auth){
        mAuth = auth;
    }

    public static MainScreenFragment newInstance() {
        MainScreenFragment fragment = new MainScreenFragment();

        return fragment;
    }
    public MainScreenFragment(){
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.main_listview_layout, container, false);
        mListview = (RecyclerView) rootView.findViewById(R.id.imageList);


        mListview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new LazyAdapter(getActivity(),new PhotoList());
        mListview.setAdapter(mAdapter);
        // click in item =>show detail image
        mListview.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                LazyAdapter adapter = (LazyAdapter)mListview.getAdapter();
                Photo content = (Photo)adapter.getItem(position);
                ImageDownloadTask task = new ImageDownloadTask(mDetailImage);
                Drawable drawable = new ImageUtils.DownloadedDrawable(task);
                mDetailImage.setImageDrawable(drawable);
                task.execute(content.getLargeUrl()); // show with high quality image

                popUp  = new PopupWindow(mDetailView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT,true);
                popUp.setBackgroundDrawable(new BitmapDrawable());
                popUp.setOutsideTouchable(true);
                popUp.setFocusable(true);

                popUp.showAtLocation(rootView, Gravity.CENTER, 0, 0);
            }
        }));

        //setup menu
        final View menuView = inflater.inflate(R.layout.choose_image_layout, container, false);
        //show popup choose photo
        TextView choosePhoto = (TextView) menuView.findViewById(R.id.choose_photo);
        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                popUp.dismiss();
            }
        });
        //show popup take new photo
        TextView takePhoto = (TextView) menuView.findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                popUp.dismiss();
            }
        });

        // button open menu
        ImageView openMenu = (ImageView) rootView.findViewById(R.id.choose_button);
        openMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUp  = new PopupWindow(menuView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT,true);
                popUp.setBackgroundDrawable(new BitmapDrawable());
                popUp.setOutsideTouchable(true);
                popUp.setFocusable(true);
                popUp.showAtLocation(rootView, Gravity.CENTER, 0, 0);
            }
        });



        // set up view detail image layout
        mDetailView = inflater.inflate(R.layout.image_detail_layout, container, false);
        ImageView btmClose = (ImageView)mDetailView.findViewById(R.id.btn_close);
        mDetailImage =  (TouchImageView)mDetailView.findViewById(R.id.detail_iamge);
        btmClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUp.dismiss();
            }
        });
        // set up result image view which chosen from gallery or camera
        mResultImageView = inflater.inflate(R.layout.image_result_layout, container, false);
        mResultImage =(ImageView)mResultImageView.findViewById(R.id.result_image);
        ImageView btnOk = (ImageView)mResultImageView.findViewById(R.id.button_upload_ok);
        final EditText chosenImageName = (EditText)mResultImageView.findViewById(R.id.chosen_image_name);
        chosenImageName.bringToFront();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mImageStream != null) {

                    new UploadImageTask(MainScreenFragment.this, mImageStream,chosenImageName.getEditableText().toString()).execute(mAuth);
                    chosenImageName.setText("");
                    popUp.dismiss();
                }
            }
        });
        ImageView btnCancel = (ImageView)mResultImageView.findViewById(R.id.button_upload_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUp.dismiss();
            }
        });
        ((FlickrjActivity)getActivity()).setPopup(popUp);
        load();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    public void load() {
        if (mAuth != null) {
           // new LoadUserTask(this, userIcon).execute(oauth);
            new LoadPhotostreamTask(getActivity(), mAdapter).execute(mAuth);
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == -1){
                    Uri selectedImage = imageReturnedIntent.getData();

                    InputStream imageStream = null;
                    try {
                        imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
//                    if(imageStream != null){
//                        mImageStream = imageStream;
//                        new UploadImageTask(this,imageStream).execute(mAuth);
//                    }
                    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                    mResultImage.setImageBitmap(yourSelectedImage);
                    // don't suprise, we can not use imageSteam outside this function(i think it's destroyed)
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    yourSelectedImage.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
                    mImageStream = bs;

                    popUp  = new PopupWindow(mResultImageView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT,true);
                    popUp.setBackgroundDrawable(new BitmapDrawable());
                    popUp.setOutsideTouchable(true);
                    popUp.setFocusable(true);
                    popUp.showAtLocation(rootView, Gravity.CENTER, 0, 0);
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                if(resultCode == -1){
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");


                    mResultImage.setImageBitmap(imageBitmap);

                    popUp  = new PopupWindow(mResultImageView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT,true);
                    popUp.setBackgroundDrawable(new BitmapDrawable());
                    popUp.setOutsideTouchable(true);
                    popUp.setFocusable(true);
                    popUp.showAtLocation(rootView, Gravity.CENTER, 0, 0);

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
                    mImageStream = bs;
//                    new UploadImageTask(this,bs).execute(mAuth);
                }
                break;
        }
    }
}
