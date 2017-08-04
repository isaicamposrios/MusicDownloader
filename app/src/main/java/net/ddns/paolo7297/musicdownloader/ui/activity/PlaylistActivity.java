package net.ddns.paolo7297.musicdownloader.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.LocalSongsAdapter;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;
import net.ddns.paolo7297.musicdownloader.playback.MasterPlayer;
import net.ddns.paolo7297.musicdownloader.playback.PlaylistDBHelper;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by paolo on 11/05/17.
 */

public class PlaylistActivity extends AppCompatActivity {

    private ListView listView;
    private PlaylistDBHelper dbHelper;
    private LocalSongsAdapter adapter;
    private Toolbar toolbar;
    private String playlist;
    private ArrayList<Song> songs;
    private MasterPlayer masterPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        Intent intent = getIntent();
        playlist = intent.getStringExtra("playlist");
        masterPlayer = MasterPlayer.getInstance(getApplicationContext());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        listView = (ListView) findViewById(R.id.list);
        songs = new ArrayList<>();
        setSupportActionBar(toolbar);
        setTitle(playlist);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        adapter = new LocalSongsAdapter(songs, getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                masterPlayer.setup(songs.toArray(new Song[songs.size()]), position);
            }
        });

        registerForContextMenu(listView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper = PlaylistDBHelper.getInstance(getApplicationContext());
        songs.clear();
        songs.addAll(dbHelper.getSongs(playlist));
        adapter.notifyDataSetChanged();
        toolbar.setSubtitle(String.format(Locale.getDefault(), "%d %s", songs.size(), getString(R.string.elements)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.options_playlist_song, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInf = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                dbHelper.deleteSong(songs.get(menuInf.position), playlist);
                onResume();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
