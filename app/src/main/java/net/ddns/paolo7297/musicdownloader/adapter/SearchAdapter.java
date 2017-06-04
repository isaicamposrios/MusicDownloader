package net.ddns.paolo7297.musicdownloader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;

import java.util.ArrayList;

/**
 * Created by paolo on 30/10/16.
 */

public class SearchAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private Context context;

    public SearchAdapter(Context context, ArrayList<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_search_result, null);
        }
        ((TextView) convertView.findViewById(R.id.title)).setText(songs.get(position).getName());
        ((TextView) convertView.findViewById(R.id.artist)).setText(songs.get(position).getArtist());
        ((TextView) convertView.findViewById(R.id.size)).setText(songs.get(position).getSize());
        ((TextView) convertView.findViewById(R.id.bitrate)).setText(songs.get(position).getBitrate());
        long i = songs.get(position).getLength() / 60;
        int d = (int) (((float) songs.get(position).getLength() / 60 - i) * 60);
        ((TextView) convertView.findViewById(R.id.duration)).setText(String.format("%d:%02d min", i, d));
        return convertView;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs.clear();
        this.songs.addAll(songs);
    }

    /*private Bitmap getTumbnail(SearchResult song) {
        Bitmap b = null;
        MediaMetadataRetriever mediaMetadataRetriever =  new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(song.getFile(),new HashMap<String, String>());
        byte art[] = mediaMetadataRetriever.getEmbeddedPicture();
        if (art != null ) b = BitmapFactory.decodeByteArray(art,0,art.length);
        return b;
    }*/
}
