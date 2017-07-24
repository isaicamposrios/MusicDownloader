package net.ddns.paolo7297.musicdownloader.ui.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
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

import net.ddns.paolo7297.musicdownloader.CacheManager;
import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.PlaylistAdapter;
import net.ddns.paolo7297.musicdownloader.adapter.SearchAdapter;
import net.ddns.paolo7297.musicdownloader.placeholder.Playlist;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;
import net.ddns.paolo7297.musicdownloader.playback.MasterPlayer;
import net.ddns.paolo7297.musicdownloader.playback.PlaylistDBHelper;
import net.ddns.paolo7297.musicdownloader.task.QueryResolverTask;
import net.ddns.paolo7297.musicdownloader.ui.DisablingImageButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.SEARCH_SERVICE;
import static android.view.View.GONE;

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
    private DisablingImageButton prev, next, quality, ascdsc, sortmode;
    private AlertDialog dialog;

    public SearchFragment() {

    }

    public void setQuery(String s) {
        query = s;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        qual = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_quality", "0"));
        sort = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_verso", "0"));
        mode = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_verso", "0"));
        cacheManager = CacheManager.getInstance(getContext());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        text = (TextView) view.findViewById(R.id.text);
        loading = (ProgressBar) view.findViewById(R.id.spinner);
        listView = (ListView) view.findViewById(R.id.list);
        prev = (DisablingImageButton) view.findViewById(R.id.back);
        next = (DisablingImageButton) view.findViewById(R.id.next);
        quality = (DisablingImageButton) view.findViewById(R.id.quality);
        ascdsc = (DisablingImageButton) view.findViewById(R.id.ascdsc);
        sortmode = (DisablingImageButton) view.findViewById(R.id.modality);
        loading.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        songsSaved = new ArrayList<>();
        //viewText("Inizia a cercare la musica");
        adapter = new SearchAdapter(getActivity(), songsSaved);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Song s = songsSaved.get(position);
                final int p1 = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_songinfo, parent, false);
                /*Bitmap b = getTumbnail(s);
                if (b != null) {
                    ((ImageView)v.findViewById(R.id.image)).setImageBitmap(b);
                } else {
                    ((ImageView)v.findViewById(R.id.image)).setImageResource(R.drawable.ic_music_note_black_48dp);
                }*/
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
                ((TextView) v.findViewById(R.id.time)).setText(String.format(Locale.getDefault(), "%d:%02d min", i, d));
                v.findViewById(R.id.button_stream).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MasterPlayer mp = MasterPlayer.getInstance(getActivity().getApplicationContext());
                        mp.setup(songsSaved.toArray(new Song[songsSaved.size()]), position, MasterPlayer.SHUFFLE_DISABLED, MasterPlayer.REPEAT_ONE);
                        //mp.setup(temp, 0);
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
                ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(), R.style.FooterPopupStyle);
                PopupMenu menu = new PopupMenu(ctw, v);
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
                ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(), R.style.FooterPopupStyle);
                PopupMenu menu = new PopupMenu(ctw, v);
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
        menuInflater.inflate(R.menu.main, menu);
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
                    viewText(getString(R.string.start_search_music));
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

            viewText(getString(R.string.start_search_music));

            if (query != null) {
                MenuItemCompat.expandActionView(search);
                querySongs = query;
                searchView.setQuery(query, true);
                query = null;
            }
        }
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
                if (songs.size() > 0) {
                    viewList(page);
                } else {
                    viewText(getString(R.string.nothing_found));
                }
                //adapter.setSongs(songsSaved);
                adapter.notifyDataSetChanged();
                //listView.setSelectionAfterHeaderView();

            }

            @Override
            public void setMaxPage(int limit) {
                maxPage = limit;
            }

        }, getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void viewList(int page) {
        if (text != null) text.setVisibility(GONE);
        if (listView != null) {
            listView.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(0);
                }
            }, 100);
        }
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
        int s = 0, d = 0;
        if (page == 0) {
            d++;
            s++;
            if (prev != null) prev.setEnabled(false);
            if (next != null) next.setEnabled(false);
        }
        if (page == 1) {
            s++;
            if (prev != null) prev.setEnabled(false);

        }
        if (page >= maxPage / 20) {
            d++;
            if (next != null) next.setEnabled(false);
        }
        if (prev != null && s == 0) prev.setEnabled(true);
        if (next != null && d == 0) next.setEnabled(true);
    }

    public Bitmap getTumbnail(Song song) {
        Bitmap b = null;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(song.getFile(), new HashMap<String, String>());
        byte art[] = mediaMetadataRetriever.getEmbeddedPicture();
        if (art != null) b = BitmapFactory.decodeByteArray(art, 0, art.length);
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
