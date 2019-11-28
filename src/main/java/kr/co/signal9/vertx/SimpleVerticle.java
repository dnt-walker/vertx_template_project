package kr.co.signal9.vertx;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class SimpleVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(SimpleVerticle.class);

    @Override
    public void start(final Future<Void> startFuture) throws RuntimeException {
        try {
            JsonObject configuration = Optional.ofNullable(context.config())
                    .orElseThrow(()->new Exception("configure error."));
            log.info(configuration.encodePrettily());

            System.out.println("this is std out print. start verticle");
            System.err.println("this is std err print. start verticle");

            startFuture.complete();
        } catch (Throwable th) {
            startFuture.fail(th);
        }
    }

    @Override
    public void stop() throws Exception {
        System.out.println("this is std out print. stop verticle");
        System.err.println("this is std err print. stop verticle");
    }


    public static void main(String[] args) {
        String appDir;

        if (args.length >= 1) {
            appDir = args[0];
            System.out.println("Get Working Directory At Args = " + System.getProperty("user.dir"));
        } else {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            appDir = System.getProperty("user.dir");
        }

        if (!Files.exists(Paths.get(appDir + "/configure.json"))) {
            System.out.println("not exist configure file.");
            System.exit(1);
        }

        ConfigRetriever retriever = null;
        try {
            ConfigStoreOptions fileStore = new ConfigStoreOptions()
                    .setType("file")
                    .setConfig(new JsonObject().put("path", appDir + "/configure.json"));
            ConfigStoreOptions sysPropsStore = new ConfigStoreOptions().setType("sys");
            ConfigRetrieverOptions configureOptions = new ConfigRetrieverOptions()
                    .addStore(fileStore);//.addStore(sysPropsStore);

            retriever = ConfigRetriever.create(Vertx.vertx(), configureOptions);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        VertxOptions options = new VertxOptions();
        Vertx vertx = Vertx.vertx(options);

        retriever.getConfig(ar -> {
            if (ar.failed()) {
                // Failed to retrieve the configuration
                System.out.println("Failed to retrieve the configuration");
            } else {
                JsonObject config = ar.result();

                DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(config);
                vertx.deployVerticle("kr.co.signal9.vertx.SimpleVerticle", deploymentOptions, res -> {
                    if (res.succeeded()) {
                        System.out.println("Deployment id is: " + res.result());
                    } else {
                        System.out.println("Deployment failed! " + res.cause().getMessage());
                        System.exit(1);
                    }
                });
                System.out.println(config.encodePrettily());
            }
        });
    }
}
