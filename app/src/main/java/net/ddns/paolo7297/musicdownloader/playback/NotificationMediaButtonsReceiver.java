package net.ddns.paolo7297.musicdownloader.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_NEXT;
import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_PP;
import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_PREV;

/**
 * Created by paolo on 28/04/17.
 */

public class NotificationMediaButtonsReceiver extends BroadcastReceiver {

    public NotificationMediaButtonsReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MasterPlayer mp = MasterPlayer.getInstance(context.getApplicationContext());
        if (intent.getAction().equals(NOTIFICATION_PREV)) {
            //i.prevMedia();
            mp.prev();
        } else if (intent.getAction().equals(NOTIFICATION_NEXT)) {
            //i.nextMedia();
            mp.next();
        } else if (intent.getAction().equals(NOTIFICATION_PP)) {
            //i.toggleMedia();
            mp.toggle();
        } else {
            Log.e("!?","Why here?!");
        }
    }
}
