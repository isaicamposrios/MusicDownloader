package net.ddns.paolo7297.musicdownloader.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import net.ddns.paolo7297.musicdownloader.R;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.IOException;

/**
 * Created by paolo on 26/05/17.
 */

public class SongsEditActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String song;
    private EditText artist, title, album, genre, year, composer;
    private FloatingActionButton fab;
    private File songFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editsong);
        Intent intent = getIntent();
        song = intent.getStringExtra("SONG");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (EditText) findViewById(R.id.title);
        artist = (EditText) findViewById(R.id.artist);
        album = (EditText) findViewById(R.id.album);
        genre = (EditText) findViewById(R.id.genre);
        year = (EditText) findViewById(R.id.year);
        composer = (EditText) findViewById(R.id.composer);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        setSupportActionBar(toolbar);
        setTitle("Modifica");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        songFile = new File(song);
        if (songFile.exists()) {
            try {
                final MusicMetadataSet metadata = new MyID3().read(songFile);
                if (metadata != null) {
                    final MusicMetadata meta = (MusicMetadata) metadata.getSimplified();
                    title.setText(meta.getSongTitle());
                    artist.setText(meta.getArtist());
                    album.setText(meta.getAlbum());
                    genre.setText(meta.getGenre());
                    year.setText(meta.getYear());
                    composer.setText(meta.getComposer());

                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (title.getText() != null)
                                meta.setSongTitle(title.getText().toString());
                            if (artist.getText() != null)
                                meta.setArtist(artist.getText().toString());
                            if (album.getText() != null)
                                meta.setAlbum(album.getText().toString());
                            if (genre.getText() != null)
                                meta.setGenre(genre.getText().toString());
                            if (year.getText() != null)
                                meta.setYear(year.getText().toString());
                            if (composer.getText() != null)
                                meta.setComposer(composer.getText().toString());
                            try {
                                new MyID3().update(songFile, metadata, meta);
                                finish();
                            } catch (IOException | ID3WriteException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    title.setEnabled(false);
                    artist.setEnabled(false);
                    album.setEnabled(false);
                    fab.setVisibility(View.GONE);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
