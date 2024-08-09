package org.l2jbr_unity.gameserver.api.midlewares;

import com.auth0.jwt.exceptions.JWTVerificationException;
import io.javalin.http.Context;
import org.l2jbr_unity.gameserver.api.jwt.JwtUtil;


public class AuthMiddleware {

    public static void handle(Context ctx) {
        try {
            String authorizationHeader = ctx.header("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                ctx.status(401).result("Unauthorized: Missing or invalid Authorization header");
                return;
            }

            String token = authorizationHeader.replace("Bearer ", "").trim();

            // Validação do token usando JwtUtil
            JwtUtil.validateToken(token);

            // Não precisa de ctx.next(), o fluxo continuará naturalmente se não houver erros
        } catch (JWTVerificationException e) {
            ctx.status(401).result("Unauthorized: Invalid or expired token");
        } catch (Exception e) {
            ctx.status(500).result("Internal Server Error");
        }
    }
}
