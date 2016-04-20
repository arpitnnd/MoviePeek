package com.arpitnnd.moviepeek;

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

import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static APITools api;
    GridView gridView;
    boolean sortByPopularity;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        api = new APITools(this);
        refreshPosters();

        gridView = (GridView) findViewById(R.id.gridView);

        if (savedInstanceState != null)
            gridView.setSelection(savedInstanceState.getInt("scroll_state"));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("scroll_state", gridView.getFirstVisiblePosition());

        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        getMenuInflater().inflate(R.menu.menu_popup, menu);
        if (sharedPref.getBoolean("sort_by_pop", true))
            menu.findItem(R.id.pop).setChecked(true);
        else
            menu.findItem(R.id.rat).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.pop || id == R.id.rat) {
            SharedPreferences.Editor editor = sharedPref.edit();

            if (id == R.id.pop)
                editor.putBoolean("sort_by_pop", true);
            else
                editor.putBoolean("sort_by_pop", false);

            boolean temp = sharedPref.getBoolean("sort_by_pop", true);
            editor.apply();
            if (!(temp == sharedPref.getBoolean("sort_by_pop", true))) {
                refreshPosters();
            }
            invalidateOptionsMenu();
        }

        //noinspection SimplifiableIfStatement
        else if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this, "Feature not implemented yet.", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshPosters() {
        if (api.isNetworkAvailable()) {
            sortByPopularity = sharedPref.getBoolean("sort_by_pop", true);
            new ImageLoadTask().execute();
        } else {
            Snackbar.make(findViewById(R.id.coordinatorLayout), "No internet access.", Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            ArrayList<String> posterPaths = null;
            try {
                posterPaths = api.getPosterPaths(sortByPopularity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return posterPaths;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result != null) {
                GridViewAdapter adapter = new GridViewAdapter(getApplicationContext(), result);
                gridView.setAdapter(adapter);
            }
        }
    }

}