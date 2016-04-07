package se.mbark.cygni.parsers;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mbark on 05/04/16.
 */
public class CoverArtArchiveResponseParser {
    private static final Logger LOGGER = LoggerFactory.getLogger("se.mbark.cygni.CoverArtArchiveResponseParser");

    public static URL parseImageUrl(JsonObject coverArtResponse) {
        URL url = null;
        try {
            String imageUrl = coverArtResponse.getString("image");
            if(imageUrl != null) {
                url = new URL(coverArtResponse.getString("image"));
                LOGGER.debug("CoverArt image parsed succesfully {0}", url);
            }
        } catch (MalformedURLException e) {
            LOGGER.warn("CoverArt image has a malformed url", e);
        }
        return url;
    }

}
