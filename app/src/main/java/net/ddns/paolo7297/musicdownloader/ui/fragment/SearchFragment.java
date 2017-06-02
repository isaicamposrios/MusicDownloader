package net.ddns.paolo7297.musicdownloader.ui.fragment;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import net.ddns.paolo7297.musicdownloader.task.QueryResolverTask;
import net.ddns.paolo7297.musicdownloader.task.ThumbnailsDownloaderTask;
import net.ddns.paolo7297.musicdownloader.ui.DisablingImageButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.SEARCH_SERVICE;
import static android.view.View.GONE;
import static net.ddns.paolo7297.musicdownloader.Constants.FOLDER_HOME;

/**
 * Created by paolo on 01/11/16.
 */

public class SearchFragment extends Fragment {

    private ArrayList<Song> songsSaved;
    private TextView text;
    private ProgressBar loading;
    private ListView listView;
    private SearchAdapter adapter;
    private int page = 1;
    private int maxPage = 0;
    private int qual = 0;
    private int sort = 0;
    private int mode = 0;
    private String query = null;
    //private MenuItem prev,next;
    private CacheManager cacheManager;
    private String querySongs = null;
    private DisablingImageButton prev,next,quality,ascdsc,sortmode;

    public SearchFragment() {

    }

    public void setQuery(String s) {
        query = s;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        qual = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_quality","0"));
        sort = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_verso","0"));
        mode = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_verso","0"));
        cacheManager = CacheManager.getInstance(getContext());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search,container,false);
        text = (TextView) view.findViewById(R.id.text);
        loading = (ProgressBar) view.findViewById(R.id.spinner);
        listView = (ListView) view.findViewById(R.id.list);
        prev = (DisablingImageButton) view.findViewById(R.id.back);
        next = (DisablingImageButton) view.findViewById(R.id.next);
        quality = (DisablingImageButton) view.findViewById(R.id.quality);
        ascdsc = (DisablingImageButton) view.findViewById(R.id.ascdsc);
        sortmode = (DisablingImageButton) view.findViewById(R.id.modality);
        loading.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(),R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        songsSaved = new ArrayList<>();
        //viewText("Inizia a cercare la musica");
        adapter = new SearchAdapter(getActivity(),songsSaved);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final MediaPlayer[] mediaPlayer = new MediaPlayer[1];
                final Song s = songsSaved.get(position);
                final int p1 = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_songinfo,parent,false);
                /*Bitmap b = getTumbnail(s);
                if (b != null) {
                    ((ImageView)v.findViewById(R.id.image)).setImageBitmap(b);
                } else {
                    ((ImageView)v.findViewById(R.id.image)).setImageResource(R.drawable.ic_music_note_black_48dp);
                }*/
                ((ProgressBar)v.findViewById(R.id.spinner)).getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(),R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                new ThumbnailsDownloaderTask(getContext().getApplicationContext(),new ThumbnailsDownloaderTask.ThumbnailsDownloaderInterface() {
                    @Override
                    public Song getSong() {
                        return s;
                    }

                    @Override
                    public void startDownload() {
                        ((ImageView)v.findViewById(R.id.image)).setVisibility(GONE);
                        ((ProgressBar)v.findViewById(R.id.spinner)).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void setThumbnail(Bitmap b) {
                        if (b != null) {
                            ((ImageView)v.findViewById(R.id.image)).setImageBitmap(b);
                        } else {
                            ((ImageView)v.findViewById(R.id.image)).setImageResource(R.mipmap.ic_song_red);
                        }
                        ((ImageView)v.findViewById(R.id.image)).setVisibility(View.VISIBLE);
                        ((ProgressBar)v.findViewById(R.id.spinner)).setVisibility(View.GONE);
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                ((TextView)v.findViewById(R.id.title)).setText(s.getName());
                ((TextView)v.findViewById(R.id.artist)).setText(s.getArtist());
                ((TextView)v.findViewById(R.id.size)).setText(s.getSize());
                ((TextView)v.findViewById(R.id.bitrate)).setText(s.getBitrate());
                long i = s.getLength()/60;
                int d = (int) (((float) s.getLength()/60 - i)*60);
                ((TextView)v.findViewById(R.id.time)).setText(String.format("%d:%02d min",i,d));
                ((DisablingImageButton) v.findViewById(R.id.button_stream)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        MasterPlayer mp = MasterPlayer.getInstance(getActivity().getApplicationContext());
                        Song[] temp = new Song[]{songsSaved.get(position)};
                        //mp.setup(songsSaved.toArray(new Song[songsSaved.size()]),position);
                        mp.setup(temp,0);
                    }
                });
                ((DisablingImageButton) v.findViewById(R.id.button_download)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //System.out.println(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/"+FOLDER_HOME+"/")).toString());
                        int c = 0;
                        while (new File(
                                Environment.getExternalStorageDirectory() + "/"+FOLDER_HOME+"/",
                                s.getFullName()+(
                                        c==0 ? "" : String.format("(%d)",c)
                                ) + ".mp3").exists()) {
                            c++;

                        }
                        if (cacheManager.isInCache(s.getFile())) {
                            try {
                                File orig = cacheManager.retrieveFile(s.getFile());
                                File dst =new File(
                                        Environment.getExternalStorageDirectory() + "/"+FOLDER_HOME+"/",
                                        s.getFullName()+( c==0 ? "" : String.format("(%d)",c) ) + ".mp3");
                                dst.createNewFile();
                                FileChannel ifc = new FileInputStream(orig).getChannel();
                                FileChannel ofc = new FileOutputStream(dst).getChannel();
                                ifc.transferTo(0,ifc.size(),ofc);
                                orig.delete();
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
                                builder.setSmallIcon(R.mipmap.ic_songshunter);
                                Intent io = new Intent();
                                io.setAction(android.content.Intent.ACTION_VIEW);
                                io.setDataAndType(Uri.fromFile(dst),"audio/*");
                                PendingIntent iopen = PendingIntent.getActivity(getContext(),123456,io,PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(iopen);
                                builder.setContentTitle(s.getFullName());
                                builder.setContentText("Download completato.");
                                builder.setAutoCancel(true);
                                Notification notification = builder.build();
                                NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.notify(s.getLength(),notification);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(s.getFile()));
                            request.setDestinationUri(Uri.fromFile(new File(
                                    Environment.getExternalStorageDirectory() + "/"+FOLDER_HOME+"/",
                                    s.getFullName()+(
                                            c==0 ? "" : String.format("(%d)",c)
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

                ((DisablingImageButton) v.findViewById(R.id.button_addplaylist)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_playlists,null, false);
                        final AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                        //builder1.setTitle("Aggiungi a:");
                        builder1.setView(view);
                        ListView listView = (ListView) view.findViewById(R.id.list);
                        final PlaylistDBHelper dbHelper = PlaylistDBHelper.getInstance(getContext().getApplicationContext());
                        final ArrayList<Playlist> playlists = dbHelper.getPlaylists();
                        PlaylistAdapter adapter = new PlaylistAdapter(playlists,getContext());
                        listView.setAdapter(adapter);
                        final AlertDialog a = builder1.create();
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                dbHelper.addSongToPlaylist(songsSaved.get(p1),playlists.get(position).getName());
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

                builder.show();

                /**/

            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page--;
                refreshResults();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page++;
                refreshResults();
            }
        });
        quality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(),R.style.FooterPopupStyle);
                PopupMenu menu = new PopupMenu(ctw,v);
                menu.inflate(R.menu.search_quality);
                menu.getMenu().findItem(qual == 3 ? R.id.alta : qual == 2 ? R.id.media : qual == 1 ? R.id.bassa : R.id.all).setChecked(true);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int oldqual = qual;
                        switch (item.getItemId()) {
                            case R.id.alta:
                                qual = 3;
                                break;
                            case R.id.media:
                                qual = 2;
                                break;
                            case R.id.bassa:
                                qual = 1;
                                break;
                            case R.id.all:
                                qual = 0;
                        }
                        if (oldqual != qual && querySongs != null) {
                            page = 1;
                            refreshResults();
                        }
                        return true;
                    }
                });
                menu.show();
            }
        });
        ascdsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sort = sort == 0 ? 1 : 0;
                refreshOrder();
                if (querySongs != null) {
                    page = 1;
                    refreshResults();
                }
            }
        });
        sortmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(),R.style.FooterPopupStyle);
                PopupMenu menu = new PopupMenu(ctw,v);
                menu.inflate(R.menu.search_mode);
                menu.getMenu().findItem(mode == QueryResolverTask.SORT_ALPHABETIC ? R.id.alfab : mode == QueryResolverTask.SORT_DATE ? R.id.data : R.id.popol).setChecked(true);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int oldmode = mode;
                        switch (item.getItemId()) {
                            case R.id.alfab:
                                mode = QueryResolverTask.SORT_ALPHABETIC;
                                break;
                            case R.id.data:
                                mode = QueryResolverTask.SORT_DATE;
                                break;
                            case R.id.popol:
                                mode = QueryResolverTask.SORT_POPULARITY;
                                break;
                        }

                        if (oldmode != mode && querySongs != null) {
                            page = 1;
                            refreshResults();
                        }
                        return true;
                    }
                });
                menu.show();

            }
        });

        refreshOrder();

        /*final AdView ads = (AdView) view.findViewById(R.id.ads);
        ads.setVisibility(GONE);
        AdRequest.Builder builder= new AdRequest.Builder();
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
        });*/
        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.main,menu);
        MenuItem search = menu.findItem(R.id.action_search);
        //prev = menu.findItem(R.id.back);
        //next = menu.findItem(R.id.next);
        SearchManager manager = (SearchManager) getActivity().getSystemService(SEARCH_SERVICE);

        SearchView searchView = null;

        if (search != null) {
            searchView = (SearchView) search.getActionView();
        }

        if (searchView != null) {
            searchView.setSearchableInfo(manager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setIconifiedByDefault(false);
            searchView.setQueryRefinementEnabled(true);
            final SearchView finalSearchView = searchView;
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(final String query) {
                    /*SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getContext(), SuggestionProvider.AUTHORITY,SuggestionProvider.MODE);
                    suggestions.saveRecentQuery(query,null);*/
                    querySongs = query;
                    page = 1;
                    refreshResults();
                    finalSearchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

            MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    songsSaved.clear();
                    querySongs = null;
                    adapter.notifyDataSetChanged();
                    viewText("Inizia a cercare la musica");
                    return true;
                }
            });

            /*prev.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    page--;
                    refreshResults(querySongs);
                    return true;
                }
            });

            next.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    page++;
                    refreshResults(querySongs);
                    return true;
                }
            });*/

            viewText("Inizia a cercare la musica");

            if (query != null) {
                MenuItemCompat.expandActionView(search);
                querySongs = query;
                searchView.setQuery(query,true);
                query = null;
            }
        }
    }

    private void refreshResults() {

        new QueryResolverTask(new QueryResolverTask.MusicRequestInterface() {
            @Override
            public String getSearchQuery() {
                return querySongs;
            }

            @Override
            public int getPage() {
                return page;
            }

            @Override
            public String getQuality() {
                switch (qual) {
                    case 1:
                        return QueryResolverTask.QUALITY_LOW;
                    case 2:
                        return QueryResolverTask.QUALITY_MED;
                    case 3:
                        return QueryResolverTask.QUALITY_HIGH;
                    default:
                        return QueryResolverTask.QUALITY_ALL;
                }
            }

            @Override
            public int getSortMode() {
                return sort;
            }

            @Override
            public int getSortedBy() {
                return mode;
            }

            @Override
            public void startSearch() {
                viewProgress();
            }

            @Override
            public void setResults(ArrayList<Song> songs) {
                songsSaved.clear();
                songsSaved.addAll(songs);
                if (songs.size()>0) {
                    viewList(page);
                } else {
                    viewText("Non ho trovato nulla");
                }
                //adapter.setSongs(songsSaved);
                adapter.notifyDataSetChanged();
                listView.setSelectionAfterHeaderView();
            }

            @Override
            public void setMaxPage(int limit) {
                maxPage = limit;
            }

        },getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void viewList(int page) {
        if (text != null) text.setVisibility(GONE);
        if (listView != null) listView.setVisibility(View.VISIBLE);
        if (loading != null) loading.setVisibility(GONE);
        viewCursors(page);

    }

    private void viewText(String tx) {
        if (text != null) text.setText(tx);
        if (text != null) text.setVisibility(View.VISIBLE);
        if (listView != null) listView.setVisibility(GONE);
        if (loading != null) loading.setVisibility(GONE);
        viewCursors(0);
    }

    private void viewProgress() {
        if (text != null) text.setVisibility(GONE);
        if (listView != null) listView.setVisibility(GONE);
        if (loading != null) loading.setVisibility(View.VISIBLE);
        viewCursors(0);
    }

    private void viewCursors(int page) {
        /*if (page == 0 || page>maxPage/20) {
            if (prev != null) prev.setVisible(false);
            if (next != null) next.setVisible(false);
        } else if (page== 1) {
            if (prev != null) prev.setVisible(false);
            if (next != null) next.setVisible(true);
        } else {
            if (prev != null) prev.setVisible(true);
            if (next != null) next.setVisible(true);
        }
        if (page == 0 ) {
            if (prev != null) prev.setEnabled(false);
            if (next != null) next.setEnabled(false);
        } else if (page== 1) {
            if (prev != null) prev.setEnabled(false);
            if (next != null) next.setEnabled(true);
        } else if (page>maxPage/20) {
            if (prev != null) prev.setEnabled(true);
            if (next != null) next.setEnabled(false);
        } else {
            if (prev != null) prev.setEnabled(true);
            if (next != null) next.setEnabled(true);
        }*/
        int s = 0,d = 0;
        if (page == 0 ) {
            d++;
            s++;
            if (prev != null) prev.setEnabled(false);
            if (next != null) next.setEnabled(false);
        }
        if (page== 1) {
            s++;
            if (prev != null) prev.setEnabled(false);

        }
        if (page>=maxPage/20) {
            d++;
            if (next != null) next.setEnabled(false);
        }
        if (prev != null && s == 0) prev.setEnabled(true);
        if (next != null && d == 0) next.setEnabled(true);
    }

    public Bitmap getTumbnail(Song song) {
        Bitmap b = null;
        MediaMetadataRetriever mediaMetadataRetriever =  new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(song.getFile(),new HashMap<String, String>());
        byte art[] = mediaMetadataRetriever.getEmbeddedPicture();
        if (art != null ) b = BitmapFactory.decodeByteArray(art,0,art.length);
        return b;
    }

    public void refreshOrder() {
        if (sort == 0) {
            ascdsc.setImageResource(R.drawable.ic_arrow_drop_down_white_48dp);
        } else {
            ascdsc.setImageResource(R.drawable.ic_arrow_drop_up_white_48dp);
        }
    }


}
