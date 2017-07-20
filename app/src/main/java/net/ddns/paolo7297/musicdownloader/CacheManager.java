package net.ddns.paolo7297.musicdownloader;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import net.ddns.paolo7297.musicdownloader.placeholder.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import static net.ddns.paolo7297.musicdownloader.Constants.FOLDER_HOME;

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

    public void download(Song s) {
        int c = 0;
        while (new File(
                Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/",
                s.getFullName() + (
                        c == 0 ? "" : String.format(Locale.getDefault(), "(%d)", c)
                ) + ".mp3").exists()) {
            c++;

        }
        Toast.makeText(context.getApplicationContext(), context.getString(R.string.download_started) + "...", Toast.LENGTH_LONG).show();
        if (isInCache(s.getFile())) {
            try {
                File orig = retrieveFile(s.getFile());
                File dst = new File(
                        Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/",
                        s.getFullName() + (c == 0 ? "" : String.format(Locale.getDefault(), "(%d)", c)) + ".mp3");
                dst.createNewFile();
                FileChannel ifc = new FileInputStream(orig).getChannel();
                FileChannel ofc = new FileOutputStream(dst).getChannel();
                ifc.transferTo(0, ifc.size(), ofc);
                orig.deleteOnExit();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setSmallIcon(R.mipmap.ic_songhunter);
                Intent io = new Intent();
                io.setAction(android.content.Intent.ACTION_VIEW);
                io.setDataAndType(Uri.parse(dst.getAbsolutePath()), "audio/*");
                io.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                PendingIntent iopen = PendingIntent.getActivity(context, 123456, io, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(iopen);
                builder.setContentTitle(s.getFullName());
                builder.setContentText(context.getString(R.string.download_completed));
                builder.setAutoCancel(true);
                Notification notification = builder.build();
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(s.getLength(), notification);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(s.getFile()));
            request.setDestinationUri(Uri.fromFile(new File(
                    Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/",
                    s.getFullName() + (
                            c == 0 ? "" : String.format(Locale.getDefault(), "(%d)", c)
                    ) + ".mp3"))
            );
            request.setTitle(s.getFullName());
            request.allowScanningByMediaScanner();
            request.setVisibleInDownloadsUi(true);

            request.setMimeType("audio/MP3");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.addRequestHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; rv:12.0) Gecko/20120403211507 Firefox/12.0");
            Toast.makeText(context.getApplicationContext(), context.getString(R.string.download_started) + "...", Toast.LENGTH_LONG).show();
            downloadManager.enqueue(request);
        }
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
