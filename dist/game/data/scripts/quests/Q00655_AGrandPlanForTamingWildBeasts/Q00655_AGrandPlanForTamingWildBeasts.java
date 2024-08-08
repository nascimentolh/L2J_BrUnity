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
package quests.Q00655_AGrandPlanForTamingWildBeasts;

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
public class Q00655_AGrandPlanForTamingWildBeasts extends Quest
{
	private static final int MESSENGER = 35627;
	
	private static final int STONE = 8084;
	private static final int TRAINER_LICENSE = 8293;
	
	private static final SiegableHall BEAST_STRONGHOLD = CHSiegeManager.getInstance().getSiegableHall(63);
	
	public Q00655_AGrandPlanForTamingWildBeasts()
	{
		super(655);
		
		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);
	}
	
	@Override
	public final String onTalk(Npc npc, Player player)
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
				if (BEAST_STRONGHOLD.getSiege().getAttackers().size() >= 5)
				{
					htmltext = "35627-00.htm";
				}
				else
				{
					htmltext = "35627-01.htm";
					st.startQuest();
				}
				break;
			}
			case State.STARTED:
			{
				if (getQuestItemsCount(player, STONE) < 10)
				{
					htmltext = "35627-02.htm";
				}
				else
				{
					takeItems(player, STONE, 10);
					giveItems(player, TRAINER_LICENSE, 1);
					st.exitQuest(true, true);
					htmltext = "35627-03.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	public static void checkCrystalofPurity(Player player)
	{
		final QuestState st = player.getQuestState(Q00655_AGrandPlanForTamingWildBeasts.class.getSimpleName());
		if ((st != null) && st.isCond(1) && (getQuestItemsCount(player, STONE) < 10))
		{
			giveItems(player, STONE, 1);
		}
	}
}
