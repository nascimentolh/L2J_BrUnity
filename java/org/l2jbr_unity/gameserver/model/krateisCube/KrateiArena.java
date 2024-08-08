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
package org.l2jbr_unity.gameserver.model.krateisCube;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.threads.ThreadPool;
import org.l2jbr_unity.commons.util.Rnd;
import org.l2jbr_unity.gameserver.data.SpawnTable;
import org.l2jbr_unity.gameserver.instancemanager.games.KrateisCubeManager;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.Spawn;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.instance.Door;
import org.l2jbr_unity.gameserver.model.actor.instance.KrateisMatchManager;
import org.l2jbr_unity.gameserver.model.events.AbstractScript;
import org.l2jbr_unity.gameserver.model.skill.Skill;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.ExPCCafePointInfo;
import org.l2jbr_unity.gameserver.network.serverpackets.ExPVPMatchCCMyRecord;
import org.l2jbr_unity.gameserver.network.serverpackets.ExPVPMatchCCRecord;
import org.l2jbr_unity.gameserver.network.serverpackets.ExPVPMatchCCRetire;
import org.l2jbr_unity.gameserver.network.serverpackets.SystemMessage;

/**
 * @author LordWinter
 */
public class KrateiArena
{
	private final int _id;
	private final int _minLevel;
	private final int _maxLevel;
	private final int _minPlayers;
	private final int _maxPlayers;
	private KrateisMatchManager _manager = null;
	private final Map<Player, KrateiCubePlayer> _players = new ConcurrentHashMap<>();
	private final List<Skill> _buffs = new ArrayList<>();
	private final List<Door> _doorListA = new ArrayList<>();
	private final List<Door> _doorListB = new ArrayList<>();
	private final List<Location> _waitLoc = new ArrayList<>();
	private final List<Location> _battleLoc = new ArrayList<>();
	private final List<Location> _watcherLoc = new ArrayList<>();
	private final List<KrateisReward> _rewards = new ArrayList<>();
	private final List<Npc> _watchers = new ArrayList<>();
	
	private StatSet _params;
	
	private ScheduledFuture<?> _eventTask = null;
	private ScheduledFuture<?> _timeTask = null;
	private ScheduledFuture<?> _watcherTask = null;
	private ScheduledFuture<?> _doorTask = null;
	
	private boolean _doorsRotation = false;
	private boolean _watcherRotation = false;
	private boolean _isBattleNow = false;
	private boolean _isActiveNow = false;
	
	public KrateiArena(int id, int manager, int minLevel, int maxLevel, int minPlayers, int maxPlayers)
	{
		_id = id;
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_minPlayers = minPlayers;
		_maxPlayers = maxPlayers;
		_rewards.clear();
		
		final Spawn spawn = SpawnTable.getInstance().getAnySpawn(manager);
		if (spawn != null)
		{
			_manager = (KrateisMatchManager) spawn.getLastSpawn();
			_manager.setArenaId(getId());
		}
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public int getMinPlayers()
	{
		return _minPlayers;
	}
	
	public int getMaxPlayers()
	{
		return _maxPlayers;
	}
	
	public KrateisMatchManager getManager()
	{
		return _manager;
	}
	
	public boolean isBattleNow()
	{
		return _isBattleNow;
	}
	
	public void setIsBattleNow(boolean value)
	{
		_isBattleNow = value;
	}
	
	public boolean isActiveNow()
	{
		return _isActiveNow;
	}
	
	public void setReward(KrateisReward ucReward)
	{
		_rewards.add(ucReward);
	}
	
	public List<KrateisReward> getRewards()
	{
		return _rewards;
	}
	
	public void addBuff(Skill skill)
	{
		_buffs.add(skill);
	}
	
	public List<Skill> getBuffList()
	{
		return _buffs;
	}
	
	public void addDoorA(Door door)
	{
		_doorListA.add(door);
	}
	
	public List<Door> getDoorListA()
	{
		return _doorListA;
	}
	
	public void addDoorB(Door door)
	{
		_doorListB.add(door);
	}
	
	public List<Door> getDoorListB()
	{
		return _doorListB;
	}
	
	public List<Location> getWaitLoc()
	{
		return _waitLoc;
	}
	
	public void addWaitLoc(Location loc)
	{
		_waitLoc.add(loc);
	}
	
	public List<Location> getBattleLoc()
	{
		return _battleLoc;
	}
	
	public void addBattleLoc(Location loc)
	{
		_battleLoc.add(loc);
	}
	
	public List<Location> getWatcherLoc()
	{
		return _watcherLoc;
	}
	
	public void addWatcherLoc(Location loc)
	{
		_watcherLoc.add(loc);
	}
	
	public void addParam(StatSet params)
	{
		_params = params;
	}
	
	public StatSet getParams()
	{
		return _params;
	}
	
	public boolean addRegisterPlayer(Player player)
	{
		if (!_players.containsKey(player))
		{
			final KrateiCubePlayer pl = _players.computeIfAbsent(player, data -> new KrateiCubePlayer(player));
			_players.put(player, pl);
			pl.setIsRegister(true);
			player.setKrateiArena(this);
			player.setRegisteredOnEvent(true);
			return true;
		}
		return false;
	}
	
	public boolean removePlayer(Player player)
	{
		if (_players.containsKey(player))
		{
			_players.remove(player);
			player.setKrateiArena(null);
			player.setRegisteredOnEvent(false);
			return true;
		}
		return false;
	}
	
	public boolean isRegisterPlayer(Player player)
	{
		return _players.containsKey(player);
	}
	
	public Map<Player, KrateiCubePlayer> getPlayers()
	{
		return _players;
	}
	
	public void startEvent()
	{
		if (_eventTask != null)
		{
			_eventTask.cancel(false);
		}
		
		if (_timeTask != null)
		{
			_timeTask.cancel(false);
		}
		
		if (_watcherTask != null)
		{
			_watcherTask.cancel(false);
		}
		
		if (_doorTask != null)
		{
			_doorTask.cancel(false);
		}
		
		_doorsRotation = true;
		changeDoorStatus(true);
		
		if (!getWatcherLoc().isEmpty())
		{
			for (Location loc : getWatcherLoc())
			{
				if (loc != null)
				{
					final Npc npc = AbstractScript.addSpawn(18601, loc, false, 0);
					npc.disableCoreAI(true);
					npc.setImmobilized(true);
					_watchers.add(npc);
				}
			}
			_watcherRotation = true;
		}
		teleportAllPlayers();
		final long time = 1200000;
		_eventTask = ThreadPool.schedule(this::endEvent, time);
		_timeTask = ThreadPool.schedule(this::timeLeftInfo, time - 11000);
		_watcherTask = ThreadPool.schedule(this::changeWatchers, getParams().getInt("watcherRotationTime") * 1000);
		_isActiveNow = true;
	}
	
	public void endEvent()
	{
		if (_eventTask != null)
		{
			_eventTask.cancel(true);
			_eventTask = null;
		}
		
		if (_watcherTask != null)
		{
			_watcherTask.cancel(true);
			_watcherTask = null;
		}
		
		if (_doorTask != null)
		{
			_doorTask.cancel(true);
			_doorTask = null;
		}
		_watcherRotation = false;
		changeDoorStatus(false);
		
		if (!_watchers.isEmpty())
		{
			for (Npc npc : _watchers)
			{
				if (npc != null)
				{
					npc.deleteMe();
				}
			}
			_watchers.clear();
		}
		
		final Set<Player> participants = getPlayers().keySet();
		if ((participants != null) && !participants.isEmpty())
		{
			for (Player player : participants)
			{
				if (player != null)
				{
					removeAllEffects(player);
					player.teleToLocation(-70381, -70937, -1428, 0, true);
					player.setKrateiArena(null);
					player.sendPacket(ExPVPMatchCCRetire.STATIC);
					player.setCanRevive(true);
				}
			}
			checkRewardPlayers(getPlayers());
		}
		_players.clear();
		_isBattleNow = false;
		_isActiveNow = false;
		KrateisCubeManager.getInstance().setIsActivate(false);
	}
	
	private void changeWatchers()
	{
		if (_watcherTask != null)
		{
			_watcherTask.cancel(true);
			_watcherTask = null;
		}
		
		if (!_watchers.isEmpty())
		{
			for (Npc npc : _watchers)
			{
				if (npc != null)
				{
					npc.deleteMe();
				}
			}
			_watchers.clear();
		}
		
		if (!getWatcherLoc().isEmpty())
		{
			for (Location loc : getWatcherLoc())
			{
				if (loc != null)
				{
					final Npc npc = AbstractScript.addSpawn(_watcherRotation ? 18602 : 18601, loc, false, 0);
					npc.disableCoreAI(true);
					npc.setImmobilized(true);
					_watchers.add(npc);
				}
			}
		}
		
		if (_watcherRotation)
		{
			_watcherRotation = false;
		}
		else
		{
			_watcherRotation = true;
		}
		_watcherTask = ThreadPool.schedule(this::changeWatchers, getParams().getInt("watcherRotationTime") * 1000);
	}
	
	public void chaneWatcher(Npc npc)
	{
		if (_watchers.contains(npc))
		{
			npc.deleteMe();
			final Npc newNpc = AbstractScript.addSpawn(npc.getId() == 18602 ? 18601 : 18602, npc.getLocation(), false, 0);
			newNpc.disableCoreAI(true);
			newNpc.setImmobilized(true);
			_watchers.add(newNpc);
			_watchers.remove(npc);
		}
	}
	
	private Map<Player, Integer> getParticipants()
	{
		final Map<Player, Integer> participants = new HashMap<>();
		if (!getPlayers().isEmpty())
		{
			for (Player player : getPlayers().keySet())
			{
				if ((player != null) && !participants.containsKey(player))
				{
					participants.put(player, getPoints(player));
				}
			}
			
			if (participants.isEmpty())
			{
				return null;
			}
		}
		final Map<Player, Integer> sortedMap = participants.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
	}
	
	private void teleportAllPlayers()
	{
		if (!getPlayers().isEmpty())
		{
			final Map<Player, Integer> participants = getParticipants();
			if (participants == null)
			{
				return;
			}
			
			final ExPVPMatchCCRecord p = new ExPVPMatchCCRecord(ExPVPMatchCCRecord.UPDATE, participants);
			for (Player player : getPlayers().keySet())
			{
				if (player != null)
				{
					player.teleToLocation(getBattleLoc().get(Rnd.get(getBattleLoc().size())), true);
					addEffects(player);
					player.sendPacket(new ExPVPMatchCCMyRecord(getPoints(player)));
					player.sendPacket(p);
					player.setCanRevive(false);
				}
			}
		}
	}
	
	public void teleportToBattle(Player player)
	{
		if (player != null)
		{
			if (getPlayers().containsKey(player))
			{
				player.teleToLocation(getBattleLoc().get(Rnd.get(getBattleLoc().size())), true);
			}
		}
		else
		{
			if (!getPlayers().isEmpty())
			{
				for (Player pl : getPlayers().keySet())
				{
					if (pl != null)
					{
						teleportToWaitingRoom(pl, false);
					}
				}
			}
		}
	}
	
	public void teleportToWaitingRoom(Player player, boolean restoreEffects)
	{
		if (getPlayers().containsKey(player))
		{
			if (restoreEffects)
			{
				respawnEffects(player);
			}
			player.teleToLocation(getWaitLoc().get(Rnd.get(getWaitLoc().size())), true);
		}
	}
	
	private void changeDoorStatus(boolean active)
	{
		if (_doorTask != null)
		{
			_doorTask.cancel(false);
		}
		
		if (_doorsRotation)
		{
			_doorsRotation = false;
		}
		else
		{
			_doorsRotation = true;
		}
		
		if (!getDoorListA().isEmpty() && !getDoorListB().isEmpty())
		{
			final List<Door> doorsA = (_doorsRotation) ? getDoorListA() : getDoorListB();
			final List<Door> doorsB = (_doorsRotation) ? getDoorListB() : getDoorListA();
			if (active)
			{
				changeDoorsStatus(doorsA, true);
				changeDoorsStatus(doorsB, false);
			}
			else
			{
				changeDoorsStatus(doorsA, false);
				changeDoorsStatus(doorsB, false);
			}
		}
		_doorTask = ThreadPool.schedule(() -> changeDoorStatus(active), (getParams().getInt("doorRotationTime") * 1000));
	}
	
	private void changeDoorsStatus(List<Door> doors, boolean isOpen)
	{
		for (Door door : doors)
		{
			if (door != null)
			{
				if (isOpen)
				{
					door.openMe();
				}
				else
				{
					door.closeMe();
				}
			}
		}
	}
	
	public void addEffects(Player player)
	{
		player.setCurrentHp(player.getMaxHp());
		player.setCurrentCp(player.getMaxCp());
		player.setCurrentMp(player.getMaxMp());
		
		if (player.getSummon() != null)
		{
			player.getSummon().setCurrentHp(player.getSummon().getMaxHp());
			player.getSummon().setCurrentCp(player.getSummon().getMaxCp());
			player.getSummon().setCurrentMp(player.getSummon().getMaxMp());
		}
		
		if (!getBuffList().isEmpty())
		{
			for (Skill skill : getBuffList())
			{
				if (skill != null)
				{
					skill.applyEffects(player, player);
					if (player.getSummon() != null)
					{
						skill.applyEffects(player.getSummon(), player.getSummon());
					}
				}
			}
		}
		
		player.broadcastUserInfo();
		
		if (player.getSummon() != null)
		{
			player.getSummon().broadcastInfo();
		}
	}
	
	private void removeAllEffects(Player player)
	{
		player.getEffectList().stopAllEffects();
		player.stopAllEffects();
		if (player.getSummon() != null)
		{
			player.getSummon().stopAllEffects();
		}
		
		player.broadcastUserInfo();
		
		if (player.getSummon() != null)
		{
			player.getSummon().broadcastInfo();
		}
	}
	
	private void respawnEffects(Player player)
	{
		removeAllEffects(player);
		
		if (player.isDead())
		{
			player.doRevive(100.);
		}
		player.broadcastStatusUpdate();
		player.broadcastUserInfo();
	}
	
	private void checkRewardPlayers(Map<Player, KrateiCubePlayer> players)
	{
		final List<KrateiCubePlayer> playerList = new ArrayList<>();
		for (KrateiCubePlayer info : players.values())
		{
			if (info != null)
			{
				playerList.add(info);
			}
		}
		
		final Comparator<KrateiCubePlayer> statsComparator = new SortPointInfo();
		Collections.sort(playerList, statsComparator);
		
		ExPVPMatchCCRecord p = null;
		final Map<Player, Integer> participants = getParticipants();
		if (participants != null)
		{
			p = new ExPVPMatchCCRecord(ExPVPMatchCCRecord.INITIALIZE, participants);
		}
		
		double dif = 0.05;
		int pos = 0;
		for (KrateiCubePlayer pl : playerList)
		{
			if ((pl != null) && (pl.getPlayer() != null))
			{
				pos++;
				if (p != null)
				{
					pl.getPlayer().sendPacket(p);
				}
				
				if (pl.getPoints() >= getParams().getInt("minPoints"))
				{
					final int count = (int) (pl.getPoints() * dif * (1.0 + ((players.size() / pos) * 0.04)));
					dif -= 0.0016;
					if (count > 0)
					{
						final int exp = count * getParams().getInt("expModifier");
						final int sp = count * getParams().getInt("spModifier");
						pl.getPlayer().addExpAndSp(exp, sp);
						if (!getRewards().isEmpty())
						{
							for (KrateisReward reward : getRewards())
							{
								if (reward != null)
								{
									long amount = reward.isAllowMidifier() ? count : reward.getAmount();
									if (reward.getId() == -100)
									{
										if ((pl.getPlayer().getPcCafePoints() + amount) > Config.PC_CAFE_MAX_POINTS)
										{
											amount = Config.PC_CAFE_MAX_POINTS - pl.getPlayer().getPcCafePoints();
										}
										final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
										sm.addInt((int) amount);
										pl.getPlayer().sendPacket(sm);
										pl.getPlayer().setPcCafePoints((int) (pl.getPlayer().getPcCafePoints() + amount));
										pl.getPlayer().sendPacket(new ExPCCafePointInfo(pl.getPlayer().getPcCafePoints(), (int) amount, 1));
									}
									else if (reward.getId() == -200)
									{
										if (pl.getPlayer().getClan() != null)
										{
											pl.getPlayer().getClan().addReputationScore((int) amount);
										}
									}
									else if (reward.getId() == -300)
									{
										pl.getPlayer().setFame((int) (pl.getPlayer().getFame() + amount));
										final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_REPUTATION);
										sm.addInt((int) amount);
										pl.getPlayer().sendPacket(sm);
										pl.getPlayer().updateUserInfo();
									}
									else if (reward.getId() > 0)
									{
										pl.getPlayer().addItem("Krateis Cube Reward", reward.getId(), amount, pl.getPlayer(), true);
									}
								}
							}
						}
					}
				}
			}
		}
		playerList.clear();
	}
	
	private class SortPointInfo implements Comparator<KrateiCubePlayer>
	{
		@Override
		public int compare(KrateiCubePlayer o1, KrateiCubePlayer o2)
		{
			return Integer.compare(o2.getPoints(), o1.getPoints());
		}
	}
	
	public void addPoints(Player player, boolean isPlayer)
	{
		final KrateiCubePlayer krateiPlayer = getPlayers().get(player);
		if (krateiPlayer != null)
		{
			krateiPlayer.addPoints(krateiPlayer.getPoints() + (isPlayer ? getParams().getInt("pointPerPlayer") : getParams().getInt("pointPerMob")));
			krateiPlayer.getPlayer().sendPacket(new ExPVPMatchCCMyRecord(krateiPlayer.getPoints()));
			
			final Map<Player, Integer> participants = getParticipants();
			if (participants != null)
			{
				final ExPVPMatchCCRecord p = new ExPVPMatchCCRecord(ExPVPMatchCCRecord.UPDATE, participants);
				if (!getPlayers().isEmpty())
				{
					for (Player pl : getPlayers().keySet())
					{
						if (pl != null)
						{
							pl.sendPacket(p);
						}
					}
				}
			}
		}
	}
	
	public int getPoints(Player player)
	{
		int kills = 0;
		final KrateiCubePlayer pl = getPlayers().get(player);
		if (pl != null)
		{
			kills = pl.getPoints();
		}
		return kills;
	}
	
	private void timeLeftInfo()
	{
		if (_timeTask != null)
		{
			_timeTask.cancel(true);
			_timeTask = null;
		}
		
		if (!getPlayers().isEmpty())
		{
			ThreadPool.schedule(new MessageTask(getPlayers(), 10, 1), 0);
		}
	}
	
	public void waitTimeInfo()
	{
		if (!getPlayers().isEmpty())
		{
			ThreadPool.schedule(new MessageTask(getPlayers(), 10, 0), 0);
		}
	}
	
	public class MessageTask implements Runnable
	{
		private final Map<Player, KrateiCubePlayer> _players;
		private int _time;
		private final int _type;
		
		public MessageTask(Map<Player, KrateiCubePlayer> players, int time, int type)
		{
			_players = players;
			_time = time;
			_type = type;
		}
		
		@Override
		public void run()
		{
			if (((_players != null) && !_players.isEmpty()) && (_time > 0))
			{
				SystemMessage msg = null;
				switch (_type)
				{
					case 0:
					{
						msg = new SystemMessage(SystemMessageId.THE_MATCH_WILL_START_IN_S1_SECOND_S).addInt(_time);
						break;
					}
					case 1:
					{
						msg = new SystemMessage(SystemMessageId.THE_GAME_WILL_END_IN_S1_SECOND_S_2).addInt(_time);
						break;
					}
				}
				
				for (Player player : _players.keySet())
				{
					if ((player != null) && (msg != null))
					{
						player.sendPacket(msg);
					}
				}
				
				_time--;
				if (_time > 0)
				{
					ThreadPool.schedule(new MessageTask(_players, _time, _type), 1000);
				}
			}
		}
	}
	
	public void setRespawnTask(Player player)
	{
		if (player != null)
		{
			ThreadPool.schedule(new RespawnTask(player, getParams().getInt("respawnTime")), 0);
		}
	}
	
	private class RespawnTask implements Runnable
	{
		private final Player _player;
		private int _time;
		
		public RespawnTask(Player player, int time)
		{
			_player = player;
			_time = time;
		}
		
		@Override
		public void run()
		{
			if ((_player != null) && (_time > 0))
			{
				_player.sendPacket(new SystemMessage(SystemMessageId.RESURRECTION_WILL_TAKE_PLACE_IN_THE_WAITING_ROOM_AFTER_S1_SECONDS).addInt(_time));
				_time--;
				if (_time > 0)
				{
					ThreadPool.schedule(new RespawnTask(_player, _time), 1000);
				}
				else
				{
					respawnEffects(_player);
					_player.teleToLocation(getWaitLoc().get(Rnd.get(getWaitLoc().size())), true);
				}
			}
		}
	}
}