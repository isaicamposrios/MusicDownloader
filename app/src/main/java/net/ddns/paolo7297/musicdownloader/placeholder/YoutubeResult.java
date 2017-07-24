package net.ddns.paolo7297.musicdownloader.placeholder;

/**
 * Created by paolo on 24/07/17.
 */

public class YoutubeResult {
    String title, channel, thumbnailUrl, id;

    public YoutubeResult(String title, String channel, String thumbnailUrl, String id) {
        this.title = title;
        this.channel = channel;
        this.thumbnailUrl = thumbnailUrl;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getChannel() {
        return channel;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getId() {
        return id;
    }
}
