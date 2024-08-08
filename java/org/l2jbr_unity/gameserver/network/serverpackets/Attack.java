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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.model.Hit;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class Attack extends ServerPacket
{
	private final int _attackerObjId;
	private final boolean _soulshot;
	private final int _ssGrade;
	private final Location _attackerLoc;
	private final Location _targetLoc;
	private final List<Hit> _hits = new ArrayList<>();
	
	/**
	 * @param attacker
	 * @param target
	 * @param useShots
	 * @param ssGrade
	 */
	public Attack(Creature attacker, Creature target, boolean useShots, int ssGrade)
	{
		_attackerObjId = attacker.getObjectId();
		_soulshot = useShots;
		_ssGrade = ssGrade;
		_attackerLoc = new Location(attacker);
		_targetLoc = new Location(target);
	}
	
	/**
	 * Adds hit to the attack (Attacks such as dual dagger/sword/fist has two hits)
	 * @param target
	 * @param damage
	 * @param miss
	 * @param crit
	 * @param shld
	 */
	public void addHit(Creature target, int damage, boolean miss, boolean crit, byte shld)
	{
		_hits.add(new Hit(target, damage, miss, crit, shld, _soulshot, _ssGrade));
	}
	
	/**
	 * @return {@code true} if current attack contains at least 1 hit.
	 */
	public boolean hasHits()
	{
		return !_hits.isEmpty();
	}
	
	/**
	 * @return {@code true} if attack has soul shot charged.
	 */
	public boolean hasSoulshot()
	{
		return _soulshot;
	}
	
	/**
	 * Writes current hit
	 * @param hit
	 * @param buffer
	 */
	private void writeHit(Hit hit, WritableBuffer buffer)
	{
		buffer.writeInt(hit.getTargetId());
		buffer.writeInt(hit.getDamage());
		buffer.writeByte(hit.getFlags());
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		final Iterator<Hit> it = _hits.iterator();
		ServerPackets.ATTACK.writeId(this, buffer);
		buffer.writeInt(_attackerObjId);
		writeHit(it.next(), buffer);
		buffer.writeInt(_attackerLoc.getX());
		buffer.writeInt(_attackerLoc.getY());
		buffer.writeInt(_attackerLoc.getZ());
		buffer.writeShort(_hits.size() - 1);
		while (it.hasNext())
		{
			writeHit(it.next(), buffer);
		}
		buffer.writeInt(_targetLoc.getX());
		buffer.writeInt(_targetLoc.getY());
		buffer.writeInt(_targetLoc.getZ());
	}
}
