package net.ddns.paolo7297.musicdownloader;

import android.content.Context;

import java.io.File;

/**
 * Created by paolo on 31/05/17.
 */

public class CacheManager {

    public interface CachingInterface {
        void onCachingCompleted();
    }

    public static boolean isInCache(Context context,String url) {
        return (new File(context.getCacheDir(),url)).exists();
    }

    public static void cacheUrl(Context context, String url, CachingInterface i) {

        i.onCachingCompleted();
    }

    public static File retrieveFile(Context context,String url) {
        File f = new File(context.getCacheDir(),url);
        return f.exists() ? f : null;
    }

    public boolean isUrl(String url) {
        String s1 = url.trim().toLowerCase();
        return s1.startsWith("http://") || s1.startsWith("https://");
    }
}
