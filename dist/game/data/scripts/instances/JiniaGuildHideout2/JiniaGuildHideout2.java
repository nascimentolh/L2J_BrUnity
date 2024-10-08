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
package instances.JiniaGuildHideout2;

import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.instancezone.InstanceWorld;
import org.l2jbr_unity.gameserver.model.quest.QuestState;

import instances.AbstractInstance;
import quests.Q10285_MeetingSirra.Q10285_MeetingSirra;

/**
 * Jinia Guild Hideout instance zone.
 * @author Adry_85
 */
public class JiniaGuildHideout2 extends AbstractInstance
{
	// NPC
	private static final int RAFFORTY = 32020;
	// Location
	private static final Location START_LOC = new Location(-23530, -8963, -5413, 0, 0);
	// Misc
	private static final int TEMPLATE_ID = 141;
	
	private JiniaGuildHideout2()
	{
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY);
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		final QuestState qs = talker.getQuestState(Q10285_MeetingSirra.class.getSimpleName());
		if ((qs != null) && qs.isMemoState(1))
		{
			enterInstance(talker, TEMPLATE_ID);
			qs.setCond(2, true);
		}
		return super.onTalk(npc, talker);
	}
	
	@Override
	public void onEnterInstance(Player player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player);
		}
		teleportPlayer(player, START_LOC, world.getInstanceId(), false);
	}
	
	public static void main(String[] args)
	{
		new JiniaGuildHideout2();
	}
}
