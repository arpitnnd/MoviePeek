package com.arpitnnd.moviepeek;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.arpitnnd.moviepeek.adapters.GridViewAdapter;
import com.arpitnnd.moviepeek.data.DBHandler;
import com.arpitnnd.moviepeek.data.MovieDetails;

import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private boolean mIsTablet;
    private APITools mApi;
    private DBHandler mDbHandler;
    private GridView mGridView;
    private SharedPreferences mSharedPref;
    private String mSortCriteria;
    private ArrayList<String> mMovieIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIsTablet = findViewById(R.id.details_frame) != null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        mApi = new APITools(this);
        refreshPosters();

        mGridView = (GridView) findViewById(R.id.gridView);
        if (savedInstanceState != null)
            mGridView.setSelection(savedInstanceState.getInt("GRID_SCROLL_STATE"));
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIsTablet && getFragmentManager().findFragmentById(R.id.details_frame) != null)
                    getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.details_frame)).commit();
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                new DetailsLoadTask(mSortCriteria, position).execute();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("GRID_SCROLL_STATE", mGridView.getFirstVisiblePosition());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        getMenuInflater().inflate(R.menu.menu_popup, menu);
        String criteria = mSharedPref.getString("sort_criteria", "pop");
        switch (criteria) {
            case "pop":
                menu.findItem(R.id.pop).setChecked(true);
                break;
            case "rat":
                menu.findItem(R.id.rat).setChecked(true);
                break;
            default:
                menu.findItem(R.id.fav).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.pop || id == R.id.rat || id == R.id.fav) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            String criteria;

            if (id == R.id.pop)
                criteria = "pop";
            else if (id == R.id.rat)
                criteria = "rat";
            else criteria = "fav";
            editor.putString("sort_criteria", criteria);

            String temp = mSharedPref.getString("sort_criteria", "pop");
            editor.apply();
            if (!(temp.equals(mSharedPref.getString("sort_criteria", "pop")))) {
                refreshPosters();
            }
            invalidateOptionsMenu();
        } else if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this, "Feature not implemented yet.",
                    Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshPosters() {
        mSortCriteria = mSharedPref.getString("sort_criteria", "pop");
        new ImageLoadTask().execute(mSortCriteria);
    }

    public boolean checkNetwork() {
        if (mApi.isNetworkAvailable())
            return true;
        else {
            Snackbar.make(findViewById(R.id.coordinatorLayout),
                    "No internet access.",
                    Snackbar.LENGTH_LONG).show();
            return false;
        }
    }

    public class ImageLoadTask extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            ArrayList<String> posterPaths = new ArrayList<>();

            if (!params[0].equals("fav")) {
                if (checkNetwork()) {
                    boolean sortByPopularity = params[0].equals("pop");
                    try {
                        posterPaths = mApi.getPosterPaths(sortByPopularity);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                mDbHandler = new DBHandler(getApplicationContext());
                mMovieIds = mDbHandler.fetchFavouriteIds();

                for (String Id : mMovieIds)
                    posterPaths.add(mDbHandler.fetchPosterPath(Id));
            }
            return posterPaths;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result != null) {
                GridViewAdapter adapter = new GridViewAdapter(getApplicationContext(), result);
                mGridView.setAdapter(adapter);
                if (adapter.getCount() == 0)
                    findViewById(R.id.noItems_textView).setVisibility(View.VISIBLE);
                else findViewById(R.id.noItems_textView).setVisibility(View.GONE);
            }
        }

    }

    public class DetailsLoadTask extends AsyncTask<Void, Void, MovieDetails> {

        String sortCriteria;
        int position;

        public DetailsLoadTask(String sortCriteria, int position) {
            this.sortCriteria = sortCriteria;
            this.position = position;
        }

        @Override
        protected MovieDetails doInBackground(Void... params) {

            MovieDetails movieDetails = new MovieDetails();

            if (!sortCriteria.equals("fav")) {
                if (checkNetwork())
                    try {
                        movieDetails = mApi.getMovieDetails(sortCriteria.equals("pop"), position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                else movieDetails = null;
            } else movieDetails = mDbHandler.fetchMovieDetails(mMovieIds.get(position));
            return movieDetails;
        }

        @Override
        protected void onPostExecute(MovieDetails result) {
            if (result != null) {
                if (mIsTablet) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("movie", Parcels.wrap(result));
                    Fragment fragment = new DetailsFragment();
                    fragment.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.details_frame, fragment).
                            commit();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                    intent.putExtra("movie", Parcels.wrap(result));
                    startActivity(intent);
                }
            }
            findViewById(R.id.progressBar).setVisibility(View.GONE);
        }

    }

}