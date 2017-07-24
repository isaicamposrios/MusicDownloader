package net.ddns.paolo7297.musicdownloader.task;

import android.content.Context;
import android.os.AsyncTask;

import net.ddns.paolo7297.musicdownloader.placeholder.YoutubeResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by paolo on 24/07/17.
 */

public class YoutubeQueryResolverTask extends AsyncTask<Void, Void, ArrayList<YoutubeResult>> {

    private static final String url = "https://www.googleapis.com/youtube/v3/search?q=";
    private static final String url2 = "&maxResults=30&part=snippet&key=AIzaSyDmRvuMetpc9eCY3ulnQQR1bNcxweRXkaQ";
    private YoutubeQueryInterface i;
    private Context context;

    public YoutubeQueryResolverTask(YoutubeQueryInterface i, Context context) {
        this.i = i;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        i.setup();
    }

    @Override
    protected ArrayList<YoutubeResult> doInBackground(Void... voids) {
        ArrayList<YoutubeResult> res = new ArrayList<>();
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url + i.getQuery().replace(" ", "%20") + url2).openConnection();
            urlConnection.connect();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String tmp;
                StringBuilder stringBuilder = new StringBuilder();
                while ((tmp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(tmp);
                }
                JSONObject obj = new JSONObject(stringBuilder.toString());
                JSONArray array = obj.getJSONArray("items");
                int j;
                for (j = 0; j < array.length(); j++) {
                    JSONObject o = array.getJSONObject(j);
                    if (o.getJSONObject("id").getString("kind").equals("youtube#video")) {
                        JSONObject jsonObject = o.getJSONObject("snippet");
                        res.add(new YoutubeResult(
                                jsonObject.getString("title"),
                                jsonObject.getString("channelTitle"),
                                jsonObject.getJSONObject("thumbnails").getJSONObject("medium").getString("url"),
                                o.getJSONObject("id").getString("videoId")
                        ));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    protected void onPostExecute(ArrayList<YoutubeResult> res) {
        super.onPostExecute(res);
        i.setResult(res);
    }

    public interface YoutubeQueryInterface {
        void setup();

        String getQuery();

        void setResult(ArrayList<YoutubeResult> res);
    }
}
