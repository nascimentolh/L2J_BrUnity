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
package org.l2jbr_unity.gameserver.model.fishing;

import org.l2jbr_unity.gameserver.model.StatSet;

/**
 * Class for the Fishing Monsters object.
 * @author nonom
 */
public class FishingMonster
{
	private final int _userMinLevel;
	private final int _userMaxLevel;
	private final int _fishingMonsterId;
	private final int _probability;
	
	public FishingMonster(StatSet set)
	{
		_userMinLevel = set.getInt("userMinLevel");
		_userMaxLevel = set.getInt("userMaxLevel");
		_fishingMonsterId = set.getInt("fishingMonsterId");
		_probability = set.getInt("probability");
	}
	
	/**
	 * @return the minimum user level.
	 */
	public int getUserMinLevel()
	{
		return _userMinLevel;
	}
	
	/**
	 * @return the maximum user level.
	 */
	public int getUserMaxLevel()
	{
		return _userMaxLevel;
	}
	
	/**
	 * @return the fishing monster Id.
	 */
	public int getFishingMonsterId()
	{
		return _fishingMonsterId;
	}
	
	/**
	 * @return the probability.
	 */
	public int getProbability()
	{
		return _probability;
	}
}