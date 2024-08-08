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

import org.l2jbr_unity.Config;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.conditions.Condition;
import org.l2jbr_unity.gameserver.model.effects.AbstractEffect;
import org.l2jbr_unity.gameserver.model.skill.Skill;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.ExVoteSystemInfo;
import org.l2jbr_unity.gameserver.network.serverpackets.SystemMessage;

/**
 * Give Recommendation effect implementation.
 * @author NosBit
 */
public class GiveRecommendation extends AbstractEffect
{
	private final int _amount;
	
	public GiveRecommendation(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_amount = params.getInt("amount", 0);
		if (_amount == 0)
		{
			LOGGER.warning(getClass().getSimpleName() + ": amount parameter is missing or set to 0. id:" + set.getInt("id", -1));
		}
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		final Player target = (effected != null) && effected.isPlayer() ? (Player) effected : null;
		if (target != null)
		{
			int recommendationsGiven = _amount;
			if ((target.getRecomHave() + _amount) >= 255)
			{
				recommendationsGiven = 255 - target.getRecomHave();
			}
			
			if (recommendationsGiven > 0)
			{
				target.setRecomHave(target.getRecomHave() + recommendationsGiven);
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATION_S);
				sm.addInt(recommendationsGiven);
				target.sendPacket(sm);
				target.updateUserInfo();
				if (Config.NEVIT_ENABLED)
				{
					target.sendPacket(new ExVoteSystemInfo(target));
				}
			}
			else
			{
				final Player player = (effector != null) && effector.isPlayer() ? (Player) effector : null;
				if (player != null)
				{
					player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
			}
		}
	}
}
