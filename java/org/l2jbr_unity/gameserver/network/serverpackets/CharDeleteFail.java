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

public class CharDeleteFail extends ServerPacket
{
	public static final int REASON_DELETION_FAILED = 1;
	public static final int REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = 2;
	public static final int REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = 3;
	
	private final int _error;
	
	public CharDeleteFail(int errorCode)
	{
		_error = errorCode;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CHARACTER_DELETE_FAIL.writeId(this, buffer);
		buffer.writeInt(_error);
	}
}
