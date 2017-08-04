package net.ddns.paolo7297.musicdownloader.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.placeholder.Album;

import java.util.ArrayList;

/**
 * Created by paolo on 27/07/17.
 */

public class AlbumAdapter extends BaseAdapter {
    private ArrayList<Album> albums;
    private Context context;

    public AlbumAdapter(ArrayList<Album> albums, Context context) {
        this.albums = albums;
        this.context = context;
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int position) {
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_album, null);
        }
        ((TextView) convertView.findViewById(R.id.title)).setText(albums.get(position).getAlbum());
        ((TextView) convertView.findViewById(R.id.artist)).setText(albums.get(position).getArtist());
        if (albums.get(position).getAlbumArt() == null) {
            ((ImageView) convertView.findViewById(R.id.img)).setImageResource(R.drawable.logo_red_white);
        } else {
            ((ImageView) convertView.findViewById(R.id.img)).setImageDrawable(BitmapDrawable.createFromPath(albums.get(position).getAlbumArt()));
        }
        return convertView;
    }
}
