package com.arpitnnd.moviepeek;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.arpitnnd.moviepeek.data.MovieDetails;
import com.arpitnnd.moviepeek.data.Review;
import com.arpitnnd.moviepeek.data.Trailer;

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

    private static String API_KEY = BuildConfig.API_KEY; // TODO: Replace value with your API key.
    private String moviesJSON;
    private Context context;
    private JSONArray moviesArray;

    APITools(Context context) {
        this.context = context;
    }

    public ArrayList<String> getPosterPaths(boolean sortByPop) throws JSONException {
        if (isNetworkAvailable())
            moviesJSON = getMoviesJSON(sortByPop);
        moviesArray = new JSONObject(moviesJSON).getJSONArray("results");
        ArrayList<String> posterPaths = new ArrayList<>();

        for (int i = 0; i < moviesArray.length(); i++)
            posterPaths.add(moviesArray.getJSONObject(i).getString("poster_path"));
        return posterPaths;
    }

    public MovieDetails getMovieDetails(int position) throws JSONException {
        MovieDetails movieDetails = new MovieDetails();

        movieDetails.setMovieTitle(moviesArray.getJSONObject(position).getString("original_title"));
        movieDetails.setReleaseDate(moviesArray.getJSONObject(position).getString("release_date"));
        movieDetails.setBackdropPath(moviesArray.getJSONObject(position).getString("backdrop_path"));
        movieDetails.setPosterPath(moviesArray.getJSONObject(position).getString("poster_path"));
        movieDetails.setVoteAverage(moviesArray.getJSONObject(position).getString("vote_average"));
        movieDetails.setPlot(moviesArray.getJSONObject(position).getString("overview"));
        return movieDetails;
    }

    public ArrayList<Trailer> getTrailers(int position) throws JSONException {
        String movieUrlString = "http://api.themoviedb.org/3/movie/" + moviesArray.getJSONObject(position).getString("id");
        JSONArray trailerArray = new JSONObject(getJSON(movieUrlString + "/videos?api_key=" + API_KEY)).getJSONArray("results");
        ArrayList<Trailer> trailers = new ArrayList<>();

        for (int i = 0; i < trailerArray.length(); i++) {
            if (trailerArray.getJSONObject(i).getString("site").equals("YouTube")) {
                Trailer trailer = new Trailer();
                trailer.setTName(trailerArray.getJSONObject(i).getString("name"));
                trailer.setKey(trailerArray.getJSONObject(i).getString("key"));
                trailers.add(trailer);
            }
        }
        return trailers;
    }

    public ArrayList<Review> getReviews(int position) throws JSONException {
        String movieUrlString = "http://api.themoviedb.org/3/movie/" + moviesArray.getJSONObject(position).getString("id");
        JSONArray reviewArray = new JSONObject(getJSON(movieUrlString + "/reviews?api_key=" + API_KEY)).getJSONArray("results");
        ArrayList<Review> reviews = new ArrayList<>();

        for (int i = 0; i < reviewArray.length(); i++) {
            Review review = new Review();
            review.setAuthor(reviewArray.getJSONObject(i).getString("author"));
            review.setContent(reviewArray.getJSONObject(i).getString("content"));
            reviews.add(review);
        }
        return reviews;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public String getMoviesJSON(boolean sortByPop) {
        String urlString;

        if (sortByPop) {
            urlString = "http://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY;
        } else {
            urlString = "http://api.themoviedb.org/3/movie/top_rated?api_key=" + API_KEY;
        }
        return getJSON(urlString);
    }

    public String getJSON(String urlString) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuilder buffer = new StringBuilder();

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
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

}