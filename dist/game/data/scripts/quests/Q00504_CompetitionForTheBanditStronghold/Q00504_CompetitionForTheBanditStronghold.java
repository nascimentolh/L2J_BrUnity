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
package quests.Q00504_CompetitionForTheBanditStronghold;

import org.l2jbr_unity.gameserver.enums.QuestSound;
import org.l2jbr_unity.gameserver.instancemanager.CHSiegeManager;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.quest.Quest;
import org.l2jbr_unity.gameserver.model.quest.QuestState;
import org.l2jbr_unity.gameserver.model.quest.State;
import org.l2jbr_unity.gameserver.model.siege.clanhalls.SiegableHall;

/**
 * @author LordWinter
 */
public final class Q00504_CompetitionForTheBanditStronghold extends Quest
{
	private static final int MESSENGER = 35437;
	
	private static final int TARLK_AMULET = 4332;
	private static final int TROPHY_OF_ALLIANCE = 5009;
	
	private static final int[] MOBS =
	{
		20570,
		20571,
		20572,
		20573,
		20574
	};
	
	private static final SiegableHall BANDIT_STRONGHOLD = CHSiegeManager.getInstance().getSiegableHall(35);
	
	public Q00504_CompetitionForTheBanditStronghold()
	{
		super(504);
		
		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);
		
		for (int mob : MOBS)
		{
			addKillId(mob);
		}
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (BANDIT_STRONGHOLD.getSiege().getAttackers().size() >= 5)
				{
					htmltext = "35437-00.htm";
				}
				else
				{
					htmltext = "35437-01.htm";
					st.startQuest();
				}
				break;
			}
			case State.STARTED:
			{
				if (getQuestItemsCount(player, TARLK_AMULET) < 30)
				{
					htmltext = "35437-02.htm";
				}
				else
				{
					takeItems(player, TARLK_AMULET, 30);
					rewardItems(player, TROPHY_OF_ALLIANCE, 1);
					st.exitQuest(true);
					htmltext = "35437-03.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState st = getQuestState(killer, false);
		if (st == null)
		{
			return null;
		}
		
		if (st.isStarted() && st.isCond(1))
		{
			giveItems(killer, TARLK_AMULET, 1);
			if (getQuestItemsCount(killer, TARLK_AMULET) < 30)
			{
				playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else
			{
				st.setCond(2, true);
			}
		}
		
		return super.onKill(npc, killer, isSummon);
	}
}
