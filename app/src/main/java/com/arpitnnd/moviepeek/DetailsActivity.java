package com.arpitnnd.moviepeek;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.arpitnnd.moviepeek.data.MovieDetails;
import com.bumptech.glide.Glide;

import org.parceler.Parcels;

public class DetailsActivity extends AppCompatActivity {

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        MovieDetails movie = Parcels.unwrap(getIntent().getParcelableExtra("movie"));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(movie.getMovieTitle());
        }
        Glide.with(this).load("http://image.tmdb.org/t/p/w780/" + movie.getBackdropPath())
                .into((ImageView) findViewById(R.id.backdrop));

        mFragment = new DetailsFragment();
        if (savedInstanceState != null) {
            mFragment = getFragmentManager().getFragment(savedInstanceState, "fragment");
        } else {
            Bundle bundle = new Bundle();
            bundle.putParcelable("movie", Parcels.wrap(movie));
            mFragment.setArguments(bundle);
        }
        getFragmentManager().beginTransaction().
                replace(R.id.activity_details_frame, mFragment).
                commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getFragmentManager().putFragment(outState, "fragment", mFragment);
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