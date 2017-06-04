package net.ddns.paolo7297.musicdownloader.ui.activity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import net.ddns.paolo7297.musicdownloader.CacheManager;
import net.ddns.paolo7297.musicdownloader.Constants;
import net.ddns.paolo7297.musicdownloader.R;

import java.io.File;

/**
 * Created by paolo on 02/06/17.
 */

public class CacheActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CacheManager cacheManager;
    private ColorStateList color;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Gestione Cache");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        cacheManager = CacheManager.getInstance(getApplicationContext());
        update();

        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (File f : cacheManager.getCachedSongs()) {
                    f.delete();
                }
                update();
            }
        });
        ((EditText) findViewById(R.id.threshold)).setText("" + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(Constants.CACHE_THRESHOLD, 200));
        ((CheckBox) findViewById(R.id.autodelete)).setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Constants.CACHE_AUTODELETE, true));
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt(Constants.CACHE_THRESHOLD, Integer.parseInt(((EditText) findViewById(R.id.threshold)).getText().toString())).apply();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.CACHE_AUTODELETE, ((CheckBox) findViewById(R.id.autodelete)).isChecked()).apply();
                update();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void update() {
        ((TextView) findViewById(R.id.count)).setText("Numero di canzoni: " + cacheManager.cachedSongsCount());
        ((TextView) findViewById(R.id.size)).setText("Dimensione cache: " + cacheManager.getCachedSongsSize() + "MB");
        findViewById(R.id.clear).setEnabled(cacheManager.getCachedSongsSize() != 0);
        if (cacheManager.getCachedSongsSize() > PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(Constants.CACHE_THRESHOLD, 200)) {
            System.out.println(((TextView) findViewById(R.id.size)).getTextColors().toString());
            color = ((TextView) findViewById(R.id.size)).getTextColors();
            ((TextView) findViewById(R.id.size)).setTextColor(Color.RED);
        } else {
            if (color != null) ((TextView) findViewById(R.id.size)).setTextColor(color);
        }
    }
}
