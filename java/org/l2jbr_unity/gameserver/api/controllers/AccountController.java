package org.l2jbr_unity.gameserver.api.controllers;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.l2jbr_unity.commons.database.DatabaseFactory;
import org.l2jbr_unity.gameserver.api.enums.AccessLevel;
import org.l2jbr_unity.gameserver.api.jwt.JwtUtil;
import org.l2jbr_unity.gameserver.api.responses.CharacterResponse;
import org.l2jbr_unity.gameserver.api.responses.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccountController {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class.getName());

    public static void listCharacters(Context context) {
        String token = Objects.requireNonNull(context.header("Authorization")).replace("Bearer ", "");
        String subject = JwtUtil.getSubject(token);
        int userAccessLevel = JwtUtil.getAccessLevel(token);

        String query = "SELECT * FROM characters WHERE account_name = ?";
        if (userAccessLevel == AccessLevel.ADMIN.getLevel() || userAccessLevel == AccessLevel.MASTER.getLevel()) {
            if (context.queryParam("account_name") != null) {
                query = "SELECT * FROM characters WHERE account_name = ?";
                subject = context.queryParam("account_name");
            } else if (context.queryParam("nobless") != null) {
                query = "SELECT * FROM characters WHERE nobless = 1";
            } else if (context.queryParam("top_xp") != null) {
                query = "SELECT * FROM characters ORDER BY exp DESC LIMIT 10";
            } else if (context.queryParam("online") != null) {
                query = "SELECT * FROM characters WHERE online = 1";
            } else if (context.queryParam("clanid") != null) {
                query = "SELECT * FROM characters WHERE clanid = ?";
            } else if (context.queryParam("createDate") != null) {
                query = "SELECT * FROM characters WHERE createDate = ?";
            }
        }

        try (Connection con = DatabaseFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, subject);
            if (context.queryParam("clanid") != null) {
                ps.setInt(1, Integer.parseInt(Objects.requireNonNull(context.queryParam("clanid"))));
            } else if (context.queryParam("createDate") != null) {
                ps.setString(1, context.queryParam("createDate"));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<CharacterResponse> characters = new ArrayList<>();
                while (rs.next()) {
                    CharacterResponse character = new CharacterResponse.Builder()
                            .withCode(HttpStatus.OK.getCode())
                            .withMessage("Character retrieved successfully")
                            .withCharId(rs.getInt("charId"))
                            .withCharName(rs.getString("char_name"))
                            .withLevel(rs.getInt("level"))
                            .withExp(rs.getLong("exp"))
                            .withOnline(rs.getInt("online") == 1)
                            .withClanId(rs.getInt("clanid"))
                            .withNobless(rs.getInt("nobless") == 1)
                            .build();
                    characters.add(character);
                }
                context.json(characters).status(HttpStatus.OK);
            }
        } catch (SQLException e) {
            LOGGER.error("Database error during character retrieval for account: {}", subject, e);
            context.json(new Response(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "Failed to retrieve characters"))
                    .status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}