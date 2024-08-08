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
import org.l2jbr_unity.gameserver.model.conditions.Condition;
import org.l2jbr_unity.gameserver.model.effects.AbstractEffect;
import org.l2jbr_unity.gameserver.model.skill.Skill;
import org.l2jbr_unity.gameserver.model.stats.Formulas;
import org.l2jbr_unity.gameserver.network.SystemMessageId;

/**
 * Lethal effect implementation.
 * @author Adry_85
 */
public class Lethal extends AbstractEffect
{
	private final int _fullLethal;
	private final int _halfLethal;
	
	public Lethal(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_fullLethal = params.getInt("fullLethal", 0);
		_halfLethal = params.getInt("halfLethal", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		if (effector.isPlayer() && !effector.getAccessLevel().canGiveDamage())
		{
			return;
		}
		
		if (skill.getMagicLevel() < (effected.getLevel() - 6))
		{
			return;
		}
		
		if (!effected.isLethalable() || effected.isInvul())
		{
			return;
		}
		
		final double chanceMultiplier = Formulas.calcAttributeBonus(effector, effected, skill) * Formulas.calcGeneralTraitBonus(effector, effected, skill.getTraitType(), false);
		// Lethal Strike
		if (Rnd.get(100) < (_fullLethal * chanceMultiplier))
		{
			// for Players CP and HP is set to 1.
			if (effected.isPlayer())
			{
				effected.notifyDamageReceived(effected.getCurrentHp() - 1, effector, skill, true, false);
				effected.setCurrentCp(1);
				effected.setCurrentHp(1);
				effected.sendPacket(SystemMessageId.LETHAL_STRIKE);
			}
			// for Monsters HP is set to 1.
			else if (effected.isMonster() || effected.isSummon())
			{
				effected.notifyDamageReceived(effected.getCurrentHp() - 1, effector, skill, true, false);
				effected.setCurrentHp(1);
			}
			effector.sendPacket(SystemMessageId.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
		}
		// Half-Kill
		else if (Rnd.get(100) < (_halfLethal * chanceMultiplier))
		{
			// for Players CP is set to 1.
			if (effected.isPlayer())
			{
				effected.setCurrentCp(1);
				effected.sendPacket(SystemMessageId.HALF_KILL);
				effected.sendPacket(SystemMessageId.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_HALF_KILL_SKILL);
			}
			// for Monsters HP is set to 50%.
			else if (effected.isMonster() || effected.isSummon())
			{
				effected.notifyDamageReceived(effected.getCurrentHp() * 0.5, effector, skill, true, false);
				effected.setCurrentHp(effected.getCurrentHp() * 0.5);
			}
			effector.sendPacket(SystemMessageId.HALF_KILL);
		}
	}
}