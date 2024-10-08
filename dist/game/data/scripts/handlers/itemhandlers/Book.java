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
package handlers.itemhandlers;

import org.l2jbr_unity.gameserver.cache.HtmCache;
import org.l2jbr_unity.gameserver.handler.IItemHandler;
import org.l2jbr_unity.gameserver.model.actor.Playable;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.item.instance.Item;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.ActionFailed;
import org.l2jbr_unity.gameserver.network.serverpackets.NpcHtmlMessage;

public class Book implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final Player player = (Player) playable;
		final int itemId = item.getId();
		final String filename = "data/html/help/" + itemId + ".htm";
		final String content = HtmCache.getInstance().getHtm(player, filename);
		if (content == null)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(0, item.getId());
			html.setHtml("<html><body>My Text is missing:<br>" + filename + "</body></html>");
			player.sendPacket(html);
		}
		else
		{
			final NpcHtmlMessage itemReply = new NpcHtmlMessage(0, item.getId());
			itemReply.setHtml(content);
			player.sendPacket(itemReply);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
		return true;
	}
}
