package com.irk.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Rajesh on 06-Dec-16.
 */

public class GridViewAdapter extends ArrayAdapter<GridItem> {

    private Context mContext;
    private ArrayList<GridItem> gridItems = new ArrayList<>();
    public GridViewAdapter(Context context, ArrayList<GridItem> objects) {
        super(context, 0, objects);
        mContext = context;
        gridItems = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        View rootView = convertView;
        if (rootView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            rootView = inflater.inflate(R.layout.grid_item_poster, parent, false);
            imageView = (ImageView) rootView.findViewById(R.id.movie_poster);
            rootView.setTag(imageView);
            }else {
            imageView = (ImageView) rootView.getTag();
        }
        GridItem item = gridItems.get(position);
        Picasso.with(mContext).load(item.getPoster()).into(imageView);
        return rootView;
    }
}
