package net.ddns.paolo7297.musicdownloader.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

import net.ddns.paolo7297.musicdownloader.placeholder.Song;

import java.util.HashMap;

/**
 * Created by paolo on 03/11/16.
 */

public class ThumbnailsDownloaderTask extends AsyncTask<Void,Void,Bitmap> {

    private ThumbnailsDownloaderInterface i;

    public interface ThumbnailsDownloaderInterface {
        Song getSong();
        void startDownload();
        void setThumbnail(Bitmap b);
    }

    public ThumbnailsDownloaderTask(ThumbnailsDownloaderInterface i) {
        this.i = i;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        i.startDownload();
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Song s = i.getSong();
        Bitmap b = null;
        byte art[] = null;
        MediaMetadataRetriever mediaMetadataRetriever =  new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(s.getFile(), new HashMap<String, String>());
            art = mediaMetadataRetriever.getEmbeddedPicture();
        } catch (RuntimeException e) {

        }
        if (art != null ) b = BitmapFactory.decodeByteArray(art,0,art.length);
        return b;
    }

    @Override
    protected void onPostExecute(Bitmap b) {
        super.onPostExecute(b);
        i.setThumbnail(b);
    }
}
