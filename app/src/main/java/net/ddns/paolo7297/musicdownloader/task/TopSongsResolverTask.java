package net.ddns.paolo7297.musicdownloader.task;

import android.os.AsyncTask;

import net.ddns.paolo7297.musicdownloader.placeholder.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by paolo on 21/04/17.
 */

public class TopSongsResolverTask extends AsyncTask<Void,Void,ArrayList<Song>> {

    private final String urlString = "http://pleer.net/en/gettopperiod?";
    private final String target = "target1=e";
    public static final int TARGET_WEEK = 1;
    public static final int TARGET_3_MONTH = 2;
    public static final int TARGET_6_MONTH = 3;
    public static final int TARGET_YEAR = 4;
    public static final int TARGET_ALL = 5;

    private final String end = "&target2=r1&select=e&page_ru=1";

    private TopSongsResolverInterface i;

    public interface TopSongsResolverInterface {
        void startSearch();
        int getTarget();
        void setResults(ArrayList<Song> songs);
    }

    public TopSongsResolverTask(TopSongsResolverInterface i) {
        this.i = i;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        i.startSearch();
    }

    @Override
    protected ArrayList<Song> doInBackground(Void... params) {
        ArrayList<Song> songs = new ArrayList<>();
        try {
            String url = urlString+target+i.getTarget()+end;

            Document doc = Jsoup.connect(url).get();
            Element element = doc.getElementById("search-results");

            if (element != null) {
                Elements s = element.getElementsByTag("li");
                for (Element a : s) {
                    songs.add(new Song(
                            a.attr("file_id"),
                            a.attr("singer"),
                            a.attr("song"),
                            Integer.parseInt(a.attr("duration")),
                            "http://pleer.com/browser-extension/files/" + a.attr("link") + ".mp3",
                            a.attr("size"),
                            a.attr("rate")
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return songs;
    }

    @Override
    protected void onPostExecute(ArrayList<Song> songs) {
        super.onPostExecute(songs);
        i.setResults(songs);
    }
}
