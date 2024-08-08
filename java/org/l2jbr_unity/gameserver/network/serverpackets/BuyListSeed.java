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

import java.util.ArrayList;
import java.util.List;

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.instancemanager.CastleManorManager;
import org.l2jbr_unity.gameserver.model.SeedProduction;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author l3x
 */
public class BuyListSeed extends ServerPacket
{
	private final int _manorId;
	private final long _money;
	private final List<SeedProduction> _list = new ArrayList<>();
	
	public BuyListSeed(long currentMoney, int castleId)
	{
		_money = currentMoney;
		_manorId = castleId;
		for (SeedProduction s : CastleManorManager.getInstance().getSeedProduction(castleId, false))
		{
			if ((s.getAmount() > 0) && (s.getPrice() > 0))
			{
				_list.add(s);
			}
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.BUY_LIST_SEED.writeId(this, buffer);
		buffer.writeLong(_money); // current money
		buffer.writeInt(_manorId); // manor id
		if (!_list.isEmpty())
		{
			buffer.writeShort(_list.size()); // list length
			for (SeedProduction s : _list)
			{
				buffer.writeInt(s.getId());
				buffer.writeInt(s.getId());
				buffer.writeInt(0);
				buffer.writeLong(s.getAmount()); // item count
				buffer.writeShort(5); // Custom Type 2
				buffer.writeShort(0); // Custom Type 1
				buffer.writeShort(0); // Equipped
				buffer.writeInt(0); // Body Part
				buffer.writeShort(0); // Enchant
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
				buffer.writeLong(s.getPrice()); // price
			}
			_list.clear();
		}
		else
		{
			buffer.writeShort(0);
		}
	}
}