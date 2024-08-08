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
import org.l2jbr_unity.gameserver.model.item.instance.Item;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author KenM
 */
public class ExRpItemLink extends ServerPacket
{
	private final Item _item;
	
	public ExRpItemLink(Item item)
	{
		_item = item;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RP_ITEM_LINK.writeId(this, buffer);
		buffer.writeInt(_item.getObjectId());
		buffer.writeInt(_item.getDisplayId());
		buffer.writeInt(_item.getLocationSlot());
		buffer.writeLong(_item.getCount());
		buffer.writeShort(_item.getTemplate().getType2());
		buffer.writeShort(_item.getCustomType1());
		buffer.writeShort(_item.isEquipped());
		buffer.writeInt(_item.getTemplate().getBodyPart());
		buffer.writeShort(_item.getEnchantLevel());
		buffer.writeShort(_item.getCustomType2());
		if (_item.isAugmented())
		{
			buffer.writeInt(_item.getAugmentation().getAugmentationId());
		}
		else
		{
			buffer.writeInt(0);
		}
		buffer.writeInt(_item.getMana());
		buffer.writeInt(_item.isTimeLimitedItem() ? (int) (_item.getRemainingTime() / 1000) : -9999);
		buffer.writeShort(_item.getAttackElementType());
		buffer.writeShort(_item.getAttackElementPower());
		for (byte i = 0; i < 6; i++)
		{
			buffer.writeShort(_item.getElementDefAttr(i));
		}
		// Enchant Effects
		for (int op : _item.getEnchantOptions())
		{
			buffer.writeShort(op);
		}
	}
}
