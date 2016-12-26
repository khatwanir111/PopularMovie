package com.irk.android.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MoviesFragment extends Fragment  {


    private GridViewAdapter mMoviesAdapter;
    private ArrayList<GridItem> mGridData;
    String BASE_URL;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private SharedPreferences sharedPref;

    public MoviesFragment() {}


    @Override
    public void onStart() {
        super.onStart();
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                sharedPref = sharedPreferences;
                mMoviesAdapter.clear();
                if (mGridData.isEmpty()) {
                    new FetchMoviesTask().execute();
                }
            }
        };

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        new FetchMoviesTask().execute();
        mGridData = new ArrayList<>();
        mMoviesAdapter = new GridViewAdapter(getActivity(), mGridData);
        gridView.setAdapter(mMoviesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridItem poster = mMoviesAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(getString(R.string.title), poster.getTitle());
                intent.putExtra(getString(R.string.poster), poster.getPoster());
                intent.putExtra(getString(R.string.plot), poster.getPlot());
                intent.putExtra(getString(R.string.rating), poster.getRating().toString());
                intent.putExtra(getString(R.string.release_date), poster.getReleaseDate());
                startActivity(intent);
            }
        });

        return rootView;
    }

    private ArrayList<GridItem> getPosterDataFromJSON(String posterJsonStr)
            throws Exception {
        final String TMDB_RESULTS = "results";
        final String TMDB_TITLE = "title";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_PLOT = "overview";
        final String TMDB_RATING = "vote_average";
        final String TMDB_RELEASEDATE = "release_date";
        final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185";

        JSONObject jsonObject = new JSONObject(posterJsonStr);
        JSONArray resultsArray = jsonObject.getJSONArray(TMDB_RESULTS);

        ArrayList<GridItem> gridItems = new ArrayList<>();
        GridItem item;
        Log.v("Result", "" + resultsArray.length());

        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject movieInfo = resultsArray.getJSONObject(i);
            String title = movieInfo.getString(TMDB_TITLE);
            String posterPath = movieInfo.getString(TMDB_POSTER_PATH);
            String plot = movieInfo.getString(TMDB_PLOT);
            Double rating = movieInfo.getDouble(TMDB_RATING);
            String release_date = movieInfo.getString(TMDB_RELEASEDATE);
            String POSTER_URL = POSTER_BASE_URL + posterPath;

            item = new GridItem();

            item.setTitle(title);
            item.setPoster(POSTER_URL);
            item.setPlot(plot);
            item.setRating(rating);
            item.setReleaseDate(release_date);
            gridItems.add(item);
        }
        return gridItems;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private String selectBaseUrl() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
        String sort = sharedPref.getString(getString(R.string.key_sort_by), getString(R.string.popular_value));

        if (sort.equals(getString(R.string.popular_value))) {
            BASE_URL = "http://api.themoviedb.org/3/movie/popular?";
        } else {
            BASE_URL = "http://api.themoviedb.org/3/movie/top_rated?";
        }

        return BASE_URL;
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, ArrayList<GridItem>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected void onPostExecute(ArrayList<GridItem> gridItems) {
            super.onPostExecute(gridItems);
            mMoviesAdapter.clear();
            if (gridItems != null) {
                mMoviesAdapter.addAll(gridItems);
            }
        }

        @Override
        protected ArrayList<GridItem> doInBackground(Void... params) {

            final String API_KEY = "api_key";
            String posterJsonStr;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                selectBaseUrl();

                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendQueryParameter(API_KEY, BuildConfig.API_KEY).build();

                URL url = new URL(buildUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                posterJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MoviesFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getPosterDataFromJSON(posterJsonStr);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

    }

}
