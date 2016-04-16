package com.arpitnnd.moviepeek;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class APITools {

    static String API_KEY = BuildConfig.API_KEY; //Add you API key here.
    String moviesJSON;
    Context context;

    APITools(Context context) {
        this.context = context;
    }

    public MovieDetails getMovieDetails(int position) throws JSONException {
        JSONArray moviesArray = new JSONObject(moviesJSON).getJSONArray("results");

        MovieDetails movieDetails = new MovieDetails();

        movieDetails.movieTitle = moviesArray.getJSONObject(position).getString("original_title");
        movieDetails.releaseDate = moviesArray.getJSONObject(position).getString("release_date");
        movieDetails.posterPath = moviesArray.getJSONObject(position).getString("poster_path");
        movieDetails.voteAverage = moviesArray.getJSONObject(position).getString("vote_average");
        movieDetails.plot = moviesArray.getJSONObject(position).getString("overview");

        return movieDetails;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public String getMoviesJSON(boolean sortByPop) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuilder buffer = new StringBuilder();


        try {
            String urlString;
            if (sortByPop) {
                urlString = "http://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY;
            } else {
                urlString = "http://api.themoviedb.org/3/movie/top_rated?api_key=" + API_KEY;
            }
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            if (buffer.length() == 0) {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {

                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer.toString();
    }

    public ArrayList<String> getPosterPaths(boolean sortByPop) throws JSONException {

        if (isNetworkAvailable())
            moviesJSON = getMoviesJSON(sortByPop);

        JSONArray moviesArray = new JSONObject(moviesJSON).getJSONArray("results");
        ArrayList<String> posterPaths = new ArrayList<>();

        for (int i = 0; i < moviesArray.length(); i++)
            posterPaths.add(moviesArray.getJSONObject(i).getString("poster_path"));

        return posterPaths;
    }

}