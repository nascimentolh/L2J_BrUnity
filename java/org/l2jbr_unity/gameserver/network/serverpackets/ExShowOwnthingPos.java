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
import org.l2jbr_unity.gameserver.instancemanager.TerritoryWarManager;
import org.l2jbr_unity.gameserver.model.TerritoryWard;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author -Gigiikun-
 */
public class ExShowOwnthingPos extends ServerPacket
{
	public static final ExShowOwnthingPos STATIC_PACKET = new ExShowOwnthingPos();
	
	private ExShowOwnthingPos()
	{
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_OWNTHING_POS.writeId(this, buffer);
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			final Collection<TerritoryWard> territoryWardList = TerritoryWarManager.getInstance().getAllTerritoryWards();
			buffer.writeInt(territoryWardList.size());
			for (TerritoryWard ward : territoryWardList)
			{
				buffer.writeInt(ward.getTerritoryId());
				if (ward.getNpc() != null)
				{
					buffer.writeInt(ward.getNpc().getX());
					buffer.writeInt(ward.getNpc().getY());
					buffer.writeInt(ward.getNpc().getZ());
				}
				else if (ward.getPlayer() != null)
				{
					buffer.writeInt(ward.getPlayer().getX());
					buffer.writeInt(ward.getPlayer().getY());
					buffer.writeInt(ward.getPlayer().getZ());
				}
				else
				{
					buffer.writeInt(0);
					buffer.writeInt(0);
					buffer.writeInt(0);
				}
			}
		}
		else
		{
			buffer.writeInt(0);
		}
	}
}
