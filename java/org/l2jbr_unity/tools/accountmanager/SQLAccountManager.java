/*
 * This file is part of the L2J BrUnity project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jbr_unity.tools.accountmanager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Logger;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.database.DatabaseFactory;
import org.l2jbr_unity.commons.enums.ServerMode;

/**
 * This class SQL Account Manager
 * @author netimperia
 */
public class SQLAccountManager {
	private static final Logger LOGGER = Logger.getLogger(SQLAccountManager.class.getName());

	private static String username = "";
	private static String password = "";
	private static String accessLevel = "";
	private static String mode = "";

	public static void main(String[] args) {
		Config.load(ServerMode.LOGIN);

		try {
			DatabaseFactory.init();
		} catch (SQLException e) {
			LOGGER.severe("Failed to initialize the database: " + e.getMessage());
			System.exit(1); // Exit if database initialization fails
		}

		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				displayMenu();
				mode = scanner.nextLine().trim();

				if ("1".equals(mode) || "2".equals(mode) || "3".equals(mode)) {
					promptForAccountDetails(scanner);
				}

				switch (mode) {
					case "1":
						addOrUpdateAccount(username, password, accessLevel);
						break;
					case "2":
						changeAccountLevel(username, accessLevel);
						break;
					case "3":
						deleteAccount(scanner);
						break;
					case "4":
						listAccounts(scanner);
						break;
					case "5":
						System.exit(0);
						break;
					default:
						System.out.println("Invalid choice, please try again.");
				}

				resetInputFields();
			}
		}
	}

	private static void displayMenu() {
		System.out.println("Please choose an option");
		System.out.println();
		System.out.println("1 - Create new account or update existing one (change pass and access level)");
		System.out.println("2 - Change access level");
		System.out.println("3 - Delete existing account");
		System.out.println("4 - List accounts and access levels");
		System.out.println("5 - Exit");
		System.out.print("Your choice: ");
	}

	private static void promptForAccountDetails(Scanner scanner) {
		while (username.isEmpty()) {
			System.out.print("Username: ");
			username = scanner.nextLine().trim().toLowerCase();
		}

		if ("1".equals(mode)) {
			while (password.isEmpty()) {
				System.out.print("Password: ");
				password = scanner.nextLine().trim();
			}
		}

		if ("1".equals(mode) || "2".equals(mode)) {
			while (accessLevel.isEmpty()) {
				System.out.print("Access level: ");
				accessLevel = scanner.nextLine().trim();
			}
		}
	}

	private static void addOrUpdateAccount(String account, String password, String level) {
		String sql = "REPLACE INTO accounts(login, password, accessLevel) VALUES (?, ?, ?)";
		try (Connection con = DatabaseFactory.getConnection();
			 PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, account);
			ps.setString(2, hashPassword(password));
			ps.setString(3, level);
			if (ps.executeUpdate() > 0) {
				System.out.println("Account " + account + " has been created or updated");
			} else {
				System.out.println("Account " + account + " does not exist");
			}
		} catch (SQLException | NoSuchAlgorithmException e) {
			LOGGER.severe("There was an error while adding/updating the account: " + e.getMessage());
		}
	}

	private static void changeAccountLevel(String account, String level) {
		String sql = "UPDATE accounts SET accessLevel = ? WHERE login = ?";
		try (Connection con = DatabaseFactory.getConnection();
			 PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, level);
			ps.setString(2, account);
			if (ps.executeUpdate() > 0) {
				System.out.println("Account " + account + " has been updated");
			} else {
				System.out.println("Account " + account + " does not exist");
			}
		} catch (SQLException e) {
			LOGGER.severe("There was an error while updating the account: " + e.getMessage());
		}
	}

	private static void deleteAccount(Scanner scanner) {
		System.out.println("WARNING: This will not delete the gameserver data (characters, items, etc.)");
		System.out.println("It will only delete the account login server data.");
		System.out.print("Do you really want to delete this account? Y/N: ");
		String confirmation = scanner.nextLine().trim();
		if ("Y".equalsIgnoreCase(confirmation)) {
			try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement ps = con.prepareStatement("DELETE FROM accounts WHERE login = ?")) {
				ps.setString(1, username);
				if (ps.executeUpdate() > 0) {
					System.out.println("Account " + username + " has been deleted");
				} else {
					System.out.println("Account " + username + " does not exist");
				}
			} catch (SQLException e) {
				LOGGER.severe("There was an error while deleting the account: " + e.getMessage());
			}
		} else {
			System.out.println("Deletion cancelled.");
		}
	}

	private static void listAccounts(Scanner scanner) {
		System.out.println("Please choose a listing mode");
		System.out.println();
		System.out.println("1 - Banned accounts only (accessLevel < 0)");
		System.out.println("2 - GM/privileged accounts (accessLevel > 0)");
		System.out.println("3 - Regular accounts only (accessLevel = 0)");
		System.out.println("4 - List all");
		System.out.print("Your choice: ");
		String choice = scanner.nextLine().trim();

		String sql = "SELECT login, accessLevel FROM accounts ";
		switch (choice) {
			case "1":
				sql += "WHERE accessLevel < 0";
				break;
			case "2":
				sql += "WHERE accessLevel > 0";
				break;
			case "3":
				sql += "WHERE accessLevel = 0";
				break;
			case "4":
				// List all, no need to add any where clause
				break;
			default:
				System.out.println("Invalid choice, returning to main menu.");
				return;
		}

		sql += " ORDER BY login ASC";
		try (Connection con = DatabaseFactory.getConnection();
			 PreparedStatement ps = con.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			int count = 0;
			while (rs.next()) {
				System.out.println(rs.getString("login") + " -> " + rs.getInt("accessLevel"));
				count++;
			}
			System.out.println("Displayed accounts: " + count);
		} catch (SQLException e) {
			LOGGER.severe("There was an error while displaying accounts: " + e.getMessage());
		}
	}

	private static String hashPassword(String password) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA");
		byte[] hashedPassword = md.digest(password.getBytes());
		return Base64.getEncoder().encodeToString(hashedPassword);
	}

	private static void resetInputFields() {
		username = "";
		password = "";
		accessLevel = "";
		mode = "";
		System.out.println();
	}
}
