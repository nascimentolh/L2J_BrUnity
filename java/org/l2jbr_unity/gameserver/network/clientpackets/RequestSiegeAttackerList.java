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

import org.l2jbr_unity.gameserver.instancemanager.CHSiegeManager;
import org.l2jbr_unity.gameserver.instancemanager.CastleManager;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.siege.Castle;
import org.l2jbr_unity.gameserver.model.siege.clanhalls.SiegableHall;
import org.l2jbr_unity.gameserver.network.serverpackets.SiegeAttackerList;

/**
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestSiegeAttackerList extends ClientPacket
{
	private int _castleId;
	
	@Override
	protected void readImpl()
	{
		_castleId = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle != null)
		{
			player.sendPacket(new SiegeAttackerList(castle));
		}
		else
		{
			final SiegableHall hall = CHSiegeManager.getInstance().getSiegableHall(_castleId);
			if (hall != null)
			{
				player.sendPacket(new SiegeAttackerList(hall));
			}
		}
	}
}
