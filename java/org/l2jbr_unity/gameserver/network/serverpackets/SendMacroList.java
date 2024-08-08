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
import org.l2jbr_unity.gameserver.model.Macro;
import org.l2jbr_unity.gameserver.model.MacroCmd;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class SendMacroList extends ServerPacket
{
	private final int _rev;
	private final int _count;
	private final Macro _macro;
	
	public SendMacroList(int rev, int count, Macro macro)
	{
		_rev = rev;
		_count = count;
		_macro = macro;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.MACRO_LIST.writeId(this, buffer);
		buffer.writeInt(_rev); // macro change revision (changes after each macro edition)
		buffer.writeByte(0); // unknown
		buffer.writeByte(_count); // count of Macros
		buffer.writeByte(_macro != null); // unknown
		if (_macro != null)
		{
			buffer.writeInt(_macro.getId()); // Macro ID
			buffer.writeString(_macro.getName()); // Macro Name
			buffer.writeString(_macro.getDescr()); // Desc
			buffer.writeString(_macro.getAcronym()); // acronym
			buffer.writeByte(_macro.getIcon()); // icon
			buffer.writeByte(_macro.getCommands().size()); // count
			int i = 1;
			for (MacroCmd cmd : _macro.getCommands())
			{
				buffer.writeByte(i++); // command count
				buffer.writeByte(cmd.getType().ordinal()); // type 1 = skill, 3 = action, 4 = shortcut
				buffer.writeInt(cmd.getD1()); // skill id
				buffer.writeByte(cmd.getD2()); // shortcut id
				buffer.writeString(cmd.getCmd()); // command name
			}
		}
	}
}
