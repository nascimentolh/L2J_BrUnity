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
package quests.Q00697_DefendTheHallOfErosion;

import org.l2jbr_unity.gameserver.instancemanager.SoIManager;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.quest.Quest;
import org.l2jbr_unity.gameserver.model.quest.QuestState;
import org.l2jbr_unity.gameserver.model.quest.State;

public class Q00697_DefendTheHallOfErosion extends Quest
{
	private static final int TEPIOS = 32603;
	private static final int VESPER_STONE = 14052;
	
	public Q00697_DefendTheHallOfErosion()
	{
		super(697);
		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("32603-03.html"))
		{
			qs.startQuest();
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (player.getLevel() < 75)
				{
					htmltext = "32603-00.html";
					qs.exitQuest(true);
					break;
				}
				if (SoIManager.getCurrentStage() != 4)
				{
					htmltext = "32603-00a.html";
					qs.exitQuest(true);
					break;
				}
				htmltext = "32603-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1) && (qs.getInt("defenceDone") == 0))
				{
					htmltext = "32603-04.html";
				}
				else if (qs.isCond(1) && (qs.getInt("defenceDone") != 0))
				{
					giveItems(player, VESPER_STONE, getRandom(12, 20));
					qs.unset("defenceDone");
					qs.exitQuest(true);
					htmltext = "32603-05.html";
				}
				break;
			}
		}
		return htmltext;
	}
}