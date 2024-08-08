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
import org.l2jbr_unity.gameserver.model.ItemInfo;
import org.l2jbr_unity.gameserver.model.TradeItem;
import org.l2jbr_unity.gameserver.model.item.instance.Item;
import org.l2jbr_unity.gameserver.model.itemcontainer.PlayerInventory;

/**
 * @author UnAfraid
 */
public abstract class AbstractItemPacket extends ServerPacket
{
	protected void writeItem(TradeItem item, WritableBuffer buffer)
	{
		writeItem(new ItemInfo(item), buffer);
	}
	
	protected void writeItem(Item item, WritableBuffer buffer)
	{
		writeItem(new ItemInfo(item), buffer);
	}
	
	protected void writeItem(ItemInfo item, WritableBuffer buffer)
	{
		buffer.writeInt(item.getObjectId()); // ObjectId
		buffer.writeInt(item.getItem().getDisplayId()); // ItemId
		buffer.writeInt(item.getLocation()); // T1
		buffer.writeLong(item.getCount()); // Quantity
		buffer.writeShort(item.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
		buffer.writeShort(item.getCustomType1()); // Filler (always 0)
		buffer.writeShort(item.getEquipped()); // Equipped : 00-No, 01-yes
		buffer.writeInt(item.getItem().getBodyPart()); // Slot : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand
		buffer.writeShort(item.getEnchant()); // Enchant level (pet level shown in control item)
		buffer.writeShort(item.getCustomType2()); // Pet name exists or not shown in control item
		buffer.writeInt(item.getAugmentationBonus());
		buffer.writeInt(item.getMana());
		buffer.writeInt(item.getTime());
		writeItemElementalAndEnchant(item, buffer);
	}
	
	protected void writeItemElementalAndEnchant(ItemInfo item, WritableBuffer buffer)
	{
		buffer.writeShort(item.getAttackElementType());
		buffer.writeShort(item.getAttackElementPower());
		for (byte i = 0; i < 6; i++)
		{
			buffer.writeShort(item.getElementDefAttr(i));
		}
		// Enchant Effects
		for (int op : item.getEnchantOptions())
		{
			buffer.writeShort(op);
		}
	}
	
	protected void writeInventoryBlock(PlayerInventory inventory, WritableBuffer buffer)
	{
		if (inventory.hasInventoryBlock())
		{
			buffer.writeShort(inventory.getBlockItems().length);
			buffer.writeByte(inventory.getBlockMode());
			for (int i : inventory.getBlockItems())
			{
				buffer.writeInt(i);
			}
		}
		else
		{
			buffer.writeShort(0);
		}
	}
}
