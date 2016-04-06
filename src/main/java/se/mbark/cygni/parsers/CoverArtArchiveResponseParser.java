package se.mbark.cygni.parsers;

import io.vertx.core.json.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mbark on 05/04/16.
 */
public class CoverArtArchiveResponseParser {
    public static URL parseImageUrl(JsonObject coverArtResponse) {
        URL url = null;
        try {
            url = new URL(coverArtResponse.getString("image"));
        } catch (MalformedURLException e) {
        } catch (Exception e) {
        }
        return url;
    }

}
