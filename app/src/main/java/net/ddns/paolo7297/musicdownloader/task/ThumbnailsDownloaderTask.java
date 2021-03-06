package net.ddns.paolo7297.musicdownloader.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

import net.ddns.paolo7297.musicdownloader.placeholder.Song;
import net.ddns.paolo7297.musicdownloader.playback.CacheManager;

import java.io.File;
import java.util.HashMap;

/**
 * Created by paolo on 03/11/16.
 */

public class ThumbnailsDownloaderTask extends AsyncTask<Void, Void, Void> {

    private ThumbnailsDownloaderInterface i;
    private CacheManager cacheManager;
    private byte[] art;
    private Bitmap b;
    private boolean completed;

    public ThumbnailsDownloaderTask(Context context, ThumbnailsDownloaderInterface i) {
        this.i = i;
        cacheManager = CacheManager.getInstance(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        i.startDownload();
    }

    @Override
    protected Void doInBackground(final Void... params) {
        Song s = i.getSong();
        b = null;
        art = null;
        completed = false;
        final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        if (cacheManager.isUrl(s.getFile())) {
            if (cacheManager.isInCache(s.getFile())) {
                File f = cacheManager.retrieveFile(s.getFile());
                try {
                    mediaMetadataRetriever.setDataSource(f.getAbsolutePath());
                    art = mediaMetadataRetriever.getEmbeddedPicture();
                    completed = true;
                } catch (RuntimeException e) {

                }
            } else {
                cacheManager.cacheUrl(s.getFile(), new CacheManager.CachingInterface() {
                    @Override
                    public void onCachingCompleted(File f) {
                        try {
                            mediaMetadataRetriever.setDataSource(f.getAbsolutePath());
                            art = mediaMetadataRetriever.getEmbeddedPicture();
                            completed = true;
                            onPostExecute(null);
                        } catch (IllegalArgumentException e) {

                        }

                    }
                });
            }
        } else {
            try {
                mediaMetadataRetriever.setDataSource(s.getFile(), new HashMap<String, String>());
                art = mediaMetadataRetriever.getEmbeddedPicture();
                completed = true;
            } catch (RuntimeException e) {
                art = null;
                completed = true;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        if (completed) {
            if (art != null) b = BitmapFactory.decodeByteArray(art, 0, art.length);
            i.setThumbnail(b);
        }
    }

    public interface ThumbnailsDownloaderInterface {
        Song getSong();

        void startDownload();

        void setThumbnail(Bitmap b);
    }
}
