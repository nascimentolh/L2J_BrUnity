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
package org.l2jbr_unity.gameserver.cache;

/**
 * @author Sahar
 */
public class RelationCache
{
	private final int _relation;
	private final boolean _isAutoAttackable;
	
	public RelationCache(int relation, boolean isAutoAttackable)
	{
		_relation = relation;
		_isAutoAttackable = isAutoAttackable;
	}
	
	public int getRelation()
	{
		return _relation;
	}
	
	public boolean isAutoAttackable()
	{
		return _isAutoAttackable;
	}
}