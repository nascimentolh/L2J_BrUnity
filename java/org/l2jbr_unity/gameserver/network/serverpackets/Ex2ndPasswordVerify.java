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
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author mrTJO
 */
public class Ex2ndPasswordVerify extends ServerPacket
{
	public static final int PASSWORD_OK = 0;
	public static final int PASSWORD_WRONG = 1;
	public static final int PASSWORD_BAN = 2;
	
	private final int _wrongTentatives;
	private final int _mode;
	
	public Ex2ndPasswordVerify(int mode, int wrongTentatives)
	{
		_mode = mode;
		_wrongTentatives = wrongTentatives;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_2ND_PASSWORD_VERIFY.writeId(this, buffer);
		buffer.writeInt(_mode);
		buffer.writeInt(_wrongTentatives);
	}
}
