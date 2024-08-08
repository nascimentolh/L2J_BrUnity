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

import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.instance.Door;
import org.l2jbr_unity.gameserver.model.variables.PlayerVariables;

public class UCPoint
{
	private final Location _loc;
	private final List<Door> _doors;
	private final List<Player> _players = new ArrayList<>();
	
	public UCPoint(List<Door> doors, Location loc)
	{
		_doors = doors;
		_loc = loc;
	}
	
	public void teleportPlayer(Player player)
	{
		if (player == null)
		{
			return;
		}
		
		player.getVariables().set(PlayerVariables.RESTORE_LOCATION, player.getLocation().getX() + ";" + player.getLocation().getY() + ";" + player.getLocation().getZ());
		
		if (player.isDead())
		{
			UCTeam.resPlayer(player);
		}
		
		player.teleToLocation(_loc, true);
		_players.add(player);
	}
	
	public void actionDoors(boolean open)
	{
		if (_doors.isEmpty())
		{
			return;
		}
		
		for (Door door : _doors)
		{
			if (open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
		}
	}
	
	public Location getLocation()
	{
		return _loc;
	}
	
	public List<Player> getPlayers()
	{
		return _players;
	}
	
	public boolean checkPlayer(Player player)
	{
		if (_players.contains(player))
		{
			actionDoors(true);
			for (Player pl : _players)
			{
				if (pl != null)
				{
					pl.setUCState(Player.UC_STATE_ARENA);
				}
			}
			return true;
		}
		return false;
	}
}
