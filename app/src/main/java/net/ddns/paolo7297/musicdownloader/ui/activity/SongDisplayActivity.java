package net.ddns.paolo7297.musicdownloader.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.ddns.paolo7297.musicdownloader.Constants;
import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.LocalSongsAdapter;
import net.ddns.paolo7297.musicdownloader.adapter.PlaylistAdapter;
import net.ddns.paolo7297.musicdownloader.placeholder.Album;
import net.ddns.paolo7297.musicdownloader.placeholder.Artist;
import net.ddns.paolo7297.musicdownloader.placeholder.Playlist;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;
import net.ddns.paolo7297.musicdownloader.playback.MasterPlayer;
import net.ddns.paolo7297.musicdownloader.playback.PlaylistDBHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by paolo on 26/07/17.
 */

public class SongDisplayActivity extends AppCompatActivity {
    private ListView listView;
    private LocalSongsAdapter adapter;
    private ArrayList<Song> songs;
    private Toolbar toolbar;
    private int type;
    private Album album;
    private Artist artist;
    private MasterPlayer masterPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_song);
        listView = (ListView) findViewById(R.id.list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        songs = new ArrayList<>();
        adapter = new LocalSongsAdapter(songs, getApplicationContext());
        masterPlayer = MasterPlayer.getInstance(getApplicationContext());
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                masterPlayer.setup(songs.toArray(new Song[songs.size()]), position);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        type = getIntent().getIntExtra(Constants.SONGS_TYPE, 0);
        if (type == 0) {
            album = getIntent().getParcelableExtra(Constants.SONGS_CONTENT);
            setTitle(album.getAlbum());
            songs.clear();
            songs.addAll(Song.getSongs(getApplicationContext(), album));
            adapter.notifyDataSetChanged();
        } else {
            artist = getIntent().getParcelableExtra(Constants.SONGS_CONTENT);
            setTitle(artist.getName());
            songs.clear();
            songs.addAll(Song.getSongs(getApplicationContext(), artist));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_downloads, menu);
        //menu.setHeaderTitle(songs.get(((AdapterView.AdapterContextMenuInfo) menuInfo).position).getName());

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.share:
                Uri uri = Uri.fromFile(new File(songs.get(menuInfo.position).getFile()));
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("audio/mp3");
                i.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(i, String.format(Locale.getDefault(), "%s \"%s\"", getString(R.string.share), songs.get(menuInfo.position).getName())));
                return true;
            case R.id.delete:

                //Toast.makeText(getActivity(), "So di dovere mettere un messaggio di conferma, ma sono pigro", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.are_you_sure);
                builder.setMessage(String.format(Locale.getDefault(), "%s \"%s\"?", getString(R.string.want_to_del), songs.get(menuInfo.position).getName()));
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA + "='" + songs.get(menuInfo.position).getFile() + "'", null);
                        new File(songs.get(menuInfo.position).getFile()).delete();
                        onResume();
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                builder.show();
                return true;
            case R.id.add:
                View view = LayoutInflater.from(this).inflate(R.layout.dialog_playlists, null, false);
                final AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                //builder1.setTitle("Aggiungi a:");
                builder1.setView(view);
                ListView listView = (ListView) view.findViewById(R.id.list);
                final PlaylistDBHelper dbHelper = PlaylistDBHelper.getInstance(getApplicationContext());
                final ArrayList<Playlist> playlists = dbHelper.getPlaylists();
                PlaylistAdapter adapter = new PlaylistAdapter(playlists, this);
                listView.setAdapter(adapter);
                final AlertDialog a = builder1.create();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dbHelper.addSongToPlaylist(songs.get(menuInfo.position), playlists.get(position).getName());
                        a.dismiss();
                    }
                });
                a.show();
                return true;
            case R.id.edit:
                Intent i1 = new Intent(this, SongsEditActivity.class);
                i1.putExtra("SONG", songs.get(menuInfo.position).getFile());
                startActivity(i1);
                return true;
            default:
                return false;
        }
    }
}
