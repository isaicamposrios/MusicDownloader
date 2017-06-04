package net.ddns.paolo7297.musicdownloader.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.PlaylistAdapter;
import net.ddns.paolo7297.musicdownloader.placeholder.Playlist;
import net.ddns.paolo7297.musicdownloader.playback.PlaylistDBHelper;
import net.ddns.paolo7297.musicdownloader.ui.activity.PlaylistActivity;

import java.util.ArrayList;


/**
 * Created by paolo on 10/05/17.
 */

public class PlaylistsFragment extends Fragment {

    private ListView listView;
    private PlaylistAdapter adapter;
    private ArrayList<Playlist> playlists;
    private PlaylistDBHelper dbHelper;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        listView = (ListView) view.findViewById(R.id.list);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        playlists = new ArrayList<>();
        adapter = new PlaylistAdapter(playlists, getContext().getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getContext(), PlaylistActivity.class);
                i.putExtra("playlist", playlists.get(position).getName());
                startActivity(i);
            }
        });
        dbHelper = PlaylistDBHelper.getInstance(getContext().getApplicationContext());
        registerForContextMenu(listView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(new ContextThemeWrapper(getContext(), R.style.FooterPopupStyle));
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Nuova playlist");
                builder.setView(editText);
                builder.setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.addEmptyPlaylist(editText.getText().toString().trim());
                        onResume();
                    }
                });
                builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.options_playlist, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                dbHelper.deletePlaylist(playlists.get(menuInfo.position).getName());
                onResume();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PlaylistDBHelper dbHelper = PlaylistDBHelper.getInstance(getContext().getApplicationContext());
        playlists.clear();
        playlists.addAll(dbHelper.getPlaylists());
        adapter.notifyDataSetChanged();
        Log.e("PLAYLIST", playlists.size() + "");

    }
}
