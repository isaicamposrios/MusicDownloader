package net.ddns.paolo7297.musicdownloader.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

/**
 * Created by paolo on 23/07/17.
 */

public class EarphoneDisconnectReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MasterPlayer masterPlayer = MasterPlayer.getInstance(context);
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            if (masterPlayer.isPlaying()) {
                masterPlayer.pause();
            }
        }
    }
}
