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

import java.util.ArrayList;
import java.util.List;

public class UCRunningTask implements Runnable
{
	private final UCArena _arena;
	
	public UCRunningTask(UCArena arena)
	{
		_arena = arena;
	}
	
	@Override
	public void run()
	{
		_arena.generateWinner();
		_arena.removeTeams();
		
		UCTeam winnerTeam = null;
		for (UCTeam team : _arena.getTeams())
		{
			if (team.getStatus() == UCTeam.WIN)
			{
				winnerTeam = team;
			}
			else if (team.getStatus() == UCTeam.FAIL)
			{
				team.cleanUp();
			}
		}
		
		for (UCPoint point : _arena.getPoints())
		{
			point.actionDoors(false);
			point.getPlayers().clear();
		}
		
		if (winnerTeam != null)
		{
			if (_arena.getWaitingList().size() >= 1)
			{
				final UCTeam other = winnerTeam.getOtherTeam();
				final UCWaiting otherWaiting = _arena.getWaitingList().get(0);
				other.setParty(otherWaiting.getParty());
				other.setRegisterTime(otherWaiting.getRegisterMillis());
				_arena.getWaitingList().remove(0);
				_arena.prepareStart();
				return;
			}
			
			winnerTeam.cleanUp();
		}
		
		if (_arena.getWaitingList().size() >= 2)
		{
			int i = 0;
			UCWaiting teamWaiting = null;
			final List<UCWaiting> removeList = new ArrayList<>();
			for (UCTeam team : _arena.getTeams())
			{
				teamWaiting = _arena.getWaitingList().get(i);
				removeList.add(teamWaiting);
				team.setParty(teamWaiting.getParty());
				team.setRegisterTime(teamWaiting.getRegisterMillis());
				i++;
				if (i == 2)
				{
					break;
				}
			}
			
			for (UCWaiting tm : removeList)
			{
				if (_arena.getWaitingList().contains(tm))
				{
					_arena.getWaitingList().remove(tm);
				}
			}
			removeList.clear();
			_arena.prepareStart();
			return;
		}
		
		_arena.setIsBattleNow(false);
		_arena.runNewTask(false);
	}
}
