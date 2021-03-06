package net.ddns.paolo7297.musicdownloader.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;
import net.ddns.paolo7297.musicdownloader.playback.MasterPlayer;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by paolo on 20/04/17.
 */

public class LocalSongsAdapter extends BaseAdapter {

    private ArrayList<Song> files;
    private Context context;
    private MasterPlayer masterPlayer;

    public LocalSongsAdapter(ArrayList<Song> files, Context context) {
        this.files = files;
        this.context = context;
        masterPlayer = MasterPlayer.getInstance(context);
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

        long i = files.get(position).getLength() / 60;
        int d = (int) (((float) files.get(position).getLength() / 60 - i) * 60);
        ((TextView) convertView.findViewById(R.id.duration)).setText(String.format(Locale.getDefault(), "%d:%02d min", i, d));
        if (masterPlayer.getSong() != null && files.get(position).equals(masterPlayer.getSong())) {
            ((TextView) convertView.findViewById(R.id.title)).setTypeface(null, Typeface.BOLD);
            ((TextView) convertView.findViewById(R.id.artist)).setTypeface(null, Typeface.BOLD);
        } else {
            ((TextView) convertView.findViewById(R.id.title)).setTypeface(null, Typeface.NORMAL);
            ((TextView) convertView.findViewById(R.id.artist)).setTypeface(null, Typeface.NORMAL);
        }
        return convertView;
    }
}
