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

import org.l2jbr_unity.gameserver.data.xml.AdminData;
import org.l2jbr_unity.gameserver.enums.ChatType;
import org.l2jbr_unity.gameserver.handler.IAdminCommandHandler;
import org.l2jbr_unity.gameserver.model.World;
import org.l2jbr_unity.gameserver.model.WorldObject;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.CreatureSay;

/**
 * This class handles following admin commands: - gmchat text = sends text to all online GM's - gmchat_menu text = same as gmchat, displays the admin panel after chat
 * @version $Revision: 1.2.4.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminGmChat implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gmchat",
		"admin_snoop",
		"admin_gmchat_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_gmchat"))
		{
			handleGmChat(command, activeChar);
		}
		else if (command.startsWith("admin_snoop"))
		{
			snoop(command, activeChar);
		}
		if (command.startsWith("admin_gmchat_menu"))
		{
			AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
		}
		return true;
	}
	
	/**
	 * @param command
	 * @param activeChar
	 */
	private void snoop(String command, Player activeChar)
	{
		WorldObject target = null;
		if (command.length() > 12)
		{
			target = World.getInstance().getPlayer(command.substring(12));
		}
		if (target == null)
		{
			target = activeChar.getTarget();
		}
		
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.SELECT_TARGET);
			return;
		}
		if (!target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		final Player player = (Player) target;
		player.addSnooper(activeChar);
		activeChar.addSnooped(player);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	/**
	 * @param command
	 * @param activeChar
	 */
	private void handleGmChat(String command, Player activeChar)
	{
		try
		{
			int offset = 0;
			String text;
			if (command.startsWith("admin_gmchat_menu"))
			{
				offset = 18;
			}
			else
			{
				offset = 13;
			}
			text = command.substring(offset);
			AdminData.getInstance().broadcastToGMs(new CreatureSay(null, ChatType.ALLIANCE, activeChar.getName(), text));
		}
		catch (StringIndexOutOfBoundsException e)
		{
			// Who cares?
		}
	}
}
