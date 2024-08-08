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
import org.l2jbr_unity.gameserver.model.item.ItemTemplate;
import org.l2jbr_unity.gameserver.model.item.instance.Item;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class EquipUpdate extends ServerPacket
{
	private final Item _item;
	private final int _change;
	
	public EquipUpdate(Item item, int change)
	{
		_item = item;
		_change = change;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EQUIP_UPDATE.writeId(this, buffer);
		buffer.writeInt(_change);
		buffer.writeInt(_item.getObjectId());
		int bodypart = 0;
		switch (_item.getTemplate().getBodyPart())
		{
			case ItemTemplate.SLOT_L_EAR:
			{
				bodypart = 0x01;
				break;
			}
			case ItemTemplate.SLOT_R_EAR:
			{
				bodypart = 0x02;
				break;
			}
			case ItemTemplate.SLOT_NECK:
			{
				bodypart = 0x03;
				break;
			}
			case ItemTemplate.SLOT_R_FINGER:
			{
				bodypart = 0x04;
				break;
			}
			case ItemTemplate.SLOT_L_FINGER:
			{
				bodypart = 0x05;
				break;
			}
			case ItemTemplate.SLOT_HEAD:
			{
				bodypart = 0x06;
				break;
			}
			case ItemTemplate.SLOT_R_HAND:
			{
				bodypart = 0x07;
				break;
			}
			case ItemTemplate.SLOT_L_HAND:
			{
				bodypart = 0x08;
				break;
			}
			case ItemTemplate.SLOT_GLOVES:
			{
				bodypart = 0x09;
				break;
			}
			case ItemTemplate.SLOT_CHEST:
			{
				bodypart = 0x0a;
				break;
			}
			case ItemTemplate.SLOT_LEGS:
			{
				bodypart = 0x0b;
				break;
			}
			case ItemTemplate.SLOT_FEET:
			{
				bodypart = 0x0c;
				break;
			}
			case ItemTemplate.SLOT_BACK:
			{
				bodypart = 0x0d;
				break;
			}
			case ItemTemplate.SLOT_LR_HAND:
			{
				bodypart = 0x0e;
				break;
			}
			case ItemTemplate.SLOT_HAIR:
			{
				bodypart = 0x0f;
				break;
			}
			case ItemTemplate.SLOT_BELT:
			{
				bodypart = 0x10;
				break;
			}
		}
		buffer.writeInt(bodypart);
	}
}
