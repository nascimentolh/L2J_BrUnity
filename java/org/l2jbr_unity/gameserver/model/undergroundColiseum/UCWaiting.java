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

import org.l2jbr_unity.gameserver.model.Party;

public class UCWaiting
{
	private final Party _party;
	private long _registerMillis;
	private final UCArena _baseArena;
	
	public UCWaiting(Party party, UCArena baseArena)
	{
		_party = party;
		_baseArena = baseArena;
	}
	
	public void clean()
	{
		_registerMillis = 0L;
	}
	
	public UCArena getBaseArena()
	{
		return _baseArena;
	}
	
	public Party getParty()
	{
		if ((_party != null) && (_party.getLeader() == null))
		{
			setParty(false);
		}
		
		return _party;
	}
	
	public void setParty(boolean isActive)
	{
		if (isActive)
		{
			_party.setUCState(this);
		}
		else
		{
			_party.setUCState(null);
		}
	}
	
	public void hasRegisterdNow()
	{
		_registerMillis = System.currentTimeMillis();
	}
	
	public long getRegisterMillis()
	{
		return _registerMillis;
	}
}
