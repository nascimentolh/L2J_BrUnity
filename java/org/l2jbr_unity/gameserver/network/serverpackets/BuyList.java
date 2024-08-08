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
import org.l2jbr_unity.gameserver.model.buylist.BuyListHolder;
import org.l2jbr_unity.gameserver.model.buylist.Product;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class BuyList extends ServerPacket
{
	private final int _listId;
	private final Collection<Product> _list;
	private final long _money;
	private double _taxRate = 0;
	
	public BuyList(BuyListHolder list, long currentMoney, double taxRate)
	{
		_listId = list.getListId();
		_list = list.getProducts();
		_money = currentMoney;
		_taxRate = taxRate;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BUY_SELL_LIST.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeLong(_money); // current money
		buffer.writeInt(_listId);
		buffer.writeShort(_list.size());
		for (Product product : _list)
		{
			if ((product.getCount() > 0) || !product.hasLimitedStock())
			{
				buffer.writeInt(product.getItemId());
				buffer.writeInt(product.getItemId());
				buffer.writeInt(0);
				buffer.writeLong(product.getCount() < 0 ? 0 : product.getCount());
				buffer.writeShort(product.getItem().getType2());
				buffer.writeShort(product.getItem().getType1()); // Custom Type 1
				buffer.writeShort(0); // isEquipped
				buffer.writeInt(product.getItem().getBodyPart()); // Body Part
				buffer.writeShort(product.getItem().getDefaultEnchantLevel()); // Enchant
				buffer.writeShort(0); // Custom Type
				buffer.writeInt(0); // Augment
				buffer.writeInt(-1); // Mana
				buffer.writeInt(-9999); // Time
				buffer.writeShort(0); // Element Type
				buffer.writeShort(0); // Element Power
				for (byte i = 0; i < 6; i++)
				{
					buffer.writeShort(0);
				}
				// Enchant Effects
				buffer.writeShort(0);
				buffer.writeShort(0);
				buffer.writeShort(0);
				if ((product.getItemId() >= 3960) && (product.getItemId() <= 4026))
				{
					buffer.writeLong((long) (product.getPrice() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
				}
				else
				{
					buffer.writeLong((long) (product.getPrice() * (1 + _taxRate)));
				}
			}
		}
	}
}
