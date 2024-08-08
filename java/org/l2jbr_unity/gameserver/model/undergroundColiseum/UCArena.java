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
package org.l2jbr_unity.gameserver.model.undergroundColiseum;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.threads.ThreadPool;
import org.l2jbr_unity.gameserver.data.SpawnTable;
import org.l2jbr_unity.gameserver.enums.Team;
import org.l2jbr_unity.gameserver.enums.TeleportWhereType;
import org.l2jbr_unity.gameserver.model.Party;
import org.l2jbr_unity.gameserver.model.Spawn;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.variables.PlayerVariables;
import org.l2jbr_unity.gameserver.network.NpcStringId;
import org.l2jbr_unity.gameserver.network.serverpackets.ExPVPMatchRecord;
import org.l2jbr_unity.gameserver.network.serverpackets.ExPVPMatchUserDie;
import org.l2jbr_unity.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jbr_unity.gameserver.network.serverpackets.ServerPacket;

public class UCArena
{
	private static final long MINUTES_IN_MILISECONDS = 10 * 60000;
	
	private final int _id;
	private final int _minLevel;
	private final int _maxLevel;
	
	private final UCPoint[] _points = new UCPoint[4];
	private final UCTeam[] _teams = new UCTeam[2];
	private Npc _manager = null;
	
	private ScheduledFuture<?> _taskFuture = null;
	private final List<UCWaiting> _waitingPartys = new CopyOnWriteArrayList<>();
	private final List<UCReward> _rewards = new ArrayList<>();
	private boolean _isBattleNow = false;
	
	public UCArena(int id, int curator, int minLevel, int maxLevel)
	{
		_id = id;
		final Spawn spawn = SpawnTable.getInstance().getAnySpawn(curator);
		if (spawn != null)
		{
			_manager = spawn.getLastSpawn();
		}
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_rewards.clear();
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
	
	public Npc getManager()
	{
		return _manager;
	}
	
	public void setUCPoint(int index, UCPoint point)
	{
		if (index > 4)
		{
			return;
		}
		_points[index] = point;
	}
	
	public void setUCTeam(int index, UCTeam team)
	{
		if (index > 2)
		{
			return;
		}
		_teams[index] = team;
	}
	
	public UCTeam[] getTeams()
	{
		return _teams;
	}
	
	public UCPoint[] getPoints()
	{
		return _points;
	}
	
	public List<UCWaiting> getWaitingList()
	{
		return _waitingPartys;
	}
	
	public void switchStatus(boolean start)
	{
		if ((_taskFuture == null) && start)
		{
			runNewTask(false);
		}
		else
		{
			if (_taskFuture != null)
			{
				_taskFuture.cancel(true);
				_taskFuture = null;
			}
			generateWinner();
			removeTeams();
			for (UCTeam team : getTeams())
			{
				team.cleanUp();
			}
			
			for (UCPoint point : getPoints())
			{
				point.actionDoors(false);
				point.getPlayers().clear();
			}
			_isBattleNow = false;
		}
	}
	
	public void runNewTask(boolean isFullTime)
	{
		final long time = isFullTime ? MINUTES_IN_MILISECONDS : MINUTES_IN_MILISECONDS - 60000L;
		_taskFuture = ThreadPool.schedule(new UCRunningTask(this), time);
	}
	
	public void runTaskNow()
	{
		if (_taskFuture != null)
		{
			_taskFuture.cancel(true);
			_taskFuture = null;
		}
		_taskFuture = ThreadPool.schedule(new UCRunningTask(this), 0);
	}
	
	public void generateWinner()
	{
		final UCTeam blueTeam = _teams[0];
		final UCTeam redTeam = _teams[1];
		UCTeam winnerTeam = null;
		
		if ((blueTeam.getStatus() == UCTeam.WIN) || (redTeam.getStatus() == UCTeam.WIN))
		{
			winnerTeam = blueTeam.getStatus() == UCTeam.WIN ? blueTeam : redTeam;
		}
		else
		{
			if ((blueTeam.getParty() == null) && (redTeam.getParty() != null))
			{
				redTeam.setStatus(UCTeam.WIN);
				winnerTeam = redTeam;
			}
			else if ((redTeam.getParty() == null) && (blueTeam.getParty() != null))
			{
				blueTeam.setStatus(UCTeam.WIN);
				winnerTeam = blueTeam;
			}
			else if ((redTeam.getParty() != null) && (blueTeam.getParty() != null))
			{
				if (blueTeam.getKillCount() > redTeam.getKillCount())
				{
					blueTeam.setStatus(UCTeam.WIN);
					redTeam.setStatus(UCTeam.FAIL);
					winnerTeam = blueTeam;
				}
				else if (redTeam.getKillCount() > blueTeam.getKillCount())
				{
					blueTeam.setStatus(UCTeam.FAIL);
					redTeam.setStatus(UCTeam.WIN);
					winnerTeam = redTeam;
				}
				else if (blueTeam.getKillCount() == redTeam.getKillCount())
				{
					if (blueTeam.getRegisterTime() > redTeam.getRegisterTime())
					{
						blueTeam.setStatus(UCTeam.FAIL);
						redTeam.setStatus(UCTeam.WIN);
						winnerTeam = redTeam;
					}
					else
					{
						blueTeam.setStatus(UCTeam.WIN);
						redTeam.setStatus(UCTeam.FAIL);
						winnerTeam = blueTeam;
					}
				}
			}
		}
		
		if (winnerTeam != null)
		{
			broadcastRecord(ExPVPMatchRecord.FINISH, (winnerTeam.getIndex() + 1));
		}
		else
		{
			broadcastRecord(ExPVPMatchRecord.FINISH, 0);
		}
		blueTeam.setLastParty(redTeam.getParty());
		redTeam.setLastParty(blueTeam.getParty());
	}
	
	public void broadcastToAll(ServerPacket packet)
	{
		for (UCTeam team : getTeams())
		{
			final Party party = team.getParty();
			if (party != null)
			{
				for (Player member : party.getMembers())
				{
					if (member != null)
					{
						member.sendPacket(packet);
					}
				}
			}
		}
	}
	
	public void prepareStart()
	{
		_isBattleNow = true;
		broadcastToAll(new ExShowScreenMessage(NpcStringId.MATCH_BEGINS_IN_S1_MINUTE_S_PLEASE_GATHER_AROUND_THE_ADMINISTRATOR, 2, 5000, "1"));
		try
		{
			Thread.sleep(30000);
		}
		catch (InterruptedException e)
		{
		}
		
		broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECOND_S_REMAINING, 2, 5000, "30"));
		try
		{
			Thread.sleep(20000);
		}
		catch (InterruptedException e)
		{
		}
		
		broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECOND_S_REMAINING, 2, 3000, "10"));
		
		try
		{
			Thread.sleep(5000);
		}
		catch (InterruptedException e)
		{
		}
		
		broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECOND_S_REMAINING, 2, 1000, "5"));
		
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
		}
		
		broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECOND_S_REMAINING, 2, 1000, "4"));
		
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
		}
		
		broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECOND_S_REMAINING, 2, 1000, "3"));
		
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
		}
		
		broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECOND_S_REMAINING, 2, 1000, "2"));
		
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
		}
		
		broadcastToAll(new ExShowScreenMessage(NpcStringId.S1_SECOND_S_REMAINING, 2, 1000, "1"));
		
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
		}
		
		boolean isValid = true;
		for (UCTeam team : _teams)
		{
			if (team.getParty() == null)
			{
				isValid = false;
				continue;
			}
			
			if (team.getParty().getMemberCount() < Config.UC_PARTY_SIZE)
			{
				isValid = false;
				continue;
			}
			
			for (Player pl : team.getParty().getMembers())
			{
				if ((pl != null) && (pl.calculateDistance3D(_manager) > Config.ALT_PARTY_RANGE))
				{
					isValid = false;
					break;
				}
			}
		}
		
		if (!isValid)
		{
			broadcastToAll(new ExShowScreenMessage(NpcStringId.THE_MATCH_IS_AUTOMATICALLY_CANCELED_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_ADMISSION_MANAGER, 2, 5000));
			for (UCTeam team : _teams)
			{
				team.setParty(null);
				team.setRegisterTime(0);
			}
			_isBattleNow = false;
			runNewTask(false);
			return;
		}
		
		runNewTask(true);
		splitMembersAndTeleport();
		startFight();
	}
	
	public void splitMembersAndTeleport()
	{
		final UCPoint[] positions = getPoints();
		for (UCPoint point : positions)
		{
			point.getPlayers().clear();
		}
		
		broadcastRecord(ExPVPMatchRecord.START, 0);
		
		for (UCTeam team : getTeams())
		{
			final Party party = team.getParty();
			if (party != null)
			{
				int i = 0;
				for (Player player : party.getMembers())
				{
					if (player != null)
					{
						player.setUCState(Player.UC_STATE_POINT);
						positions[i].teleportPlayer(player);
						i++;
						if (i >= 3)
						{
							i = 0;
						}
					}
				}
			}
		}
		broadcastRecord(ExPVPMatchRecord.UPDATE, 0);
	}
	
	public void broadcastRecord(int type, int teamType)
	{
		final ExPVPMatchRecord packet = new ExPVPMatchRecord(type, teamType, this);
		final ExPVPMatchUserDie packet2 = type == ExPVPMatchRecord.UPDATE ? new ExPVPMatchUserDie(this) : null;
		for (UCTeam team : getTeams())
		{
			final Party party = team.getParty();
			if (party != null)
			{
				for (Player member : party.getMembers())
				{
					if (member != null)
					{
						member.sendPacket(packet);
						if (packet2 != null)
						{
							member.sendPacket(packet2);
						}
					}
				}
			}
		}
	}
	
	public void startFight()
	{
		for (UCTeam team : _teams)
		{
			team.spawnTower();
			for (Player player : team.getParty().getMembers())
			{
				if (player != null)
				{
					player.setTeam(team.getIndex() == 1 ? Team.BLUE : Team.RED);
				}
			}
		}
	}
	
	public void removeTeams()
	{
		for (UCTeam team : _teams)
		{
			if (team.getParty() != null)
			{
				for (Player player : team.getParty().getMembers())
				{
					if (player == null)
					{
						continue;
					}
					
					player.setTeam(Team.NONE);
					player.cleanUCStats();
					player.setUCState(Player.UC_STATE_NONE);
					if (player.isDead())
					{
						UCTeam.resPlayer(player);
					}
					
					final String restore = player.getVariables().getString(PlayerVariables.RESTORE_LOCATION, "");
					if (!restore.isEmpty())
					{
						final String[] split = restore.split(";");
						player.teleToLocation(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), true);
					}
					else
					{
						player.teleToLocation(TeleportWhereType.TOWN);
					}
				}
			}
		}
	}
	
	public boolean isBattleNow()
	{
		return _isBattleNow;
	}
	
	public void setIsBattleNow(boolean value)
	{
		_isBattleNow = value;
	}
	
	public void setReward(UCReward ucReward)
	{
		_rewards.add(ucReward);
	}
	
	public List<UCReward> getRewards()
	{
		return _rewards;
	}
}
