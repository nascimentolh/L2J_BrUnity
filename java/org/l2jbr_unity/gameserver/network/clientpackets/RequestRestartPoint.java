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
package org.l2jbr_unity.gameserver.network.clientpackets;

import org.l2jbr_unity.commons.threads.ThreadPool;
import org.l2jbr_unity.gameserver.data.sql.ClanHallTable;
import org.l2jbr_unity.gameserver.enums.TeleportWhereType;
import org.l2jbr_unity.gameserver.instancemanager.CHSiegeManager;
import org.l2jbr_unity.gameserver.instancemanager.CastleManager;
import org.l2jbr_unity.gameserver.instancemanager.FortManager;
import org.l2jbr_unity.gameserver.instancemanager.MapRegionManager;
import org.l2jbr_unity.gameserver.instancemanager.TerritoryWarManager;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.instance.SiegeFlag;
import org.l2jbr_unity.gameserver.model.events.EventType;
import org.l2jbr_unity.gameserver.model.events.listeners.AbstractEventListener;
import org.l2jbr_unity.gameserver.model.quest.Event;
import org.l2jbr_unity.gameserver.model.residences.ClanHall;
import org.l2jbr_unity.gameserver.model.siege.Castle;
import org.l2jbr_unity.gameserver.model.siege.Fort;
import org.l2jbr_unity.gameserver.model.siege.SiegeClan;
import org.l2jbr_unity.gameserver.model.siege.clanhalls.SiegableHall;
import org.l2jbr_unity.gameserver.network.PacketLogger;

/**
 * @version $Revision: 1.7.2.3.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRestartPoint extends ClientPacket
{
	protected int _requestedPointType;
	protected boolean _continuation;
	
	@Override
	protected void readImpl()
	{
		_requestedPointType = readInt();
	}
	
	class DeathTask implements Runnable
	{
		final Player _player;
		
		DeathTask(Player player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			portPlayer(_player);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!player.canRevive())
		{
			return;
		}
		
		if (player.isFakeDeath())
		{
			player.stopFakeDeath(true);
			return;
		}
		else if (!player.isDead())
		{
			return;
		}
		
		// Custom event resurrection management.
		if (player.isOnEvent())
		{
			for (AbstractEventListener listener : player.getListeners(EventType.ON_CREATURE_DEATH))
			{
				if (listener.getOwner() instanceof Event)
				{
					((Event) listener.getOwner()).notifyEvent("ResurrectPlayer", null, player);
					return;
				}
			}
		}
		
		final Castle castle = CastleManager.getInstance().getCastle(player.getX(), player.getY(), player.getZ());
		if ((castle != null) && castle.getSiege().isInProgress() && (player.getClan() != null) && castle.getSiege().checkIsAttacker(player.getClan()))
		{
			// Schedule respawn delay for attacker
			ThreadPool.schedule(new DeathTask(player), castle.getSiege().getAttackerRespawnDelay());
			if (castle.getSiege().getAttackerRespawnDelay() > 0)
			{
				player.sendMessage("You will be re-spawned in " + (castle.getSiege().getAttackerRespawnDelay() / 1000) + " seconds");
			}
			return;
		}
		
		portPlayer(player);
	}
	
	protected void portPlayer(Player player)
	{
		Location loc = null;
		Castle castle = null;
		Fort fort = null;
		SiegableHall hall = null;
		final boolean isInDefense = false;
		int instanceId = 0;
		
		// force jail
		if (player.isJailed())
		{
			_requestedPointType = 27;
		}
		else if (player.isFestivalParticipant())
		{
			_requestedPointType = 5;
		}
		switch (_requestedPointType)
		{
			case 1: // to clanhall
			{
				if ((player.getClan() == null) || (player.getClan().getHideoutId() == 0))
				{
					PacketLogger.warning("Player [" + player.getName() + "] called RestartPointPacket - To Clanhall and he doesn't have Clanhall!");
					return;
				}
				loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.CLANHALL);
				if ((ClanHallTable.getInstance().getClanHallByOwner(player.getClan()) != null) && (ClanHallTable.getInstance().getClanHallByOwner(player.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP) != null))
				{
					player.restoreExp(ClanHallTable.getInstance().getClanHallByOwner(player.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP).getLevel());
				}
				break;
			}
			case 2: // to castle
			{
				castle = CastleManager.getInstance().getCastle(player);
				if ((castle != null) && castle.getSiege().isInProgress())
				{
					// Siege in progress
					if (castle.getSiege().checkIsDefender(player.getClan()))
					{
						loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.CASTLE);
					}
					else if (castle.getSiege().checkIsAttacker(player.getClan()))
					{
						loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
					}
					else
					{
						PacketLogger.warning("Player [" + player.getName() + "] called RestartPointPacket - To Castle and he doesn't have Castle!");
						return;
					}
				}
				else
				{
					if ((player.getClan() == null) || (player.getClan().getCastleId() == 0))
					{
						return;
					}
					loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.CASTLE);
				}
				if ((CastleManager.getInstance().getCastleByOwner(player.getClan()) != null) && (CastleManager.getInstance().getCastleByOwner(player.getClan()).getFunction(Castle.FUNC_RESTORE_EXP) != null))
				{
					player.restoreExp(CastleManager.getInstance().getCastleByOwner(player.getClan()).getFunction(Castle.FUNC_RESTORE_EXP).getLvl());
				}
				break;
			}
			case 3: // to fortress
			{
				if (((player.getClan() == null) || (player.getClan().getFortId() == 0)) && !isInDefense)
				{
					PacketLogger.warning("Player [" + player.getName() + "] called RestartPointPacket - To Fortress and he doesn't have Fortress!");
					return;
				}
				loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.FORTRESS);
				if ((FortManager.getInstance().getFortByOwner(player.getClan()) != null) && (FortManager.getInstance().getFortByOwner(player.getClan()).getFunction(Fort.FUNC_RESTORE_EXP) != null))
				{
					player.restoreExp(FortManager.getInstance().getFortByOwner(player.getClan()).getFunction(Fort.FUNC_RESTORE_EXP).getLevel());
				}
				break;
			}
			case 4: // to siege HQ
			{
				SiegeClan siegeClan = null;
				castle = CastleManager.getInstance().getCastle(player);
				fort = FortManager.getInstance().getFort(player);
				hall = CHSiegeManager.getInstance().getNearbyClanHall(player);
				SiegeFlag flag = TerritoryWarManager.getInstance().getHQForClan(player.getClan());
				if (flag == null)
				{
					flag = TerritoryWarManager.getInstance().getFlagForClan(player.getClan());
				}
				
				if ((castle != null) && castle.getSiege().isInProgress())
				{
					siegeClan = castle.getSiege().getAttackerClan(player.getClan());
				}
				else if ((fort != null) && fort.getSiege().isInProgress())
				{
					siegeClan = fort.getSiege().getAttackerClan(player.getClan());
				}
				else if ((hall != null) && hall.isInSiege())
				{
					siegeClan = hall.getSiege().getAttackerClan(player.getClan());
				}
				if (((siegeClan == null) || siegeClan.getFlag().isEmpty()) && (flag == null))
				{
					// Check if clan hall has inner spawns loc
					if (hall != null)
					{
						loc = hall.getSiege().getInnerSpawnLoc(player);
						if (loc != null)
						{
							break;
						}
					}
					
					PacketLogger.warning("Player [" + player.getName() + "] called RestartPointPacket - To Siege HQ and he doesn't have Siege HQ!");
					return;
				}
				loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.SIEGEFLAG);
				break;
			}
			case 5: // Fixed or Player is a festival participant
			{
				if (!player.isGM() && !player.isFestivalParticipant() && !player.getInventory().haveItemForSelfResurrection())
				{
					PacketLogger.warning("Player [" + player.getName() + "] called RestartPointPacket - Fixed and he isn't festival participant!");
					return;
				}
				if (player.isGM() || player.destroyItemByItemId("Feather", 10649, 1, player, false) || player.destroyItemByItemId("Feather", 13300, 1, player, false) || player.destroyItemByItemId("Feather", 13128, 1, player, false))
				{
					player.doRevive(100.00);
				}
				else
				// Festival Participant
				{
					instanceId = player.getInstanceId();
					loc = new Location(player);
				}
				break;
			}
			case 6: // TODO: agathion ress
			{
				break;
			}
			case 27: // to jail
			{
				if (!player.isJailed())
				{
					return;
				}
				loc = new Location(-114356, -249645, -2984);
				break;
			}
			default:
			{
				loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
				break;
			}
		}
		
		// Teleport and revive
		if (loc != null)
		{
			player.setInstanceId(instanceId);
			player.setIn7sDungeon(false);
			player.setIsPendingRevive(true);
			player.teleToLocation(loc, true);
		}
	}
}
