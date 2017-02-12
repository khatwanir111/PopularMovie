package com.irk.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.R.attr.focusable;
import static android.R.attr.key;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<TrailerItem>> {

    public DetailFragment() {
    }

    private static final int TRAILER_LOADER = 22;
    private ArrayAdapter<String> mTrailerAdapter;
    private ListView listView;
    private String TRAILER_BASE_URL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();

            if (bundle != null) {
                String title = bundle.getString(getString(R.string.title));
                ((TextView) rootview.findViewById(R.id.title)).setText(title);

                String poster = bundle.getString(getString(R.string.poster));
                ImageView imageView = (ImageView) rootview.findViewById(R.id.detail_poster);
                Picasso.with(getContext()).load(poster).into(imageView);

                String releaseDate = bundle.getString(getString(R.string.release_date));
                ((TextView) rootview.findViewById(R.id.release_date)).setText(releaseDate);

                String rating = bundle.getString(getString(R.string.rating));
                ((TextView) rootview.findViewById(R.id.rating)).setText(rating);

                String plot = bundle.getString(getString(R.string.plot));
                ((TextView) rootview.findViewById(R.id.plot)).setText(plot);

                String id = bundle.getString(getString(R.string.id));
                TRAILER_BASE_URL = "http://api.themoviedb.org/3/movie/" + id + "/videos";

            }
        }


        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<String[]> loader = loaderManager.getLoader(TRAILER_LOADER);
        if (loader == null) {
            loaderManager.initLoader(TRAILER_LOADER, null, this).forceLoad();
        } else {
            loaderManager.restartLoader(TRAILER_LOADER, null, this).forceLoad();
        }

        mTrailerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.trailer_list_item, R.id.trailer_text_view, new ArrayList<String>());
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        listView = (ListView) rootView.findViewById(R.id.trailer_list_view);

        listView.setAdapter(mTrailerAdapter);
        return rootview;
    }

    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private ArrayList<TrailerItem> getTrailerDataFromJson(String trailerJsonString) throws Exception {
        final String TMDB_TRAILER_RESULTS = "results";
        final String TMDB_TRAILER_NAME = "name";
        final String TMDB_TRAILER_KEY = "key";

        String key;
        String name;

        JSONObject jsonObject = new JSONObject(trailerJsonString);
        JSONArray resultsArray = jsonObject.getJSONArray(TMDB_TRAILER_RESULTS);

        ArrayList<TrailerItem> trailerList = new ArrayList<>();

        TrailerItem trailerItem;

        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject info = resultsArray.getJSONObject(i);
            key = info.getString(TMDB_TRAILER_KEY);
            name = info.getString(TMDB_TRAILER_NAME);

            trailerItem = new TrailerItem();

            trailerItem.setKey(key);
            trailerItem.setTrailerName(name);
            trailerList.add(trailerItem);

        }
        return trailerList;
    }

    @Override
    public Loader<ArrayList<TrailerItem>> onCreateLoader(int id_loader, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<TrailerItem>>(getContext()) {

            private final String LOG_TAG = DetailFragment.class.getSimpleName();

            @Override
            public ArrayList<TrailerItem> loadInBackground() {
                final String API_KEY = "api_key";
                String trailerJsonString;

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                try {
                    Uri buildUri = Uri.parse(TRAILER_BASE_URL).buildUpon().appendQueryParameter(API_KEY, BuildConfig.API_KEY).build();
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
                    trailerJsonString = buffer.toString();
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
                            Log.e("DetailActivity", "Error closing stream", e);
                        }
                    }
                }
                try {
                    return getTrailerDataFromJson(trailerJsonString);
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }

                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<TrailerItem>> loader, ArrayList<TrailerItem> data) {
        final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
        final String YOUTUBE_QUERY = "v";
        String key;
        String name;
        mTrailerAdapter.clear();
        for (int i = 0; i < data.size(); i++) {

        if (data != null) {

            key = data.get(i).getKey();
            name = data.get(i).getTrailerName();

            Uri uri = Uri.parse(YOUTUBE_BASE_URL).buildUpon().appendQueryParameter(YOUTUBE_QUERY, key).build();
            URL url = null;
            try {
                url = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            mTrailerAdapter.add(name);
            final URL finalUrl = url;
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    openWebPage(finalUrl.toString());
                }
            });

        }
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<TrailerItem>> loader) {

    }

}
