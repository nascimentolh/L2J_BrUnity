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
package org.l2jbr_unity.gameserver.model.actor.tasks.player;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.stat.PlayerStat;
import org.l2jbr_unity.gameserver.model.zone.ZoneId;
import org.l2jbr_unity.gameserver.network.serverpackets.ExVitalityPointInfo;

/**
 * Task dedicated to reward player with vitality.
 * @author UnAfraid
 */
public class VitalityTask implements Runnable
{
	private final Player _player;
	
	public VitalityTask(Player player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if (!_player.isInsideZone(ZoneId.PEACE))
		{
			return;
		}
		
		if (_player.getVitalityPoints() >= PlayerStat.MAX_VITALITY_POINTS)
		{
			return;
		}
		
		_player.updateVitalityPoints(Config.RATE_RECOVERY_VITALITY_PEACE_ZONE, false, false);
		_player.sendPacket(new ExVitalityPointInfo(_player.getVitalityPoints()));
	}
}
