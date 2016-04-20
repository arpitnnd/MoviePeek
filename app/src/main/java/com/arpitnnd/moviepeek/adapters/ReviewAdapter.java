package com.arpitnnd.moviepeek.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arpitnnd.moviepeek.R;
import com.arpitnnd.moviepeek.data.Review;

import java.util.ArrayList;


public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private ArrayList<Review> mDataset;

    public ReviewAdapter(ArrayList<Review> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTextViewAuthor.setText(mDataset.get(position).getAuthor());
        holder.mTextViewContent.setText(mDataset.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void swap(ArrayList<Review> newDataset) {
        mDataset.clear();
        mDataset.addAll(newDataset);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextViewAuthor, mTextViewContent;

        public ViewHolder(View v) {
            super(v);
            mTextViewAuthor = (TextView) v.findViewById(R.id.author_textView);
            mTextViewContent = (TextView) v.findViewById(R.id.content_textView);
        }
    }

}
