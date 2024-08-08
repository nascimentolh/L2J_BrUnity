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

import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jbr_unity.gameserver.model.partymatching.PartyMatchRoomList;
import org.l2jbr_unity.gameserver.model.partymatching.PartyMatchWaitingList;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.ActionFailed;
import org.l2jbr_unity.gameserver.network.serverpackets.ExPartyRoomMember;
import org.l2jbr_unity.gameserver.network.serverpackets.ListPartyWating;
import org.l2jbr_unity.gameserver.network.serverpackets.PartyMatchDetail;

/**
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPartyMatchConfig extends ClientPacket
{
	private int _auto;
	private int _loc;
	private int _level;
	
	@Override
	protected void readImpl()
	{
		_auto = readInt(); //
		_loc = readInt(); // Location
		_level = readInt(); // my level
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!player.isInPartyMatchRoom() && (player.getParty() != null) && (player.getParty().getLeader() != player))
		{
			player.sendPacket(SystemMessageId.THE_LIST_OF_PARTY_ROOMS_CAN_ONLY_BE_VIEWED_BY_A_PERSON_WHO_IS_NOT_PART_OF_A_PARTY);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInPartyMatchRoom())
		{
			// If Player is in Room show him room, not list
			final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
			if (list == null)
			{
				return;
			}
			
			final PartyMatchRoom room = list.getPlayerRoom(player);
			if (room == null)
			{
				return;
			}
			
			player.sendPacket(new PartyMatchDetail(room));
			player.sendPacket(new ExPartyRoomMember(room, 2));
			player.setPartyRoom(room.getId());
			// _activeChar.setPartyMatching(1);
			player.broadcastUserInfo();
		}
		else
		{
			// Add to waiting list
			PartyMatchWaitingList.getInstance().addPlayer(player);
			
			// Send Room list
			player.sendPacket(new ListPartyWating(player, _auto, _loc, _level));
		}
	}
}
