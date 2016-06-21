package com.arpitnnd.moviepeek.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.arpitnnd.moviepeek.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class GridViewAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> paths;

    public GridViewAdapter(Context c, ArrayList<String> paths) {
        mContext = c;
        this.paths = paths;
    }

    @Override
    public int getCount() {
        return paths.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null)
            imageView = new ImageView(mContext);
        else
            imageView = (ImageView) convertView;
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Drawable d = ContextCompat.getDrawable(mContext, R.drawable.loading);
        Glide.with(mContext).
                load("http://image.tmdb.org/t/p/w342/" + paths.get(position)).
                placeholder(d).
                into(imageView);
        return imageView;
    }
}