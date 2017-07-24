package net.ddns.paolo7297.musicdownloader.ui.fragment;

import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.SparseArray;
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

import com.squareup.picasso.Picasso;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.YoutubeSearchAdapter;
import net.ddns.paolo7297.musicdownloader.placeholder.YoutubeResult;
import net.ddns.paolo7297.musicdownloader.task.YoutubeQueryResolverTask;

import java.io.File;
import java.util.ArrayList;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import static android.content.Context.SEARCH_SERVICE;
import static android.view.View.GONE;
import static net.ddns.paolo7297.musicdownloader.Constants.FOLDER_HOME;

/**
 * Created by paolo on 24/07/17.
 */

public class YoutubeSearchFragment extends Fragment {

    private final static String url = "https://www.yt-download.org/api-console/audio/";

    private String searchQuery;
    private TextView text;
    private ProgressBar loading;
    private ListView listView;
    private ArrayList<YoutubeResult> results;
    private YoutubeSearchAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_youtube_search, container, false);
        text = (TextView) view.findViewById(R.id.text);
        loading = (ProgressBar) view.findViewById(R.id.spinner);
        listView = (ListView) view.findViewById(R.id.list);
        results = new ArrayList<>();
        adapter = new YoutubeSearchAdapter(results, getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            TextView title, channel, textSize;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_youtube, parent, false);
                Picasso.with(getActivity()).load(results.get(position).getThumbnailUrl()).into((ImageView) v.findViewById(R.id.image));
                title = (TextView) v.findViewById(R.id.title);
                channel = (TextView) v.findViewById(R.id.channel);
                textSize = (TextView) v.findViewById(R.id.size);
                title.setText(results.get(position).getTitle());
                channel.setText(results.get(position).getChannel());

                v.findViewById(R.id.button_stream).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent ytbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + results.get(position).getId()));
                        Intent brwIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + results.get(position).getId()));
                        try {
                            startActivity(ytbIntent);
                        } catch (ActivityNotFoundException ex) {
                            startActivity(brwIntent);
                        }
                    }
                });

                v.findViewById(R.id.button_download).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new YouTubeExtractor(getActivity()) {

                            @Override
                            protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
                                if (sparseArray == null) {
                                    Toast.makeText(getActivity(), "Errore", Toast.LENGTH_LONG).show();
                                } else {
                                    int maxAudio = -1;
                                    int maxAudioTag = -1;
                                    for (int i = 0, itag; i < sparseArray.size(); i++) {
                                        itag = sparseArray.keyAt(i);
                                        YtFile f = sparseArray.get(itag);
                                        if (f.getFormat().getHeight() == -1) {
                                            if (f.getFormat().getAudioBitrate() > maxAudio) {
                                                maxAudio = f.getFormat().getAudioBitrate();
                                                maxAudioTag = itag;
                                            }
                                        }
                                    }
                                    if (maxAudio != -1) {
                                        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(sparseArray.get(maxAudioTag).getUrl()));
                                        request.setDestinationUri(Uri.fromFile(new File(
                                                Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/", results.get(position).getTitle() + ".mp3"))
                                        );
                                        request.setTitle(results.get(position).getTitle());
                                        request.allowScanningByMediaScanner();
                                        request.setVisibleInDownloadsUi(true);
                                        request.setMimeType("audio/MP3");
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                        request.addRequestHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; rv:12.0) Gecko/20120403211507 Firefox/12.0");
                                        downloadManager.enqueue(request);
                                        Toast.makeText(getActivity(), R.string.remember_details, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }.extract("http://www.youtube.com/watch?v=" + results.get(position).getId(), true, false);
                        /*if (downloadUrl != null) {
                            DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                            request.setDestinationUri(Uri.fromFile(new File(
                                    Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/", results.get(position).getTitle() + ".mp3"))
                            );
                            request.setTitle(results.get(position).getTitle());
                            request.allowScanningByMediaScanner();
                            request.setVisibleInDownloadsUi(true);
                            request.setMimeType("audio/MP3");
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.addRequestHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; rv:12.0) Gecko/20120403211507 Firefox/12.0");
                            Toast.makeText(getActivity().getApplicationContext(), getActivity().getString(R.string.download_started) + "...", Toast.LENGTH_LONG).show();
                            downloadManager.enqueue(request);
                        }*/
                    }

                });
                builder.setView(v);
                builder.show();
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        final MenuItem search = menu.findItem(R.id.action_search);
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

                    searchQuery = query;
                    doSearch();
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
                    results.clear();
                    searchQuery = null;
                    adapter.notifyDataSetChanged();
                    viewText(getString(R.string.start_search_youtube_music));
                    return true;
                }
            });

            viewText(getString(R.string.start_search_youtube_music));

        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void doSearch() {
        new YoutubeQueryResolverTask(new YoutubeQueryResolverTask.YoutubeQueryInterface() {
            @Override
            public void setup() {
                viewProgress();
            }

            @Override
            public String getQuery() {
                return searchQuery;
            }

            @Override
            public void setResult(ArrayList<YoutubeResult> res) {
                results.clear();
                results.addAll(res);
                adapter.notifyDataSetChanged();
                viewList();
            }
        }, getActivity().getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void viewList() {
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

    }

    private void viewText(String tx) {
        if (text != null) text.setText(tx);
        if (text != null) text.setVisibility(View.VISIBLE);
        if (listView != null) listView.setVisibility(GONE);
        if (loading != null) loading.setVisibility(GONE);
    }

    private void viewProgress() {
        if (text != null) text.setVisibility(GONE);
        if (listView != null) listView.setVisibility(GONE);
        if (loading != null) loading.setVisibility(View.VISIBLE);
    }

}
