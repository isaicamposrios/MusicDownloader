package net.ddns.paolo7297.musicdownloader.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.ddns.paolo7297.musicdownloader.CacheManager;
import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.PlaylistAdapter;
import net.ddns.paolo7297.musicdownloader.adapter.SearchAdapter;
import net.ddns.paolo7297.musicdownloader.placeholder.Playlist;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;
import net.ddns.paolo7297.musicdownloader.playback.MasterPlayer;
import net.ddns.paolo7297.musicdownloader.playback.PlaylistDBHelper;
import net.ddns.paolo7297.musicdownloader.task.TopSongsResolverTask;

import java.util.ArrayList;

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
                final Song s = songsSaved.get(position);
                final int p1 = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_songinfo, parent, false);
                /*v.findViewById(R.id.button_stream).setEnabled(false);
                v.findViewById(R.id.button_download).setEnabled(false);
                v.findViewById(R.id.button_addplaylist).setEnabled(false);
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
                            ((ImageView) v.findViewById(R.id.image)).setImageResource(R.drawable.logo_red_white);
                        }
                        v.findViewById(R.id.image).setVisibility(View.VISIBLE);
                        v.findViewById(R.id.spinner).setVisibility(View.GONE);
                        v.findViewById(R.id.button_stream).setEnabled(true);
                        v.findViewById(R.id.button_download).setEnabled(true);
                        v.findViewById(R.id.button_addplaylist).setEnabled(true);
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/
                ((ImageView) v.findViewById(R.id.image)).setImageResource(R.drawable.logo_red_white);
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
                        MasterPlayer mp = MasterPlayer.getInstance(getActivity().getApplicationContext());
                        mp.setup(songsSaved.toArray(new Song[songsSaved.size()]), position);
                    }
                });
                v.findViewById(R.id.button_download).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cacheManager.download(s);
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
