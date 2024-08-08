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

public class AskJoinPledge extends ServerPacket
{
	private final int _requestorObjId;
	private final String _subPledgeName;
	private final int _pledgeType;
	private final String _pledgeName;
	
	public AskJoinPledge(int requestorObjId, String subPledgeName, int pledgeType, String pledgeName)
	{
		_requestorObjId = requestorObjId;
		_subPledgeName = subPledgeName;
		_pledgeType = pledgeType;
		_pledgeName = pledgeName;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ASK_JOIN_PLEDGE.writeId(this, buffer);
		buffer.writeInt(_requestorObjId);
		if (_subPledgeName != null)
		{
			buffer.writeString(_pledgeType > 0 ? _subPledgeName : _pledgeName);
		}
		if (_pledgeType != 0)
		{
			buffer.writeInt(_pledgeType);
		}
		buffer.writeString(_pledgeName);
	}
}
