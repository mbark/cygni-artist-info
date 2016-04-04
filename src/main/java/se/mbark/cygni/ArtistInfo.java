package se.mbark.cygni;

import java.util.List;

/**
 * Created by mbark on 04/04/16.
 */
public class ArtistInfo {
    final private String mbid;
    private String description;
    private List<AlbumInfo> albums;

    public ArtistInfo(String mbid) {
        this.mbid = mbid;
    }
    public List<AlbumInfo> getAlbums() {
        return albums;
    }

    public void setAlbums(List<AlbumInfo> albums) {
        this.albums = albums;
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

}
