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
import org.l2jbr_unity.gameserver.model.Shortcut;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class ShortCutInit extends ServerPacket
{
	private Collection<Shortcut> _shortCuts;
	
	public ShortCutInit(Player player)
	{
		if (player == null)
		{
			return;
		}
		_shortCuts = player.getAllShortCuts();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SHORT_CUT_INIT.writeId(this, buffer);
		buffer.writeInt(_shortCuts.size());
		for (Shortcut sc : _shortCuts)
		{
			buffer.writeInt(sc.getType().ordinal());
			buffer.writeInt(sc.getSlot() + (sc.getPage() * 12));
			switch (sc.getType())
			{
				case ITEM:
				{
					buffer.writeInt(sc.getId());
					buffer.writeInt(1);
					buffer.writeInt(sc.getSharedReuseGroup());
					buffer.writeInt(0);
					buffer.writeInt(0);
					buffer.writeShort(0);
					buffer.writeShort(0);
					break;
				}
				case SKILL:
				{
					buffer.writeInt(sc.getId());
					buffer.writeInt(sc.getLevel());
					buffer.writeByte(0); // C5
					buffer.writeInt(1); // C6
					break;
				}
				case ACTION:
				case MACRO:
				case RECIPE:
				case BOOKMARK:
				{
					buffer.writeInt(sc.getId());
					buffer.writeInt(1); // C6
				}
			}
		}
	}
}
