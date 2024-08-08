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
import java.util.Collections;

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.model.WorldObject;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * MagicSkillLaunched server packet implementation.
 * @author UnAfraid
 */
public class MagicSkillLaunched extends ServerPacket
{
	private final int _objectId;
	private final int _skillId;
	private final int _skillLevel;
	private final Collection<WorldObject> _targets;
	
	public MagicSkillLaunched(Creature creature, int skillId, int skillLevel, Collection<WorldObject> targets)
	{
		_objectId = creature.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		if (targets == null)
		{
			_targets = Collections.singletonList(creature);
			return;
		}
		_targets = targets;
	}
	
	public MagicSkillLaunched(Creature creature, int skillId, int skillLevel, WorldObject target)
	{
		this(creature, skillId, skillLevel, Collections.singletonList(target == null ? creature : target));
	}
	
	public MagicSkillLaunched(Creature creature, int skillId, int skillLevel)
	{
		this(creature, skillId, skillLevel, Collections.singletonList(creature));
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.MAGIC_SKILL_LAUNCHED.writeId(this, buffer);
		buffer.writeInt(_objectId);
		buffer.writeInt(_skillId);
		buffer.writeInt(_skillLevel);
		buffer.writeInt(_targets.size());
		for (WorldObject target : _targets)
		{
			buffer.writeInt(target.getObjectId());
		}
	}
}
