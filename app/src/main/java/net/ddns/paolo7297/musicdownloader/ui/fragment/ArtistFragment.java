package net.ddns.paolo7297.musicdownloader.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import net.ddns.paolo7297.musicdownloader.Constants;
import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.ArtistAdapter;
import net.ddns.paolo7297.musicdownloader.placeholder.Artist;
import net.ddns.paolo7297.musicdownloader.ui.activity.SongDisplayActivity;

import java.util.ArrayList;

/**
 * Created by paolo on 26/07/17.
 */

public class ArtistFragment extends Fragment {

    private ListView listView;
    private ArtistAdapter adapter;
    private ArrayList<Artist> artists;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artists, null);
        listView = (ListView) v.findViewById(R.id.list);
        artists = new ArrayList<>();
        adapter = new ArtistAdapter(artists, getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SongDisplayActivity.class);
                intent.putExtra(Constants.SONGS_TYPE, 1);
                intent.putExtra(Constants.SONGS_CONTENT, artists.get(position));
                startActivity(intent);
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        artists.clear();
        artists.addAll(Artist.getArtists(getActivity()));
        System.out.println(artists.size());
    }
}
