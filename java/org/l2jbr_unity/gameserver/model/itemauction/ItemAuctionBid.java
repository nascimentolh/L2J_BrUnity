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
package org.l2jbr_unity.gameserver.model.itemauction;

import org.l2jbr_unity.gameserver.model.World;
import org.l2jbr_unity.gameserver.model.actor.Player;

/**
 * @author Forsaiken
 */
public class ItemAuctionBid
{
	private final int _playerObjId;
	private long _lastBid;
	
	public ItemAuctionBid(int playerObjId, long lastBid)
	{
		_playerObjId = playerObjId;
		_lastBid = lastBid;
	}
	
	public int getPlayerObjId()
	{
		return _playerObjId;
	}
	
	public long getLastBid()
	{
		return _lastBid;
	}
	
	public void setLastBid(long lastBid)
	{
		_lastBid = lastBid;
	}
	
	public void cancelBid()
	{
		_lastBid = -1;
	}
	
	public boolean isCanceled()
	{
		return _lastBid <= 0;
	}
	
	public Player getPlayer()
	{
		return World.getInstance().getPlayer(_playerObjId);
	}
}