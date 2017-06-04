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
 * Created by paolo on 20/04/17.
 */

public class DownloadedSongsAdapter extends BaseAdapter {

    ArrayList<Song> files;
    Context context;

    public DownloadedSongsAdapter(ArrayList<Song> files, Context context) {
        this.files = files;
        this.context = context;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_download, null);
        }
        ((TextView) convertView.findViewById(R.id.title)).setText(files.get(position).getName());
        ((TextView) convertView.findViewById(R.id.artist)).setText(files.get(position).getArtist());
        //((TextView) convertView.findViewById(R.id.size)).setText();
        ((TextView) convertView.findViewById(R.id.bitrate)).setText(files.get(position).getBitrate());
        long i = files.get(position).getLength() / 60;
        int d = (int) (((float) files.get(position).getLength() / 60 - i) * 60);
        ((TextView) convertView.findViewById(R.id.duration)).setText(String.format("%d:%02d min", i, d));

        return convertView;
    }
}
