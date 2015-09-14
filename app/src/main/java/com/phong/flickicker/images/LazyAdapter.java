/**
 * 
 */
package com.phong.flickicker.images;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.phong.flickicker.R;
import com.phong.flickicker.tasks.ImageDownloadTask;


/**
 * Created by Phong on 9/13/2015.
 */
public class LazyAdapter extends RecyclerView.Adapter<LazyAdapter.ViewHolder> {
    
    private Activity activity;
    private PhotoList photos;
    private static LayoutInflater inflater=null;
    
    public LazyAdapter(Activity a, PhotoList d) {
        activity = a;
        photos = d;

    }

    public interface OnItemClickListener {
        public void onClick(View view, int position);
    }

    public void setData(PhotoList lst){
        photos = lst;
    }


    public Object getItem(int position) {
        return photos.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View vi = inflater.inflate(R.layout.row, null);
        ViewHolder vh = new ViewHolder(vi);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Photo photo = photos.get(i);
        if (viewHolder.mIcon != null) {
            ImageDownloadTask task = new ImageDownloadTask(viewHolder.mIcon);
            Drawable drawable = new ImageUtils.DownloadedDrawable(task);
            viewHolder.mIcon.setImageDrawable(drawable);
            task.execute(photo.getSmallSquareUrl());
        }
        viewHolder.mTitle.setText(photo.getTitle());
        viewHolder.mViewText.setText(String.valueOf(photo.getViews()));
    }



    @Override
    public int getItemCount() {
        return photos.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTitle;
        public ImageView mIcon;
        public TextView mViewText;
        public ViewHolder(View v) {
            super(v);
            mTitle = (TextView)v.findViewById(R.id.imageTitle);
            mIcon = (ImageView)v.findViewById(R.id.imageIcon);
            mViewText = (TextView)v.findViewById(R.id.viewsText);
        }
    }
}
