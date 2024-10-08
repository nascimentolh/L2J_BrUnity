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

import java.util.List;
import java.util.StringTokenizer;

import org.l2jbr_unity.gameserver.handler.IAdminCommandHandler;
import org.l2jbr_unity.gameserver.instancemanager.FortManager;
import org.l2jbr_unity.gameserver.model.WorldObject;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.clan.Clan;
import org.l2jbr_unity.gameserver.model.siege.Fort;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jbr_unity.gameserver.network.serverpackets.SystemMessage;
import org.l2jbr_unity.gameserver.util.BuilderUtil;

/**
 * This class handles all siege commands: Todo: change the class name, and neaten it up
 */
public class AdminFortSiege implements IAdminCommandHandler
{
	// private static final Logger LOGGER = Logger.getLogger(AdminFortSiege.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_fortsiege",
		"admin_add_fortattacker",
		"admin_list_fortsiege_clans",
		"admin_clear_fortsiege_list",
		"admin_spawn_fortdoors",
		"admin_endfortsiege",
		"admin_startfortsiege",
		"admin_setfort",
		"admin_removefort"
	};
	
	@Override
	public boolean useAdminCommand(String commandValue, Player activeChar)
	{
		String command = commandValue;
		final StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command
		
		// Get fort
		Fort fort = null;
		int fortId = 0;
		if (st.hasMoreTokens())
		{
			fortId = Integer.parseInt(st.nextToken());
			fort = FortManager.getInstance().getFortById(fortId);
		}
		// Get fort
		if (((fort == null) || (fortId == 0)))
		{
			// No fort specified
			showFortSelectPage(activeChar);
		}
		else
		{
			final WorldObject target = activeChar.getTarget();
			Player player = null;
			if ((target != null) && target.isPlayer())
			{
				player = (Player) target;
			}
			
			if (command.equalsIgnoreCase("admin_add_fortattacker"))
			{
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				}
				else
				{
					if (fort.getSiege().addAttacker(player, false) == 4)
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_CLAN_HAS_BEEN_REGISTERED_TO_S1_S_FORTRESS_BATTLE);
						sm.addCastleId(fort.getResidenceId());
						player.sendPacket(sm);
					}
					else
					{
						player.sendMessage("During registering error occurred!");
					}
				}
			}
			else if (command.equalsIgnoreCase("admin_clear_fortsiege_list"))
			{
				fort.getSiege().clearSiegeClan();
			}
			else if (command.equalsIgnoreCase("admin_endfortsiege"))
			{
				fort.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("admin_list_fortsiege_clans"))
			{
				BuilderUtil.sendSysMessage(activeChar, "Not implemented yet.");
			}
			else if (command.equalsIgnoreCase("admin_setfort"))
			{
				if ((player == null) || (player.getClan() == null))
				{
					activeChar.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				}
				else
				{
					fort.endOfSiege(player.getClan());
				}
			}
			else if (command.equalsIgnoreCase("admin_removefort"))
			{
				final Clan clan = fort.getOwnerClan();
				if (clan != null)
				{
					fort.removeOwner(true);
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Unable to remove fort");
				}
			}
			else if (command.equalsIgnoreCase("admin_spawn_fortdoors"))
			{
				fort.resetDoors();
			}
			else if (command.equalsIgnoreCase("admin_startfortsiege"))
			{
				fort.getSiege().startSiege();
			}
			
			showFortSiegePage(activeChar, fort);
		}
		return true;
	}
	
	private void showFortSelectPage(Player activeChar)
	{
		int i = 0;
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar, "data/html/admin/forts.htm");
		
		final List<Fort> forts = FortManager.getInstance().getForts();
		final StringBuilder cList = new StringBuilder(forts.size() * 100);
		for (Fort fort : forts)
		{
			if (fort != null)
			{
				cList.append("<td fixwidth=90><a action=\"bypass -h admin_fortsiege " + fort.getResidenceId() + "\">" + fort.getName() + " id: " + fort.getResidenceId() + "</a></td>");
				i++;
			}
			
			if (i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		
		adminReply.replace("%forts%", cList.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showFortSiegePage(Player activeChar, Fort fort)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar, "data/html/admin/fort.htm");
		adminReply.replace("%fortName%", fort.getName());
		adminReply.replace("%fortId%", String.valueOf(fort.getResidenceId()));
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
