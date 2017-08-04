package net.ddns.paolo7297.musicdownloader.placeholder;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import net.ddns.paolo7297.musicdownloader.R;

import java.util.ArrayList;

/**
 * Created by paolo on 25/07/17.
 */

public class Artist implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Artist createFromParcel(Parcel source) {
            return new Artist(source);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
    private String name, key;
    private int nTracks;

    public Artist(String name, String key, int nTracks) {
        this.name = name;
        this.key = key;
        this.nTracks = nTracks;
    }

    public Artist(Parcel in) {
        String[] strings = new String[3];
        in.readStringArray(strings);
        this.name = strings[0];
        this.key = strings[1];
        this.nTracks = Integer.parseInt(strings[2]);
    }

    public static ArrayList<Artist> getArtists(Context context) {
        ArrayList<Artist> al = new ArrayList<>();
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Artists.ARTIST,
                        MediaStore.Audio.Artists.ARTIST_KEY,
                        MediaStore.Audio.Artists.NUMBER_OF_TRACKS
                },
                null,
                null,
                MediaStore.Audio.Artists.ARTIST + " ASC"
        );
        if (c != null) {
            while (c.moveToNext()) {
                String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
                if ("<unknown>".equals(artist)) {
                    artist = context.getString(R.string.unknown_artist);
                }
                al.add(new Artist(
                        artist,
                        c.getString(c.getColumnIndex(MediaStore.Audio.Artists.ARTIST_KEY)),
                        c.getInt(c.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
                ));
            }
            c.close();
        }
        return al;

    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public int getnTracks() {
        return nTracks;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                name,
                key,
                nTracks + ""
        });
    }
}
