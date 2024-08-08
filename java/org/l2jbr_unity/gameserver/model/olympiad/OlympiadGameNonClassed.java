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

/**
 * @author DS
 */
public class OlympiadGameNonClassed extends OlympiadGameNormal
{
	private OlympiadGameNonClassed(int id, Participant[] opponents)
	{
		super(id, opponents);
	}
	
	@Override
	public CompetitionType getType()
	{
		return CompetitionType.NON_CLASSED;
	}
	
	@Override
	protected final int getDivider()
	{
		return Config.OLYMPIAD_DIVIDER_NON_CLASSED;
	}
	
	@Override
	protected final int[][] getReward()
	{
		return Config.OLYMPIAD_NONCLASSED_REWARD;
	}
	
	@Override
	protected final String getWeeklyMatchType()
	{
		return COMP_DONE_WEEK_NON_CLASSED;
	}
	
	protected static OlympiadGameNonClassed createGame(int id, List<Integer> list)
	{
		final Participant[] opponents = OlympiadGameNormal.createListOfParticipants(list);
		return opponents == null ? null : new OlympiadGameNonClassed(id, opponents);
	}
}
