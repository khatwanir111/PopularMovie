package com.irk.android.popularmovies;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (findViewById(R.id.activity_detail) != null) {
            if (savedInstanceState != null) {
                return;
            }
            getSupportFragmentManager().beginTransaction().add(R.id.activity_detail, new DetailFragment()).commit();
        }
    }

    public static class DetailFragment extends Fragment {

        public DetailFragment(){}


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootview = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                String title = intent.getStringExtra(getString(R.string.title));
                ((TextView) rootview.findViewById(R.id.title)).setText(title);

                String poster = intent.getStringExtra(getString(R.string.poster));
                ImageView imageView = (ImageView) rootview.findViewById(R.id.detail_poster);
                Picasso.with(getContext()).load(poster).into(imageView);

                String releaseDate = intent.getStringExtra(getString(R.string.release_date));
                ((TextView) rootview.findViewById(R.id.release_date)).setText(releaseDate);

                String rating = intent.getStringExtra(getString(R.string.rating));
                ((TextView) rootview.findViewById(R.id.rating)).setText(rating);

                String plot = intent.getStringExtra(getString(R.string.plot));
                ((TextView) rootview.findViewById(R.id.plot)).setText(plot);

        }
            return rootview;
        }
    }

}
