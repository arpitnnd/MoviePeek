package com.arpitnnd.moviepeek;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

    MovieDetails movieDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int position = getIntent().getIntExtra("position", 0);
        try {
            movieDetails = MainActivity.api.getMovieDetails(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getSupportActionBar().setTitle(movieDetails.movieTitle);
        ((TextView) findViewById(R.id.title)).setText(movieDetails.movieTitle);
        Drawable d = ContextCompat.getDrawable(this, R.drawable.loading);
        Glide.with(this).load("http://image.tmdb.org/t/p/w185/" + movieDetails.posterPath)
                .placeholder(d).into((ImageView) findViewById(R.id.poster));
        SimpleDateFormat input = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault());
        try {
            ((TextView) findViewById(R.id.date)).setText(DateFormat.getDateInstance(DateFormat.SHORT)
                    .format(input.parse(movieDetails.releaseDate)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.rating)).setText(String.format("%.2f", Float.valueOf(movieDetails.voteAverage)));
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