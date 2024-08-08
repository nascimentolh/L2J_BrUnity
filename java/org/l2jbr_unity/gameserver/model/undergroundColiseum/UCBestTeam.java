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
package org.l2jbr_unity.gameserver.model.undergroundColiseum;

public class UCBestTeam
{
	private final int _arenaId;
	private String _leaderName;
	private int _wins;
	
	public UCBestTeam(int arenaId, String leaderName, int wins)
	{
		_arenaId = arenaId;
		_leaderName = leaderName;
		_wins = wins;
	}
	
	public int getArenaId()
	{
		return _arenaId;
	}
	
	public String getLeaderName()
	{
		return _leaderName;
	}
	
	public void setLeader(String leader)
	{
		_leaderName = leader;
	}
	
	public int getWins()
	{
		return _wins;
	}
	
	public void setWins(int wins)
	{
		_wins = wins;
	}
}