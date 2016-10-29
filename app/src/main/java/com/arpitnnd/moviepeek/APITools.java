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

class APITools {

    private static String API_KEY = BuildConfig.API_KEY;

    ArrayList<String> getPosterPaths(boolean sortByPop) throws JSONException {
        String moviesJSON = getMoviesJSON(sortByPop);
        JSONArray moviesArray = new JSONObject(moviesJSON).getJSONArray("results");
        ArrayList<String> posterPaths = new ArrayList<>();

        for (int i = 0; i < moviesArray.length(); i++)
            posterPaths.add(moviesArray.getJSONObject(i).getString("poster_path"));
        return posterPaths;
    }

    MovieDetails getMovieDetails(boolean sortByPop, int position) throws JSONException {
        String moviesJSON = getMoviesJSON(sortByPop);
        JSONArray moviesArray = new JSONObject(moviesJSON).getJSONArray("results");
        MovieDetails movie = new MovieDetails();

        movie.setMovieId(moviesArray.getJSONObject(position).getString("id"));
        movie.setMovieTitle(moviesArray.getJSONObject(position).getString("original_title"));
        movie.setReleaseDate(moviesArray.getJSONObject(position).getString("release_date"));
        movie.setBackdropPath(moviesArray.getJSONObject(position).getString("backdrop_path"));
        movie.setPosterPath(moviesArray.getJSONObject(position).getString("poster_path"));
        movie.setVoteAverage(moviesArray.getJSONObject(position).getString("vote_average"));
        movie.setPlot(moviesArray.getJSONObject(position).getString("overview"));
        movie.setTrailers(getTrailers(movie.getMovieId()));
        movie.setReviews(getReviews(movie.getMovieId()));
        return movie;
    }

    ArrayList<Trailer> getTrailers(String id) throws JSONException {
        JSONArray trailerArray = new JSONObject(getJSON(
                "http://api.themoviedb.org/3/movie/" + id + "/videos?api_key=" + API_KEY))
                .getJSONArray("results");
        ArrayList<Trailer> trailers = new ArrayList<>();

        for (int i = 0; i < trailerArray.length(); i++) {
            if (trailerArray.getJSONObject(i).getString("site").equals("YouTube")) {
                Trailer trailer = new Trailer();
                trailer.setTrailerName(trailerArray.getJSONObject(i).getString("name"));
                trailer.setKey(trailerArray.getJSONObject(i).getString("key"));
                trailers.add(trailer);
            }
        }
        return trailers;
    }

    private ArrayList<Review> getReviews(String id) throws JSONException {
        JSONArray reviewArray = new JSONObject(getJSON(
                "http://api.themoviedb.org/3/movie/" + id + "/reviews?api_key=" + API_KEY))
                .getJSONArray("results");
        ArrayList<Review> reviews = new ArrayList<>();

        for (int i = 0; i < reviewArray.length(); i++) {
            Review review = new Review();
            review.setAuthor(reviewArray.getJSONObject(i).getString("author"));
            review.setContent(reviewArray.getJSONObject(i).getString("content"));
            reviews.add(review);
        }
        return reviews;
    }

    boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String getMoviesJSON(boolean sortByPop) {
        String urlString;
        if (sortByPop) {
            urlString = "http://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY;
        } else {
            urlString = "http://api.themoviedb.org/3/movie/top_rated?api_key=" + API_KEY;
        }
        return getJSON(urlString);
    }

    String getJSON(String urlString) {
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