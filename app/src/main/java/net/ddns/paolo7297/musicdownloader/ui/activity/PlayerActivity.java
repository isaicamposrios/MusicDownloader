package net.ddns.paolo7297.musicdownloader.ui.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import net.ddns.paolo7297.musicdownloader.CacheManager;
import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.DownloadedSongsAdapter;
import net.ddns.paolo7297.musicdownloader.playback.MasterPlayer;
import net.ddns.paolo7297.musicdownloader.ui.SquaredImageView;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by paolo on 05/07/17.
 */

public class PlayerActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MasterPlayer masterPlayer;
    private SeekBar bar;
    private TextView timeLapsed, timeCompleted;
    private SquaredImageView imgView;
    private ImageButton prev, pp, next, shuffle, repeat;
    private CacheManager cacheManager;
    private ListView listView;
    private DownloadedSongsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        cacheManager = CacheManager.getInstance(getApplicationContext());
        masterPlayer = MasterPlayer.getInstance(getApplicationContext());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        bar = (SeekBar) findViewById(R.id.seekbar);
        timeLapsed = (TextView) findViewById(R.id.time_lapsed);
        timeCompleted = (TextView) findViewById(R.id.time_completed);
        imgView = (SquaredImageView) findViewById(R.id.img);
        prev = (ImageButton) findViewById(R.id.rewind);
        pp = (ImageButton) findViewById(R.id.play);
        next = (ImageButton) findViewById(R.id.forward);
        repeat = (ImageButton) findViewById(R.id.repeat);
        shuffle = (ImageButton) findViewById(R.id.shuffle);
        listView = (ListView) findViewById(R.id.list);

        adapter = new DownloadedSongsAdapter(masterPlayer.getSongs(), getApplicationContext());
        listView.setAdapter(adapter);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                masterPlayer.prev();
            }
        });
        pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                masterPlayer.toggle();
                refreshplayer();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                masterPlayer.next();
            }
        });
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                masterPlayer.toggleRepeat();
                refreshplayer();
            }
        });
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                masterPlayer.toggleShuffle();
                refreshplayer();
            }
        });

        setSupportActionBar(toolbar);
        //setTitle("In riproduzione");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setupPlayer();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                masterPlayer.setSong(position);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player, menu);
        if (!cacheManager.isUrl(masterPlayer.getInfos().getFile())) {
            menu.findItem(R.id.download).setVisible(false);
        }
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download:
                cacheManager.download(masterPlayer.getSong());
                return true;
            case R.id.showqueue:
                toggleQueue();
                return true;
            default:
                return false;
        }
    }

    private void toggleQueue() {
        if (listView.getVisibility() == View.VISIBLE) {
            listView.setVisibility(View.GONE);
            imgView.setAlpha(1.0f);
        } else {
            listView.setVisibility(View.VISIBLE);
            imgView.setAlpha(0.3f);
            if (adapter.getCount() > masterPlayer.getIndex()) {
                listView.smoothScrollToPosition(masterPlayer.getIndex());
            }
        }
    }

    private void refreshplayer() {
        invalidateOptionsMenu();
        final MasterPlayer.MPInfo info = masterPlayer.getInfos();
        if (info.getStatus() != MasterPlayer.STATUS_NEED_CONFIGURATION) {
            setTitle(info.getTitle());
            toolbar.setSubtitle(info.getArtist());
            if (info.getStatus() == MasterPlayer.STATUS_PREPARING) {
                prev.setEnabled(false);
                pp.setEnabled(false);
                next.setEnabled(false);
            } else {
                prev.setEnabled(true);
                pp.setEnabled(true);
                next.setEnabled(true);
            }
            setPlaying(info.getStatus() == MasterPlayer.STATUS_PLAYING);
            if (masterPlayer.getShuffle() == MasterPlayer.SHUFFLE_ENABLED) {
                shuffle.setImageResource(R.drawable.controller_shuffle_enabled);
            } else {
                shuffle.setImageResource(R.drawable.controller_shuffle_disabled);
            }
            if (masterPlayer.getRepeat() == MasterPlayer.REPEAT_ALL) {
                repeat.setImageResource(R.drawable.controller_repeat_all);
            } else {
                repeat.setImageResource(R.drawable.controller_repeat_one);
            }
            if (listView.getVisibility() == View.VISIBLE && adapter.getCount() > masterPlayer.getIndex()) {
                listView.smoothScrollToPosition(masterPlayer.getIndex());
            }
        }
    }

    private void updateSeekBar() {
        MasterPlayer.MPInfo info = masterPlayer.getInfos();
        bar.setMax((int) (info.getTotalDuration()));
        bar.setProgress((int) (info.getDuration()));
        updateTimers();
    }

    private void updateTimers() {
        MasterPlayer.MPInfo info = masterPlayer.getInfos();
        timeCompleted.setText(String.format(Locale.getDefault(), "%2d:%02d", (info.getTotalDuration() / 1000) / 60, (info.getTotalDuration() / 1000) % 60));
        timeLapsed.setText(String.format(Locale.getDefault(), "%2d:%02d", (info.getDuration() / 1000) / 60, (info.getDuration() / 1000) % 60));
    }

    private void setPlaying(boolean isPlaying) {
        if (isPlaying) {
            pp.setImageResource(R.drawable.controller_pause);
        } else {
            pp.setImageResource(R.drawable.controller_play);
        }
    }


    private void setupPlayer() {
        final MasterPlayer.MPInfo info = masterPlayer.getInfos();
        if (info.getStatus() == MasterPlayer.STATUS_NEED_CONFIGURATION) {
            finish();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final int curIndex = masterPlayer.getIndex();
                    final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    System.out.println(info.getFile());
                    if (cacheManager.isUrl(info.getFile())) {
                        mediaMetadataRetriever.setDataSource(info.getFile(), new HashMap<String, String>());
                    } else {
                        mediaMetadataRetriever.setDataSource(info.getFile());
                    }
                    byte[] art = mediaMetadataRetriever.getEmbeddedPicture();
                    final Bitmap b;
                    if (art != null) {
                        b = BitmapFactory.decodeByteArray(art, 0, art.length);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (curIndex == masterPlayer.getIndex())
                                    if (imgView != null)
                                        imgView.setImageBitmap(b);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (curIndex == masterPlayer.getIndex())
                                    if (imgView != null)
                                        imgView.setImageResource(R.drawable.logo_red_white);
                            }
                        });
                    }
                }
            }).start();

            setPlaying(masterPlayer.isPlaying());
            //updateSeekBar();

            final Handler h = new Handler();
            h.post(new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                    h.postDelayed(this, 500);
                }
            });
            masterPlayer.setCallback(new MasterPlayer.MasterPlayerTrackChange() {
                @Override
                public void OnTrackChange() {
                    setupPlayer();
                }
            });

            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) masterPlayer.seek(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            refreshplayer();
            adapter.notifyDataSetChanged();
        }
    }
}
