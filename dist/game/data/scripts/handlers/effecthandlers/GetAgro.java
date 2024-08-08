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

import java.util.Set;

import org.l2jbr_unity.commons.util.Rnd;
import org.l2jbr_unity.gameserver.ai.CtrlIntention;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.World;
import org.l2jbr_unity.gameserver.model.actor.Attackable;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.actor.templates.NpcTemplate;
import org.l2jbr_unity.gameserver.model.conditions.Condition;
import org.l2jbr_unity.gameserver.model.effects.AbstractEffect;
import org.l2jbr_unity.gameserver.model.effects.EffectType;
import org.l2jbr_unity.gameserver.model.skill.Skill;

/**
 * Get Agro effect implementation.
 * @author Adry_85, Mobius
 */
public class GetAgro extends AbstractEffect
{
	private final int _chance;
	
	public GetAgro(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_chance = params.getInt("chance", 100);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.AGGRESSION;
	}
	
	@Override
	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		return Rnd.get(100) < _chance;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		if ((effected != null) && effected.isAttackable())
		{
			effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, effector);
			
			// Monsters from the same clan should assist.
			final NpcTemplate template = ((Attackable) effected).getTemplate();
			final Set<Integer> clans = template.getClans();
			if (clans != null)
			{
				World.getInstance().forEachVisibleObjectInRange(effected, Attackable.class, template.getClanHelpRange(), nearby ->
				{
					if (!nearby.isMovementDisabled() && nearby.getTemplate().isClan(clans))
					{
						nearby.addDamageHate(effector, 1, 200);
						nearby.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, effector);
						nearby.setRunning();
					}
				});
			}
		}
	}
}