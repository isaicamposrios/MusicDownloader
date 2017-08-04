package net.ddns.paolo7297.musicdownloader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.placeholder.Artist;

import java.util.ArrayList;

/**
 * Created by paolo on 26/07/17.
 */

public class ArtistAdapter extends BaseAdapter {

    private ArrayList<Artist> artists;
    private Context context;

    public ArtistAdapter(ArrayList<Artist> artists, Context context) {
        this.artists = artists;
        this.context = context;
    }

    @Override
    public int getCount() {
        return artists.size();
    }

    @Override
    public Object getItem(int position) {
        return artists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) convertView = inflater.inflate(R.layout.row_artist, null);
        ((TextView) convertView.findViewById(R.id.name)).setText(artists.get(position).getName());
        ((TextView) convertView.findViewById(R.id.info)).setText(artists.get(position).getnTracks() + " " + context.getString(R.string.songs_in_playlist) + ".");
        return convertView;
    }
}
