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
package org.l2jbr_unity.gameserver.network.serverpackets;

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.clan.Clan;
import org.l2jbr_unity.gameserver.model.clan.Clan.SubPledge;
import org.l2jbr_unity.gameserver.model.clan.ClanMember;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class PledgeShowMemberListAll extends ServerPacket
{
	private final Clan _clan;
	private final Player _player;
	private final ClanMember[] _members;
	private int _pledgeType;
	
	public PledgeShowMemberListAll(Clan clan, Player player)
	{
		_clan = clan;
		_player = player;
		_members = _clan.getMembers();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		_pledgeType = 0;
		// FIXME: That's wrong on retail sends this whole packet few times (depending how much sub pledges it has)
		writePledge(0, buffer);
		for (SubPledge subPledge : _clan.getAllSubPledges())
		{
			_player.sendPacket(new PledgeReceiveSubPledgeCreated(subPledge, _clan));
		}
		for (ClanMember m : _members)
		{
			if (m.getPledgeType() == 0)
			{
				continue;
			}
			_player.sendPacket(new PledgeShowMemberListAdd(m));
		}
		// unless this is sent sometimes, the client doesn't recognise the player as the leader
		_player.updateUserInfo();
	}
	
	private void writePledge(int mainOrSubpledge, WritableBuffer buffer)
	{
		ServerPackets.PLEDGE_SHOW_MEMBER_LIST_ALL.writeId(this, buffer);
		buffer.writeInt(mainOrSubpledge);
		buffer.writeInt(_clan.getId());
		buffer.writeInt(_pledgeType);
		buffer.writeString(_clan.getName());
		buffer.writeString(_clan.getLeaderName());
		buffer.writeInt(_clan.getCrestId()); // crest id .. is used again
		buffer.writeInt(_clan.getLevel());
		buffer.writeInt(_clan.getCastleId());
		buffer.writeInt(_clan.getHideoutId());
		buffer.writeInt(_clan.getFortId());
		buffer.writeInt(_clan.getRank());
		buffer.writeInt(_clan.getReputationScore());
		buffer.writeInt(0); // 0
		buffer.writeInt(0); // 0
		buffer.writeInt(_clan.getAllyId());
		buffer.writeString(_clan.getAllyName());
		buffer.writeInt(_clan.getAllyCrestId());
		buffer.writeInt(_clan.isAtWar()); // new c3
		buffer.writeInt(0); // Territory castle ID
		buffer.writeInt(_clan.getSubPledgeMembersCount(_pledgeType));
		for (ClanMember m : _members)
		{
			if (m.getPledgeType() != _pledgeType)
			{
				continue;
			}
			buffer.writeString(m.getName());
			buffer.writeInt(m.getLevel());
			buffer.writeInt(m.getClassId());
			final Player player = m.getPlayer();
			if (player != null)
			{
				buffer.writeInt(player.getAppearance().isFemale()); // no visible effect
				buffer.writeInt(player.getRace().ordinal()); // buffer.writeInt(1);
			}
			else
			{
				buffer.writeInt(1); // no visible effect
				buffer.writeInt(1); // buffer.writeInt(1);
			}
			buffer.writeInt(m.isOnline() ? m.getObjectId() : 0); // objectId = online 0 = offline
			buffer.writeInt(m.getSponsor() != 0);
		}
	}
}
