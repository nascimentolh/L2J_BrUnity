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
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.instancezone.Instance;
import org.l2jbr_unity.gameserver.model.olympiad.OlympiadManager;
import org.l2jbr_unity.gameserver.model.variables.PlayerVariables;
import org.l2jbr_unity.gameserver.network.Disconnection;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.serverpackets.ActionFailed;
import org.l2jbr_unity.gameserver.network.serverpackets.LeaveWorld;
import org.l2jbr_unity.gameserver.util.OfflineTradeUtil;

/**
 * @version $Revision: 1.9.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class Logout extends ClientPacket
{
	protected static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final GameClient client = getClient();
		final Player player = client.getPlayer();
		if (player == null)
		{
			client.disconnect();
			return;
		}
		
		if (!player.canLogout())
		{
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
		
		LOGGER_ACCOUNTING.info("Logged out, " + client);
		
		if (!OfflineTradeUtil.enteredOfflineMode(player))
		{
			Disconnection.of(client, player).defaultSequence(LeaveWorld.STATIC_PACKET);
		}
	}
}