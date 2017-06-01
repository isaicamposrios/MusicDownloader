package net.ddns.paolo7297.musicdownloader.placeholder;

/**
 * Created by paolo on 10/05/17.
 */

public class Playlist {
    private int count;
    private String name;

    public Playlist(int count, String name) {
        this.count = count;
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }
}
