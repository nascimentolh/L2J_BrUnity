package org.l2jbr_unity.gameserver.api.controllers;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.l2jbr_unity.commons.database.DatabaseFactory;
import org.l2jbr_unity.gameserver.api.enums.AccessLevel;
import org.l2jbr_unity.gameserver.api.jwt.JwtUtil;
import org.l2jbr_unity.gameserver.api.models.AccountInfo;
import org.l2jbr_unity.gameserver.api.requests.LoginRequest;
import org.l2jbr_unity.gameserver.api.responses.LoginResponse;
import org.l2jbr_unity.gameserver.api.responses.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthController {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class.getName());

    public static void login(Context context) {
        LoginRequest loginRequest = context.bodyAsClass(LoginRequest.class);
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        String query = "SELECT login, password, IF(? > value OR value IS NULL, accessLevel, -1) AS accessLevel, lastServer FROM accounts LEFT JOIN (account_data) ON (account_data.account_name=accounts.login AND account_data.var=\"ban_temp\") WHERE login=?";
        try (Connection con = DatabaseFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, Long.toString(System.currentTimeMillis()));
            ps.setString(2, username);
            try (ResultSet loginResultSet = ps.executeQuery()) {
                if (loginResultSet.next()) {
                    AccountInfo info = new AccountInfo.Builder()
                            .withLogin(loginResultSet.getString("login"))
                            .withPassHash(loginResultSet.getString("password"))
                            .withAccessLevel(AccessLevel.getByLevel(loginResultSet.getInt("accessLevel")))
                            .withLastServer(loginResultSet.getInt("lastServer"))
                            .build();

                    if (info.checkPassHash(password)) {
                        String token = JwtUtil.generateToken(info.getLogin(), info.getAccessLevel().getLevel());
                        context.json(new LoginResponse(HttpStatus.OK.getCode(), "Successful login", token));
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error during login attempt for user: {}", username, e);
            context.json(new Response(500, "Unsuccessful login"))
                    .status(HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        LOGGER.warn("Invalid login attempt for user: {}", username);
        context.json(new Response(401, "Invalid credentials"))
                .status(HttpStatus.UNAUTHORIZED);
    }
}
