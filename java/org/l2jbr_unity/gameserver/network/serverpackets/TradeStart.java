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
package org.l2jbr_unity.gameserver.network.serverpackets;

import java.util.Collection;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.enums.PlayerCondOverride;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.item.instance.Item;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class TradeStart extends AbstractItemPacket
{
	private final Player _player;
	private final Collection<Item> _itemList;
	
	public TradeStart(Player player)
	{
		_player = player;
		_itemList = _player.getInventory().getAvailableItems(true, (_player.canOverrideCond(PlayerCondOverride.ITEM_CONDITIONS) && Config.GM_TRADE_RESTRICTED_ITEMS), false);
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if ((_player.getActiveTradeList() == null) || (_player.getActiveTradeList().getPartner() == null))
		{
			return;
		}
		
		ServerPackets.TRADE_START.writeId(this, buffer);
		buffer.writeInt(_player.getActiveTradeList().getPartner().getObjectId());
		buffer.writeShort(_itemList.size());
		for (Item item : _itemList)
		{
			writeItem(item, buffer);
		}
	}
}
