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

import org.l2jbr_unity.gameserver.ai.CtrlEvent;
import org.l2jbr_unity.gameserver.ai.CtrlIntention;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.conditions.Condition;
import org.l2jbr_unity.gameserver.model.effects.AbstractEffect;
import org.l2jbr_unity.gameserver.model.effects.EffectFlag;
import org.l2jbr_unity.gameserver.model.effects.EffectType;
import org.l2jbr_unity.gameserver.model.skill.Skill;

/**
 * Sleep effect implementation.
 * @author mkizub
 */
public class Sleep extends AbstractEffect
{
	public Sleep(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.SLEEP.getMask();
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SLEEP;
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		if (effected.isPlayer())
		{
			effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		else
		{
			effected.getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		effected.abortAttack();
		effected.abortCast();
		effected.stopMove(null);
		effected.getAI().notifyEvent(CtrlEvent.EVT_SLEEPING);
	}
}
