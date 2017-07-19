package net.ddns.paolo7297.musicdownloader.playback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.annotation.IntDef;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.KeyEvent;

import net.ddns.paolo7297.musicdownloader.CacheManager;
import net.ddns.paolo7297.musicdownloader.Constants;
import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;
import net.ddns.paolo7297.musicdownloader.ui.activity.NavigationActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_NEXT;
import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_OPEN;
import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_PP;
import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_PREV;

/**
 * Created by paolo on 23/04/17.
 */

public class MasterPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    public static final int STATUS_NEED_CONFIGURATION = 0;
    public static final int STATUS_OK = 1;
    public static final int STATUS_PREPARING = 2;
    public static final int STATUS_PLAYING = 3;
    public static final int STATUS_PAUSED = 4;
    public static final int STATUS_STOPPED = 5;
    public static final int SHUFFLE_ENABLED = 0;
    public static final int SHUFFLE_DISABLED = 1;
    public static final int REPEAT_ONE = 0;
    public static final int REPEAT_ALL = 1;
    private static MasterPlayer mp;
    private MediaPlayer player;
    private int status, repeat, shuffle;
    private int index;
    private ArrayList<Song> songs;
    //private ArrayList<String> titles;
    //private ArrayList<String> artists;
    private Context context;
    private MasterPlayerTrackChange callback;
    //private String title, artist;
    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;
    private CacheManager cacheManager;
    private Stack<Integer> prevIndex;
    private MasterPlayer(Context c) {
        context = c;
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        cacheManager = CacheManager.getInstance(c);
        shuffle = PreferenceManager.getDefaultSharedPreferences(c).getInt(Constants.PREFERENCE_SHUFFLE, SHUFFLE_DISABLED);
        repeat = PreferenceManager.getDefaultSharedPreferences(c).getInt(Constants.PREFERENCE_REPEAT, REPEAT_ALL);
        prevIndex = new Stack<>();
        setupMediaSession();
        status = STATUS_NEED_CONFIGURATION;

    }

    public static MasterPlayer getInstance(Context c) {
        if (mp == null) mp = new MasterPlayer(c);
        return mp;
    }

    public void play() {
        int result = audioManager.requestAudioFocus(this,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED && (status == STATUS_OK || status == STATUS_PAUSED)) {
            player.start();
            status = STATUS_PLAYING;
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            if (callback != null) callback.OnTrackChange();
            setupNotification();
        }
    }

    public void pause() {
        if (status == STATUS_PLAYING) {
            player.pause();
            status = STATUS_PAUSED;
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            if (callback != null) callback.OnTrackChange();
            setupNotification();
        }
    }

    public void toggle() {
        if (status == STATUS_PLAYING) {
            pause();
        } else {
            play();
        }

    }

    public void stop() {
        if (status == STATUS_PLAYING || status == STATUS_PAUSED) {
            player.stop();
            status = STATUS_STOPPED;
            if (callback != null) callback.OnTrackChange();
        }
    }

    public void next() {
        if (shuffle == SHUFFLE_DISABLED) {
            index++;
            if (index >= songs.size()) {
                index = 0;
            }
        } else {
            int previndex = index;
            while (index == previndex) {
                index = (int) (Math.random() * songs.size());
            }
            prevIndex.push(previndex);
        }
        setSong(index);
    }

    public void prev() {
        if (shuffle == SHUFFLE_DISABLED || prevIndex.size() == 0) {
            index--;
            if (index < 0) {
                index = songs.size() - 1;
            }
        } else {
            index = prevIndex.pop();
        }
        setSong(index);
    }

    public void setup(Song[] files, int index) {
        songs = new ArrayList<>(Arrays.asList(files));
        this.index = index;
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        /*for (final Song s: files) {
            if (cacheManager.isUrl(s.getFile())) {
                if (cacheManager.isInCache(s.getFile())) {
                    File f = cacheManager.retrieveFile(s.getFile());
                    if (f != null) s.setFile(f.getAbsolutePath());
                }
            }
        }*/
        setSong(index);
    }

    public void setup(Song[] files, int index, @ShuffleMode int shuffle, @RepeatMode int repeat) {
        setShuffle(shuffle);
        setRepeat(repeat);
        setup(files, index);
    }

    public MPInfo getInfos() {
        if (status == STATUS_NEED_CONFIGURATION) {
            return new MPInfo(0, 0, null, null, status, null);
        } else {
            return new MPInfo(
                    status == STATUS_PREPARING ? 0 : player.getCurrentPosition(),
                    status == STATUS_PREPARING ? 0 : player.getDuration(),
                    songs.get(index).getName(),
                    songs.get(index).getArtist(),
                    status,
                    songs.get(index).getFile()
            );
        }
    }

    public boolean isPlaying() {
        return status == STATUS_PLAYING;
    }

    public void setCallback(MasterPlayerTrackChange callback) {
        this.callback = callback;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        status = STATUS_OK;
        play();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (repeat == REPEAT_ALL) {
            next();
        } else {
            mp.seekTo(0);
            mp.start();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Permanent loss of audio focus
            // Pause playback immediately
            pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
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
        builder.setSmallIcon(R.mipmap.ic_songhunter);
        Intent i1 = new Intent();
        i1.setClass(context, NotificationMediaButtonsReceiver.class);
        i1.setAction(NOTIFICATION_PREV);
        PendingIntent iprev = PendingIntent.getBroadcast(context, 123456, i1, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent i2 = new Intent();
        i2.setClass(context, NotificationMediaButtonsReceiver.class);
        i2.setAction(NOTIFICATION_PP);
        PendingIntent ipp = PendingIntent.getBroadcast(context, 123456, i2, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent i3 = new Intent();
        i3.setClass(context, NotificationMediaButtonsReceiver.class);
        i3.setAction(NOTIFICATION_NEXT);
        PendingIntent inext = PendingIntent.getBroadcast(context, 123456, i3, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent io = new Intent();
        io.setClass(context, NavigationActivity.class);
        io.setAction(NOTIFICATION_OPEN);
        PendingIntent iopen = PendingIntent.getActivity(context, 123456, io, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.ic_skip_previous_black_48dp, "Indietro", iprev);
        builder.addAction(status == STATUS_PLAYING ? R.drawable.ic_pause_circle_outline_black_48dp : R.drawable.ic_play_circle_outline_black_48dp, "Play", ipp);
        builder.addAction(R.drawable.ic_skip_next_black_48dp, "Successivo", inext);
        builder.setContentIntent(iopen);
        builder.setStyle(new NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1)
                .setMediaSession(mediaSession.getSessionToken()));
        builder.setContentTitle(songs.get(index).getName());
        builder.setContentText(songs.get(index).getArtist());
        builder.setOngoing(status == STATUS_PLAYING);
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1111, notification);
    }

    private void setupMediaSession() {
        mediaSession = new MediaSessionCompat(context, "MediaSession");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onCommand(String command, Bundle extras, ResultReceiver cb) {
                super.onCommand(command, extras, cb);
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                KeyEvent k = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (k.getAction() == KeyEvent.ACTION_UP) {
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
        if (state == PlaybackStateCompat.STATE_PLAYING) {
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

    public void seek(int millis) {
        player.seekTo(millis);
    }

    public int getIndex() {
        return index;
    }

    public Song getSong() {
        return songs.get(index);
    }

    public void setSong(final int index) {
        try {
            if (!isUrl(songs.get(index).getFile())) {
                if (!new File(songs.get(index).getFile()).exists()) {
                    songs.remove(index);
                }
            }
            player.reset();
            player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            if (cacheManager.isUrl(songs.get(index).getFile())) {
                if (!cacheManager.isInCache(songs.get(index).getFile())) {
                    status = STATUS_PREPARING;
                    cacheManager.cacheUrl(songs.get(index).getFile(), new CacheManager.CachingInterface() {
                        @Override
                        public void onCachingCompleted(File f) {
                            try {
                                player.reset();
                                player.setDataSource(f.getAbsolutePath());
                                //title = songs.get(index);
                                //artist = songs.get(index);
                                //setupNotification();
                                player.prepareAsync();

                                if (callback != null) callback.OnTrackChange();

                                mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songs.get(index).getName())
                                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songs.get(index).getArtist()).build());
                            } catch (IOException e) {
                                e.printStackTrace();
                                status = STATUS_NEED_CONFIGURATION;
                            }
                        }
                    });
                } else {
                    try {
                        File f = cacheManager.retrieveFile(songs.get(index).getFile());
                        player.setDataSource(f.getAbsolutePath());
                        //title = songs.get(index);
                        //artist = songs.get(index);
                        //setupNotification();
                        player.prepareAsync();
                        status = STATUS_PREPARING;
                        if (callback != null) callback.OnTrackChange();

                        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songs.get(index).getName())
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songs.get(index).getArtist()).build());
                    } catch (IOException e) {
                        e.printStackTrace();
                        status = STATUS_NEED_CONFIGURATION;
                    }
                }
            } else {
                player.setDataSource(songs.get(index).getFile());

                //title = songs.get(index);
                //artist = songs.get(index);
                //setupNotification();
                player.prepareAsync();
                status = STATUS_PREPARING;
                if (callback != null) callback.OnTrackChange();

                mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songs.get(index).getName())
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songs.get(index).getArtist()).build());
            }
        } catch (IOException e) {
            e.printStackTrace();
            status = STATUS_NEED_CONFIGURATION;
        }

    }

    public void toggleRepeat() {
        if (repeat == REPEAT_ALL) {
            setRepeat(REPEAT_ONE);
        } else {
            setRepeat(REPEAT_ALL);
        }
    }

    public void toggleShuffle() {
        if (shuffle == SHUFFLE_ENABLED) {
            setShuffle(SHUFFLE_DISABLED);
        } else {
            setShuffle(SHUFFLE_ENABLED);
        }
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(@RepeatMode int repeat) {
        this.repeat = repeat;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.PREFERENCE_REPEAT, repeat).apply();
    }

    public int getShuffle() {
        return shuffle;
    }

    public void setShuffle(@ShuffleMode int shuffle) {
        this.shuffle = shuffle;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.PREFERENCE_SHUFFLE, shuffle).apply();
    }

    @IntDef({REPEAT_ALL, REPEAT_ONE})
    public @interface RepeatMode {
    }

    @IntDef({SHUFFLE_DISABLED, SHUFFLE_ENABLED})
    public @interface ShuffleMode {
    }

    public interface MasterPlayerTrackChange {
        void OnTrackChange();
    }

    public class MPInfo {
        private long duration, totalDuration;
        private String title;
        private String artist;
        private int status;
        private String file;

        public MPInfo(long duration, long totalDuration, String title, String artist, int status, String file) {
            this.duration = duration;
            this.totalDuration = totalDuration;
            this.title = title;
            this.artist = artist;
            this.status = status;
            this.file = file;
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

        public String getFile() {
            return file;
        }
    }
}
