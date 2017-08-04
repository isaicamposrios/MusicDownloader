package net.ddns.paolo7297.musicdownloader.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import net.ddns.paolo7297.musicdownloader.Constants;
import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.AlbumAdapter;
import net.ddns.paolo7297.musicdownloader.placeholder.Album;
import net.ddns.paolo7297.musicdownloader.ui.activity.SongDisplayActivity;

import java.util.ArrayList;

/**
 * Created by paolo on 27/07/17.
 */

public class AlbumFragment extends Fragment {
    private GridView gridView;
    private AlbumAdapter adapter;
    private ArrayList<Album> albums;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_album, null);
        gridView = (GridView) v.findViewById(R.id.grid);
        albums = new ArrayList<>();
        adapter = new AlbumAdapter(albums, getActivity());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SongDisplayActivity.class);
                intent.putExtra(Constants.SONGS_TYPE, 0);
                intent.putExtra(Constants.SONGS_CONTENT, albums.get(position));
                startActivity(intent);
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        albums.clear();
        albums.addAll(Album.getAlbums(getActivity()));
        System.out.println(albums.size());
    }
}
