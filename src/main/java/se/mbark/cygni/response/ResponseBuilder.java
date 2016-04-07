package se.mbark.cygni.response;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import se.mbark.cygni.beans.AlbumInfo;
import se.mbark.cygni.beans.ArtistInfo;

import java.net.URL;
import java.util.List;

/**
 * Created by mbark on 06/04/16.
 */
public class ResponseBuilder {
    final private static Logger LOGGER = LoggerFactory.getLogger("se.mbark.cygni.ResponseBuilder");

    private ArtistInfo artistInfo;
    private boolean hasWikipediaInfo = false;
    private int albumInfosNotSet = 0;

    public ResponseBuilder() {
    }

    public void addArtistInfo(ArtistInfo artistInfo) {
        this.artistInfo = artistInfo;
        List albums = artistInfo.getAlbums();
        if(albums != null) {
            albumInfosNotSet = albums.size();
        }
    }

    public void addWikipediaInfo(String extract) {
        hasWikipediaInfo = true;
        artistInfo.setDescription(extract);
    }

    public void addAlbumInfo(AlbumInfo album, URL imageUrl) {
        albumInfosNotSet--;
        album.setImage(imageUrl);

    }

    public ArtistInfo getArtistInfoRespoonse() {
        return artistInfo;
    }

    public boolean allInformationAdded() {
        return hasWikipediaInfo && albumInfosNotSet <= 0;
    }

}
