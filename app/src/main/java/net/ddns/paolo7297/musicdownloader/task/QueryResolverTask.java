package net.ddns.paolo7297.musicdownloader.task;

import android.content.Context;
import android.os.AsyncTask;

import net.ddns.paolo7297.musicdownloader.placeholder.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * Created by paolo on 28/10/16.
 */
public class QueryResolverTask extends AsyncTask<Void,Void,ArrayList<Song>>{

    private final String urlString = "http://pleer.net/search?q=";
    private final String limit = "&limit=20";
    private final String page = "&page=";
    private final String quality = "&quality=";
    public static final String QUALITY_ALL = "all";
    public static final String QUALITY_LOW ="bad";
    public static final String QUALITY_MED ="good";
    public static final String QUALITY_HIGH ="best";
    private final String sortmode = "&sort_mode=";
    private final String sortby = "&sort_by=";
    public static final int SORT_POPULARITY = 0;
    public static final int SORT_DATE =1;
    public static final int SORT_ALPHABETIC =2;

    private MusicRequestInterface i;
    private Context context;

    public interface MusicRequestInterface {
        String getSearchQuery();
        int getPage();
        String getQuality();
        int getSortMode();
        int getSortedBy();
        void startSearch();
        void setResults(ArrayList<Song> songs);
        void setMaxPage(int limit);
    }

    public QueryResolverTask(MusicRequestInterface i, Context context) {
        this.i= i;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        i.startSearch();
    }

    @Override
    protected ArrayList<Song> doInBackground(Void... params) {
        ArrayList<Song> songs = new ArrayList<>();
        String par = i.getSearchQuery().replaceAll("\\s+", "+");
        try {
            String url = urlString+par+limit+page+i.getPage()+quality+i.getQuality()+sortmode+i.getSortMode()+sortby+i.getSortedBy();
            //URL url = new URL(urlString);
            //connection = (HttpURLConnection) url.openConnection();
            //if (connection.getResponseCode() == 200) {
            /*InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder strb = new StringBuilder();
            String s;
            while ((s = br.readLine()) != null) {
                strb.append(s);
            }*/
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.getElementsByClass("scrolledPagination");
            if (elements.first() != null) {
                Elements s = elements.first().getElementsByTag("li");
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
                Elements e2 = doc.getElementsByClass("tabs clearfix");
                String a = e2.first().getElementsByTag("a").first().text();
                a = a.split(" ")[2].replace("(", "").replace(")", "");
                i.setMaxPage(Integer.parseInt(a));
            } else {
                i.setMaxPage(0);
            }



            /*System.out.println(songs.size());
            URL url = new URL(urlString+par+limit+page+i.getPage()+quality+i.getQuality()+sortmode+i.getSortMode()+sortby+i.getSortedBy());
            System.out.println(urlString+par+limit+page+i.getPage()+quality+i.getQuality()+sortmode+i.getSortMode()+sortby+i.getSortedBy());
            connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() == 200) {
                InputStream in= connection.getInputStream();
                String s = readFullyAsString(in,"UTF-8");
                System.out.println(s.length());
                JSONObject o = new JSONObject(s);
                i.setMaxPage(o.getInt("found"));
                JSONArray array = o.getJSONArray("tracks");
                for (int i = 0; i<array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    songs.add(new SearchResult(
                            obj.getString("id"),
                            obj.getString("artist"),
                            obj.getString("track"),
                            obj.getInt("length"),
                            obj.getString("file"),
                            obj.getString("link"),
                            obj.getLong("size"),
                            obj.getString("bitrate")
                    ));
                }
                System.out.println(songs.size());*/
            //}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return songs;
    }

    @Override
    protected void onPostExecute(ArrayList<Song> s) {
        super.onPostExecute(s);
        i.setResults(s);
    }

}
