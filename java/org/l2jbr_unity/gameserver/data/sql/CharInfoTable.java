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
package org.l2jbr_unity.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jbr_unity.commons.database.DatabaseFactory;
import org.l2jbr_unity.gameserver.model.actor.Player;

public class CharInfoTable
{
	private static final Logger LOGGER = Logger.getLogger(CharInfoTable.class.getName());
	
	private final Map<Integer, String> _names = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> _accessLevels = new ConcurrentHashMap<>();
	
	protected CharInfoTable()
	{
		try (Connection con = DatabaseFactory.getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT charId, char_name, accesslevel FROM characters"))
		{
			while (rs.next())
			{
				final int id = rs.getInt("charId");
				_names.put(id, rs.getString("char_name"));
				_accessLevels.put(id, rs.getInt("accesslevel"));
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't retrieve all char id/name/access: " + e.getMessage(), e);
		}
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _names.size() + " char names.");
	}
	
	public void addName(Player player)
	{
		if (player != null)
		{
			addName(player.getObjectId(), player.getName());
			_accessLevels.put(player.getObjectId(), player.getAccessLevel().getLevel());
		}
	}
	
	private void addName(int objectId, String name)
	{
		if ((name != null) && !name.equals(_names.get(objectId)))
		{
			_names.put(objectId, name);
		}
	}
	
	public void removeName(int objId)
	{
		_names.remove(objId);
		_accessLevels.remove(objId);
	}
	
	public int getIdByName(String name)
	{
		if ((name == null) || name.isEmpty())
		{
			return -1;
		}
		
		for (Entry<Integer, String> entry : _names.entrySet())
		{
			if (entry.getValue().equalsIgnoreCase(name))
			{
				return entry.getKey();
			}
		}
		
		// Should not continue after the above?
		
		int id = -1;
		int accessLevel = 0;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT charId,accesslevel FROM characters WHERE char_name=?"))
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					id = rs.getInt("charId");
					accessLevel = rs.getInt("accesslevel");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Could not check existing char name: " + e.getMessage(), e);
		}
		
		if (id > 0)
		{
			_names.put(id, name);
			_accessLevels.put(id, accessLevel);
			return id;
		}
		
		return -1; // Not found.
	}
	
	public String getNameById(int id)
	{
		if (id <= 0)
		{
			return null;
		}
		
		String name = _names.get(id);
		if (name != null)
		{
			return name;
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT char_name,accesslevel FROM characters WHERE charId=?"))
		{
			ps.setInt(1, id);
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					name = rset.getString("char_name");
					_names.put(id, name);
					_accessLevels.put(id, rset.getInt("accesslevel"));
					return name;
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Could not check existing char id: " + e.getMessage(), e);
		}
		
		return null; // not found
	}
	
	public int getAccessLevelById(int objectId)
	{
		return getNameById(objectId) != null ? _accessLevels.get(objectId) : 0;
	}
	
	public synchronized boolean doesCharNameExist(String name)
	{
		boolean result = false;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as count FROM characters WHERE char_name=?"))
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					result = rs.getInt("count") > 0;
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Could not check existing charname: " + e.getMessage(), e);
		}
		return result;
	}
	
	public int getAccountCharacterCount(String account)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT COUNT(char_name) as count FROM characters WHERE account_name=?"))
		{
			ps.setString(1, account);
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					return rset.getInt("count");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "Couldn't retrieve account for id: " + e.getMessage(), e);
		}
		return 0;
	}
	
	public static CharInfoTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CharInfoTable INSTANCE = new CharInfoTable();
	}
}
