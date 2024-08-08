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
import org.l2jbr_unity.gameserver.model.actor.Summon;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author KenM
 */
public class ExPartyPetWindowAdd extends ServerPacket
{
	private final Summon _summon;
	
	public ExPartyPetWindowAdd(Summon summon)
	{
		_summon = summon;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PARTY_PET_WINDOW_ADD.writeId(this, buffer);
		buffer.writeInt(_summon.getObjectId());
		buffer.writeInt(_summon.getTemplate().getDisplayId() + 1000000);
		buffer.writeInt(_summon.getSummonType());
		buffer.writeInt(_summon.getOwner().getObjectId());
		buffer.writeString(_summon.getName());
		buffer.writeInt((int) _summon.getCurrentHp());
		buffer.writeInt(_summon.getMaxHp());
		buffer.writeInt((int) _summon.getCurrentMp());
		buffer.writeInt(_summon.getMaxMp());
		buffer.writeInt(_summon.getLevel());
	}
}
