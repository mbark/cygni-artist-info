package se.mbark.cygni;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import se.mbark.cygni.beans.AlbumInfo;
import se.mbark.cygni.response.ResponseHandler;
import se.mbark.cygni.response.ResponseTracker;
import se.mbark.cygni.restapis.AbstractRestApi;
import se.mbark.cygni.restapis.MusicBrainzApi;
import se.mbark.cygni.util.UrlBuilder;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created by mbark on 08/04/16.
 */
public class ApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger("se.mbark.cygni.ApiController");

    private final RoutingContext context;
    private final UrlBuilder urlBuilder;
    private final ResponseHandler responseHandler;
    private final ResponseTracker responseTracker;

    public ApiController(RoutingContext context, UrlBuilder urlBuilder, ResponseHandler handler, ResponseTracker tracker) {
        this.context = context;
        this.urlBuilder = urlBuilder;
        this.responseHandler = handler;
        this.responseTracker = tracker;
    }

    public void doMusicBrainzGet(AbstractRestApi api, Runnable success, BiConsumer<Integer, String> fail) {
        String mbid = responseHandler.getMbid();
        String musicBrainzUrl = urlBuilder.getMusicBrainzUrl(mbid);
        api.get(musicBrainzUrl, musicBrainzResponse -> {
            responseHandler.handleMusicBrainzResponse(musicBrainzResponse);
            success.run();
        }, fail);
    }

    public void doWikipediaGet(AbstractRestApi api) {
        String wikipediaUrl = urlBuilder.getWikipediaUrl(responseHandler.getWikipediaTitle());
        api.get(wikipediaUrl, wikipediaResponse -> {
            responseHandler.handleWikipediaResponse(wikipediaResponse);
            responseTracker.wikipediaRequestReceived();
        }, (statusCode, errorMsg) -> {
            LOGGER.warn("Wikipedia request failed with status code = {0} and content {1}", statusCode, errorMsg);
            responseTracker.wikipediaRequestReceived();
        });
    }

    public void doCoverArtArchiveGet(AbstractRestApi api) {
        List<AlbumInfo> albums = responseHandler.getArtistInfo().getAlbums();
        responseTracker.setExpectedAlbumInfoRespones(albums.size());

        for(AlbumInfo album : responseHandler.getArtistInfo().getAlbums()) {

            String coverArtArchiveUrl = urlBuilder.getCoverArtArchiveUrl(album.getId());
            api.get(coverArtArchiveUrl, coverArtResponse -> {
                responseHandler.handleCoverArtArchiveResponse(album, coverArtResponse);
                responseTracker.albumInfoReceived();
            }, (statusCode, errorMsg) -> {
                if(statusCode == 404) {
                    LOGGER.info("No cover art for album with id {0}", album.getId());
                } else {
                    LOGGER.warn("CoverArt archive request failed with status code = {0}", statusCode);
                }
                responseTracker.albumInfoReceived();
            });
        }
    }
}
