package org.l2jbr_unity.gameserver.api.controllers;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.l2jbr_unity.commons.database.DatabaseFactory;
import org.l2jbr_unity.gameserver.api.enums.AccessLevel;
import org.l2jbr_unity.gameserver.api.jwt.JwtUtil;
import org.l2jbr_unity.gameserver.api.responses.AccountInfoResponse;
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

    private static final String ACCOUNT_QUERY = "SELECT * FROM accounts WHERE login = ?";
    private static final String CHARACTERS_COUNT_QUERY = "SELECT COUNT(*) AS count FROM characters WHERE account_name = ?";
    private static final String CURRENCY_QUERY = "SELECT currency_balance FROM virtual_currency WHERE account_name = ?";
    private static final String BASE_CHARACTER_QUERY =
            "SELECT ch.charId, ch.char_name, ch.level, ch.exp, ch.online, " +
                    "cl.clan_name, cl.clan_level, cl.reputation_score, cl.ally_name " +
                    "FROM characters ch " +
                    "LEFT JOIN clan_data cl ON ch.clanid = cl.clan_id " +
                    "WHERE ch.account_name = ?";

    public static void getAccountInfo(Context context) {
        String token = getAuthorizationToken(context);
        String subject = JwtUtil.getSubject(token);

        try (Connection con = DatabaseFactory.getConnection();
             PreparedStatement accountPs = con.prepareStatement(ACCOUNT_QUERY);
             PreparedStatement charactersPs = con.prepareStatement(CHARACTERS_COUNT_QUERY);
             PreparedStatement currencyPs = con.prepareStatement(CURRENCY_QUERY)) {

            accountPs.setString(1, subject);
            charactersPs.setString(1, subject);
            currencyPs.setString(1, subject);

            try (ResultSet accountRs = accountPs.executeQuery();
                 ResultSet charactersRs = charactersPs.executeQuery();
                 ResultSet currencyRs = currencyPs.executeQuery()) {

                if (accountRs.next()) {
                    AccountInfoResponse response = buildAccountInfoResponse(accountRs, charactersRs, currencyRs);
                    context.json(response).status(HttpStatus.OK);
                } else {
                    context.json(new Response(HttpStatus.NOT_FOUND.getCode(), "Account not found"))
                            .status(HttpStatus.NOT_FOUND);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error while retrieving account information for user: {}", subject, e);
            context.json(new Response(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "Failed to retrieve account information"))
                    .status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static void listCharacters(Context context) {
        String token = getAuthorizationToken(context);
        String subject = JwtUtil.getSubject(token);
        int userAccessLevel = JwtUtil.getAccessLevel(token);

        StringBuilder queryBuilder = new StringBuilder(BASE_CHARACTER_QUERY);
        List<Object> parameters = new ArrayList<>();
        parameters.add(subject);

        if (userAccessLevel == AccessLevel.ADMIN.getLevel() || userAccessLevel == AccessLevel.MASTER.getLevel()) {
            addFilters(context, queryBuilder, parameters);
        }

        try (Connection con = DatabaseFactory.getConnection();
             PreparedStatement ps = prepareStatement(con, queryBuilder.toString(), parameters);
             ResultSet rs = ps.executeQuery()) {

            List<CharacterResponse> characters = new ArrayList<>();
            while (rs.next()) {
                characters.add(buildCharacterResponse(rs));
            }
            context.json(characters).status(HttpStatus.OK);
        } catch (SQLException e) {
            LOGGER.error("Database error during character retrieval for account: {}", subject, e);
            context.json(new Response(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "Failed to retrieve characters"))
                    .status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static void addFilters(Context context, StringBuilder queryBuilder, List<Object> parameters) {
        if (context.queryParam("account_name") != null) {
            queryBuilder.append(" AND ch.account_name = ?");
            parameters.add(context.queryParam("account_name"));
        } else {
            if (context.queryParam("nobless") != null) {
                queryBuilder.append(" AND ch.nobless = 1");
            }
            if (context.queryParam("top_xp") != null) {
                queryBuilder.append(" ORDER BY ch.exp DESC LIMIT 10");
            }
            if (context.queryParam("online") != null) {
                queryBuilder.append(" AND ch.online = 1");
            }
            if (context.queryParam("clanid") != null) {
                queryBuilder.append(" AND cl.clan_id = ?");
                parameters.add(Integer.parseInt(Objects.requireNonNull(context.queryParam("clanid"))));
            }
            if (context.queryParam("createDate") != null) {
                queryBuilder.append(" AND ch.createDate = ?");
                parameters.add(context.queryParam("createDate"));
            }
        }
    }

    private static PreparedStatement prepareStatement(Connection con, String query, List<Object> parameters) throws SQLException {
        PreparedStatement ps = con.prepareStatement(query);
        for (int i = 0; i < parameters.size(); i++) {
            ps.setObject(i + 1, parameters.get(i));
        }
        return ps;
    }

    private static CharacterResponse buildCharacterResponse(ResultSet rs) throws SQLException {
        return new CharacterResponse.Builder()
                .withCode(HttpStatus.OK.getCode())
                .withMessage("Character retrieved successfully")
                .withCharId(rs.getInt("charId"))
                .withCharName(rs.getString("char_name"))
                .withLevel(rs.getInt("level"))
                .withExp(rs.getLong("exp"))
                .withOnline(rs.getInt("online") == 1)
                .withClanName(rs.getString("clan_name"))
                .withClanLevel(rs.getInt("clan_level"))
                .withReputationScore(rs.getInt("reputation_score"))
                .withAllyName(rs.getString("ally_name"))
                .build();
    }

    private static AccountInfoResponse buildAccountInfoResponse(ResultSet accountRs, ResultSet charactersRs, ResultSet currencyRs) throws SQLException {
        String login = accountRs.getString("login");
        String email = accountRs.getString("email");
        String createdTime = accountRs.getString("created_time");
        long lastActive = accountRs.getLong("lastactive");
        String lastIP = accountRs.getString("lastIP");
        int accessLevelValue = accountRs.getInt("accessLevel");

        AccessLevel accessLevel = AccessLevel.getByLevel(accessLevelValue);

        int charactersCount = 0;
        if (charactersRs.next()) {
            charactersCount = charactersRs.getInt("count");
        }

        double virtualCurrencyBalance = 0.0;
        if (currencyRs.next()) {
            virtualCurrencyBalance = currencyRs.getDouble("currency_balance");
        }

        return new AccountInfoResponse.Builder()
                .withCode(HttpStatus.OK.getCode())
                .withMessage("Account information retrieved successfully")
                .withLogin(login)
                .withEmail(email)
                .withCreatedTime(createdTime)
                .withLastActive(lastActive)
                .withLastIP(lastIP)
                .withCharactersCount(charactersCount)
                .withVirtualCurrencyBalance(virtualCurrencyBalance)
                .withAccessLevelName(accessLevel.getName())
                .withAccessLevelColor(accessLevel.getNameColor())
                .withIsGM(accessLevel.isGM())
                .build();
    }

    private static String getAuthorizationToken(Context context) {
        return Objects.requireNonNull(context.header("Authorization")).replace("Bearer ", "");
    }
}