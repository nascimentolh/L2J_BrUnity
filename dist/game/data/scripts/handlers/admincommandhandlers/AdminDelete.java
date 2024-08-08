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
package handlers.admincommandhandlers;

import org.l2jbr_unity.gameserver.data.SpawnTable;
import org.l2jbr_unity.gameserver.handler.AdminCommandHandler;
import org.l2jbr_unity.gameserver.handler.IAdminCommandHandler;
import org.l2jbr_unity.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jbr_unity.gameserver.model.Spawn;
import org.l2jbr_unity.gameserver.model.World;
import org.l2jbr_unity.gameserver.model.WorldObject;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.zone.type.NpcSpawnTerritory;
import org.l2jbr_unity.gameserver.util.BuilderUtil;
import org.l2jbr_unity.gameserver.util.Util;

/**
 * @author Mobius
 */
public class AdminDelete implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_delete", // supports range parameter
		"admin_delete_group" // for territory spawns
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.contains("group"))
		{
			handleDeleteGroup(activeChar);
		}
		else if (command.startsWith("admin_delete"))
		{
			final String[] split = command.split(" ");
			handleDelete(activeChar, (split.length > 1) && Util.isDigit(split[1]) ? Integer.parseInt(split[1]) : 0);
		}
		return true;
	}
	
	private void handleDelete(Player player, int range)
	{
		if (range > 0)
		{
			World.getInstance().forEachVisibleObjectInRange(player, Npc.class, range, target -> deleteNpc(player, target));
			return;
		}
		
		final WorldObject obj = player.getTarget();
		if (obj instanceof Npc)
		{
			deleteNpc(player, (Npc) obj);
		}
		else
		{
			BuilderUtil.sendSysMessage(player, "Incorrect target.");
		}
	}
	
	private void handleDeleteGroup(Player player)
	{
		final WorldObject obj = player.getTarget();
		if (obj instanceof Npc)
		{
			deleteGroup(player, (Npc) obj);
		}
		else
		{
			BuilderUtil.sendSysMessage(player, "Incorrect target.");
		}
	}
	
	private void deleteNpc(Player player, Npc target)
	{
		final Spawn spawn = target.getSpawn();
		if (spawn != null)
		{
			final NpcSpawnTerritory npcSpawnTerritory = spawn.getSpawnTerritory();
			if (npcSpawnTerritory == null)
			{
				target.deleteMe();
				spawn.stopRespawn();
				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getId()))
				{
					RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					SpawnTable.getInstance().deleteSpawn(spawn, true);
				}
				BuilderUtil.sendSysMessage(player, "Deleted " + target.getName() + " from " + target.getObjectId() + ".");
			}
			else
			{
				AdminCommandHandler.getInstance().useAdminCommand(player, AdminDelete.ADMIN_COMMANDS[1], true);
			}
		}
	}
	
	private void deleteGroup(Player player, Npc target)
	{
		final Spawn spawn = target.getSpawn();
		if (spawn != null)
		{
			final NpcSpawnTerritory npcSpawnTerritory = spawn.getSpawnTerritory();
			if (npcSpawnTerritory == null)
			{
				BuilderUtil.sendSysMessage(player, "Incorrect target.");
			}
			else
			{
				target.deleteMe();
				spawn.stopRespawn();
				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getId()))
				{
					RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					SpawnTable.getInstance().deleteSpawn(spawn, true);
				}
				
				for (WorldObject wo : World.getInstance().getVisibleObjects())
				{
					if (!wo.isNpc())
					{
						continue;
					}
					
					final Spawn npcSpawn = ((Npc) wo).getSpawn();
					if (npcSpawn != null)
					{
						final NpcSpawnTerritory territory = npcSpawn.getSpawnTerritory();
						if ((territory != null) && !territory.getName().isEmpty() && territory.getName().equals(npcSpawnTerritory.getName()))
						{
							((Npc) wo).deleteMe();
							npcSpawn.stopRespawn();
						}
					}
				}
				
				BuilderUtil.sendSysMessage(player, "Deleted " + target.getName() + " group from " + target.getObjectId() + ".");
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
