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

import org.l2jbr_unity.gameserver.data.xml.EnchantItemData;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.item.enchant.EnchantScroll;
import org.l2jbr_unity.gameserver.model.item.enchant.EnchantSupportItem;
import org.l2jbr_unity.gameserver.model.item.instance.Item;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.ExPutEnchantSupportItemResult;

/**
 * @author KenM
 */
public class RequestExTryToPutEnchantSupportItem extends ClientPacket
{
	private int _supportObjectId;
	private int _enchantObjectId;
	
	@Override
	protected void readImpl()
	{
		_supportObjectId = readInt();
		_enchantObjectId = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.isEnchanting())
		{
			final Item item = player.getInventory().getItemByObjectId(_enchantObjectId);
			final Item scroll = player.getInventory().getItemByObjectId(player.getActiveEnchantItemId());
			final Item support = player.getInventory().getItemByObjectId(_supportObjectId);
			if ((item == null) || (scroll == null) || (support == null))
			{
				// message may be custom
				player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.setActiveEnchantSupportItemId(Player.ID_NONE);
				return;
			}
			
			final EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
			final EnchantSupportItem supportTemplate = EnchantItemData.getInstance().getSupportItem(support);
			if ((scrollTemplate == null) || (supportTemplate == null) || !scrollTemplate.isValid(item, supportTemplate))
			{
				// message may be custom
				player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.setActiveEnchantSupportItemId(Player.ID_NONE);
				player.sendPacket(new ExPutEnchantSupportItemResult(0));
				return;
			}
			player.setActiveEnchantSupportItemId(support.getObjectId());
			player.sendPacket(new ExPutEnchantSupportItemResult(_supportObjectId));
		}
	}
}
