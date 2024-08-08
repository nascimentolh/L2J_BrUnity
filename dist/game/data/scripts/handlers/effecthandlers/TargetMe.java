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

import org.l2jbr_unity.commons.util.Rnd;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.actor.Playable;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.conditions.Condition;
import org.l2jbr_unity.gameserver.model.effects.AbstractEffect;
import org.l2jbr_unity.gameserver.model.skill.Skill;

/**
 * Target Me effect implementation.
 * @author -Nemesiss-, Mobius
 */
public class TargetMe extends AbstractEffect
{
	private final int _chance;
	
	public TargetMe(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_chance = params.getInt("chance", 100);
	}
	
	@Override
	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		return Rnd.get(100) < _chance;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		if (effected.isPlayable())
		{
			if (effected.getTarget() != effector)
			{
				// If effector is null, then its not a player, but NPC. If it is not null, then it should check if the skill is pvp skill.
				final Player player = effector.getActingPlayer();
				if ((player == null) || player.checkPvpSkill(effected, skill))
				{
					// Target is different.
					effected.setTarget(player);
				}
			}
			
			((Playable) effected).setLockedTarget(effector);
		}
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		if (effected.isPlayable())
		{
			((Playable) effected).setLockedTarget(null);
		}
	}
}
