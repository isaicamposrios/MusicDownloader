package net.ddns.paolo7297.musicdownloader.playback;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.ddns.paolo7297.musicdownloader.placeholder.Playlist;
import net.ddns.paolo7297.musicdownloader.placeholder.Song;

import java.util.ArrayList;

/**
 * Created by paolo on 10/05/17.
 */

public class PlaylistDBHelper extends SQLiteOpenHelper {

    private static final String NAME = "playlists.db";
    private static final int VERSION = 1;
    private static final String QUERY_PLAYLIST = "CREATE TABLE Playlist (" +
            " PID integer PRIMARY KEY AUTOINCREMENT," +
            " Name varchar UNIQUE" +
            ");";
    private static final String QUERY_SONG = "CREATE TABLE Song (" +
            " SID integer PRIMARY KEY AUTOINCREMENT," +
            " Title varchar," +
            " Artist varchar," +
            " Path varchar," +
            " Bitrate varchar," +
            " Durate integer," +
            " Size varchar" +
            ");";
    private static final String QUERY_RELATIONSHIP = "CREATE TABLE SRP (" +
            " Playlist_ID integer," +
            " Song_ID integer," +
            " FOREIGN KEY('Playlist_ID') REFERENCES Playlist ( PID ) ON DELETE CASCADE ON UPDATE NO ACTION," +
            " FOREIGN KEY('Song_ID') REFERENCES Song ( SID ) ON DELETE CASCADE ON UPDATE NO ACTION" +
            ");";
    private static PlaylistDBHelper dbHelper;

    private PlaylistDBHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    public static PlaylistDBHelper getInstance(Context c) {
        if (dbHelper == null) {
            dbHelper = new PlaylistDBHelper(c);
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUERY_PLAYLIST);
        db.execSQL(QUERY_SONG);
        db.execSQL(QUERY_RELATIONSHIP);
        ContentValues cv = new ContentValues();
        cv.put("Name", "Preferiti");
        db.insert("playlist", null, cv);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<String> getPlaylistsNames() {
        Cursor c = getReadableDatabase().query("playlist", new String[]{"Name"}, null, null, null, null, null);
        ArrayList<String> strings = new ArrayList<>();
        while (c.moveToNext()) {
            strings.add(c.getString(c.getColumnIndex("Name")));
        }
        c.close();
        return strings;
    }

    public ArrayList<Song> getSongs(String name) {
        ArrayList<Song> songs = new ArrayList<>();
        Cursor c = getReadableDatabase().query(
                "song INNER JOIN SRP ON song.SID = SRP.Song_ID INNER JOIN playlist ON playlist.PID = SRP.Playlist_ID",
                new String[]{"Title", "Artist", "Path", "Bitrate", "Durate", "Size"},
                "Name = ?",
                new String[]{name},
                null,
                null,
                null);
        while (c.moveToNext()) {
            songs.add(new Song(
                    "",
                    c.getString(c.getColumnIndex("Artist")),
                    c.getString(c.getColumnIndex("Title")),
                    c.getInt(c.getColumnIndex("Durate")),
                    c.getString(c.getColumnIndex("Path")),
                    c.getString(c.getColumnIndex("Size")),
                    c.getString(c.getColumnIndex("Bitrate"))
            ));
        }
        c.close();
        return songs;
    }

    public int getSongsCount(String name) {
        return (int) DatabaseUtils.queryNumEntries(
                getReadableDatabase(),
                "song INNER JOIN SRP ON song.SID = SRP.Song_ID INNER JOIN playlist ON playlist.PID = SRP.Playlist_ID",
                "Name = ?",
                new String[]{name}
        );
    }

    public void addEmptyPlaylist(String name) {
        boolean cont = true;
        ArrayList<String> playlists = getPlaylistsNames();
        for (String s : playlists) {
            if (s.equals(name)) {
                cont = false;
            }
        }
        if (cont) {
            ContentValues cv = new ContentValues();
            cv.put("Name", name);
            getWritableDatabase().insert("playlist", null, cv);
        }
    }

    public void addSongToPlaylist(Song song, String playlist) {
        boolean cont = true;
        ArrayList<Song> songs = getSongs(playlist);
        for (Song s : songs) {
            if (s.equals(song)) {
                cont = false;
            }
        }
        if (cont) {
            ContentValues cv1 = new ContentValues();
            ContentValues cv2 = new ContentValues();
            cv1.put("Title", song.getName());
            cv1.put("Artist", song.getArtist());
            cv1.put("Path", song.getFile());
            cv1.put("Bitrate", song.getBitrate());
            cv1.put("Durate", song.getLength());
            cv1.put("Size", song.getSize());
            int song_id = (int) getWritableDatabase().insert("song", null, cv1);
            Cursor c = getReadableDatabase().query("playlist", new String[]{"PID"}, "Name = ?", new String[]{playlist}, null, null, null);
            if (c.moveToFirst()) {
                int playlist_id = c.getInt(c.getColumnIndex("PID"));
                cv2.put("Playlist_ID", playlist_id);
                cv2.put("Song_ID", song_id);
                getWritableDatabase().insert("SRP", null, cv2);
            }
        }
    }

    public ArrayList<Playlist> getPlaylists() {
        ArrayList<Playlist> playlists = new ArrayList<>();
        String query = "SELECT Name, (SELECT COUNT(*) FROM SRP WHERE PID = Playlist_ID) AS \"C\"" +
                " FROM playlist";
        Cursor c = getReadableDatabase().rawQuery(query, null);
        while (c.moveToNext()) {
            playlists.add(new Playlist(
                    c.getInt(c.getColumnIndex("C")),
                    c.getString(c.getColumnIndex("Name"))
            ));
        }
        c.close();
        return playlists;
    }

    public void deletePlaylist(String playlist) {
        getWritableDatabase().delete("playlist", "Name = ?", new String[]{playlist});
    }

    public void deleteSong(Song song, String playlist) {
        String QUERY = "DELETE FROM Song" +
                " WHERE Title = '" + song.getName() + "' AND" +
                " Artist = '" + song.getArtist() + "' AND" +
                " Path = '" + song.getFile() + "' AND" +
                " Bitrate = '" + song.getBitrate() + "' AND" +
                " Durate = " + song.getLength() + " AND" +
                " SID IN (SELECT Song_ID FROM SRP INNER JOIN playlist ON SRP.Playlist_ID = playlist.PID WHERE Name = '" + playlist + "')";
        getWritableDatabase().execSQL(QUERY);
        //Cursor c = getReadableDatabase().rawQuery(QUERY,null);
        //c.moveToFirst();
        //System.out.println(c.getInt(c.getColumnIndex("SID")));
    }
}
