package com.arpitnnd.moviepeek.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arpitnnd.moviepeek.R;
import com.arpitnnd.moviepeek.data.Trailer;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {

    private static OnItemClickListener mItemClickListener;
    private ArrayList<Trailer> mDataset;
    private Context mContext;

    public TrailerAdapter(ArrayList<Trailer> myDataset, Context myContext) {
        mDataset = myDataset;
        mContext = myContext;
    }

    @Override
    public TrailerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trailer_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Glide.with(mContext).
                load("http://img.youtube.com/vi/" + mDataset.get(position).getKey() + "/0.jpg").
                into(holder.mImageViewThumbnail);
        holder.mTextViewTitle.setText(mDataset.get(position).getTrailerName());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void swap(ArrayList<Trailer> newDataset) {
        mDataset.clear();
        mDataset.addAll(newDataset);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        TrailerAdapter.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTextViewTitle;
        ImageView mImageViewThumbnail;

        ViewHolder(View v) {
            super(v);
            mImageViewThumbnail = (ImageView) v.findViewById(R.id.video_thumbnail);
            mTextViewTitle = (TextView) v.findViewById(R.id.video_title);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

}