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
import org.l2jbr_unity.gameserver.model.item.instance.Item;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author -Wooden-
 * @author UnAfraid, mrTJO
 */
public class PackageSendableList extends AbstractItemPacket
{
	private final Collection<Item> _items;
	private final int _playerObjId;
	private final long _adena;
	
	public PackageSendableList(Collection<Item> items, int playerObjId, long adena)
	{
		_items = items;
		_playerObjId = playerObjId;
		_adena = adena;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PACKAGE_SENDABLE_LIST.writeId(this, buffer);
		buffer.writeInt(_playerObjId);
		buffer.writeLong(_adena);
		buffer.writeInt(_items.size());
		for (Item item : _items)
		{
			writeItem(item, buffer);
			buffer.writeInt(item.getObjectId());
		}
	}
}
