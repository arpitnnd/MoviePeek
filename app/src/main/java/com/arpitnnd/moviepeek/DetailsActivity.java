package com.arpitnnd.moviepeek;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int position = getIntent().getIntExtra("position", 0);
        MovieDetails movieDetails = new MovieDetails();
        try {
            movieDetails = MainActivity.api.getMovieDetails(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ((TextView) findViewById(R.id.title)).setText(movieDetails.movieTitle);
        Drawable d = this.getResources().getDrawable(R.drawable.loading);
        findViewById(R.id.poster).getLayoutParams().width = getResources()
                .getDisplayMetrics().widthPixels / 2 - 50;
        findViewById(R.id.poster).requestLayout();
        Glide.with(this).load("http://image.tmdb.org/t/p/w185/" + movieDetails.posterPath)
                .placeholder(d).into((ImageView) findViewById(R.id.poster));
        SimpleDateFormat input = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault());
        try {
            ((TextView) findViewById(R.id.date)).setText(DateFormat.getDateInstance(DateFormat.SHORT)
                    .format(input.parse(movieDetails.releaseDate)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.rating)).setText(movieDetails.voteAverage);
        ((TextView) findViewById(R.id.overview)).setText(movieDetails.plot);

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
}