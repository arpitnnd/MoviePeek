package com.arpitnnd.moviepeek.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    private static final String TABLE_FAV = "favourites";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MOVIE_ID = "movie_id";
    private static final String COLUMN_MOVIE_TITLE = "original_title";
    private static final String COLUMN_MOVIE_POSTER_PATH = "poster_path";
    private static final String COLUMN_MOVIE_OVERVIEW = "overview";
    private static final String COLUMN_MOVIE_VOTE_AVERAGE = "vote_average";
    private static final String COLUMN_MOVIE_RELEASE_DATE = "release_date";
    private static final String COLUMN_MOVIE_BACKDROP_PATH = "backdrop_path";
    private static final String COLUMN_MOVIE_TRAILERS = "trailer_paths";
    private static final String COLUMN_MOVIE_REVIEWS = "reviews";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "movies.db";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_FAV
                + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL, " +
                COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL, " +
                COLUMN_MOVIE_VOTE_AVERAGE + " TEXT NOT NULL, " +
                COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL, " +
                COLUMN_MOVIE_BACKDROP_PATH + " TEXT NOT NULL, " +
                COLUMN_MOVIE_TRAILERS + " TEXT, " +
                COLUMN_MOVIE_REVIEWS + " TEXT" +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FAV);
        onCreate(sqLiteDatabase);
    }

    public void addFavMovie(MovieDetails movie) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_MOVIE_ID, movie.getMovieId());
        values.put(COLUMN_MOVIE_TITLE, movie.getMovieTitle());
        values.put(COLUMN_MOVIE_POSTER_PATH, movie.getPosterPath());
        values.put(COLUMN_MOVIE_OVERVIEW, movie.getPlot());
        values.put(COLUMN_MOVIE_VOTE_AVERAGE, movie.getVoteAverage());
        values.put(COLUMN_MOVIE_RELEASE_DATE, movie.getReleaseDate());
        values.put(COLUMN_MOVIE_BACKDROP_PATH, movie.getBackdropPath());
        values.put(COLUMN_MOVIE_TRAILERS, trailersToString(movie.getTrailers()));
        values.put(COLUMN_MOVIE_REVIEWS, reviewsToString(movie.getReviews()));
        db.insert(TABLE_FAV, null, values);
        values.clear();
        db.close();
    }

    public void deleteFavMovie(String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_FAV, COLUMN_MOVIE_ID + " = ?", new String[]{id});
        db.close();
    }

    public boolean isFav(String id) {
        String existsQuery = "SELECT * FROM " + TABLE_FAV +
                " WHERE " + COLUMN_MOVIE_ID +
                " = '" + id + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(existsQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    public String fetchPosterPath(String id) {
        String listQuery = "SELECT " + COLUMN_MOVIE_POSTER_PATH +
                " FROM " + TABLE_FAV +
                " WHERE " + COLUMN_MOVIE_ID +
                " = '" + id + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(listQuery, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_POSTER_PATH));
        cursor.close();
        db.close();
        return path;
    }

    public ArrayList<String> fetchFavouriteIds() {
        String listQuery = "SELECT " + COLUMN_MOVIE_ID +
                " FROM " + TABLE_FAV +
                " ORDER BY " + COLUMN_ID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(listQuery, null);
        ArrayList<String> ids = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_ID));
                ids.add(id);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return ids;
    }

    public MovieDetails fetchMovieDetails(String id) {
        String listQuery = "SELECT * FROM " + TABLE_FAV +
                " WHERE " + COLUMN_MOVIE_ID +
                " = '" + id + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(listQuery, null);
        MovieDetails movie = new MovieDetails();

        cursor.moveToFirst();
        movie.setMovieId(cursor
                .getString(cursor.getColumnIndex(COLUMN_MOVIE_ID)));
        movie.setMovieTitle(cursor
                .getString(cursor.getColumnIndex(COLUMN_MOVIE_TITLE)));
        movie.setPosterPath(cursor
                .getString(cursor.getColumnIndex(COLUMN_MOVIE_POSTER_PATH)));
        movie.setPlot(cursor
                .getString(cursor.getColumnIndex(COLUMN_MOVIE_OVERVIEW)));
        movie.setVoteAverage(cursor
                .getString(cursor.getColumnIndex(COLUMN_MOVIE_VOTE_AVERAGE)));
        movie.setReleaseDate(cursor
                .getString(cursor.getColumnIndex(COLUMN_MOVIE_RELEASE_DATE)));
        movie.setBackdropPath(cursor
                .getString(cursor.getColumnIndex(COLUMN_MOVIE_BACKDROP_PATH)));
        movie.setTrailers(parseTrailers(cursor
                .getString(cursor.getColumnIndex(COLUMN_MOVIE_TRAILERS))));
        movie.setReviews(parseReviews(cursor
                .getString(cursor.getColumnIndex(COLUMN_MOVIE_REVIEWS))));
        cursor.close();
        db.close();
        return movie;
    }

    private String reviewsToString(ArrayList<Review> reviews) {
        String string = "";
        for (Review review : reviews)
            string += "<--new>" + review.getAuthor() +
                    ";" + review.getContent();
        return string;
    }

    private String trailersToString(ArrayList<Trailer> trailers) {
        String string = "";
        for (Trailer trailer : trailers)
            string += "--" + trailer.getTrailerName() +
                    ";" + trailer.getKey();
        return string;
    }

    private ArrayList<Trailer> parseTrailers(String string) {
        ArrayList<Trailer> trailers = new ArrayList<>();
        String[] trailersArray = string.split("--");

        for (int i = 1; i < trailersArray.length; i++) {
            Trailer trailer = new Trailer();
            String[] trailerItems = trailersArray[i].split(";", 2);
            trailer.setTrailerName(trailerItems[0]);
            trailer.setKey(trailerItems[1]);
            trailers.add(trailer);
        }
        return trailers;
    }

    private ArrayList<Review> parseReviews(String string) {
        ArrayList<Review> reviews = new ArrayList<>();
        String[] reviewsArray = string.split("<--new>");

        for (int i = 1; i < reviewsArray.length; i++) {
            Review review = new Review();
            String[] reviewItems = reviewsArray[i].split(";", 2);
            review.setAuthor(reviewItems[0]);
            review.setContent(reviewItems[1]);
            reviews.add(review);
        }
        return reviews;
    }

}
