package se.mbark.cygni.beans;

import java.util.List;

/**
 * Created by mbark on 04/04/16.
 */
public class ArtistInfo {
    final private String mbid;
    private String name;
    private String description;
    private List<AlbumInfo> albums;

    public ArtistInfo(String mbid) {
        this.mbid = mbid;
    }

    public String getMbid() {
        return mbid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AlbumInfo> getAlbums() {
        return albums;
    }

    public void setAlbums(List<AlbumInfo> albums) {
        this.albums = albums;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
