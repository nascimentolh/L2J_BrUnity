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
package org.l2jbr_unity.gameserver.model.krateisCube;

import org.l2jbr_unity.gameserver.model.actor.Player;

/**
 * @author LordWinter
 */
public class KrateiCubePlayer
{
	private final Player _player;
	private boolean _isInside = false;
	private boolean _isRegister = false;
	private int _points = 0;
	
	public KrateiCubePlayer(Player player)
	{
		_player = player;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public void setIsRegister(boolean value)
	{
		_isRegister = value;
	}
	
	public boolean isRegister()
	{
		return _isRegister;
	}
	
	public void setIsInside(boolean value)
	{
		_isInside = value;
	}
	
	public boolean isInside()
	{
		return _isInside;
	}
	
	public void addPoints(int points)
	{
		_points = points;
	}
	
	public int getPoints()
	{
		return _points;
	}
}