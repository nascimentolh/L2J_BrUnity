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

import java.util.HashMap;
import java.util.Map;

import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.conditions.Condition;
import org.l2jbr_unity.gameserver.model.effects.AbstractEffect;
import org.l2jbr_unity.gameserver.model.effects.EffectFlag;
import org.l2jbr_unity.gameserver.model.effects.EffectType;
import org.l2jbr_unity.gameserver.model.skill.Skill;
import org.l2jbr_unity.gameserver.model.stats.Stat;

/**
 * Servitor Share effect implementation.<br>
 * Synchronizing effects on player and servitor if one of them gets removed for some reason the same will happen to another. Partner's effect exit is executed in own thread, since there is no more queue to schedule the effects,<br>
 * partner's effect is called while this effect is still exiting issuing an exit call for the effect, causing a stack over flow.
 * @author UnAfraid, Zoey76
 */
public class ServitorShare extends AbstractEffect
{
	private final Map<Stat, Double> _stats = new HashMap<>(9);
	
	public ServitorShare(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		for (String key : params.getSet().keySet())
		{
			_stats.put(Stat.valueOfXml(key), params.getDouble(key, 1.));
		}
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.SERVITOR_SHARE.getMask();
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		super.onStart(effector, effected, skill);
		
		effected.getActingPlayer().setServitorShare(_stats);
		if (effected.getActingPlayer().getSummon() != null)
		{
			effected.getActingPlayer().getSummon().broadcastInfo();
			effected.getActingPlayer().getSummon().getStatus().startHpMpRegeneration();
		}
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.getActingPlayer().setServitorShare(null);
		if (effected.getSummon() != null)
		{
			if (effected.getSummon().getCurrentHp() > effected.getSummon().getMaxHp())
			{
				effected.getSummon().setCurrentHp(effected.getSummon().getMaxHp());
			}
			if (effected.getSummon().getCurrentMp() > effected.getSummon().getMaxMp())
			{
				effected.getSummon().setCurrentMp(effected.getSummon().getMaxMp());
			}
			effected.getSummon().broadcastInfo();
		}
	}
}
