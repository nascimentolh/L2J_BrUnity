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

import org.l2jbr_unity.Config;
import org.l2jbr_unity.gameserver.instancemanager.MailManager;
import org.l2jbr_unity.gameserver.model.Message;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.zone.ZoneId;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.ExReplySentPost;
import org.l2jbr_unity.gameserver.util.Util;

/**
 * @author Migi, DS
 */
public class RequestSentPost extends ClientPacket
{
	private int _msgId;
	
	@Override
	protected void readImpl()
	{
		_msgId = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if ((player == null) || !Config.ALLOW_MAIL)
		{
			return;
		}
		
		final Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}
		
		if (!player.isInsideZone(ZoneId.PEACE) && msg.hasAttachments())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_OR_SEND_MAIL_WITH_ATTACHED_ITEMS_IN_NON_PEACE_ZONE_REGIONS);
			return;
		}
		
		if (msg.getSenderId() != player.getObjectId())
		{
			Util.handleIllegalPlayerAction(player, player + " tried to read not own post!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (msg.isDeletedBySender())
		{
			return;
		}
		
		player.sendPacket(new ExReplySentPost(msg));
	}
}
