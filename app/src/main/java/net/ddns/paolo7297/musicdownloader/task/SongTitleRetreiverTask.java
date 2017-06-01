package net.ddns.paolo7297.musicdownloader.task;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by paolo on 14/05/17.
 */

public class SongTitleRetreiverTask extends AsyncTask<Void,Void,String> {
    private SongTitleRetreiverInterface i;

    public interface SongTitleRetreiverInterface {
        String getUrl();
        void setup();
        void complete(String s);
    }

    public SongTitleRetreiverTask(SongTitleRetreiverInterface i) {
        this.i = i;
    }

    @Override
    protected String doInBackground(Void... params) {
        String s = null;
        try {
            Document doc = Jsoup.connect(i.getUrl()).get();
            s = doc.title();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        i.setup();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        i.complete(s);
    }
}
