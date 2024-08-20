package org.l2jbr_unity.gameserver.api;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.l2jbr_unity.gameserver.api.controllers.AuthController;

public class RestServer {
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

    public static RestServer getInstance() {
        return RestServer.SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        protected static final RestServer INSTANCE = new RestServer();
    }
}
