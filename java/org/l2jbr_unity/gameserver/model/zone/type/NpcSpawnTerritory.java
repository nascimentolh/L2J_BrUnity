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
package org.l2jbr_unity.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;

import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.zone.ZoneForm;

/**
 * Just dummy zone, needs only for geometry calculations
 * @author GKR, Mobius
 */
public class NpcSpawnTerritory
{
	private final String _name;
	private final ZoneForm _territory;
	private List<ZoneForm> _bannedTerritories;
	
	public NpcSpawnTerritory(String name, ZoneForm territory)
	{
		_name = name;
		_territory = territory;
	}
	
	public void addBannedTerritory(ZoneForm territory)
	{
		if (_bannedTerritories == null)
		{
			_bannedTerritories = new ArrayList<>(1);
		}
		_bannedTerritories.add(territory);
	}
	
	public String getName()
	{
		return _name;
	}
	
	public Location getRandomPoint()
	{
		if (_bannedTerritories != null)
		{
			int count = 0; // Prevent infinite loop from wrongly written data.
			Location location;
			SEARCH: while (count++ < 1000)
			{
				location = _territory.getRandomPoint();
				for (ZoneForm territory : _bannedTerritories)
				{
					if (territory.isInsideZone(location.getX(), location.getY(), location.getZ()))
					{
						continue SEARCH;
					}
				}
				
				return location;
			}
		}
		
		return _territory.getRandomPoint();
	}
	
	public boolean isInsideZone(int x, int y, int z)
	{
		return _territory.isInsideZone(x, y, z);
	}
	
	public void visualizeZone(int z)
	{
		_territory.visualizeZone(z);
	}
}