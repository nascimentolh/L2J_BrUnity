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
package org.l2jbr_unity.gameserver.model.actor.instance;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.gameserver.enums.ClassId;
import org.l2jbr_unity.gameserver.enums.Race;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.templates.NpcTemplate;
import org.l2jbr_unity.gameserver.model.quest.QuestState;

public class VillageMasterKamael extends VillageMaster
{
	/**
	 * Creates a village master.
	 * @param template the village master NPC template
	 */
	public VillageMasterKamael(NpcTemplate template)
	{
		super(template);
	}
	
	@Override
	protected final String getSubClassMenu(Race race)
	{
		if (Config.ALT_GAME_SUBCLASS_EVERYWHERE || (race == Race.KAMAEL))
		{
			return "data/html/villagemaster/SubClass.htm";
		}
		return "data/html/villagemaster/SubClass_NoKamael.htm";
	}
	
	@Override
	protected final String getSubClassFail()
	{
		return "data/html/villagemaster/SubClass_Fail_Kamael.htm";
	}
	
	@Override
	protected final boolean checkQuests(Player player)
	{
		// Noble players can add subbclasses without quests
		if (player.isNoble())
		{
			return true;
		}
		
		QuestState qs = player.getQuestState("Q00234_FatesWhisper");
		if ((qs == null) || !qs.isCompleted())
		{
			return false;
		}
		
		qs = player.getQuestState("Q00236_SeedsOfChaos");
		if ((qs == null) || !qs.isCompleted())
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	protected final boolean checkVillageMasterRace(ClassId pClass)
	{
		if (pClass == null)
		{
			return false;
		}
		return pClass.getRace() == Race.KAMAEL;
	}
}