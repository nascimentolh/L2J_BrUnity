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
import org.l2jbr_unity.gameserver.data.xml.PrimeShopData;
import org.l2jbr_unity.gameserver.model.holders.PrimeShopProductHolder;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class ExBrProductList extends ServerPacket
{
	private final Collection<PrimeShopProductHolder> _itemList = PrimeShopData.getInstance().getAllItems();
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BR_PRODUCT_LIST.writeId(this, buffer);
		buffer.writeInt(_itemList.size());
		for (PrimeShopProductHolder product : _itemList)
		{
			final int category = product.getCategory();
			buffer.writeInt(product.getProductId()); // product id
			buffer.writeShort(category); // category id
			buffer.writeInt(product.getPrice()); // points
			switch (category)
			{
				case 6:
				{
					buffer.writeInt(1); // event
					break;
				}
				case 7:
				{
					buffer.writeInt(2); // best
					break;
				}
				case 8:
				{
					buffer.writeInt(3); // event & best
					break;
				}
				default:
				{
					buffer.writeInt(0); // normal
					break;
				}
			}
			buffer.writeInt(0); // start sale
			buffer.writeInt(0); // end sale
			buffer.writeByte(0); // day week
			buffer.writeByte(0); // start hour
			buffer.writeByte(0); // start min
			buffer.writeByte(0); // end hour
			buffer.writeByte(0); // end min
			buffer.writeInt(0); // current stock
			buffer.writeInt(0); // max stock
		}
	}
}
