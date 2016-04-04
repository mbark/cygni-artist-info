package se.mbark.cygni;

import java.net.URL;

/**
 * Created by mbark on 04/04/16.
 */
public class AlbumInfo {
    private String title;
    private String id;
    private URL image;

    public AlbumInfo(String title, String id, URL image) {
        this.title = title;
        this.id = id;
        this.image = image;
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

    public void setId(String id) {
        this.id = id;
    }

    public URL getImage() {
        return image;
    }

    public void setImage(URL image) {
        this.image = image;
    }
}
