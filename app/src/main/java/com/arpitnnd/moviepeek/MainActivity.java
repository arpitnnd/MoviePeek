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

    private boolean mIsTablet, mSnackbarShown;
    private APITools mApi;
    private DBHandler mDbHandler;
    private GridView mGridView;
    private SharedPreferences mSharedPref;
    private String mSortCriteria;
    private ArrayList<String> mPosterPaths, mMovieIds;
    //private NetworkReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mIsTablet = findViewById(R.id.details_frame) != null;
        mSharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        mApi = new APITools(this);
        mGridView = (GridView) findViewById(R.id.gridView);
        //mReceiver = new NetworkReceiver();

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadDetails(position);
            }
        });

        if (savedInstanceState != null) {
            mSortCriteria = savedInstanceState.getString("SORT_CRITERIA");
            mPosterPaths = savedInstanceState.getStringArrayList("POSTER_PATHS");
            if (savedInstanceState.getBoolean("SNACKBAR_VISIBLE"))
                showOfflineSnackbar();
            if (mPosterPaths != null) {
                GridViewAdapter adapter = new GridViewAdapter(getApplicationContext(), mPosterPaths);
                mGridView.setAdapter(adapter);
                mGridView.setSelection(savedInstanceState.getInt("GRID_SCROLL_STATE"));
                if (mIsTablet) {
                    getFragmentManager().beginTransaction().
                            replace(R.id.details_frame,
                                    getFragmentManager().
                                            getFragment(savedInstanceState, "DETAILS_FRAGMENT")).
                            commit();
                }
            }
        } else refreshContent();
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mReceiver);
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("SORT_CRITERIA", mSortCriteria);
        outState.putStringArrayList("POSTER_PATHS", mPosterPaths);
        outState.putInt("GRID_SCROLL_STATE", mGridView.getFirstVisiblePosition());
        outState.putBoolean("SNACKBAR_VISIBLE", mSnackbarShown);
        if (mIsTablet && getFragmentManager().findFragmentById(R.id.details_frame) != null)
            getFragmentManager().putFragment(outState,
                    "DETAILS_FRAGMENT",
                    getFragmentManager().findFragmentById(R.id.details_frame));
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
            case "fav":
                menu.findItem(R.id.fav).setChecked(true);
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

            String temp = mSharedPref.getString("sort_criteria", "pop");
            if (!criteria.equals(temp)) {
                editor.putString("sort_criteria", criteria);
                editor.apply();
                invalidateOptionsMenu();
                refreshContent();
            }
        } else if (id == R.id.action_settings)
            Toast.makeText(MainActivity.this, "Coming soon.", Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    public void refreshContent() {
        mSortCriteria = mSharedPref.getString("sort_criteria", "pop");
        if (mApi.isNetworkAvailable() || mSortCriteria.equals("fav")) {
            new ImageLoadTask().execute(mSortCriteria);
            //Load first movie's details on tablet if no selection had been made yet
            if (mIsTablet && (getFragmentManager().findFragmentById(R.id.details_frame) == null)) {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                new DetailsLoadTask(mSortCriteria, 0).execute();
            }
        } else showOfflineSnackbar();
    }

    public void showOfflineSnackbar() {
        Snackbar.make(findViewById(R.id.coordinatorLayout),
                R.string.offline_message,
                Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSnackbarShown = false;
                refreshContent();
            }
        }).show();
        mSnackbarShown = true;
    }

    public void loadDetails(final int position) {
        if (mApi.isNetworkAvailable() || mSortCriteria.equals("fav")) {
            if (mIsTablet && (getFragmentManager().findFragmentById(R.id.details_frame) != null))
                getFragmentManager().
                        beginTransaction().
                        remove(getFragmentManager().findFragmentById(R.id.details_frame)).
                        commit();
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            new DetailsLoadTask(mSortCriteria, position).execute();
        } else Snackbar.make(findViewById(R.id.coordinatorLayout),
                R.string.offline_message,
                Snackbar.LENGTH_LONG).
                show();
    }

    public class ImageLoadTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            ArrayList<String> posterPaths = new ArrayList<>();

            if (!params[0].equals("fav")) {
                if (mApi.isNetworkAvailable()) {
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
            if (params[0].equals("fav"))
                mPosterPaths = posterPaths;
            else if (posterPaths.size() != 0)
                mPosterPaths = posterPaths;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            GridViewAdapter adapter = new GridViewAdapter(getApplicationContext(), mPosterPaths);
            mGridView.setAdapter(adapter);
            if (adapter.getCount() == 0)
                findViewById(R.id.noItems_textView).setVisibility(View.VISIBLE);
            else findViewById(R.id.noItems_textView).setVisibility(View.GONE);
        }

    }

    public class DetailsLoadTask extends AsyncTask<Void, Void, MovieDetails> {

        private String sortCriteria;
        private int position;

        public DetailsLoadTask(String sortCriteria, int position) {
            this.sortCriteria = sortCriteria;
            this.position = position;
        }

        @Override
        protected MovieDetails doInBackground(Void... params) {
            MovieDetails movieDetails = new MovieDetails();

            if (!sortCriteria.equals("fav")) {
                if (mApi.isNetworkAvailable())
                    try {
                        movieDetails = mApi.getMovieDetails(sortCriteria.equals("pop"), position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                else movieDetails = null;
            } else if (mMovieIds.size() != 0)
                movieDetails = mDbHandler.fetchMovieDetails(mMovieIds.get(position));
            else movieDetails = null;
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

    /*public class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ((mApi.isNetworkAvailable() && mPosterPaths.size() == 0) || !mLoadSuccessful)
                refreshContent();
        }

    }*/

}