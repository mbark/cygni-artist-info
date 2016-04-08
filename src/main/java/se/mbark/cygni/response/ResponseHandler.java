package se.mbark.cygni.response;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import se.mbark.cygni.beans.AlbumInfo;
import se.mbark.cygni.beans.ArtistInfo;
import se.mbark.cygni.parsers.CoverArtArchiveResponseParser;
import se.mbark.cygni.parsers.MusicBrainzResponseParser;
import se.mbark.cygni.parsers.WikipediaResponeParser;

import java.net.URL;

/**
 * Created by mbark on 07/04/16.
 */
public class ResponseHandler {
    final private static Logger LOGGER = LoggerFactory.getLogger("se.mbark.cygni.response.ResponseHandler");

    private final String mbid;
    private ArtistInfo artistInfo;
    private String wikipediaTitle;

    public ResponseHandler(String mbid) {
        this.mbid = mbid;
    }

    public String getMbid() {
        return mbid;
    }

    public ArtistInfo getArtistInfo() {
        return artistInfo;
    }

    public String getWikipediaTitle() {
        return wikipediaTitle;
    }

    public void handleMusicBrainzResponse(JsonObject musicBrainzResponse) {
        LOGGER.debug("MusicBrainz request finished successfully");

        ArtistInfo artist = MusicBrainzResponseParser.parseMusicBrainzResponse(mbid, musicBrainzResponse);
        if (artist == null) {
            LOGGER.debug("Unable to parse MusicBrainz response {0}", musicBrainzResponse);
        } else {
            wikipediaTitle = MusicBrainzResponseParser.parseWikipediaArtistTitle(musicBrainzResponse);
            artistInfo = artist;
        }
    }

    public void handleWikipediaResponse(JsonObject wikipediaResponse) {
        LOGGER.debug("Wikipedia request done {0}", wikipediaResponse);

        String extract = WikipediaResponeParser.parseExtract(wikipediaResponse);
        artistInfo.setDescription(extract);
    }

    public void handleCoverArtArchiveResponse(AlbumInfo album, JsonObject coverArtResponse) {
        LOGGER.debug("CoverArt archive request {0} done for album with id={1}", coverArtResponse, album.getId());

        URL imageUrl = CoverArtArchiveResponseParser.parseImageUrl(coverArtResponse);
        album.setImage(imageUrl);
    }
}
