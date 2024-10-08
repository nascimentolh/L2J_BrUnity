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

import java.util.Collection;

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.data.sql.ClanTable;
import org.l2jbr_unity.gameserver.model.clan.Clan;
import org.l2jbr_unity.gameserver.model.clan.ClanInfo;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;
import org.l2jbr_unity.gameserver.network.clientpackets.RequestAllyInfo;

/**
 * Sent in response to {@link RequestAllyInfo}, if applicable.<br>
 * @author afk5min
 */
public class AllianceInfo extends ServerPacket
{
	private final String _name;
	private final int _total;
	private final int _online;
	private final String _leaderC;
	private final String _leaderP;
	private final ClanInfo[] _allies;
	
	public AllianceInfo(int allianceId)
	{
		final Clan leader = ClanTable.getInstance().getClan(allianceId);
		_name = leader.getAllyName();
		_leaderC = leader.getName();
		_leaderP = leader.getLeaderName();
		final Collection<Clan> allies = ClanTable.getInstance().getClanAllies(allianceId);
		_allies = new ClanInfo[allies.size()];
		int idx = 0;
		int total = 0;
		int online = 0;
		for (Clan clan : allies)
		{
			final ClanInfo ci = new ClanInfo(clan);
			_allies[idx++] = ci;
			total += ci.getTotal();
			online += ci.getOnline();
		}
		_total = total;
		_online = online;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ALLIANCE_INFO.writeId(this, buffer);
		buffer.writeString(_name);
		buffer.writeInt(_total);
		buffer.writeInt(_online);
		buffer.writeString(_leaderC);
		buffer.writeString(_leaderP);
		buffer.writeInt(_allies.length);
		for (ClanInfo aci : _allies)
		{
			buffer.writeString(aci.getClan().getName());
			buffer.writeInt(0);
			buffer.writeInt(aci.getClan().getLevel());
			buffer.writeString(aci.getClan().getLeaderName());
			buffer.writeInt(aci.getTotal());
			buffer.writeInt(aci.getOnline());
		}
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getTotal()
	{
		return _total;
	}
	
	public int getOnline()
	{
		return _online;
	}
	
	public String getLeaderC()
	{
		return _leaderC;
	}
	
	public String getLeaderP()
	{
		return _leaderP;
	}
	
	public ClanInfo[] getAllies()
	{
		return _allies;
	}
}