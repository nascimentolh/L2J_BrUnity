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

import org.l2jbr_unity.gameserver.enums.PartyMessageType;
import org.l2jbr_unity.gameserver.model.Party;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jbr_unity.gameserver.model.partymatching.PartyMatchRoomList;
import org.l2jbr_unity.gameserver.network.serverpackets.ExClosePartyRoom;
import org.l2jbr_unity.gameserver.network.serverpackets.ExPartyRoomMember;
import org.l2jbr_unity.gameserver.network.serverpackets.PartyMatchDetail;

/**
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestWithDrawalParty extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Party party = player.getParty();
		if (party != null)
		{
			if (player.getUCState() != Player.UC_STATE_NONE)
			{
				player.sendMessage("You can't dismiss party member when you are in Underground Coliseum.");
			}
			else if (party.isInDimensionalRift() && !party.getDimensionalRift().getRevivedAtWaitingRoom().contains(player))
			{
				player.sendMessage("You can't exit party when you are in Dimensional Rift.");
			}
			else
			{
				party.removePartyMember(player, PartyMessageType.LEFT);
				if (player.isInPartyMatchRoom())
				{
					final PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
					if (room != null)
					{
						player.sendPacket(new PartyMatchDetail(room));
						player.sendPacket(new ExPartyRoomMember(room, 0));
						player.sendPacket(new ExClosePartyRoom());
						room.deleteMember(player);
					}
					player.setPartyRoom(0);
					// player.setPartyMatching(0);
					player.broadcastUserInfo();
				}
			}
		}
	}
}
