package se.mbark.cygni.restapis;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * Created by mbark on 04/04/16.
 */
public class CovertArtArchiveApi extends AbstractRestApi {
    final private static Logger LOGGER = LoggerFactory.getLogger("se.mbark.cygni.util.CoverArtArchiveApi");

    private final HttpClient client;

    public CovertArtArchiveApi(Vertx vertx) {
        client = vertx.createHttpClient();
    }

    public void get(String url, Consumer<JsonObject> success, BiConsumer<Integer, String> fail) {
        LOGGER.debug("GET CoverArt image from {0}", url);
        client.getAbs(url, response -> {
            if(response.statusCode() == 307 || response.statusCode() == 302) {
                String location = response.getHeader("Location");
                JsonObject image = new JsonObject();
                image.put("image", location);

                LOGGER.debug("GET CoverArt image from {0} got response {1}", url, image);
                success.accept(image);
            } else {
                LOGGER.debug("GET cover art image from {0} failed with status code {1}", url, response.statusCode());
                fail.accept(response.statusCode(), "");
            }
        }).end();
    }
}
