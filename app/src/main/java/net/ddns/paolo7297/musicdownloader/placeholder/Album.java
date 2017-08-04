package net.ddns.paolo7297.musicdownloader.placeholder;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import net.ddns.paolo7297.musicdownloader.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by paolo on 25/07/17.
 */

public class Album implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
    ArrayList<String> albumKey;
    private String artist, album, albumArt;
    private int id;

    public Album(int id, String artist, String album, String albumArt, String albumKey) {
        this.albumKey = new ArrayList<>();
        this.artist = artist;
        this.album = album;
        this.albumArt = albumArt;
        this.albumKey.add(albumKey);
        this.id = id;
    }

    public Album(Parcel in) {
        artist = in.readString();
        album = in.readString();
        albumArt = in.readString();
        id = in.readInt();
        int size = in.readInt();
        String[] strings = new String[size];
        in.readStringArray(strings);
        albumKey = new ArrayList<>(Arrays.asList(strings));

    }

    public static ArrayList<Album> getAlbums(Context context) {
        ArrayList<Album> al = new ArrayList<>();
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.Albums.ARTIST,
                        MediaStore.Audio.Albums.ALBUM,
                        MediaStore.Audio.Albums.ALBUM_ART,
                        MediaStore.Audio.Albums.ALBUM_KEY
                },
                null,
                null,
                MediaStore.Audio.Albums.ALBUM + " ASC"
        );
        if (c != null) {
            while (c.moveToNext()) {
                String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Albums.ARTIST));
                if ("<unknown>".equals(artist)) {
                    artist = context.getString(R.string.unknown_artist);
                }
                Album temp = new Album(
                        c.getInt(c.getColumnIndex(MediaStore.Audio.Albums._ID)),
                        artist,
                        c.getString(c.getColumnIndex(MediaStore.Audio.Albums.ALBUM)),
                        c.getString(c.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)),
                        c.getString(c.getColumnIndex(MediaStore.Audio.Albums.ALBUM_KEY))
                );
                Album b = null;
                for (Album a : al) {
                    if (a.album.equals(temp.album) && a.artist.equals(temp.artist))
                        b = a;
                }
                if (b != null) {
                    b.albumKey.add(temp.albumKey.get(0));
                    if (b.albumArt == null) {
                        b.albumArt = temp.albumArt;
                    }
                } else {
                    al.add(temp);
                }
            }
            c.close();
        }
        return al;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public int getId() {
        return id;
    }

    public ArrayList<String> getAlbumKey() {
        return albumKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(albumArt);
        dest.writeInt(id);
        dest.writeInt(albumKey.size());
        dest.writeStringArray(albumKey.toArray(new String[albumKey.size()]));

    }
}
