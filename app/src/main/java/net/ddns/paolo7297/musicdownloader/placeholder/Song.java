package net.ddns.paolo7297.musicdownloader.placeholder;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import net.ddns.paolo7297.musicdownloader.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static net.ddns.paolo7297.musicdownloader.Constants.FOLDER_HOME;

/**
 * Created by paolo on 28/10/16.
 */

/*
"id":"44662018SBp",
"artist":"Linkin Park -",
"track":"Numb",
"length":187,
"file":"http://pleer.com/browser-extension/files/44662018SBp.mp3",
"link":"http://pleer.com/tracks/44662018SBp",
"size":5112052,
"bitrate":"VBR"
 */
public class Song implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
    private String id, name, artist, file, bitrate, size;
    private int length;

    public Song(String id, String artist, String name, int length, String file, String size, String bitrate) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.file = file;
        this.bitrate = bitrate;
        this.length = length;
        this.size = size;
    }

    public Song(Parcel in) {
        String[] strings = new String[7];
        in.readStringArray(strings);
        this.id = strings[0];
        this.name = strings[1];
        this.artist = strings[2];
        this.file = strings[3];
        this.bitrate = strings[4];
        this.length = Integer.parseInt(strings[5]);
        this.size = strings[6];
    }

    public static ArrayList<Song> getSongs(Context context) {
        ArrayList<Song> al = new ArrayList<>();
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.SIZE,
                },
                null,
                null,
                MediaStore.Audio.Media.TITLE + " ASC"
        );
        if (c != null) {
            while (c.moveToNext()) {
                String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                if ("<unknown>".equals(artist)) {
                    artist = context.getString(R.string.unknown_artist);
                }
                al.add(new Song(
                        c.getLong(c.getColumnIndex(MediaStore.Audio.Media._ID)) + "",
                        artist,
                        c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        (int) ((c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000)),
                        c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        ((c.getLong(c.getColumnIndex(MediaStore.Audio.Media.SIZE)) / 1024) / 1024) + " MB",
                        null
                ));
            }
            c.close();
        }
        return al;
    }

    public static ArrayList<Song> getSongs(Context context, Album a) {
        ArrayList<Song> al = new ArrayList<>();
        for (String albumkey : a.getAlbumKey()) {
            Cursor c = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.SIZE,
                            MediaStore.Audio.Media.ALBUM_KEY
                    },
                    MediaStore.Audio.Media.ALBUM_KEY + " = ?",
                    new String[]{
                            albumkey
                    },
                    MediaStore.Audio.Media.TITLE + " ASC"
            );
            if (c != null) {
                while (c.moveToNext()) {
                    String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    if ("<unknown>".equals(artist)) {
                        artist = context.getString(R.string.unknown_artist);
                    }
                    al.add(new Song(
                            c.getLong(c.getColumnIndex(MediaStore.Audio.Media._ID)) + "",
                            artist,
                            c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                            (int) ((c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000)),
                            c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA)),
                            ((c.getLong(c.getColumnIndex(MediaStore.Audio.Media.SIZE)) / 1024) / 1024) + " MB",
                            null
                    ));
                }
                c.close();
            }
        }
        return al;
    }

    /*public String getWebPage() {
        return page;
    }*/

    /*public void setFile(String file) {
        this.file = file;
    }*/

    public static ArrayList<Song> getSongs(Context context, Artist a) {
        ArrayList<Song> al = new ArrayList<>();
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.ARTIST_KEY
                },
                MediaStore.Audio.Media.ARTIST_KEY + " = ?",
                new String[]{
                        a.getKey()
                },
                MediaStore.Audio.Media.TITLE + " ASC"
        );
        if (c != null) {
            while (c.moveToNext()) {
                String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                if ("<unknown>".equals(artist)) {
                    artist = context.getString(R.string.unknown_artist);
                }
                al.add(new Song(
                        c.getLong(c.getColumnIndex(MediaStore.Audio.Media._ID)) + "",
                        artist,
                        c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        (int) ((c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000)),
                        c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        ((c.getLong(c.getColumnIndex(MediaStore.Audio.Media.SIZE)) / 1024) / 1024) + " MB",
                        null
                ));
            }
            c.close();
        }
        return al;
    }

    public static ArrayList<Song> getDownloadedSongs(Context context) throws IOException {
        File f = new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_HOME + "/");
        ArrayList<Song> al = new ArrayList<>();
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.SIZE,
                },
                MediaStore.Audio.Media.DATA + " LIKE '" + f.getCanonicalPath() + "%' ",
                null,
                MediaStore.Audio.Media.TITLE + " ASC"
        );
        if (c != null) {
            while (c.moveToNext()) {
                String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                if ("<unknown>".equals(artist)) {
                    artist = context.getString(R.string.unknown_artist);
                }
                al.add(new Song(
                        c.getLong(c.getColumnIndex(MediaStore.Audio.Media._ID)) + "",
                        artist,
                        c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        (int) ((c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000)),
                        c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        ((c.getLong(c.getColumnIndex(MediaStore.Audio.Media.SIZE)) / 1024) / 1024) + " MB",
                        null
                ));
            }
            c.close();
        }
        return al;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist/* == null ? null : artist.replace("-","").trim()*/;
    }

    public String getFile() {
        return file;
    }

    public String getBitrate() {
        return bitrate;
    }

    public int getLength() {
        return length;
    }

    public String getSize() {
        //return String.format("%.2f MB",(float)size/(1024*1024))
        return size;
    }

    public String getFullName() {
        return getArtist() + " - " + getName();
    }

    @Override
    public boolean equals(Object s) {
        return s != null &&
                ((id != null && id.equals(((Song) s).id)) || (id == null && ((Song) s).id == null)) &&
                ((name != null && name.equals(((Song) s).name)) || (name == null && ((Song) s).name == null)) &&
                ((artist != null && artist.equals(((Song) s).artist)) || (artist == null && ((Song) s).artist == null)) &&
                ((file != null && file.equals(((Song) s).file)) || (file == null && ((Song) s).file == null)) &&
                ((bitrate != null && bitrate.equals(((Song) s).bitrate)) || (bitrate == null && ((Song) s).bitrate == null)) &&
                (length == ((Song) s).length);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String[] strings = new String[]{
                id,
                name,
                artist,
                file,
                bitrate,
                length + "",
                size
        };
        dest.writeStringArray(strings);
    }
}
