package net.ddns.paolo7297.musicdownloader.ui.fragment;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.DownloadedSongsAdapter;
import net.ddns.paolo7297.musicdownloader.adapter.PlaylistAdapter;
import net.ddns.paolo7297.musicdownloader.placeholder.Playlist;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;
import net.ddns.paolo7297.musicdownloader.playback.MasterPlayer;
import net.ddns.paolo7297.musicdownloader.playback.PlaylistDBHelper;
import net.ddns.paolo7297.musicdownloader.ui.activity.SongsEditActivity;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static net.ddns.paolo7297.musicdownloader.Constants.FOLDER_HOME;

/**
 * Created by paolo on 20/04/17.
 */

public class DownloadedSongsFragment extends Fragment {
    private DownloadedSongsAdapter adapter;
    private ProgressBar progressbar;
    private ListView listView;
    private ArrayList<Song> results;
    private PlaylistDBHelper dbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_download, container, false);
        listView = (ListView) view.findViewById(R.id.list);
        progressbar = (ProgressBar) view.findViewById(R.id.spinner);
        progressbar.setVisibility(View.VISIBLE);
        progressbar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        results = new ArrayList<>();
        adapter = new DownloadedSongsAdapter(results, getActivity());
        listView.setAdapter(adapter);
        progressbar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        registerForContextMenu(listView);
        dbHelper = PlaylistDBHelper.getInstance(getContext());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MasterPlayer mp = MasterPlayer.getInstance(getActivity().getApplicationContext());
                mp.setup(results.toArray(new Song[results.size()]), position);

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        progressbar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);

        if (results == null || results.size() == 0) {
            ArrayList<File> files = new ArrayList<>(Arrays.asList(new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/").listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".mp3");
                }
            })));
            //File f = new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/");
            ContentResolver cr = getActivity().getContentResolver();

            Cursor c = null;
            try {
                results.clear();
                for (File f : files) {
                    c = cr.query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            null,
                            MediaStore.Audio.Media.DATA + " LIKE ? ",
                            new String[]{f.getCanonicalPath()},
                            MediaStore.Audio.Media.TITLE + " ASC"
                    );
                    while (c != null && c.moveToNext()) {
                        results.add(new Song(
                                c.getLong(c.getColumnIndex(MediaStore.Audio.Media._ID)) + "",
                                c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                                c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                                (int) ((c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000)),
                                c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA)),
                                ((c.getLong(c.getColumnIndex(MediaStore.Audio.Media.SIZE)) / 1024) / 1024) + " MB",
                                ""
                        ));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            /*new DownloadedSongsLoaderTask(new DownloadedSongsLoaderTask.DownloadedSongLoaderInterface() {
                @Override
                public void prepareUI() {
                    listView.setVisibility(View.GONE);
                    progressbar.setVisibility(View.VISIBLE);
                }

                @Override
                public void updateFiles(ArrayList<Song> al) {
                    results.clear();
                    Song[] sr = al.toArray(new Song[al.size()]);
                    Arrays.sort(sr, new Comparator<Song>() {
                        @Override
                        public int compare(Song lhs, Song rhs) {
                            return lhs.getName().compareTo(rhs.getName());
                        }
                    });
                    results.addAll(Arrays.asList(sr));
                    adapter.notifyDataSetChanged();
                    listView.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.GONE);
                }

                @Override
                public ArrayList<File> getFiles() {
                    return new ArrayList<>(Arrays.asList(new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/").listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return pathname.getName().endsWith(".mp3");
                        }
                    })));
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!new File(results.get(i).getFile()).exists()) {
                    results.remove(i);
                    i--;
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.options_downloads, menu);
        //menu.setHeaderTitle(results.get(((AdapterView.AdapterContextMenuInfo) menuInfo).position).getName());

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.share:
                Uri uri = Uri.fromFile(new File(results.get(menuInfo.position).getFile()));
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("audio/mp3");
                i.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(i, "Condividi \"" + results.get(menuInfo.position).getName() + "\""));
                return true;
            case R.id.delete:

                //Toast.makeText(getActivity(), "So di dovere mettere un messaggio di conferma, ma sono pigro", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Sei sicuro?");
                builder.setMessage("Vuoi eliminare \"" + results.get(menuInfo.position).getName() + "\"?");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new File(results.get(menuInfo.position).getFile()).delete();
                        onResume();
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
                return true;
            case R.id.add:
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
                        dbHelper.addSongToPlaylist(results.get(menuInfo.position), playlists.get(position).getName());
                        a.dismiss();
                    }
                });
                a.show();
                return true;
            case R.id.edit:
                Intent i1 = new Intent(getContext(), SongsEditActivity.class);
                i1.putExtra("SONG", results.get(menuInfo.position).getFile());
                startActivity(i1);
                return true;
            default:
                return false;
        }
    }
}
