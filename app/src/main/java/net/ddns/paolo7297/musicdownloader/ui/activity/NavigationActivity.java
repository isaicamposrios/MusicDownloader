package net.ddns.paolo7297.musicdownloader.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import net.ddns.paolo7297.musicdownloader.BuildConfig;
import net.ddns.paolo7297.musicdownloader.CacheManager;
import net.ddns.paolo7297.musicdownloader.Constants;
import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.ServerCommands;
import net.ddns.paolo7297.musicdownloader.playback.MasterPlayer;
import net.ddns.paolo7297.musicdownloader.playback.PlaylistDBHelper;
import net.ddns.paolo7297.musicdownloader.task.SongTitleRetreiverTask;
import net.ddns.paolo7297.musicdownloader.ui.DisablingImageButton;
import net.ddns.paolo7297.musicdownloader.ui.fragment.DownloadedSongsFragment;
import net.ddns.paolo7297.musicdownloader.ui.fragment.PlaylistsFragment;
import net.ddns.paolo7297.musicdownloader.ui.fragment.PreferenceFragment;
import net.ddns.paolo7297.musicdownloader.ui.fragment.SearchFragment;
import net.ddns.paolo7297.musicdownloader.ui.fragment.TabManagerFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static net.ddns.paolo7297.musicdownloader.Constants.FOLDER_HOME;
import static net.ddns.paolo7297.musicdownloader.Constants.NOTIFICATION_OPEN;

/**
 * Created by paolo on 31/10/16.
 */

public class NavigationActivity extends AppCompatActivity {

    private final static int REQ_CODE = 1;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private MasterPlayer masterPlayer;
    private PlaylistDBHelper playlists;
    private LinearLayout playerUI;
    private TextView artist, title;
    private DisablingImageButton prev, pp, next;
    private ProgressBar spinner;
    private Fragment f;
    private FirebaseAnalytics mFirebaseAnalytics;
    //private AdView ads;
    private CacheManager cacheManager;
    private String[] perms = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        masterPlayer = MasterPlayer.getInstance(getApplicationContext());
        cacheManager = CacheManager.getInstance(getApplicationContext());
        playlists = PlaylistDBHelper.getInstance(getApplicationContext());
        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        //((TextView)navigationView.getHeaderView(0).findViewById(R.id.text)).setText(getString(R.string.app_name) + " Ver:"+ BuildConfig.VERSION_NAME);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        playerUI = (LinearLayout) findViewById(R.id.playerui);
        title = (TextView) findViewById(R.id.title);
        artist = (TextView) findViewById(R.id.artist);
        prev = (DisablingImageButton) findViewById(R.id.rewind);
        pp = (DisablingImageButton) findViewById(R.id.play);
        next = (DisablingImageButton) findViewById(R.id.forward);
        spinner = (ProgressBar) findViewById(R.id.spinner);
        spinner.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //ads = (AdView) findViewById(R.id.ads);
        setSupportActionBar(toolbar);

        /*masterPlayer.setCallback(new MasterPlayer.MasterPlayerTrackChange() {
            @Override
            public void OnTrackChange() {
                refreshPlayer();
            }
        });*/

        playerUI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Arriverà anche una UI per questo poveretto!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                startActivity(intent);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                setTitle(item.getTitle());
                FragmentManager fm = getSupportFragmentManager();
                switch (item.getItemId()) {
                    case R.id.top:
                        f = new TabManagerFragment();
                        ((TabManagerFragment) f).setTabs(tabLayout);
                        tabLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.search:
                        f = new SearchFragment();
                        tabLayout.setVisibility(View.GONE);
                        break;
                    case R.id.download:
                        f = new DownloadedSongsFragment();
                        tabLayout.setVisibility(View.GONE);
                        break;
                    case R.id.settings:
                        f = new PreferenceFragment();
                        tabLayout.setVisibility(View.GONE);
                        break;
                    case R.id.playlists:
                        f = new PlaylistsFragment();
                        tabLayout.setVisibility(View.GONE);
                        break;
                    default:
                        f = new Fragment();
                        tabLayout.setVisibility(View.GONE);
                        break;
                }
                fm.beginTransaction().replace(R.id.frame, f).commit();
                drawerLayout.closeDrawer(Gravity.START);
                return true;
            }
        });

        //View header = navigationView.getHeaderView(0);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        if (savedInstanceState == null) {
            navigationView.getMenu().performIdentifierAction(R.id.search, 0);
            navigationView.setCheckedItem(R.id.search);
        }
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        checkPerms(perms);
        checkFolders();
        checkForUpdates();
        setupPlayer();
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("ShowDisclaimer", true)) {
            showDisclaimer();
        }
        logUpdate();
        //setupAds();
        clearCache();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_CODE:
                ArrayList<String> denied = new ArrayList<>();
                int j = 0;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                            denied.add(permissions[i]);
                        } else {
                            manualSetPerms();
                        }

                    }
                }
                if (denied.size() > 0) {

                    checkPerms(denied.toArray(new String[denied.size()]));
                }

        }

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        masterPlayer.setCallback(new MasterPlayer.MasterPlayerTrackChange() {
            @Override
            public void OnTrackChange() {
                refreshPlayer();
            }
        });
        refreshPlayer();
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            final ArrayList<String> ss = new ArrayList<>(Arrays.asList(intent.getStringExtra(Intent.EXTRA_TEXT).split(" ")));
            if (ss.size() > 1) {
                int i = 0;
                while (i < ss.size()) {
                    if (!ss.get(i).startsWith("http")) {
                        ss.remove(i);
                    } else {
                        i++;
                    }
                }
            }

            new SongTitleRetreiverTask(new SongTitleRetreiverTask.SongTitleRetreiverInterface() {
                @Override
                public String getUrl() {
                    return ss.get(0);
                }

                @Override
                public void setup() {

                }

                @Override
                public void complete(String s) {
                    String res = s.toLowerCase()
                            .replace("spotify web player - ", "")
                            .replace(" - youtube", "")
                            .replaceAll("-", "")
                            .replace("with lyrics", "")
                            .replace("lyrics", "")
                            .replaceAll("\\s*\\([^\\)]*\\)\\s*", " ")
                            .replaceAll(" +", " ")
                            .replaceAll("\"", "")
                            .trim();
                    System.out.println(res);
                    setIntent(new Intent());
                    f = new SearchFragment();
                    ((SearchFragment) f).setQuery(res);
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, f).commit();
                    tabLayout.setVisibility(View.GONE);
                    navigationView.setCheckedItem(R.id.search);

                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (NOTIFICATION_OPEN.equals(action)) {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(Gravity.START);
            }
        } else {
            drawerLayout.closeDrawer(Gravity.START);
        }
        //refreshAds();
        if (f != null && f instanceof TabManagerFragment) {
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            tabLayout.setVisibility(View.GONE);
        }
    }

    private void checkPerms(String[] perms) {
        ArrayList<String> denied = new ArrayList<>();
        int i = 0;
        for (String s : perms) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), s) != PackageManager.PERMISSION_GRANTED) {
                denied.add(s);
            }
        }
        if (denied.size() > 0) {
            ActivityCompat.requestPermissions(this, denied.toArray(new String[denied.size()]), REQ_CODE);
        }
    }

    private void manualSetPerms() {
        Toast.makeText(this, R.string.perms_request, Toast.LENGTH_LONG).show();
        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        i.setData(uri);
        finish();
        startActivity(i);
    }

    private void checkFolders() {
        File f1 = new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/");
        if (!f1.exists()) {
            f1.mkdirs();
        }
        File f2 = new File(f1, "Apks");
        if (!f2.exists()) {
            f2.mkdirs();
        }
    }

    private void checkForUpdates() {
        /*long lastUpdate = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getLong(PREFERENCE_LAST_UPDATE,0);
        Calendar c = Calendar.getInstance();
        Calendar c1 = Calendar.getInstance();
        c.setTimeInMillis(lastUpdate);
        c.add(Calendar.HOUR_OF_DAY,1);*/
        //if (c.before(c1)) {
        ServerCommands.checkUpdate(NavigationActivity.this);
        //PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putLong(PREFERENCE_LAST_UPDATE,System.currentTimeMillis()).commit();
        //}
        //UpdateServer.checkUpdate(NavigationActivity.this);
    }

    private void setupPlayer() {
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                masterPlayer.prev();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                masterPlayer.next();
            }
        });
        pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                masterPlayer.toggle();
                if (masterPlayer.isPlaying()) {
                    setPlaying();
                } else {
                    setPaused();
                }
            }
        });
        refreshPlayer();
    }

    private void refreshPlayer() {
        MasterPlayer.MPInfo info = masterPlayer.getInfos();
        if (info.getStatus() == MasterPlayer.STATUS_NEED_CONFIGURATION) {
            playerUI.setVisibility(View.GONE);
        } else {
            playerUI.setVisibility(View.VISIBLE);
            title.setText(info.getTitle());
            artist.setText(info.getArtist());
            if (info.getStatus() == MasterPlayer.STATUS_PREPARING) {
                spinner.setVisibility(View.VISIBLE);
                pp.setVisibility(View.GONE);
            } else {
                spinner.setVisibility(View.GONE);
                pp.setVisibility(View.VISIBLE);
            }
            if (info.getStatus() == MasterPlayer.STATUS_PLAYING) {
                setPlaying();
            } else {
                setPaused();
            }

        }
    }

    private void setPlaying() {
        pp.setImageResource(R.mipmap.ic_pause_circle);
    }

    private void setPaused() {
        pp.setImageResource(R.mipmap.ic_play_circle);
    }


    private void showDisclaimer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.dialog_disclaimer, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        view.findViewById(R.id.neveragain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkBox.setChecked(!checkBox.isChecked());
                checkBox.toggle();
            }
        });
        builder.setView(view);
        builder.setTitle(R.string.warning);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checkBox.isChecked()) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("ShowDisclaimer", false).apply();
                }
            }
        });
        builder.show();
    }

    private void log() {
        //FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        /*Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);*/
    }

    private void logUpdate() {
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("LastestVersion", BuildConfig.VERSION_NAME).equals(BuildConfig.VERSION_NAME)) {
            Bundle bundle = new Bundle();
            bundle.putString("app_update", "app_update");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
        }
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LastestVersion", BuildConfig.VERSION_NAME).apply();
    }

    /*private void setupAds() {
        /*MobileAds.initialize(this,"ca-app-pub-4387960339514312~8530411784");
        refreshAds();
    }
    private void refreshAds() {
        /*if (ads != null) {
            ads.setVisibility(View.GONE);
            AdRequest.Builder builder = new AdRequest.Builder();
            if (BuildConfig.DEBUG) {
                builder.addTestDevice("98E3C70DC084692276098B01D18BA027");
            }
            ads.loadAd(builder.build());
            ads.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    ads.setVisibility(View.VISIBLE);
                }
            });
        }
    }*/

    private void clearCache() {
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Constants.CACHE_AUTODELETE, true)) {
            int threshold = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(Constants.CACHE_THRESHOLD, 200);
            long c = Long.MAX_VALUE;
            int i;
            /*for (File f :cacheManager.getSortedCachedSongs()) {
                System.out.println(f.getName());
            }*/
            if (cacheManager.getCachedSongsSize() > threshold) {
                Toast.makeText(this, R.string.cache_over_notification, Toast.LENGTH_LONG).show();
                while (cacheManager.getCachedSongsSize() > threshold) {
                    cacheManager.getSortedCachedSongs().get(0).delete();
                }
            }

        }


    }
}
