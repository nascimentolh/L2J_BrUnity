package org.l2jbr_unity.gameserver.api;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.l2jbr_unity.gameserver.api.controllers.AuthController;

public class RestServer {
    private static RestServer instance;
    private final Javalin app;

    private RestServer() {
        app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json; charset=utf-8";
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost)
            );
        });

//        registerMiddlewares();
        registerRoutes();
    }

    public static RestServer getInstance() {
        if (instance == null) {
            instance = new RestServer();
        }
        return instance;
    }

//    private void registerMiddlewares() {
//        app.before("/api/secure-data", AuthMiddleware::handle);
//    }

    private void registerRoutes() {
        app.post("/api/login", AuthController::login);
    }

    public void start() {
        app.start(7000);
    }

    public void stop() {
        app.stop();
    }
}
