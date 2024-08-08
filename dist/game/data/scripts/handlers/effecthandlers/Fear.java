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
import org.l2jbr_unity.gameserver.enums.Race;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.actor.instance.Defender;
import org.l2jbr_unity.gameserver.model.actor.instance.FortCommander;
import org.l2jbr_unity.gameserver.model.actor.instance.SiegeFlag;
import org.l2jbr_unity.gameserver.model.conditions.Condition;
import org.l2jbr_unity.gameserver.model.effects.AbstractEffect;
import org.l2jbr_unity.gameserver.model.effects.EffectFlag;
import org.l2jbr_unity.gameserver.model.effects.EffectType;
import org.l2jbr_unity.gameserver.model.skill.Skill;

/**
 * Fear effect implementation.
 * @author littlecrow
 */
public class Fear extends AbstractEffect
{
	public Fear(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean canStart(Creature effector, Creature effected, Skill skill)
	{
		return effected.isPlayer() || effected.isSummon() || (effected.isAttackable() && //
			!((effected instanceof Defender) || (effected instanceof FortCommander) || //
				(effected instanceof SiegeFlag) || (effected.getTemplate().getRace() == Race.SIEGE_WEAPON)));
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.FEAR.getMask();
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.FEAR;
	}
	
	@Override
	public int getTicks()
	{
		return 5;
	}
	
	@Override
	public boolean onActionTime(Creature effector, Creature effected, Skill skill)
	{
		effected.getAI().notifyEvent(CtrlEvent.EVT_AFRAID, effector, false);
		return false;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		if (effected.isCastingNow() && effected.canAbortCast())
		{
			effected.abortCast();
		}
		
		effected.getAI().notifyEvent(CtrlEvent.EVT_AFRAID, effector, true);
	}
}
