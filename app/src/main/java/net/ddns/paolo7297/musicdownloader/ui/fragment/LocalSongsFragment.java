package net.ddns.paolo7297.musicdownloader.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
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
import net.ddns.paolo7297.musicdownloader.adapter.LocalSongsAdapter;
import net.ddns.paolo7297.musicdownloader.adapter.PlaylistAdapter;
import net.ddns.paolo7297.musicdownloader.placeholder.Playlist;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;
import net.ddns.paolo7297.musicdownloader.playback.MasterPlayer;
import net.ddns.paolo7297.musicdownloader.playback.PlaylistDBHelper;
import net.ddns.paolo7297.musicdownloader.ui.activity.SongsEditActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by paolo on 20/04/17.
 */

public class LocalSongsFragment extends Fragment {
    private LocalSongsAdapter adapter;
    private ProgressBar progressbar;
    private ListView listView;
    private ArrayList<Song> results;
    private PlaylistDBHelper dbHelper;
    private int target = 0;

    public void setTarget(int target) {
        this.target = target;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_music, container, false);
        listView = (ListView) view.findViewById(R.id.list);
        progressbar = (ProgressBar) view.findViewById(R.id.spinner);
        progressbar.setVisibility(View.VISIBLE);
        progressbar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        results = new ArrayList<>();
        adapter = new LocalSongsAdapter(results, getActivity());
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
                adapter.notifyDataSetInvalidated();

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        progressbar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        try {
            results.clear();
            results.addAll(target == 0 ? Song.getDownloadedSongs(getActivity()) : Song.getSongs(getActivity()));
            adapter.notifyDataSetChanged();
        } catch (IOException e) {
            results.clear();
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
                startActivity(Intent.createChooser(i, String.format(Locale.getDefault(), "%s \"%s\"", getString(R.string.share), results.get(menuInfo.position).getName())));
                return true;
            case R.id.delete:

                //Toast.makeText(getActivity(), "So di dovere mettere un messaggio di conferma, ma sono pigro", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.are_you_sure);
                builder.setMessage(String.format(Locale.getDefault(), "%s \"%s\"?", getString(R.string.want_to_del), results.get(menuInfo.position).getName()));
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA + "='" + results.get(menuInfo.position).getFile() + "'", null);
                        new File(results.get(menuInfo.position).getFile()).delete();
                        onResume();
                    }
                });
                builder.setNegativeButton(R.string.no, null);
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
