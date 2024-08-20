package org.l2jbr_unity.commons.database;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseFactory {
    public static void init() throws SQLException {
		DatabaseManager.init();
	}

	public static Connection getConnection() {
		return DatabaseManager.getConnection().orElseThrow(() -> new RuntimeException("DatabaseFactory: Could not obtain a database connection."));
	}

	public static void close() {
		DatabaseManager.close();
	}
}
