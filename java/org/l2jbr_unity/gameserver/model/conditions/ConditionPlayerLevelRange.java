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
package org.l2jbr_unity.gameserver.model.conditions;

import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.item.ItemTemplate;
import org.l2jbr_unity.gameserver.model.skill.Skill;

/**
 * @author Zoey76
 */
public class ConditionPlayerLevelRange extends Condition
{
	private final int[] _levels;
	
	/**
	 * Instantiates a new condition player levels range.
	 * @param levels the {@code levels} range.
	 */
	public ConditionPlayerLevelRange(int[] levels)
	{
		_levels = levels;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		final int level = effector.getLevel();
		return (level >= _levels[0]) && (level <= _levels[1]);
	}
}
