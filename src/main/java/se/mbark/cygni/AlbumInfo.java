package se.mbark.cygni;

import java.net.URL;

/**
 * Created by mbark on 04/04/16.
 */
public class AlbumInfo {
    private final String id;
    private String title;
    private URL image;

    public AlbumInfo(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public URL getImage() {
        return image;
    }

    public void setImage(URL image) {
        this.image = image;
    }
}
