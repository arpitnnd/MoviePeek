package com.arpitnnd.moviepeek;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arpitnnd.moviepeek.adapters.ReviewAdapter;
import com.arpitnnd.moviepeek.adapters.TrailerAdapter;
import com.arpitnnd.moviepeek.data.MovieDetails;
import com.arpitnnd.moviepeek.data.Review;
import com.arpitnnd.moviepeek.data.Trailer;
import com.bumptech.glide.Glide;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    private MovieDetails movieDetails;
    private int position;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private ArrayList<Trailer> trailers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        position = getIntent().getIntExtra("position", 0);
        new TrailerLoadTask().execute();
        new ReviewLoadTask().execute();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView mTrailerRecyclerView = (RecyclerView) findViewById(R.id.trailers_recycler);
        RecyclerView.LayoutManager llm
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        if (mTrailerRecyclerView != null) {
            mTrailerRecyclerView.setHasFixedSize(false);
            mTrailerRecyclerView.setLayoutManager(llm);
            mTrailerAdapter = new TrailerAdapter(new ArrayList<Trailer>());
            mTrailerRecyclerView.setAdapter(mTrailerAdapter);
            mTrailerRecyclerView.setNestedScrollingEnabled(false);
            mTrailerAdapter.setOnItemClickListener(new TrailerAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {
                    Intent yIntent = new Intent(Intent.ACTION_VIEW);
                    yIntent.setData(Uri.parse("https://www.youtube.com/watch?v="
                            + trailers.get(position).getKey()));
                    startActivity(yIntent);
                }
            });
        }

        RecyclerView mReviewRecyclerView = (RecyclerView) findViewById(R.id.reviews_recycler);
        if (mReviewRecyclerView != null) {
            mReviewRecyclerView.setHasFixedSize(false);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            mReviewRecyclerView.setLayoutManager(layoutManager);
            mReviewRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());
            mReviewAdapter = new ReviewAdapter(new ArrayList<Review>());
            mReviewRecyclerView.setAdapter(mReviewAdapter);
            mReviewRecyclerView.setNestedScrollingEnabled(false);
        }

        try {
            movieDetails = MainActivity.api.getMovieDetails(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getSupportActionBar().setTitle(movieDetails.getMovieTitle());
        Glide.with(this).load("http://image.tmdb.org/t/p/w780/" + movieDetails.getBackdropPath())
                .into((ImageView) findViewById(R.id.backdrop));
        ((TextView) findViewById(R.id.title)).setText(movieDetails.getMovieTitle());
        Drawable d = ContextCompat.getDrawable(this, R.drawable.loading);
        Glide.with(this).load("http://image.tmdb.org/t/p/w185/" + movieDetails.getPosterPath())
                .placeholder(d).into((ImageView) findViewById(R.id.poster));
        SimpleDateFormat input = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault());
        try {
            ((TextView) findViewById(R.id.date)).setText(DateFormat.getDateInstance(DateFormat.SHORT)
                    .format(input.parse(movieDetails.getReleaseDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ((TextView) findViewById(R.id.rating))
                .setText(String.format("%.2f", Float.valueOf(movieDetails.getVoteAverage())));
        ((TextView) findViewById(R.id.overview)).setText(movieDetails.getPlot());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class TrailerLoadTask extends AsyncTask<Void, Void, ArrayList<Trailer>> {

        @Override
        protected ArrayList<Trailer> doInBackground(Void... params) {
            ArrayList<Trailer> trailers = null;

            try {
                trailers = MainActivity.api.getTrailers(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return trailers;
        }

        @Override
        protected void onPostExecute(ArrayList<Trailer> result) {
            if (result != null) {
                trailers = result;
                mTrailerAdapter.swap(result);
            }
        }
    }

    public class ReviewLoadTask extends AsyncTask<Void, Void, ArrayList<Review>> {

        @Override
        protected ArrayList<Review> doInBackground(Void... params) {
            ArrayList<Review> reviews = null;

            try {
                reviews = MainActivity.api.getReviews(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return reviews;
        }

        @Override
        protected void onPostExecute(ArrayList<Review> result) {
            if (result != null) {
                mReviewAdapter.swap(result);
            }
        }
    }

}