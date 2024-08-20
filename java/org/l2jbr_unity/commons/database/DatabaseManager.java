package org.l2jbr_unity.commons.database;

import org.l2jbr_unity.Config;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static MariaDbPoolDataSource dataSource;

    static {
        try {
            initializeDataSource(Config.DATABASE_MAX_CONNECTIONS);
        } catch (SQLException e) {
            throw new RuntimeException("DatabaseManager: Error initializing database pool", e);
        }
    }

    private static void initializeDataSource(int maxConnections) throws SQLException {
        dataSource = new MariaDbPoolDataSource(Config.DATABASE_URL + "&user=" + Config.DATABASE_LOGIN + "&password=" + Config.DATABASE_PASSWORD + "&maxPoolSize=" + maxConnections);
    }

    public static void init() throws SQLException {
        if (Config.DATABASE_TEST_CONNECTIONS) {
            testConnections();
        } else {
            validateConnection();
        }
    }

    private static void testConnections() throws SQLException {
        int successfulConnections = 0;
        try {
            LOGGER.info("DatabaseManager: Testing database connections...");

            for (int i = 0; i < Config.DATABASE_MAX_CONNECTIONS; i++) {
                try (Connection conn = dataSource.getConnection()) {
                    successfulConnections++;
                } catch (SQLException e) {
                    LOGGER.warning("DatabaseManager: Failed to open connection " + (i + 1) + "!");
                    break;
                }
            }

            if (successfulConnections < Config.DATABASE_MAX_CONNECTIONS) {
                LOGGER.warning("DatabaseManager: You should change your configurations!");
                LOGGER.warning("DatabaseManager: Started with " + successfulConnections + " connections out of " + Config.DATABASE_MAX_CONNECTIONS + "!");
                int newConnectionCount = Math.max(Math.max(successfulConnections / 50 * 50, 2), successfulConnections);
                initializeDataSource(newConnectionCount);
                LOGGER.info("DatabaseManager: Reinitialized with new pool size " + newConnectionCount + ".");
            }
        } catch (SQLException e) {
            LOGGER.severe("DatabaseManager: Error during connection testing. " + e.getMessage());
            throw e;
        }
    }

    private static void validateConnection() {
        try (Connection conn = dataSource.getConnection()) {
            LOGGER.info("DatabaseManager: Database connection validated successfully.");
        } catch (SQLException e) {
            LOGGER.severe("DatabaseManager: Error validating connection. " + e.getMessage());
        }
    }

    public static Optional<Connection> getConnection() {
        try {
            return Optional.of(dataSource.getConnection());
        } catch (SQLException e) {
            LOGGER.severe("DatabaseManager: Could not get a connection. " + e.getMessage());
            return Optional.empty();
        }
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
