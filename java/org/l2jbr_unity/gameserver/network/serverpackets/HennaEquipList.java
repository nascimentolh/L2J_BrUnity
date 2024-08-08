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

import java.util.List;

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.data.xml.HennaData;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.item.Henna;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author Zoey76
 */
public class HennaEquipList extends ServerPacket
{
	private final Player _player;
	private final List<Henna> _hennaEquipList;
	
	public HennaEquipList(Player player)
	{
		_player = player;
		_hennaEquipList = HennaData.getInstance().getHennaList(player.getClassId());
	}
	
	public HennaEquipList(Player player, List<Henna> list)
	{
		_player = player;
		_hennaEquipList = list;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.HENNA_EQUIP_LIST.writeId(this, buffer);
		buffer.writeLong(_player.getAdena()); // activeChar current amount of Adena
		buffer.writeInt(3); // available equip slot
		buffer.writeInt(_hennaEquipList.size());
		for (Henna henna : _hennaEquipList)
		{
			// Player must have at least one dye in inventory
			// to be able to see the Henna that can be applied with it.
			if ((_player.getInventory().getItemByItemId(henna.getDyeItemId())) != null)
			{
				buffer.writeInt(henna.getDyeId()); // dye Id
				buffer.writeInt(henna.getDyeItemId()); // item Id of the dye
				buffer.writeLong(henna.getWearCount()); // amount of dyes required
				buffer.writeLong(henna.getWearFee()); // amount of Adena required
				buffer.writeInt(henna.isAllowedClass(_player.getClassId())); // meet the requirement or not
			}
		}
	}
}
