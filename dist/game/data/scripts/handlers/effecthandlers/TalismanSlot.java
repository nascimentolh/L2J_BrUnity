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
package handlers.effecthandlers;

import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.conditions.Condition;
import org.l2jbr_unity.gameserver.model.effects.AbstractEffect;
import org.l2jbr_unity.gameserver.model.skill.Skill;

/**
 * Talisman Slot effect implementation.
 * @author Adry_85
 */
public class TalismanSlot extends AbstractEffect
{
	private final int _slots;
	
	public TalismanSlot(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_slots = params.getInt("slots", 0);
	}
	
	@Override
	public boolean canStart(Creature effector, Creature effected, Skill skill)
	{
		return (effector != null) && (effected != null) && effected.isPlayer();
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		effected.getActingPlayer().getStat().addTalismanSlots(_slots);
	}
	
	@Override
	public boolean onActionTime(Creature effector, Creature effected, Skill skill)
	{
		return skill.isPassive();
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.getActingPlayer().getStat().addTalismanSlots(-_slots);
	}
}