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

public class ExRegenMax extends ServerPacket
{
	private final int _time;
	private final int _tickInterval;
	private final double _amountPerTick;
	
	public ExRegenMax(int time, int tickInterval, double amountPerTick)
	{
		_time = time;
		_tickInterval = tickInterval;
		_amountPerTick = amountPerTick;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_REGEN_MAX.writeId(this, buffer);
		buffer.writeInt(1);
		buffer.writeInt(_time);
		buffer.writeInt(_tickInterval);
		buffer.writeDouble(_amountPerTick);
	}
}
