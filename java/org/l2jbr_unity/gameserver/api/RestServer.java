package org.l2jbr_unity.gameserver.api;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.l2jbr_unity.gameserver.api.controllers.AccountController;
import org.l2jbr_unity.gameserver.api.controllers.AuthController;
import org.l2jbr_unity.gameserver.api.midlewares.AuthMiddleware;

public class RestServer {
    private final Javalin app;

    private RestServer() {
        app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json; charset=utf-8";
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
            config.router.contextPath = "/api";
        });

        registerMiddlewares();
        registerRoutes();
    }

    private void registerMiddlewares() {
        // Middleware de autenticação aplicado a todas as rotas protegidas
        app.before("/api/characters/*", AuthMiddleware::handle);
    }

    private void registerRoutes() {
        app.post("/login", AuthController::login);
        app.get("/characters", AccountController::listCharacters);

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