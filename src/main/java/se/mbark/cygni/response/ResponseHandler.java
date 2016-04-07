package se.mbark.cygni.response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import se.mbark.cygni.beans.AlbumInfo;
import se.mbark.cygni.beans.ArtistInfo;
import se.mbark.cygni.parsers.CoverArtArchiveResponseParser;
import se.mbark.cygni.parsers.MusicBrainzResponseParser;
import se.mbark.cygni.parsers.WikipediaResponeParser;

import java.net.URL;
import java.util.function.BiConsumer;

/**
 * Created by mbark on 07/04/16.
 */
public class ResponseHandler {
    final private static Logger LOGGER = LoggerFactory.getLogger("se.mbark.cygni.response.ResponseHandler");

    private final String mbid;
    private final ResponseBuilder responseBuilder;

    public ResponseHandler(String mbid) {
        this.mbid = mbid;
        responseBuilder = new ResponseBuilder();
    }

    public void handleMusicBrainzResponse(AsyncResult<JsonObject> musicBrainzRequest, BiConsumer<ArtistInfo, String> success, BiConsumer<Integer, String> fail) {
        if(musicBrainzRequest.succeeded()) {
            LOGGER.debug("MusicBrainz request finished successfully");

            JsonObject musicBrainzResponse = musicBrainzRequest.result();
            ArtistInfo artist = MusicBrainzResponseParser.parseMusicBrainzResponse(mbid, musicBrainzResponse);
            if (artist == null) {
                LOGGER.debug("Unable to parse MusicBrainz response {0}", musicBrainzResponse);
                fail.accept(500, "Unable to parse MusicBrain response");
                return;
            }
            String wikipediaTitle = MusicBrainzResponseParser.parseWikipediaArtistTitle(musicBrainzResponse);

            responseBuilder.addArtistInfo(artist);

            success.accept(artist, wikipediaTitle);
        } else {
            LOGGER.debug("MusicBrainz request failed with cause {0}", musicBrainzRequest.cause().getMessage());
            fail.accept(429, "MusicBrainz request failed, probably due to rate limiting - try again later");
        }
    }

    public void handleWikipediaResponse(AsyncResult<JsonObject> wikipediaRequest, Handler<ResponseBuilder> done) {
        LOGGER.debug("Wikipedia request done {0}", wikipediaRequest);
        String extract = null;

        if(wikipediaRequest.succeeded()) {
            JsonObject response = wikipediaRequest.result();
            extract = WikipediaResponeParser.parseExtract(response);
        } else {
            LOGGER.warn("Wikipedia request failed", wikipediaRequest.cause());
        }

        responseBuilder.addWikipediaInfo(extract);
        done.handle(responseBuilder);
    }

    public void handleCoverArtArchiveResponse(AlbumInfo album, AsyncResult<JsonObject> coverArtArchiveRequest, Handler<ResponseBuilder> done) {
        LOGGER.debug("CoverArt archive request {0} done for album with id={1}", coverArtArchiveRequest, album.getId());
        URL imageUrl = null;

        if(coverArtArchiveRequest.succeeded()) {
            imageUrl = CoverArtArchiveResponseParser.parseImageUrl(coverArtArchiveRequest.result());
        } else {
            // this happens too often for it to be anything but debugging material...
            LOGGER.debug("CoverArt request failed", coverArtArchiveRequest.cause());
        }

        responseBuilder.addAlbumInfo(album, imageUrl);
        done.handle(responseBuilder);
    }
}
