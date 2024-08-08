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
package org.l2jbr_unity.gameserver.network.clientpackets;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.gameserver.model.WorldObject;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.ExVoteSystemInfo;
import org.l2jbr_unity.gameserver.network.serverpackets.SystemMessage;

public class RequestVoteNew extends ClientPacket
{
	private int _targetId;
	
	@Override
	protected void readImpl()
	{
		_targetId = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		final WorldObject object = player.getTarget();
		if (!(object instanceof Player))
		{
			if (object == null)
			{
				player.sendPacket(SystemMessageId.SELECT_TARGET);
			}
			else
			{
				player.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
			}
			return;
		}
		
		final Player target = (Player) object;
		if (target.getObjectId() != _targetId)
		{
			return;
		}
		
		if (target == player)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
			return;
		}
		
		if (player.getRecomLeft() <= 0)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_OUT_OF_RECOMMENDATIONS_TRY_AGAIN_LATER);
			return;
		}
		
		if (target.getRecomHave() >= 255)
		{
			player.sendPacket(SystemMessageId.YOUR_SELECTED_TARGET_CAN_NO_LONGER_RECEIVE_A_RECOMMENDATION);
			return;
		}
		
		player.giveRecom(target);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT);
		sm.addPcName(target);
		sm.addInt(player.getRecomLeft());
		player.sendPacket(sm);
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_C1);
		sm.addPcName(player);
		target.sendPacket(sm);
		
		target.broadcastUserInfo();
		
		if (Config.NEVIT_ENABLED)
		{
			player.sendPacket(new ExVoteSystemInfo(player));
			target.sendPacket(new ExVoteSystemInfo(target));
		}
		
		// Store player recommendations to avoid reseting them with Nevit peace zone check.
		player.storeRecommendationValues();
		target.storeRecommendationValues();
	}
}
