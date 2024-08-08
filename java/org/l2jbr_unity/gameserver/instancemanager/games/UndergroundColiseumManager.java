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
package org.l2jbr_unity.gameserver.instancemanager.games;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.database.DatabaseFactory;
import org.l2jbr_unity.commons.threads.ThreadPool;
import org.l2jbr_unity.commons.time.SchedulingPattern;
import org.l2jbr_unity.commons.util.IXmlReader;
import org.l2jbr_unity.commons.util.TimeUtil;
import org.l2jbr_unity.gameserver.data.xml.DoorData;
import org.l2jbr_unity.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.actor.instance.Door;
import org.l2jbr_unity.gameserver.model.undergroundColiseum.UCArena;
import org.l2jbr_unity.gameserver.model.undergroundColiseum.UCBestTeam;
import org.l2jbr_unity.gameserver.model.undergroundColiseum.UCPoint;
import org.l2jbr_unity.gameserver.model.undergroundColiseum.UCReward;
import org.l2jbr_unity.gameserver.model.undergroundColiseum.UCTeam;
import org.l2jbr_unity.gameserver.util.Broadcast;

public class UndergroundColiseumManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(UndergroundColiseumManager.class.getName());
	
	private final Map<Integer, UCArena> _arenas = new HashMap<>(5);
	private boolean _isStarted = false;
	private long _periodStartTime;
	private long _periodEndTime;
	private ScheduledFuture<?> _regTask = null;
	private final Map<Integer, UCBestTeam> _bestTeams = new HashMap<>(5);
	
	protected UndergroundColiseumManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_periodStartTime = GlobalVariablesManager.getInstance().getLong("UC_START_TIME", 0);
		_periodEndTime = GlobalVariablesManager.getInstance().getLong("UC_STOP_TIME", 0);
		
		final long curerntTime = System.currentTimeMillis();
		if ((_periodStartTime < curerntTime) && (_periodEndTime < curerntTime))
		{
			generateNewDate();
		}
		
		parseDatapackFile("data/UndergroundColiseum.xml");
		
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _arenas.size() + " coliseum arenas.");
		
		if ((_periodStartTime < curerntTime) && (_periodEndTime > curerntTime))
		{
			switchStatus(true);
		}
		else
		{
			final long nextTime = _periodStartTime - curerntTime;
			_regTask = ThreadPool.schedule(new UCRegistrationTask(true), nextTime);
			LOGGER.info(getClass().getSimpleName() + ": Starts at " + TimeUtil.getDateTimeString(_periodStartTime));
		}
		
		restoreBestTeams();
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		NamedNodeMap map;
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("arena".equalsIgnoreCase(d.getNodeName()))
					{
						map = d.getAttributes();
						final int id = Integer.parseInt(map.getNamedItem("id").getNodeValue());
						final int min_level = Integer.parseInt(map.getNamedItem("minLvl").getNodeValue());
						final int max_level = Integer.parseInt(map.getNamedItem("maxLvl").getNodeValue());
						final int curator = Integer.parseInt(map.getNamedItem("curator").getNodeValue());
						
						final UCArena arena = new UCArena(id, curator, min_level, max_level);
						int index = 0;
						int index2 = 0;
						
						for (Node und = d.getFirstChild(); und != null; und = und.getNextSibling())
						{
							if ("tower".equalsIgnoreCase(und.getNodeName()))
							{
								map = und.getAttributes();
								
								final int npcId = Integer.parseInt(map.getNamedItem("id").getNodeValue());
								final int x = Integer.parseInt(map.getNamedItem("x").getNodeValue());
								final int y = Integer.parseInt(map.getNamedItem("y").getNodeValue());
								final int z = Integer.parseInt(map.getNamedItem("z").getNodeValue());
								
								final UCTeam team = new UCTeam(index, arena, x, y, z, npcId);
								arena.setUCTeam(index, team);
								
								index++;
							}
							else if ("spawn".equalsIgnoreCase(und.getNodeName()))
							{
								map = und.getAttributes();
								final List<Door> doors = new ArrayList<>();
								final String doorList = map.getNamedItem("doors") != null ? map.getNamedItem("doors").getNodeValue() : "";
								if (!doorList.isEmpty())
								{
									final String[] doorSplint = doorList.split(",");
									for (String doorId : doorSplint)
									{
										final Door door = DoorData.getInstance().getDoor(Integer.parseInt(doorId));
										if (door != null)
										{
											doors.add(door);
										}
									}
								}
								final int x = Integer.parseInt(map.getNamedItem("x").getNodeValue());
								final int y = Integer.parseInt(map.getNamedItem("y").getNodeValue());
								final int z = Integer.parseInt(map.getNamedItem("z").getNodeValue());
								
								final UCPoint point = new UCPoint(doors, new Location(x, y, z));
								arena.setUCPoint(index2, point);
								
								index2++;
							}
							else if ("rewards".equalsIgnoreCase(und.getNodeName()))
							{
								for (Node c = und.getFirstChild(); c != null; c = c.getNextSibling())
								{
									if ("item".equalsIgnoreCase(c.getNodeName()))
									{
										final int itemId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
										final long amount = Long.parseLong(c.getAttributes().getNamedItem("amount").getNodeValue());
										final boolean useModifier = c.getAttributes().getNamedItem("useModifers") != null ? Boolean.parseBoolean(c.getAttributes().getNamedItem("useModifers").getNodeValue()) : false;
										arena.setReward(new UCReward(itemId, amount, useModifier));
									}
								}
							}
						}
						_arenas.put(arena.getId(), arena);
					}
				}
			}
		}
	}
	
	private void restoreBestTeams()
	{
		_bestTeams.clear();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM underground_coliseum ORDER BY arenaId");
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				final int arenaId = rset.getInt("arenaId");
				final String leader = rset.getString("leader");
				final int wins = rset.getInt("wins");
				_bestTeams.put(arenaId, new UCBestTeam(arenaId, leader, wins));
			}
		}
		catch (SQLException e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Could not load underground_coliseum table");
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Error while initializing UndergroundColiseumManager: " + e.getMessage());
		}
	}
	
	private void saveBestTeam(UCBestTeam team, boolean isNew)
	{
		if (isNew)
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("INSERT INTO underground_coliseum (`arenaId`, `leader`, `wins`) VALUES (?,?,?) "))
			{
				ps.setInt(1, team.getArenaId());
				ps.setString(2, team.getLeaderName());
				ps.setInt(3, team.getWins());
				ps.executeUpdate();
			}
			catch (SQLException e)
			{
				LOGGER.warning(getClass().getSimpleName() + ": Could not save underground_coliseum: " + e.getMessage());
			}
		}
		else
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement stmt = con.prepareStatement("UPDATE underground_coliseum SET leader = ?, wins = ?  WHERE arenaId = ?");
				stmt.setInt(1, team.getArenaId());
				stmt.setInt(2, team.getWins());
				stmt.setInt(3, team.getArenaId());
				stmt.execute();
				stmt.close();
			}
			catch (Exception e)
			{
				LOGGER.warning(getClass().getSimpleName() + ": could not clean status for underground_coliseum areanaId: " + team.getArenaId() + " in database!");
			}
		}
	}
	
	public UCBestTeam getBestTeam(int arenaId)
	{
		return _bestTeams.get(arenaId);
	}
	
	public void updateBestTeam(int arenaId, String name, int wins)
	{
		if (_bestTeams.containsKey(arenaId))
		{
			final UCBestTeam team = getBestTeam(arenaId);
			if (team != null)
			{
				team.setLeader(name);
				team.setWins(wins);
				saveBestTeam(team, false);
			}
		}
		else
		{
			final UCBestTeam team = new UCBestTeam(arenaId, name, wins);
			_bestTeams.put(arenaId, team);
			saveBestTeam(team, true);
		}
	}
	
	private void generateNewDate()
	{
		final SchedulingPattern timePattern = new SchedulingPattern(Config.UC_START_TIME);
		_periodStartTime = timePattern.next(System.currentTimeMillis());
		_periodEndTime = _periodStartTime + (Config.UC_TIME_PERIOD * 3600000);
		GlobalVariablesManager.getInstance().set("UC_START_TIME", _periodStartTime);
		GlobalVariablesManager.getInstance().set("UC_STOP_TIME", _periodEndTime);
	}
	
	public UCArena getArena(int id)
	{
		return _arenas.get(id);
	}
	
	public void setStarted(boolean started)
	{
		_isStarted = started;
		for (UCArena arena : getAllArenas())
		{
			arena.switchStatus(started);
		}
		
		if (Config.UC_ALLOW_ANNOUNCE)
		{
			if (_isStarted)
			{
				Broadcast.toAllOnlinePlayers("Underground Coliseum has started!");
			}
			else
			{
				Broadcast.toAllOnlinePlayers("Underground Coliseum has stopped!");
			}
		}
	}
	
	public boolean isStarted()
	{
		return _isStarted;
	}
	
	public Collection<UCArena> getAllArenas()
	{
		return _arenas.values();
	}
	
	private void switchStatus(boolean isStart)
	{
		if (_regTask != null)
		{
			_regTask.cancel(false);
			_regTask = null;
		}
		
		setStarted(isStart);
		if (isStart)
		{
			final long nextTime = _periodEndTime - System.currentTimeMillis();
			_regTask = ThreadPool.schedule(new UCRegistrationTask(false), nextTime);
			LOGGER.info(getClass().getSimpleName() + ": Ends at " + TimeUtil.getDateTimeString(_periodEndTime));
		}
		else
		{
			generateNewDate();
			final long nextTime = _periodStartTime - System.currentTimeMillis();
			_regTask = ThreadPool.schedule(new UCRegistrationTask(true), nextTime);
			LOGGER.info(getClass().getSimpleName() + ": Starts at " + TimeUtil.getDateTimeString(_periodStartTime));
		}
	}
	
	public class UCRegistrationTask implements Runnable
	{
		private final boolean _status;
		
		public UCRegistrationTask(boolean status)
		{
			_status = status;
		}
		
		@Override
		public void run()
		{
			switchStatus(_status);
		}
	}
	
	public static UndergroundColiseumManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final UndergroundColiseumManager INSTANCE = new UndergroundColiseumManager();
	}
}
