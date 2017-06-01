package net.ddns.paolo7297.musicdownloader.playback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;
import net.ddns.paolo7297.musicdownloader.ui.activity.NavigationActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_NEXT;
import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_OPEN;
import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_PP;
import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_PREV;

/**
 * Created by paolo on 23/04/17.
 */

public class MasterPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener{

    private static MasterPlayer mp;

    private MediaPlayer player;
    private int status;
    private int index;
    private ArrayList<Song> songs;
    //private ArrayList<String> titles;
    //private ArrayList<String> artists;
    private Context context;
    private MasterPlayerTrackChange callback;
    //private String title, artist;
    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;


    public static final int STATUS_NEED_CONFIGURATION = 0;
    public static final int STATUS_OK = 1;
    public static final int STATUS_PREPARING = 2;
    public static final int STATUS_PLAYING = 3;
    public static final int STATUS_PAUSED = 4;
    public static final int STATUS_STOPPED = 5;
    public static final int TYPE_LOCAL = 0;
    public static final int TYPE_STREAMING = 1;

    public class MPInfo{
        private long duration, totalDuration;
        private String title;
        private String artist;
        private int status;

        public MPInfo(long duration, long totalDuration, String title, String artist, int status) {
            this.duration = duration;
            this.totalDuration = totalDuration;
            this.title = title;
            this.artist = artist;
            this.status = status;
        }

        public long getDuration() {
            return duration;
        }

        public long getTotalDuration() {
            return totalDuration;
        }

        public String getTitle() {
            return title;
        }

        public String getArtist() {
            return artist;
        }

        public int getStatus() {
            return status;
        }
    }

    public interface MasterPlayerTrackChange {
        void OnTrackChange();
    }


    private MasterPlayer(Context c) {
        Log.d("PLAYER","Costruttore");
        context = c;
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        setupMediaSession();
        status = STATUS_NEED_CONFIGURATION;

    }

    public static MasterPlayer getInstance(Context c) {
        if (mp == null) mp = new MasterPlayer(c);
        return mp;
    }

    public void play() {
        Log.d("PLAYER","Play");
        int result = audioManager.requestAudioFocus(this,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED &&(status == STATUS_OK || status == STATUS_PAUSED)) {
            player.start();
            status = STATUS_PLAYING;
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            if (callback != null) callback.OnTrackChange();
            setupNotification();
        }
    }

    public void pause() {
        Log.d("PLAYER","Pause");
        if (status == STATUS_PLAYING) {
            player.pause();
            status = STATUS_PAUSED;
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            if (callback != null) callback.OnTrackChange();
            setupNotification();
        }
    }

    public void toggle() {
        Log.d("PLAYER","Toggle");
        if (status == STATUS_PLAYING) {
            pause();
        } else {
            play();
        }

    }

    public void stop() {
        Log.d("PLAYER","Stop");
        if (status == STATUS_PLAYING || status == STATUS_PAUSED) {
            player.stop();
            status = STATUS_STOPPED;
            if (callback != null) callback.OnTrackChange();
        }
    }

    public void next() {
        Log.d("PLAYER","Next");
        index++;
        if (index >= songs.size()) {
            index = 0;
        }
        setSong(index);
    }

    public void prev() {
        Log.d("PLAYER","Prev");
        index--;
        if (index< 0) {
            index = songs.size()-1;
        }
        setSong(index);
    }

    public void setup(Song[] files, int index) {
        Log.d("PLAYER","Setup");
        songs = new ArrayList<>(Arrays.asList(files));
        this.index = index;
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        setSong(index);
    }

    public void setSong(int index){
        Log.d("PLAYER","Set song "+songs.get(index).toString());
        try {
            if (!isUrl(songs.get(index).getFile())) {
                if (!new File(songs.get(index).getFile()).exists()) {
                    songs.remove(index);
                }
            }
            player.reset();
            player.setDataSource(songs.get(index).getFile());

            //title = songs.get(index);
            //artist = songs.get(index);
            //setupNotification();
            player.prepareAsync();
            status = STATUS_PREPARING;
            if (callback != null) callback.OnTrackChange();

            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE,songs.get(index).getName())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,songs.get(index).getArtist()).build());
        } catch (IOException e) {
            e.printStackTrace();
            status = STATUS_NEED_CONFIGURATION;
        }

    }

    public MPInfo getInfos() {
        Log.d("PLAYER","Get Info");
        if (status == STATUS_NEED_CONFIGURATION) {
            return new MPInfo(0,0,null,null,status);
        } else {
            return new MPInfo(
                    status == STATUS_PREPARING ? 0 :player.getCurrentPosition(),
                    status == STATUS_PREPARING ? 0 :player.getDuration(),
                    songs.get(index).getName(),
                    songs.get(index).getArtist(),
                    status
            );
        }
    }

    public boolean isPlaying() {
        Log.d("PLAYER","Get Play status");
        return status == STATUS_PLAYING;
    }

    public void setCallback(MasterPlayerTrackChange callback) {
        Log.d("PLAYER","Set Callback");
        this.callback = callback;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d("PLAYER","ON PREPARE");
        status = STATUS_OK;
        play();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("PLAYER","ON COMPLETION");
        next();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Permanent loss of audio focus
            // Pause playback immediately
            pause();
        }
        else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // Lower the volume
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            //play();
        }
    }

    private void setupNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        //builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setSmallIcon(R.mipmap.ic_songshunter);
        Intent i1 = new Intent();
        i1.setClass(context,NotificationMediaButtonsReceiver.class);
        i1.setAction(NOTIFICATION_PREV);
        PendingIntent iprev = PendingIntent.getBroadcast(context,123456,i1,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent i2 = new Intent();
        i2.setClass(context,NotificationMediaButtonsReceiver.class);
        i2.setAction(NOTIFICATION_PP);
        PendingIntent ipp = PendingIntent.getBroadcast(context,123456,i2,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent i3 = new Intent();
        i3.setClass(context,NotificationMediaButtonsReceiver.class);
        i3.setAction(NOTIFICATION_NEXT);
        PendingIntent inext = PendingIntent.getBroadcast(context,123456,i3,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent io = new Intent();
        io.setClass(context, NavigationActivity.class);
        io.setAction(NOTIFICATION_OPEN);
        PendingIntent iopen = PendingIntent.getActivity(context,123456,io,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.ic_skip_previous_black_48dp,"Indietro",iprev);
        builder.addAction(status == STATUS_PLAYING ? R.drawable.ic_pause_circle_outline_black_48dp : R.drawable.ic_play_circle_outline_black_48dp,"Play",ipp);
        builder.addAction(R.drawable.ic_skip_next_black_48dp,"Successivo",inext);
        builder.setContentIntent(iopen);
        builder.setStyle(new NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1)
                .setMediaSession(mediaSession.getSessionToken()));
        builder.setContentTitle(songs.get(index).getName());
        builder.setContentText(songs.get(index).getArtist());
        builder.setOngoing(status == STATUS_PLAYING);
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1111,notification);
    }

    private void setupMediaSession() {
        mediaSession = new MediaSessionCompat(context,"MediaSession");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onCommand(String command, Bundle extras, ResultReceiver cb) {
                super.onCommand(command, extras, cb);
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                KeyEvent k = (KeyEvent) mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (k.getAction() == KeyEvent.ACTION_UP) {
                    Log.e("PLAYER", k.toString());
                    switch (k.getKeyCode()) {
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            //toggle();
                            return true;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            //prev();
                            return true;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            //next();
                            return true;
                        default:
                            return false;
                    }

                }

                return super.onMediaButtonEvent(mediaButtonEvent);
            }

            @Override
            public void onPlay() {
                super.onPlay();
                play();
            }

            @Override
            public void onSkipToQueueItem(long id) {
                super.onSkipToQueueItem(id);
            }

            @Override
            public void onPause() {
                super.onPause();
                pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                next();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                prev();
            }

            @Override
            public void onFastForward() {
                super.onFastForward();
                next();
            }

            @Override
            public void onRewind() {
                super.onRewind();
                prev();
            }

            @Override
            public void onStop() {
                super.onStop();
                stop();
            }
        });
    }

    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if( state == PlaybackStateCompat.STATE_PLAYING ) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mediaSession.setPlaybackState(playbackstateBuilder.build());
    }

    private boolean isUrl(String s) {
        String s1 = s.trim().toLowerCase();
        return s1.startsWith("http://") || s1.startsWith("https://");
    }
}
