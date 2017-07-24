package net.ddns.paolo7297.musicdownloader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.placeholder.YoutubeResult;

import java.util.ArrayList;

/**
 * Created by paolo on 24/07/17.
 */

public class YoutubeSearchAdapter extends BaseAdapter {

    private ArrayList<YoutubeResult> results;
    private Context context;

    public YoutubeSearchAdapter(ArrayList<YoutubeResult> results, Context context) {
        this.results = results;
        this.context = context;
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public Object getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.row_youtube_search_result, null);
        ((TextView) convertView.findViewById(R.id.title)).setText(results.get(position).getTitle());
        ((TextView) convertView.findViewById(R.id.channel)).setText(results.get(position).getChannel());
        Picasso.with(context).load(results.get(position).getThumbnailUrl()).into((ImageView) convertView.findViewById(R.id.img));

        return convertView;
    }
}
