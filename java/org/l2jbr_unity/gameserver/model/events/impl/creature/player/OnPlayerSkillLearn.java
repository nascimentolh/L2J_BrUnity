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
package org.l2jbr_unity.gameserver.model.events.impl.creature.player;

import org.l2jbr_unity.gameserver.enums.AcquireSkillType;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.events.EventType;
import org.l2jbr_unity.gameserver.model.events.impl.IBaseEvent;
import org.l2jbr_unity.gameserver.model.skill.Skill;

/**
 * @author UnAfraid
 */
public class OnPlayerSkillLearn implements IBaseEvent
{
	private final Npc _trainer;
	private final Player _player;
	private final Skill _skill;
	private final AcquireSkillType _type;
	
	public OnPlayerSkillLearn(Npc trainer, Player player, Skill skill, AcquireSkillType type)
	{
		_trainer = trainer;
		_player = player;
		_skill = skill;
		_type = type;
	}
	
	public Npc getTrainer()
	{
		return _trainer;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	public AcquireSkillType getAcquireType()
	{
		return _type;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_SKILL_LEARN;
	}
}
