package se.mbark.cygni.parsers;

import io.vertx.core.json.JsonObject;

import java.util.Map;

/**
 * Created by mbark on 05/04/16.
 */
public class WikipediaResponeParser {
    public static String parseExtract(JsonObject response) {
        JsonObject pagesJson = response.getJsonObject("query").getJsonObject("pages");

        Map<String, Object> pages = pagesJson.getMap();
        String artistExtract = null;

        for(Map.Entry<String, Object> entry : pages.entrySet()) {
            JsonObject page = pagesJson.getJsonObject(entry.getKey());
            // we only request one page
            artistExtract = page.getString("extract");
        }

        return artistExtract;
    }
}
