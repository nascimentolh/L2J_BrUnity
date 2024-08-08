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
import org.l2jbr_unity.gameserver.network.serverpackets.ExPartyRoomMember;
import org.l2jbr_unity.gameserver.network.serverpackets.PartyMatchDetail;

/**
 * @author Gnacik
 */
public class RequestPartyMatchList extends ClientPacket
{
	private int _roomid;
	private int _membersmax;
	private int _minLevel;
	private int _maxLevel;
	private int _loot;
	private String _roomtitle;
	
	@Override
	protected void readImpl()
	{
		_roomid = readInt();
		_membersmax = readInt();
		_minLevel = readInt();
		_maxLevel = readInt();
		_loot = readInt();
		_roomtitle = readString();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (_roomid > 0)
		{
			final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_roomid);
			if (room != null)
			{
				// PacketLogger.info("PartyMatchRoom #" + room.getId() + " changed by " + player.getName());
				room.setMaxMembers(_membersmax);
				room.setMinLevel(_minLevel);
				room.setMaxLevel(_maxLevel);
				room.setLootType(_loot);
				room.setTitle(_roomtitle);
				
				for (Player member : room.getPartyMembers())
				{
					if (member == null)
					{
						continue;
					}
					
					member.sendPacket(new PartyMatchDetail(room));
					member.sendPacket(SystemMessageId.THE_PARTY_ROOM_S_INFORMATION_HAS_BEEN_REVISED);
				}
			}
		}
		else
		{
			final int maxid = PartyMatchRoomList.getInstance().getMaxId();
			final PartyMatchRoom room = new PartyMatchRoom(maxid, _roomtitle, _loot, _minLevel, _maxLevel, _membersmax, player);
			
			// PacketLogger.info("PartyMatchRoom #" + maxid + " created by " + player.getName());
			// Remove from waiting list
			PartyMatchWaitingList.getInstance().removePlayer(player);
			
			PartyMatchRoomList.getInstance().addPartyMatchRoom(maxid, room);
			if (player.isInParty())
			{
				for (Player ptmember : player.getParty().getMembers())
				{
					if (ptmember == null)
					{
						continue;
					}
					if (ptmember == player)
					{
						continue;
					}
					
					ptmember.setPartyRoom(maxid);
					// ptmember.setPartyMatching(1);
					room.addMember(ptmember);
				}
			}
			player.sendPacket(new PartyMatchDetail(room));
			player.sendPacket(new ExPartyRoomMember(room, 1));
			player.sendPacket(SystemMessageId.YOU_HAVE_CREATED_A_PARTY_ROOM);
			
			player.setPartyRoom(maxid);
			// _activeChar.setPartyMatching(1);
			player.broadcastUserInfo();
		}
	}
}