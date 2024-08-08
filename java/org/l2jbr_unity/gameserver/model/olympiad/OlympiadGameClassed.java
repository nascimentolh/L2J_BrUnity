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
package org.l2jbr_unity.gameserver.model.olympiad;

import java.util.List;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.util.Rnd;

/**
 * @author DS
 */
public class OlympiadGameClassed extends OlympiadGameNormal
{
	private OlympiadGameClassed(int id, Participant[] opponents)
	{
		super(id, opponents);
	}
	
	@Override
	public CompetitionType getType()
	{
		return CompetitionType.CLASSED;
	}
	
	@Override
	protected final int getDivider()
	{
		return Config.OLYMPIAD_DIVIDER_CLASSED;
	}
	
	@Override
	protected final int[][] getReward()
	{
		return Config.OLYMPIAD_CLASSED_REWARD;
	}
	
	@Override
	protected final String getWeeklyMatchType()
	{
		return COMP_DONE_WEEK_CLASSED;
	}
	
	protected static OlympiadGameClassed createGame(int id, List<List<Integer>> classList)
	{
		if ((classList == null) || classList.isEmpty())
		{
			return null;
		}
		
		List<Integer> list;
		Participant[] opponents;
		while (!classList.isEmpty())
		{
			list = classList.get(Rnd.get(classList.size()));
			if ((list == null) || (list.size() < 2))
			{
				classList.remove(list);
				continue;
			}
			
			opponents = OlympiadGameNormal.createListOfParticipants(list);
			if (opponents == null)
			{
				classList.remove(list);
				continue;
			}
			
			return new OlympiadGameClassed(id, opponents);
		}
		return null;
	}
}