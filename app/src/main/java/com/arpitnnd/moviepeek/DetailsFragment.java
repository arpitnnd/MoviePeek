package com.arpitnnd.moviepeek;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.arpitnnd.moviepeek.adapters.ReviewAdapter;
import com.arpitnnd.moviepeek.adapters.TrailerAdapter;
import com.arpitnnd.moviepeek.data.DBHandler;
import com.arpitnnd.moviepeek.data.MovieDetails;
import com.arpitnnd.moviepeek.data.Trailer;
import com.bumptech.glide.Glide;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.parceler.Parcels;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailsFragment extends Fragment {

    private MovieDetails mMovie;
    private ShareActionProvider mShareActionProvider;

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMovie = Parcels.unwrap(getArguments().getParcelable("movie"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_details, container, false);

        ((TextView) v.findViewById(R.id.title)).setText(mMovie.getMovieTitle());
        Drawable d = ContextCompat.getDrawable(getActivity(), R.drawable.loading);
        Glide.with(this).load("http://image.tmdb.org/t/p/w185/" + mMovie.getPosterPath()).
                placeholder(d).
                into((ImageView) v.findViewById(R.id.poster));
        SimpleDateFormat input = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault());
        try {
            ((TextView) v.findViewById(R.id.date)).
                    setText(DateFormat.getDateInstance(DateFormat.SHORT).
                            format(input.parse(mMovie.getReleaseDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ((TextView) v.findViewById(R.id.rating)).
                setText(String.format("%.2f", Float.valueOf(mMovie.getVoteAverage())));
        ((TextView) v.findViewById(R.id.overview)).setText(mMovie.getPlot());

        final DBHandler db = new DBHandler(getActivity().getApplicationContext());
        CheckBox favCheckBox = (CheckBox) v.findViewById(R.id.fav_checkBox);
        if (db.isFav(mMovie.getMovieId()))
            favCheckBox.setChecked(true);
        favCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    db.addFavMovie(mMovie);
                else db.deleteFavMovie(mMovie.getMovieId());
            }
        });

        RecyclerView trailerRecyclerView = (RecyclerView) v.findViewById(R.id.trailers_recycler);
        RecyclerView.LayoutManager llm = new LinearLayoutManager(getActivity(),
                        LinearLayoutManager.HORIZONTAL, false);
        if (trailerRecyclerView != null) {
            trailerRecyclerView.setHasFixedSize(false);
            trailerRecyclerView.setLayoutManager(llm);
            TrailerAdapter trailerAdapter = new TrailerAdapter(mMovie.getTrailers(), getActivity());
            trailerRecyclerView.setAdapter(trailerAdapter);
            trailerAdapter.setOnItemClickListener(new TrailerAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {
                    Intent yIntent = new Intent(Intent.ACTION_VIEW);
                    yIntent.setData(Uri.parse("https://www.youtube.com/watch?v="
                            + mMovie.getTrailers().get(position).getKey()));
                    startActivity(yIntent);
                }
            });
        }

        if (mMovie.getReviews().size() != 0) {
            RecyclerView reviewRecyclerView = (RecyclerView) v.findViewById(R.id.reviews_recycler);
            if (reviewRecyclerView != null) {
                reviewRecyclerView.setHasFixedSize(false);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                reviewRecyclerView.setLayoutManager(layoutManager);
                reviewRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.
                        Builder(getActivity()).
                        size(5).
                        build());
                ReviewAdapter reviewAdapter = new ReviewAdapter(mMovie.getReviews());
                reviewRecyclerView.setAdapter(reviewAdapter);
            }
        } else v.findViewById(R.id.noReviews_textView).setVisibility(View.VISIBLE);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        NestedScrollView parent = (NestedScrollView) getView().findViewById(R.id.parent_scrollView);
        ScrollView child = (ScrollView) getView().findViewById(R.id.child_scrollView);
        outState.putIntArray("DETAILS_PARENT_SCROLL_STATE",
                new int[]{parent.getScrollX(), parent.getScrollY()});
        outState.putIntArray("DETAILS_CHILD_SCROLL_STATE",
                new int[]{child.getScrollX(), child.getScrollY()});
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            final int[] parentPosition = savedInstanceState.getIntArray("DETAILS_PARENT_SCROLL_STATE");
            final int[] childPosition = savedInstanceState.getIntArray("DETAILS_CHILD_SCROLL_STATE");
            NestedScrollView parent = (NestedScrollView) getView().findViewById(R.id.parent_scrollView);
            ScrollView child = (ScrollView) getView().findViewById(R.id.child_scrollView);

            parent.scrollTo(parentPosition[0], parentPosition[1]);
            child.scrollTo(childPosition[0], childPosition[1]);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_details, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareIntent(mMovie.getTrailers().get(0));
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setShareIntent(Trailer trailer) {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);

            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mMovie.getMovieTitle());
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, mMovie.getMovieTitle() +
                    ": " + trailer.getTrailerName() + ": " +
                    "https://www.youtube.com/watch?v=" +
                    trailer.getKey());
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

}