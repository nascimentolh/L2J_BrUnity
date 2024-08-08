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

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.model.TradeItem;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.item.instance.Item;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class PrivateStoreManageListBuy extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final Collection<Item> _itemList;
	private final Collection<TradeItem> _buyList;
	
	public PrivateStoreManageListBuy(Player player)
	{
		_objId = player.getObjectId();
		_playerAdena = player.getAdena();
		_itemList = player.getInventory().getUniqueItems(false, true);
		_buyList = player.getBuyList().getItems();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PRIVATE_STORE_BUY_MANAGE_LIST.writeId(this, buffer);
		// section 1
		buffer.writeInt(_objId);
		buffer.writeLong(_playerAdena);
		// section2
		buffer.writeInt(_itemList.size()); // inventory items for potential buy
		for (Item item : _itemList)
		{
			writeItem(item, buffer);
			buffer.writeLong(item.getTemplate().getReferencePrice() * 2);
		}
		// section 3
		buffer.writeInt(_buyList.size()); // count for all items already added for buy
		for (TradeItem item : _buyList)
		{
			writeItem(item, buffer);
			buffer.writeLong(item.getPrice());
			buffer.writeLong(item.getItem().getReferencePrice() * 2);
			buffer.writeLong(item.getCount());
		}
	}
}
