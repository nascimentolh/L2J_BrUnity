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
import org.l2jbr_unity.gameserver.model.Shortcut;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class ShortCutRegister extends ServerPacket
{
	private final Shortcut _shortcut;
	
	/**
	 * Register new skill shortcut
	 * @param shortcut
	 */
	public ShortCutRegister(Shortcut shortcut)
	{
		_shortcut = shortcut;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SHORT_CUT_REGISTER.writeId(this, buffer);
		buffer.writeInt(_shortcut.getType().ordinal());
		buffer.writeInt(_shortcut.getSlot() + (_shortcut.getPage() * 12)); // C4 Client
		switch (_shortcut.getType())
		{
			case ITEM:
			{
				buffer.writeInt(_shortcut.getId());
				buffer.writeInt(_shortcut.getCharacterType());
				buffer.writeInt(_shortcut.getSharedReuseGroup());
				buffer.writeInt(0); // unknown
				buffer.writeInt(0); // unknown
				buffer.writeInt(0); // item augment id
				break;
			}
			case SKILL:
			{
				buffer.writeInt(_shortcut.getId());
				buffer.writeInt(_shortcut.getLevel());
				buffer.writeByte(0); // C5
				buffer.writeInt(_shortcut.getCharacterType());
				break;
			}
			case ACTION:
			case MACRO:
			case RECIPE:
			case BOOKMARK:
			{
				buffer.writeInt(_shortcut.getId());
				buffer.writeInt(_shortcut.getCharacterType());
			}
		}
	}
}
