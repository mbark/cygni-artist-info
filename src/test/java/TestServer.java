import com.jayway.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.mbark.cygni.Server;
import se.mbark.cygni.util.UrlBuilder;

import java.io.IOException;
import java.net.ServerSocket;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by mbark on 08/04/16.
 */

@RunWith(VertxUnitRunner.class)
public class TestServer {
    private Vertx vertx;
    private Integer port;

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put("http.port", port));

        vertx.deployVerticle(Server.class.getName(), options, context.asyncAssertSuccess());
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
        RestAssured.reset();
    }

    @Test
    public void checkThatNoMbidReturnsError(TestContext context) {
        get("/")
                .then()
                .assertThat()
                .statusCode(400);
    }
}