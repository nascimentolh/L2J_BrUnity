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
package org.l2jbr_unity.gameserver.model.actor.instance;

import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.Summon;
import org.l2jbr_unity.gameserver.model.actor.templates.NpcTemplate;
import org.l2jbr_unity.gameserver.model.krateisCube.KrateiArena;
import org.l2jbr_unity.gameserver.model.olympiad.OlympiadManager;
import org.l2jbr_unity.gameserver.network.SystemMessageId;

/**
 * @author LordWinter
 */
public class KrateisCubeManager extends Folk
{
	public KrateisCubeManager(NpcTemplate template)
	{
		super(template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("Register"))
		{
			if ((player.getInventoryLimit() * 0.8) <= player.getInventory().getSize())
			{
				player.sendPacket(SystemMessageId.YOU_CAN_PROCEED_ONLY_WHEN_THE_INVENTORY_WEIGHT_IS_BELOW_80_PERCENT_AND_THE_QUANTITY_IS_BELOW_90_PERCENT);
				showChatWindow(player, "data/html/krateisCube/32503-08.htm");
				return;
			}
			
			if ((player.getKrateiArena() == null) && (OlympiadManager.getInstance().isRegistered(player) || player.isInOlympiadMode() || player.isOnEvent() || player.isRegisteredOnEvent()))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BE_SIMULTANEOUSLY_REGISTERED_FOR_PVP_MATCHES_SUCH_AS_THE_OLYMPIAD_UNDERGROUND_COLISEUM_AERIAL_CLEFT_KRATEI_S_CUBE_AND_HANDY_S_BLOCK_CHECKERS);
				return;
			}
			
			if (((player.getParty() != null) && (player.getParty().getUCState() != null)) || (player.getUCState() > 0))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BE_SIMULTANEOUSLY_REGISTERED_FOR_PVP_MATCHES_SUCH_AS_THE_OLYMPIAD_UNDERGROUND_COLISEUM_AERIAL_CLEFT_KRATEI_S_CUBE_AND_HANDY_S_BLOCK_CHECKERS);
				return;
			}
			
			if (player.isCursedWeaponEquipped())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_REGISTER_WHILE_IN_POSSESSION_OF_A_CURSED_WEAPON);
				return;
			}
			
			final int id = Integer.parseInt(command.substring(9, 10).trim());
			final KrateiArena arena = org.l2jbr_unity.gameserver.instancemanager.games.KrateisCubeManager.getInstance().getArenaId(id);
			if (arena != null)
			{
				if ((player.getLevel() < arena.getMinLevel()) || (player.getLevel() > arena.getMaxLevel()))
				{
					showChatWindow(player, "data/html/krateisCube/32503-06.htm");
					return;
				}
			}
			else
			{
				showChatWindow(player, "data/html/krateisCube/32503-09.htm");
				return;
			}
			
			if (org.l2jbr_unity.gameserver.instancemanager.games.KrateisCubeManager.getInstance().isRegisterTime())
			{
				if (arena.addRegisterPlayer(player))
				{
					showChatWindow(player, "data/html/krateisCube/32503-03.htm");
				}
				else
				{
					showChatWindow(player, "data/html/krateisCube/32503-04.htm");
				}
			}
			else
			{
				showChatWindow(player, "data/html/krateisCube/32503-07.htm");
			}
		}
		else if (command.startsWith("SeeList"))
		{
			if (player.getLevel() < 70)
			{
				showChatWindow(player, "data/html/krateisCube/32503-09.htm");
			}
			else
			{
				showChatWindow(player, "data/html/krateisCube/32503-02.htm");
			}
		}
		else if (command.startsWith("Cancel"))
		{
			for (KrateiArena arena : org.l2jbr_unity.gameserver.instancemanager.games.KrateisCubeManager.getInstance().getArenas().values())
			{
				if ((arena != null) && arena.removePlayer(player))
				{
					showChatWindow(player, "data/html/krateisCube/32503-05.htm");
					break;
				}
			}
		}
		else if (command.startsWith("TeleportToFI"))
		{
			player.teleToLocation(-59193, -56893, -2034, true);
			final Summon pet = player.getSummon();
			if (pet != null)
			{
				pet.teleToLocation(-59193, -56893, -2034, true);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-0" + val;
		}
		
		return "data/html/krateisCube/" + pom + ".htm";
	}
}
