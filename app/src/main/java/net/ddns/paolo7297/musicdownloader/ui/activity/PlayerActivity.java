package net.ddns.paolo7297.musicdownloader.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.playback.MasterPlayer;

/**
 * Created by paolo on 05/07/17.
 */

public class PlayerActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MasterPlayer masterPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        masterPlayer = MasterPlayer.getInstance(getApplicationContext());
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //setTitle("In riproduzione");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        /*MasterPlayer.MPInfo info = masterPlayer.getInfos();
        if (info.getStatus() == MasterPlayer.STATUS_NEED_CONFIGURATION) {
            finish();
        } else {

            title.setText(info.getTitle());
            artist.setText(info.getArtist());
            if (info.getStatus() == MasterPlayer.STATUS_PREPARING) {
                spinner.setVisibility(View.VISIBLE);
                pp.setVisibility(View.GONE);
            } else {
                spinner.setVisibility(View.GONE);
                pp.setVisibility(View.VISIBLE);
            }
            if (info.getStatus() == MasterPlayer.STATUS_PLAYING) {
                setPlaying();
            } else {
                setPaused();
            }

        }*/

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
