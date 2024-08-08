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

import java.util.ArrayList;
import java.util.List;

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.undergroundColiseum.UCArena;
import org.l2jbr_unity.gameserver.model.undergroundColiseum.UCTeam;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class ExPVPMatchRecord extends ServerPacket
{
	public static final int START = 0;
	public static final int UPDATE = 1;
	public static final int FINISH = 2;
	
	private final int _type;
	private final int _winnerTeam;
	private final int _blueKills;
	private final int _redKills;
	private final List<Member> _blueList;
	private final List<Member> _redList;
	
	public ExPVPMatchRecord(int type, int winnerTeam, UCArena arena)
	{
		_type = type;
		_winnerTeam = winnerTeam;
		
		final UCTeam blueTeam = arena.getTeams()[0];
		_blueKills = blueTeam.getKillCount();
		final UCTeam redTeam = arena.getTeams()[1];
		_redKills = redTeam.getKillCount();
		
		_blueList = new ArrayList<>(9);
		
		if (blueTeam.getParty() != null)
		{
			for (Player memberObject : blueTeam.getParty().getMembers())
			{
				if (memberObject != null)
				{
					_blueList.add(new Member(memberObject.getName(), memberObject.getUCKills(), memberObject.getUCDeaths()));
				}
			}
		}
		
		_redList = new ArrayList<>(9);
		
		if (redTeam.getParty() != null)
		{
			for (Player memberObject : redTeam.getParty().getMembers())
			{
				if (memberObject != null)
				{
					_redList.add(new Member(memberObject.getName(), memberObject.getUCKills(), memberObject.getUCDeaths()));
				}
			}
		}
	}
	
	public ExPVPMatchRecord(int type, int winnerTeam)
	{
		_type = type;
		_winnerTeam = winnerTeam;
		_blueKills = 0;
		_redKills = 0;
		_blueList = new ArrayList<>(9);
		_redList = new ArrayList<>(9);
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PVP_MATCH_RECORD.writeId(this, buffer);
		buffer.writeInt(_type);
		buffer.writeInt(_winnerTeam);
		buffer.writeInt(_winnerTeam == 0 ? 0 : _winnerTeam == 1 ? 2 : 1);
		buffer.writeInt(_blueKills);
		buffer.writeInt(_redKills);
		buffer.writeInt(_blueList.size());
		for (Member member : _blueList)
		{
			buffer.writeString(member._name);
			buffer.writeInt(member._kills);
			buffer.writeInt(member._deaths);
		}
		buffer.writeInt(_redList.size());
		for (Member member : _redList)
		{
			buffer.writeString(member._name);
			buffer.writeInt(member._kills);
			buffer.writeInt(member._deaths);
		}
	}
	
	public static class Member
	{
		public String _name;
		public int _kills;
		public int _deaths;
		
		public Member(String name, int kills, int deaths)
		{
			_name = name;
			_kills = kills;
			_deaths = deaths;
		}
	}
}
