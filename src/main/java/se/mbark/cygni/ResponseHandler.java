package se.mbark.cygni;

import io.vertx.core.json.JsonObject;
import se.mbark.cygni.parsers.CoverArtArchiveResponseParser;
import se.mbark.cygni.parsers.WikipediaResponeParser;

import java.net.URL;
import java.util.List;

/**
 * Created by mbark on 06/04/16.
 */
public class ResponseHandler {
    final private ArtistInfo artistInfo;

    private boolean hasWikipediaInfo = false;
    private int albumInfosNotSet = 0;

    public ResponseHandler(ArtistInfo artistInfo) {
        this.artistInfo = artistInfo;
        List albums = artistInfo.getAlbums();
        if(albums != null) {
            albumInfosNotSet = albums.size();
        }
    }

    public ArtistInfo getArtistInfo() {
        return artistInfo;
    }

    public boolean isDone() {
        return hasWikipediaInfo && albumInfosNotSet <= 0;
    }

    public void handleWikipediaResponse(JsonObject response) {
        hasWikipediaInfo = true;

        String extract = WikipediaResponeParser.parseExtract(response);
        artistInfo.setDescription(extract);
    }

    public void handleCoverArtResponse(AlbumInfo album, JsonObject response) {
        albumInfosNotSet--;

        URL imageUrl = CoverArtArchiveResponseParser.parseImageUrl(response);
        album.setImage(imageUrl);
    }
}
