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

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.data.xml.PrimeShopData;
import org.l2jbr_unity.gameserver.model.holders.PrimeShopProductHolder;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class ExBrProductInfo extends ServerPacket
{
	private final PrimeShopProductHolder _product;
	
	public ExBrProductInfo(int id)
	{
		_product = PrimeShopData.getInstance().getProduct(id);
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (_product == null)
		{
			return;
		}
		
		ServerPackets.EX_BR_PRODUCT_INFO.writeId(this, buffer);
		buffer.writeInt(_product.getProductId()); // product id
		buffer.writeInt(_product.getPrice()); // points
		buffer.writeInt(1); // components size
		buffer.writeInt(_product.getItemId()); // item id
		buffer.writeInt(_product.getItemCount()); // quality
		buffer.writeInt(_product.getItemWeight()); // weight
		buffer.writeInt(_product.isTradable()); // 0 - do not drop/trade
	}
}
