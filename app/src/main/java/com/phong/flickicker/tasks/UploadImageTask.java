package com.phong.flickicker.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.ListView;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.uploader.UploadMetaData;
import com.googlecode.flickrjandroid.uploader.Uploader;
import com.googlecode.flickrjandroid.util.StringUtilities;
import com.phong.flickicker.FlickrHelper;
import com.phong.flickicker.fragments.MainScreenFragment;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Phong on 9/13/2015.
 */
public class UploadImageTask extends AsyncTask<OAuth, Void, String> {

    MainScreenFragment mFragment;
    InputStream mInputStream;
    String mName;
    public UploadImageTask(MainScreenFragment fragment, InputStream input, String name) {
        mFragment = fragment;
        mInputStream = input;
        mName = name;
    }

    ProgressDialog mProgressDialog;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = ProgressDialog.show(mFragment.getActivity(),
                "", "Uploading..."); //$NON-NLS-1$ //$NON-NLS-2$
        mProgressDialog.setCanceledOnTouchOutside(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dlg) {
                UploadImageTask.this.cancel(true);
            }
        });
    }
    @Override
    protected String doInBackground(OAuth... params) {
        OAuthToken token = params[0].getToken();
        Flickr f = FlickrHelper.getInstance().getFlickrAuthed(token.getOauthToken(),
                token.getOauthTokenSecret());
        Uploader uploader = f.getUploader();
        UploadMetaData metaData = new UploadMetaData();

        try {
            uploader.upload(mName,mInputStream,metaData);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String id) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
       mFragment.load();
    }
}
