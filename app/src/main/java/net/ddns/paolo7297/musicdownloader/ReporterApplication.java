package net.ddns.paolo7297.musicdownloader;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

/**
 * Created by paolo on 08/05/17.
 */

public class ReporterApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        //ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }
        StrictMode.setVmPolicy(builder.build());

    }
}
