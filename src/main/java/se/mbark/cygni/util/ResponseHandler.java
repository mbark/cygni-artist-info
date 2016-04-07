package se.mbark.cygni.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import se.mbark.cygni.beans.AlbumInfo;
import se.mbark.cygni.beans.ArtistInfo;
import se.mbark.cygni.parsers.CoverArtArchiveResponseParser;
import se.mbark.cygni.parsers.WikipediaResponeParser;

import java.net.URL;
import java.util.List;

/**
 * Created by mbark on 06/04/16.
 */
public class ResponseHandler {
    final private static Logger LOGGER = LoggerFactory.getLogger("se.mbark.cygni.Server");
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

    public void handleWikipediaResponse(AsyncResult<JsonObject> request) {
        hasWikipediaInfo = true;
        LOGGER.debug("Wikipedia response received, {0} remaining responses from CoverArtArchive");

        if(request.succeeded()) {
            JsonObject response = request.result();
            String extract = WikipediaResponeParser.parseExtract(response);
            artistInfo.setDescription(extract);
        } else {
            LOGGER.warn("Wikipedia request failed", request.cause());
        }
    }

    public void handleCoverArtResponse(AlbumInfo album, AsyncResult<JsonObject> request) {
        albumInfosNotSet--;
        LOGGER.debug("CoverArt response received, {0} remaining responses got Wikipedia response={1}", albumInfosNotSet, hasWikipediaInfo);

        if(request.succeeded()) {
            JsonObject response = request.result();
            URL imageUrl = CoverArtArchiveResponseParser.parseImageUrl(response);
            album.setImage(imageUrl);
        } else {
            LOGGER.debug("No cover art found for album with id", album.getId());
        }

    }
}
