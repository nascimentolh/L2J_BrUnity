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

import java.util.logging.Logger;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.gameserver.enums.TeleportWhereType;
import org.l2jbr_unity.gameserver.instancemanager.InstanceManager;
import org.l2jbr_unity.gameserver.instancemanager.MapRegionManager;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.Party;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.instancezone.Instance;
import org.l2jbr_unity.gameserver.model.olympiad.OlympiadManager;
import org.l2jbr_unity.gameserver.model.sevensigns.SevenSignsFestival;
import org.l2jbr_unity.gameserver.model.variables.PlayerVariables;
import org.l2jbr_unity.gameserver.network.ConnectionState;
import org.l2jbr_unity.gameserver.network.Disconnection;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.PacketLogger;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.ActionFailed;
import org.l2jbr_unity.gameserver.network.serverpackets.CharSelectionInfo;
import org.l2jbr_unity.gameserver.network.serverpackets.RestartResponse;
import org.l2jbr_unity.gameserver.taskmanager.AttackStanceTaskManager;
import org.l2jbr_unity.gameserver.util.OfflineTradeUtil;

/**
 * @version $Revision: 1.11.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRestart extends ClientPacket
{
	protected static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if ((player.getActiveEnchantItemId() != Player.ID_NONE) || (player.getActiveEnchantAttrItemId() != Player.ID_NONE))
		{
			player.sendPacket(RestartResponse.valueOf(false));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isChangingClass())
		{
			PacketLogger.warning(player + " tried to restart during class change.");
			player.sendPacket(RestartResponse.valueOf(false));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInStoreMode())
		{
			player.sendMessage("Cannot restart while trading.");
			player.sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) && !(player.isGM() && Config.GM_RESTART_FIGHTING))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_RESTART_WHILE_IN_COMBAT);
			player.sendPacket(RestartResponse.valueOf(false));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Prevent player from restarting if they are a festival participant and it is in progress,
		// otherwise notify party members that the player is no longer a participant.
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot restart while you are a participant in a festival.");
				player.sendPacket(RestartResponse.valueOf(false));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			final Party playerParty = player.getParty();
			if (playerParty != null)
			{
				player.getParty().broadcastString(player.getName() + " has been removed from the upcoming festival.");
			}
		}
		
		if (!player.canLogout())
		{
			player.sendPacket(RestartResponse.valueOf(false));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Remove player from boss zone.
		player.removeFromBossZone();
		
		// Unregister from olympiad.
		if (OlympiadManager.getInstance().isRegistered(player))
		{
			OlympiadManager.getInstance().unRegisterNoble(player);
		}
		
		if (!Config.RESTORE_PLAYER_INSTANCE)
		{
			final int instanceId = player.getInstanceId();
			if (instanceId > 0)
			{
				final Instance world = InstanceManager.getInstance().getInstance(instanceId);
				if (world != null)
				{
					player.setInstanceId(0);
					Location location = world.getExitLoc();
					if (location == null)
					{
						location = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
					}
					player.getVariables().set(PlayerVariables.RESTORE_LOCATION, location.getX() + ";" + location.getY() + ";" + location.getZ());
					world.removePlayer(player.getObjectId());
				}
			}
		}
		
		final GameClient client = getClient();
		LOGGER_ACCOUNTING.info("Logged out, " + client);
		
		if (!OfflineTradeUtil.enteredOfflineMode(player))
		{
			Disconnection.of(client, player).storeMe().deleteMe();
		}
		
		// Return the client to the authenticated status.
		client.setConnectionState(ConnectionState.AUTHENTICATED);
		
		client.sendPacket(RestartResponse.valueOf(true));
		
		// Send character list.
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.sendPacket(new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1));
		client.setCharSelection(cl.getCharInfo());
	}
}