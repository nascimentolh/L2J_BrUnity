package org.l2jbr_unity.gameserver.api.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.l2jbr_unity.gameserver.model.actor.Player;

import java.util.Date;

public class JwtUtil {
    //TODO: Implementar nos arquivos de configuração uma Key para poder ser lida aqui.
    private static final String SECRET_KEY = "your_secret_key";
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    public static String generateToken(String account, int accessLevel) {
        return JWT.create()
                .withSubject(account)
                .withClaim("accessLevel", accessLevel)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000))
                .sign(algorithm);
    }

    public static void validateToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(algorithm)
                .build();
        verifier.verify(token);
    }

    public static String getSubject(String token) {
        return JWT.decode(token).getSubject();
    }

    public static int getAccessLevel(String token) {
        return JWT.decode(token).getClaim("accessLevel").asInt();
    }
}
