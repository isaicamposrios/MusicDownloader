package net.ddns.paolo7297.musicdownloader;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by paolo on 31/05/17.
 */

public class CacheManager {

    private static CacheManager manager;
    private Context context;

    private CacheManager(Context context) {
        this.context = context;
    }

    public static CacheManager getInstance(Context c) {
        if (manager == null) manager = new CacheManager(c);
        return manager;
    }

    public boolean isInCache(String url) {
        return (new File(context.getCacheDir(), urlSyntax(url))).exists();
    }

    public void cacheUrl(String url, CachingInterface i) {
        new CachingTask(i, context, url).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //i.onCachingCompleted();
    }

    public File retrieveFile(String url) {
        File f = new File(context.getCacheDir(), urlSyntax(url));
        return f.exists() ? f : null;
    }

    public boolean isUrl(String url) {
        String s1 = url.trim().toLowerCase();
        return s1.startsWith("http://") || s1.startsWith("https://");
    }

    private String urlSyntax(String url) {
        return url.replace("https://", "").replace("http://", "").replaceAll("/", "-");
    }

    public ArrayList<File> getCachedSongs() {
        ArrayList<File> cacheFiles = new ArrayList<>(Arrays.asList(context.getCacheDir().listFiles()));
        int i = 0;
        while (i < cacheFiles.size()) {
            if (!cacheFiles.get(i).getAbsolutePath().endsWith(".mp3")) {
                cacheFiles.remove(i);
            } else {
                i++;
            }
        }
        return cacheFiles;
    }

    public ArrayList<File> getSortedCachedSongs() {
        ArrayList<File> cacheFiles = getCachedSongs();
        Collections.sort(cacheFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return (int) (o1.lastModified() - o2.lastModified());
            }
        });
        return cacheFiles;
    }

    public int cachedSongsCount() {
        ArrayList<File> cacheFiles = getCachedSongs();
        return cacheFiles.size();
    }

    public int getCachedSongsSize() {
        ArrayList<File> cacheFiles = getCachedSongs();
        long sizes = 0;
        for (File f : cacheFiles) {
            sizes += f.length();
        }
        sizes = sizes / 1024;
        sizes = sizes / 1024;
        return (int) sizes;
    }

    public interface CachingInterface {
        void onCachingCompleted(File f);
    }

    private class CachingTask extends AsyncTask<Void, Void, File> {
        private CachingInterface i;
        private String url;
        private Context c;

        public CachingTask(CachingInterface i, Context c, String url) {
            this.i = i;
            this.url = url;
            this.c = c;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected File doInBackground(Void... params) {
            try {
                URLConnection urlConnection = new URL(url).openConnection();
                urlConnection.connect();
                System.out.println(url);
                InputStream is = urlConnection.getInputStream();
                File download = new File(c.getCacheDir(), urlSyntax(url));
                download.createNewFile();
                System.out.println(download.getAbsolutePath());
                FileOutputStream outputStream = new FileOutputStream(download);
                byte[] buffer = new byte[16384];

                int len;
                while ((len = is.read(buffer, 0, 16384)) != -1)
                    outputStream.write(buffer, 0, len);

                outputStream.flush();
                is.close();
                outputStream.close();
                return download;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(File aVoid) {
            super.onPostExecute(aVoid);
            i.onCachingCompleted(aVoid);
        }
    }
}
