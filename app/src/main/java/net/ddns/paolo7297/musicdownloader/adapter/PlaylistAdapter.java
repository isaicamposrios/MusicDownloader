package net.ddns.paolo7297.musicdownloader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.placeholder.Playlist;

import java.util.ArrayList;

/**
 * Created by paolo on 10/05/17.
 */

public class PlaylistAdapter extends BaseAdapter {

    private ArrayList<Playlist> playlists;
    private Context context;

    public PlaylistAdapter(ArrayList<Playlist> playlists, Context context) {
        this.playlists = playlists;
        this.context = context;
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public Object getItem(int position) {
        return playlists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (convertView == null) convertView = inflater.inflate(R.layout.row_playlist,parent,false);
        ((TextView) convertView.findViewById(R.id.title)).setText(playlists.get(position).getName());
        ((TextView) convertView.findViewById(R.id.subtitle)).setText(playlists.get(position).getCount()+" canzoni presenti");
        return convertView;
    }
}
