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
package quests.Q00510_AClansPrestige;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.gameserver.enums.QuestSound;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.clan.Clan;
import org.l2jbr_unity.gameserver.model.quest.Quest;
import org.l2jbr_unity.gameserver.model.quest.QuestState;
import org.l2jbr_unity.gameserver.model.quest.State;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jbr_unity.gameserver.network.serverpackets.SystemMessage;

/**
 * A Clan's Prestige (510)
 * @author Adry_85
 */
public class Q00510_AClansPrestige extends Quest
{
	// NPC
	private static final int VALDIS = 31331;
	// Quest Item
	private static final int TYRANNOSAURUS_CLAW = 8767;
	
	private static final int[] MOBS =
	{
		22215,
		22216,
		22217
	};
	
	public Q00510_AClansPrestige()
	{
		super(510);
		addStartNpc(VALDIS);
		addTalkId(VALDIS);
		addKillId(MOBS);
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "31331-3.html":
			{
				qs.startQuest();
				break;
			}
			case "31331-6.html":
			{
				qs.exitQuest(true, true);
				break;
			}
		}
		return event;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		if (player.getClan() == null)
		{
			return null;
		}
		
		QuestState qs = null;
		if (player.isClanLeader())
		{
			qs = getQuestState(player, false);
		}
		else
		{
			final Player pleader = player.getClan().getLeader().getPlayer();
			if ((pleader != null) && player.isInsideRadius3D(pleader, Config.ALT_PARTY_RANGE))
			{
				qs = getQuestState(pleader, false);
			}
		}
		
		if ((qs != null) && qs.isStarted())
		{
			rewardItems(player, TYRANNOSAURUS_CLAW, 1);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return null;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		final Clan clan = player.getClan();
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = ((clan == null) || !player.isClanLeader() || (clan.getLevel() < 5)) ? "31331-0.htm" : "31331-1.htm";
				break;
			}
			case State.STARTED:
			{
				if ((clan == null) || !player.isClanLeader())
				{
					qs.exitQuest(true);
					return "31331-8.html";
				}
				
				if (!hasQuestItems(player, TYRANNOSAURUS_CLAW))
				{
					htmltext = "31331-4.html";
				}
				else
				{
					final int count = (int) getQuestItemsCount(player, TYRANNOSAURUS_CLAW);
					final int reward = (count < 10) ? (30 * count) : (59 + (30 * count));
					playSound(player, QuestSound.ITEMSOUND_QUEST_FANFARE_1);
					takeItems(player, TYRANNOSAURUS_CLAW, -1);
					clan.addReputationScore(reward);
					player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_S_REPUTATION_SCORE).addInt(reward));
					clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
					htmltext = "31331-7.html";
				}
				break;
			}
			default:
			{
				break;
			}
		}
		return htmltext;
	}
}
