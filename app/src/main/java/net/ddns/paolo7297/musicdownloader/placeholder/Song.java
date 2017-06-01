package net.ddns.paolo7297.musicdownloader.placeholder;

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
public class Song {
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

    /*public String getWebPage() {
        return page;
    }*/

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
        return getArtist() + " - "+getName();
    }

    public boolean equals(Song s) {
        return id.equals(s.id) && name.equals(s.name) && artist.equals(s.artist) && file.equals(s.file) && bitrate.equals(s.bitrate) && (length == s.length) && size.equals(s.size);
    }
}
