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
package handlers.targethandlers;

import java.util.Collections;
import java.util.List;

import org.l2jbr_unity.gameserver.handler.ITargetTypeHandler;
import org.l2jbr_unity.gameserver.model.WorldObject;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.skill.Skill;
import org.l2jbr_unity.gameserver.model.skill.targets.TargetType;
import org.l2jbr_unity.gameserver.network.SystemMessageId;

/**
 * @author UnAfraid
 */
public class PartyMember implements ITargetTypeHandler
{
	@Override
	public List<WorldObject> getTargetList(Skill skill, Creature creature, boolean onlyFirst, Creature target)
	{
		if (target == null)
		{
			creature.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
			return Collections.emptyList();
		}
		
		if (!target.isDead() && ((target == creature) || (creature.isInParty() && target.isInParty() && (creature.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId())) || (creature.isPlayer() && target.isSummon() && (creature.getSummon() == target)) || (creature.isSummon() && target.isPlayer() && (creature == target.getSummon()))))
		{
			return Collections.singletonList(target);
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.PARTY_MEMBER;
	}
}
