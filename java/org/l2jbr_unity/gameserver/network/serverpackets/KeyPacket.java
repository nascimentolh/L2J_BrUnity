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

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class KeyPacket extends ServerPacket
{
	private final byte[] _key;
	private final int _result;
	
	public KeyPacket(byte[] key, int result)
	{
		_key = key;
		_result = result;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.VERSION_CHECK.writeId(this, buffer);
		buffer.writeByte(_result); // 0 - wrong protocol, 1 - protocol ok
		for (int i = 0; i < 8; i++)
		{
			buffer.writeByte(_key[i]); // key
		}
		buffer.writeInt(Config.PACKET_ENCRYPTION); // use blowfish encryption
		buffer.writeInt(Config.SERVER_ID); // server id
		buffer.writeByte(1);
		buffer.writeInt(0); // obfuscation key
	}
}
