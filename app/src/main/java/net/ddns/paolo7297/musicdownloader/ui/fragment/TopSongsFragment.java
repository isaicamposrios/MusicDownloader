package net.ddns.paolo7297.musicdownloader.ui.fragment;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.ddns.paolo7297.musicdownloader.CacheManager;
import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.PlaylistAdapter;
import net.ddns.paolo7297.musicdownloader.adapter.SearchAdapter;
import net.ddns.paolo7297.musicdownloader.placeholder.Playlist;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;
import net.ddns.paolo7297.musicdownloader.playback.MasterPlayer;
import net.ddns.paolo7297.musicdownloader.playback.PlaylistDBHelper;
import net.ddns.paolo7297.musicdownloader.task.ThumbnailsDownloaderTask;
import net.ddns.paolo7297.musicdownloader.task.TopSongsResolverTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static net.ddns.paolo7297.musicdownloader.Constants.FOLDER_HOME;

/**
 * Created by paolo on 21/04/17.
 */

public class TopSongsFragment extends Fragment {

    private ArrayList<Song> songsSaved;
    private ProgressBar loading;
    private ListView listView;
    private SearchAdapter adapter;
    private CacheManager cacheManager;
    private int target = TopSongsResolverTask.TARGET_WEEK;
    private AlertDialog dialog;

    public TopSongsFragment() {

    }

    public void setTarget(int target) {
        this.target = target;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cacheManager = CacheManager.getInstance(getContext().getApplicationContext());
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshResults();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_top, container, false);
        loading = (ProgressBar) view.findViewById(R.id.spinner);
        listView = (ListView) view.findViewById(R.id.list);
        loading.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        songsSaved = new ArrayList<>();
        adapter = new SearchAdapter(getActivity(), songsSaved);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final MediaPlayer[] mediaPlayer = new MediaPlayer[1];
                final Song s = songsSaved.get(position);
                final int p1 = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_songinfo, parent, false);
                ((ProgressBar) v.findViewById(R.id.spinner)).getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                new ThumbnailsDownloaderTask(getContext().getApplicationContext(), new ThumbnailsDownloaderTask.ThumbnailsDownloaderInterface() {
                    @Override
                    public Song getSong() {
                        return s;
                    }

                    @Override
                    public void startDownload() {
                        v.findViewById(R.id.image).setVisibility(View.GONE);
                        v.findViewById(R.id.spinner).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void setThumbnail(Bitmap b) {
                        if (b != null) {
                            ((ImageView) v.findViewById(R.id.image)).setImageBitmap(b);
                        } else {
                            ((ImageView) v.findViewById(R.id.image)).setImageResource(R.mipmap.ic_song_red);
                        }
                        v.findViewById(R.id.image).setVisibility(View.VISIBLE);
                        v.findViewById(R.id.spinner).setVisibility(View.GONE);
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                ((TextView) v.findViewById(R.id.title)).setText(s.getName());
                ((TextView) v.findViewById(R.id.artist)).setText(s.getArtist());
                ((TextView) v.findViewById(R.id.size)).setText(s.getSize());
                ((TextView) v.findViewById(R.id.bitrate)).setText(s.getBitrate());
                long i = s.getLength() / 60;
                int d = (int) (((float) s.getLength() / 60 - i) * 60);
                ((TextView) v.findViewById(R.id.time)).setText(String.format("%d:%02d min", i, d));
                v.findViewById(R.id.button_stream).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*try {
                            if (mediaPlayer[0] != null) {
                                mediaPlayer[0].stop();
                                mediaPlayer[0].release();
                                mediaPlayer[0] = null;
                                ((Button) v.findViewById(R.id.button_stream)).setText("Streaming");
                            } else {
                                mediaPlayer[0] = new MediaPlayer();
                                mediaPlayer[0].setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer[0].setDataSource(s.getFile());
                                mediaPlayer[0].prepare();
                                mediaPlayer[0].start();
                                ((Button) v.findViewById(R.id.button_stream)).setText("Stop");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        MasterPlayer mp = MasterPlayer.getInstance(getActivity().getApplicationContext());
                        /*ArrayList<String> uris = new ArrayList<String>();
                        ArrayList<String> titles= new ArrayList<String>();
                        ArrayList<String> artists = new ArrayList<String>();
                        for (SearchResult s: songsSaved) {
                            uris.add(s.getFile());
                            titles.add(s.getName());
                            artists.add(s.getArtist());
                        }*/
                        mp.setup(songsSaved.toArray(new Song[songsSaved.size()]), position);
                    }
                });
                v.findViewById(R.id.button_download).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //System.out.println(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/"+FOLDER_HOME+"/")).toString());
                        int c = 0;
                        while (new File(
                                Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/",
                                s.getFullName() + (
                                        c == 0 ? "" : String.format("(%d)", c)
                                ) + ".mp3").exists()) {
                            c++;

                        }
                        Toast.makeText(getContext().getApplicationContext(), "Download iniziato...", Toast.LENGTH_LONG).show();
                        if (cacheManager.isInCache(s.getFile())) {
                            try {
                                File orig = cacheManager.retrieveFile(s.getFile());
                                File dst = new File(
                                        Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/",
                                        s.getFullName() + (c == 0 ? "" : String.format("(%d)", c)) + ".mp3");
                                dst.createNewFile();
                                FileChannel ifc = new FileInputStream(orig).getChannel();
                                FileChannel ofc = new FileOutputStream(dst).getChannel();
                                ifc.transferTo(0, ifc.size(), ofc);
                                orig.delete();
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
                                builder.setSmallIcon(R.mipmap.ic_songhunter);
                                Intent io = new Intent();
                                io.setAction(android.content.Intent.ACTION_VIEW);
                                io.setDataAndType(Uri.fromFile(dst), "audio/*");
                                PendingIntent iopen = PendingIntent.getActivity(getContext(), 123456, io, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(iopen);
                                builder.setContentTitle(s.getFullName());
                                builder.setContentText("Download completato.");
                                builder.setAutoCancel(true);
                                Notification notification = builder.build();
                                NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.notify(s.getLength(), notification);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(s.getFile()));
                            request.setDestinationUri(Uri.fromFile(new File(
                                    Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/",
                                    s.getFullName() + (
                                            c == 0 ? "" : String.format("(%d)", c)
                                    ) + ".mp3"))
                            );
                            request.setTitle(s.getFullName());
                            request.allowScanningByMediaScanner();
                            request.setVisibleInDownloadsUi(true);

                            request.setMimeType("audio/MP3");
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.addRequestHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; rv:12.0) Gecko/20120403211507 Firefox/12.0");
                            Toast.makeText(getContext().getApplicationContext(), "Download iniziato...", Toast.LENGTH_LONG).show();
                            long id = downloadManager.enqueue(request);
                        }
                    }

                });
                v.findViewById(R.id.button_addplaylist).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_playlists, null, false);
                        final AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                        //builder1.setTitle("Aggiungi a:");
                        builder1.setView(view);
                        ListView listView = (ListView) view.findViewById(R.id.list);
                        final PlaylistDBHelper dbHelper = PlaylistDBHelper.getInstance(getContext().getApplicationContext());
                        final ArrayList<Playlist> playlists = dbHelper.getPlaylists();
                        PlaylistAdapter adapter = new PlaylistAdapter(playlists, getContext());
                        listView.setAdapter(adapter);
                        final AlertDialog a = builder1.create();
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                dbHelper.addSongToPlaylist(songsSaved.get(p1), playlists.get(position).getName());
                                a.dismiss();
                            }
                        });
                        a.show();
                    }
                });

                builder.setView(v);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        /*if (mediaPlayer[0] != null) {
                            mediaPlayer[0].stop();
                            mediaPlayer[0].release();
                            mediaPlayer[0] = null;
                        }*/
                    }
                });

                dialog = builder.show();

                /**/
            }
        });
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void refreshResults() {
        new TopSongsResolverTask(new TopSongsResolverTask.TopSongsResolverInterface() {
            @Override
            public void startSearch() {
                viewProgress();
            }

            @Override
            public int getTarget() {
                return target;
            }

            @Override
            public void setResults(ArrayList<Song> songs) {
                songsSaved.clear();
                songsSaved.addAll(songs);
                adapter.notifyDataSetChanged();
                viewList();
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void viewList() {
        if (listView != null) listView.setVisibility(View.VISIBLE);
        if (loading != null) loading.setVisibility(View.GONE);
    }


    private void viewProgress() {
        if (listView != null) listView.setVisibility(View.GONE);
        if (loading != null) loading.setVisibility(View.VISIBLE);
    }

}
