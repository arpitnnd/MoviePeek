package com.arpitnnd.moviepeek;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.arpitnnd.moviepeek.adapters.ReviewAdapter;
import com.arpitnnd.moviepeek.adapters.TrailerAdapter;
import com.arpitnnd.moviepeek.data.DBHandler;
import com.arpitnnd.moviepeek.data.MovieDetails;
import com.arpitnnd.moviepeek.data.Trailer;
import com.bumptech.glide.Glide;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.parceler.Parcels;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    private ShareActionProvider mShareActionProvider;
    private String mTitle;
    private Trailer mTrailerToShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        final MovieDetails movie = Parcels.unwrap(getIntent().getParcelableExtra("movie"));
        mTitle = movie.getMovieTitle();
        mTrailerToShare = movie.getTrailers().get(0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(movie.getMovieTitle());
        }
        Glide.with(this).load("http://image.tmdb.org/t/p/w780/" + movie.getBackdropPath())
                .into((ImageView) findViewById(R.id.backdrop));
        ((TextView) findViewById(R.id.title)).setText(movie.getMovieTitle());
        Drawable d = ContextCompat.getDrawable(this, R.drawable.loading);
        Glide.with(this).load("http://image.tmdb.org/t/p/w185/" + movie.getPosterPath())
                .placeholder(d).into((ImageView) findViewById(R.id.poster));
        SimpleDateFormat input = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault());
        try {
            ((TextView) findViewById(R.id.date)).setText(DateFormat.getDateInstance(DateFormat.SHORT)
                    .format(input.parse(movie.getReleaseDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.rating))
                .setText(String.format("%.2f", Float.valueOf(movie.getVoteAverage())));
        ((TextView) findViewById(R.id.overview)).setText(movie.getPlot());

        final DBHandler db = new DBHandler(getApplicationContext());
        CheckBox favCheckBox = (CheckBox) findViewById(R.id.fav_checkBox);
        if (db.isFav(movie.getMovieId()))
            favCheckBox.setChecked(true);
        favCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    db.addFavMovie(movie);
                else db.deleteFavMovie(movie.getMovieId());
            }
        });

        RecyclerView trailerRecyclerView = (RecyclerView) findViewById(R.id.trailers_recycler);
        RecyclerView.LayoutManager llm
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        if (trailerRecyclerView != null) {
            trailerRecyclerView.setHasFixedSize(false);
            trailerRecyclerView.setLayoutManager(llm);
            TrailerAdapter trailerAdapter =
                    new TrailerAdapter(movie.getTrailers(), getApplicationContext());
            trailerRecyclerView.setAdapter(trailerAdapter);
            trailerAdapter.setOnItemClickListener(new TrailerAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {
                    Intent yIntent = new Intent(Intent.ACTION_VIEW);
                    yIntent.setData(Uri.parse("https://www.youtube.com/watch?v="
                            + movie.getTrailers().get(position).getKey()));
                    startActivity(yIntent);
                }
            });
        }

        if (movie.getReviews().size() != 0) {
            RecyclerView reviewRecyclerView = (RecyclerView) findViewById(R.id.reviews_recycler);
            if (reviewRecyclerView != null) {
                reviewRecyclerView.setHasFixedSize(false);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
                reviewRecyclerView.setLayoutManager(layoutManager);
                reviewRecyclerView.addItemDecoration(
                        new HorizontalDividerItemDecoration.
                                Builder(this).
                                build());
                ReviewAdapter reviewAdapter = new ReviewAdapter(movie.getReviews());
                reviewRecyclerView.setAdapter(reviewAdapter);
            }
        } else findViewById(R.id.noReviews_textView).setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareIntent(mTrailerToShare);
        return true;
    }

    private void setShareIntent(Trailer trailer) {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);

            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mTitle);
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, mTitle +
                    ":" + trailer.getTrailerName() + ": "
                    + "https://www.youtube.com/watch?v=" + trailer.getKey());
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}