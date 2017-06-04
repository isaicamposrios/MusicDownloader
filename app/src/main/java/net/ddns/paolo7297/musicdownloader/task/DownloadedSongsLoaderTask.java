package net.ddns.paolo7297.musicdownloader.task;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

import net.ddns.paolo7297.musicdownloader.placeholder.Song;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by paolo on 21/04/17.
 */

public class DownloadedSongsLoaderTask extends AsyncTask<Void, Void, Void> {


    private DownloadedSongLoaderInterface i;
    private ArrayList<Song> results;
    public DownloadedSongsLoaderTask(DownloadedSongLoaderInterface i) {
        this.i = i;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        i.prepareUI();
    }

    @Override
    protected Void doInBackground(Void... params) {
        results = new ArrayList<>();
        for (File f : i.getFiles()) {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(f.getAbsolutePath());
            String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            results.add(new Song(
                    "",
                    metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                    title == null ? f.getName() : title,
                    Integer.parseInt(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000,
                    f.getAbsolutePath(),
                    "",
                    Integer.parseInt(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)) / 1000 + " Kbps"
            ));

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        i.updateFiles(results);
    }

    public interface DownloadedSongLoaderInterface {
        void prepareUI();

        void updateFiles(ArrayList<Song> al);

        ArrayList<File> getFiles();
    }
}
