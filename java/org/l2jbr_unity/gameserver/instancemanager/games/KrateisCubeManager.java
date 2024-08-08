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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.threads.ThreadPool;
import org.l2jbr_unity.commons.util.IXmlReader;
import org.l2jbr_unity.gameserver.data.SpawnTable;
import org.l2jbr_unity.gameserver.data.xml.DoorData;
import org.l2jbr_unity.gameserver.data.xml.SkillData;
import org.l2jbr_unity.gameserver.enums.ChatType;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.Spawn;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.instance.Door;
import org.l2jbr_unity.gameserver.model.krateisCube.KrateiArena;
import org.l2jbr_unity.gameserver.model.krateisCube.KrateiCubePlayer;
import org.l2jbr_unity.gameserver.model.krateisCube.KrateiMsgType;
import org.l2jbr_unity.gameserver.model.krateisCube.KrateisReward;
import org.l2jbr_unity.gameserver.model.skill.Skill;
import org.l2jbr_unity.gameserver.network.NpcStringId;
import org.l2jbr_unity.gameserver.network.serverpackets.CreatureSay;

/**
 * @author LordWinter
 */
public class KrateisCubeManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(KrateisCubeManager.class.getName());
	
	private final Map<Integer, KrateiArena> _arenas = new HashMap<>(3);
	private Npc _manager = null;
	private long _nextMatchTime;
	private boolean _registerActive = false;
	private boolean _isHalfAnHour = false;
	private EventState _state = EventState.REGISTRATION;
	
	private ScheduledFuture<?> _periodTask = null;
	private ScheduledFuture<?> _eventTask = null;
	private ScheduledFuture<?> _msgTask = null;
	
	private enum EventState
	{
		REGISTRATION,
		PREPARING,
		STARTED
	}
	
	public KrateisCubeManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/KrateisCube.xml");
		
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _arenas.size() + " arena templates.");
		
		final Spawn spawn = SpawnTable.getInstance().getAnySpawn(32503);
		if (spawn != null)
		{
			_manager = spawn.getLastSpawn();
			recalcEventTime();
		}
		else
		{
			LOGGER.info(getClass().getSimpleName() + ": Event can't be started because npc not found!");
		}
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
						final int manager = Integer.parseInt(map.getNamedItem("manager").getNodeValue());
						final int minLevel = Integer.parseInt(map.getNamedItem("minLevel").getNodeValue());
						final int maxLevel = Integer.parseInt(map.getNamedItem("maxLevel").getNodeValue());
						final int minPlayers = Integer.parseInt(map.getNamedItem("minPlayers").getNodeValue());
						final int maxPlayers = Integer.parseInt(map.getNamedItem("maxPlayers").getNodeValue());
						
						final KrateiArena arena = new KrateiArena(id, manager, minLevel, maxLevel, minPlayers, maxPlayers);
						final StatSet params = new StatSet();
						
						for (Node kc = d.getFirstChild(); kc != null; kc = kc.getNextSibling())
						{
							if ("doorA".equalsIgnoreCase(kc.getNodeName()))
							{
								map = kc.getAttributes();
								final String doorList = map.getNamedItem("list") != null ? map.getNamedItem("list").getNodeValue() : "";
								if (!doorList.isEmpty())
								{
									final String[] doorSplint = doorList.split(",");
									for (String doorId : doorSplint)
									{
										final Door door = DoorData.getInstance().getDoor(Integer.parseInt(doorId));
										if (door != null)
										{
											arena.addDoorA(door);
										}
									}
								}
							}
							else if ("doorB".equalsIgnoreCase(kc.getNodeName()))
							{
								map = kc.getAttributes();
								final String doorList = map.getNamedItem("list") != null ? map.getNamedItem("list").getNodeValue() : "";
								if (!doorList.isEmpty())
								{
									final String[] doorSplint = doorList.split(",");
									for (String doorId : doorSplint)
									{
										final Door door = DoorData.getInstance().getDoor(Integer.parseInt(doorId));
										if (door != null)
										{
											arena.addDoorB(door);
										}
									}
								}
							}
							else if ("waitLocations".equalsIgnoreCase(kc.getNodeName()))
							{
								for (Node c = kc.getFirstChild(); c != null; c = c.getNextSibling())
								{
									if ("point".equalsIgnoreCase(c.getNodeName()))
									{
										map = c.getAttributes();
										final int x = Integer.parseInt(map.getNamedItem("x").getNodeValue());
										final int y = Integer.parseInt(map.getNamedItem("y").getNodeValue());
										final int z = Integer.parseInt(map.getNamedItem("z").getNodeValue());
										arena.addWaitLoc(new Location(x, y, z));
									}
								}
							}
							else if ("battleLocations".equalsIgnoreCase(kc.getNodeName()))
							{
								for (Node c = kc.getFirstChild(); c != null; c = c.getNextSibling())
								{
									if ("point".equalsIgnoreCase(c.getNodeName()))
									{
										map = c.getAttributes();
										final int x = Integer.parseInt(map.getNamedItem("x").getNodeValue());
										final int y = Integer.parseInt(map.getNamedItem("y").getNodeValue());
										final int z = Integer.parseInt(map.getNamedItem("z").getNodeValue());
										arena.addBattleLoc(new Location(x, y, z));
									}
								}
							}
							else if ("watcherLocations".equalsIgnoreCase(kc.getNodeName()))
							{
								for (Node c = kc.getFirstChild(); c != null; c = c.getNextSibling())
								{
									if ("point".equalsIgnoreCase(c.getNodeName()))
									{
										map = c.getAttributes();
										final int x = Integer.parseInt(map.getNamedItem("x").getNodeValue());
										final int y = Integer.parseInt(map.getNamedItem("y").getNodeValue());
										final int z = Integer.parseInt(map.getNamedItem("z").getNodeValue());
										arena.addWatcherLoc(new Location(x, y, z));
									}
								}
							}
							else if ("buffs".equalsIgnoreCase(kc.getNodeName()))
							{
								for (Node c = kc.getFirstChild(); c != null; c = c.getNextSibling())
								{
									if ("skill".equalsIgnoreCase(c.getNodeName()))
									{
										final int skillId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
										final int level = Integer.parseInt(c.getAttributes().getNamedItem("level").getNodeValue());
										final Skill skill = SkillData.getInstance().getSkill(skillId, level);
										if (skill != null)
										{
											arena.addBuff(skill);
										}
									}
								}
							}
							else if ("rewards".equalsIgnoreCase(kc.getNodeName()))
							{
								for (Node c = kc.getFirstChild(); c != null; c = c.getNextSibling())
								{
									if ("item".equalsIgnoreCase(c.getNodeName()))
									{
										final int itemId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
										final long amount = Long.parseLong(c.getAttributes().getNamedItem("amount").getNodeValue());
										final boolean useModifier = c.getAttributes().getNamedItem("useModifers") != null ? Boolean.parseBoolean(c.getAttributes().getNamedItem("useModifers").getNodeValue()) : false;
										arena.setReward(new KrateisReward(itemId, amount, useModifier));
									}
								}
							}
							else if ("add_parameters".equalsIgnoreCase(kc.getNodeName()))
							{
								for (Node c = kc.getFirstChild(); c != null; c = c.getNextSibling())
								{
									if ("set".equalsIgnoreCase(c.getNodeName()))
									{
										params.set(c.getAttributes().getNamedItem("name").getNodeValue(), c.getAttributes().getNamedItem("value").getNodeValue());
									}
								}
								arena.addParam(params);
							}
						}
						_arenas.put(arena.getId(), arena);
					}
				}
			}
		}
	}
	
	public void prepareEvent()
	{
		if (_registerActive)
		{
			_registerActive = false;
		}
		
		if (_periodTask != null)
		{
			_periodTask.cancel(false);
		}
		
		if (_eventTask != null)
		{
			_eventTask.cancel(false);
		}
		
		if (_msgTask != null)
		{
			_msgTask.cancel(false);
		}
		
		boolean isActive = false;
		for (KrateiArena arena : _arenas.values())
		{
			if ((arena != null) && (arena.getPlayers().size() >= arena.getMinPlayers()))
			{
				int count = 0;
				final List<Player> invalidPlayers = new ArrayList<>();
				for (KrateiCubePlayer pl : arena.getPlayers().values())
				{
					if ((pl != null) && pl.isRegister())
					{
						if (pl.getPlayer().isInsideRadius3D(_manager, Config.ALT_PARTY_RANGE))
						{
							pl.setIsInside(true);
							pl.setIsRegister(false);
							count++;
						}
						else
						{
							invalidPlayers.add(pl.getPlayer());
						}
					}
				}
				
				if (!invalidPlayers.isEmpty())
				{
					for (Player pl : invalidPlayers)
					{
						arena.removePlayer(pl);
					}
				}
				invalidPlayers.clear();
				
				if (count > 1)
				{
					arena.setIsBattleNow(true);
					arena.teleportToBattle(null);
					arena.waitTimeInfo();
					isActive = true;
				}
			}
		}
		
		if (isActive)
		{
			_eventTask = ThreadPool.schedule(this::startEvent, 11000);
			getManagerMessage(KrateiMsgType.STARTED);
		}
		else
		{
			recalcEventTime();
		}
	}
	
	public void startEvent()
	{
		if (_eventTask != null)
		{
			_eventTask.cancel(false);
		}
		
		for (KrateiArena arena : _arenas.values())
		{
			if ((arena != null) && arena.isBattleNow())
			{
				arena.startEvent();
				_state = EventState.STARTED;
			}
		}
		_periodTask = ThreadPool.schedule(this::recalcEventTime, 180000);
	}
	
	public void abortEvent()
	{
		for (KrateiArena arena : _arenas.values())
		{
			if ((arena != null) && arena.isBattleNow())
			{
				arena.endEvent();
			}
		}
		
		if (!_registerActive)
		{
			recalcEventTime();
		}
	}
	
	public boolean isRegisterTime()
	{
		return _registerActive;
	}
	
	public boolean isActive()
	{
		return _state == EventState.STARTED;
	}
	
	public boolean isPreparing()
	{
		return _state == EventState.PREPARING;
	}
	
	public void setIsActivate(boolean val)
	{
		if (!val)
		{
			_state = EventState.REGISTRATION;
		}
	}
	
	public void recalcEventTime()
	{
		if (!_registerActive)
		{
			_registerActive = true;
		}
		
		if (_periodTask != null)
		{
			_periodTask.cancel(false);
		}
		
		if (_eventTask != null)
		{
			_eventTask.cancel(false);
		}
		
		final Calendar calendar = Calendar.getInstance();
		if (calendar.get(Calendar.MINUTE) >= 57)
		{
			calendar.add(Calendar.HOUR, 1);
			calendar.set(Calendar.MINUTE, 27);
		}
		else if ((calendar.get(Calendar.MINUTE) >= 0) && (calendar.get(Calendar.MINUTE) <= 26))
		{
			calendar.set(Calendar.MINUTE, 27);
			calendar.set(Calendar.SECOND, 0);
			_isHalfAnHour = true;
		}
		else
		{
			calendar.set(Calendar.MINUTE, 57);
			calendar.set(Calendar.SECOND, 0);
			_isHalfAnHour = false;
		}
		_nextMatchTime = calendar.getTimeInMillis();
		final long lastTime = _nextMatchTime - System.currentTimeMillis();
		_eventTask = ThreadPool.schedule(this::closeRegistration, lastTime);
		getManagerMessage(KrateiMsgType.INITIALIZED);
		
		final int time = (int) ((lastTime / 1000) / 60);
		if (time >= 5)
		{
			_msgTask = ThreadPool.schedule(() -> getManagerMessage(KrateiMsgType.REGISTRATION_5), lastTime - 300000);
		}
		else if ((time >= 3) && (time < 5))
		{
			_msgTask = ThreadPool.schedule(() -> getManagerMessage(KrateiMsgType.REGISTRATION_3), lastTime - 180000);
		}
		else if ((time >= 1) && (time < 3))
		{
			_msgTask = ThreadPool.schedule(() -> getManagerMessage(KrateiMsgType.REGISTRATION_1), lastTime - 60000);
		}
		
		if (_state != EventState.STARTED)
		{
			_state = EventState.REGISTRATION;
		}
		
		// LOGGER.info(getClass().getSimpleName() + ": Next match " + TimeUtil.getDateTimeString(calendar.getTimeInMillis()));
	}
	
	public void closeRegistration()
	{
		if (_periodTask != null)
		{
			_periodTask.cancel(false);
		}
		_registerActive = false;
		getManagerMessage(KrateiMsgType.PREPATING);
		_state = EventState.PREPARING;
		_periodTask = ThreadPool.schedule(this::prepareEvent, 180000);
	}
	
	private void getManagerMessage(KrateiMsgType state)
	{
		final long lastTime = _nextMatchTime - System.currentTimeMillis();
		switch (state)
		{
			case INITIALIZED:
			{
				final CreatureSay msg = new CreatureSay(_manager, ChatType.NPC_SHOUT, NpcStringId.YOU_HAVE_S1_MINUTE_S_TO_REGISTER_FOR_THE_MATCH);
				msg.addStringParameter(String.valueOf(!_isHalfAnHour ? 57 : 27));
				_manager.broadcastPacket(msg);
				break;
			}
			case REGISTRATION_5:
			{
				_manager.broadcastPacket(new CreatureSay(_manager, ChatType.NPC_SHOUT, NpcStringId.THERE_ARE_5_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEI_S_CUBE_MATCH), 1500);
				_msgTask = ThreadPool.schedule(() -> getManagerMessage(KrateiMsgType.REGISTRATION_3), lastTime - 180000);
				break;
			}
			case REGISTRATION_3:
			{
				_manager.broadcastPacket(new CreatureSay(_manager, ChatType.NPC_SHOUT, NpcStringId.THERE_ARE_3_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEI_S_CUBE_MATCH), 1500);
				_msgTask = ThreadPool.schedule(() -> getManagerMessage(KrateiMsgType.REGISTRATION_1), lastTime - 60000);
				break;
			}
			case REGISTRATION_1:
			{
				_manager.broadcastPacket(new CreatureSay(_manager, ChatType.NPC_SHOUT, NpcStringId.THERE_ARE_1_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEI_S_CUBE_MATCH), 1500);
				break;
			}
			case PREPATING:
			{
				final CreatureSay msg3 = new CreatureSay(_manager, ChatType.NPC_SHOUT, NpcStringId.THE_MATCH_WILL_BEGIN_IN_S1_MINUTE_S);
				msg3.addStringParameter(String.valueOf(3));
				_manager.broadcastPacket(msg3, 1500);
				break;
			}
			case STARTED:
			{
				_manager.broadcastPacket(new CreatureSay(_manager, ChatType.NPC_SHOUT, NpcStringId.THE_MATCH_WILL_BEGIN_SHORTLY), 1500);
				break;
			}
		}
	}
	
	public long getNextMatchTime()
	{
		return _nextMatchTime;
	}
	
	public KrateiArena getArenaId(int id)
	{
		return _arenas.get(id);
	}
	
	public Map<Integer, KrateiArena> getArenas()
	{
		return _arenas;
	}
	
	public static KrateisCubeManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final KrateisCubeManager INSTANCE = new KrateisCubeManager();
	}
}
