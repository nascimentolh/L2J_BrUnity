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
package ai.areas.MithrilMines.MithrilMinesTeleporter;

import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Mithril Mines teleport AI.
 * @author Charus
 */
public class MithrilMinesTeleporter extends AbstractNpcAI
{
	// NPC
	private static final int TELEPORT_CRYSTAL = 32652;
	// Location
	private static final Location[] LOCS =
	{
		new Location(171946, -173352, 3440),
		new Location(175499, -181586, -904),
		new Location(173462, -174011, 3480),
		new Location(179299, -182831, -224),
		new Location(178591, -184615, -360),
		new Location(175499, -181586, -904)
	};
	
	private MithrilMinesTeleporter()
	{
		addStartNpc(TELEPORT_CRYSTAL);
		addFirstTalkId(TELEPORT_CRYSTAL);
		addTalkId(TELEPORT_CRYSTAL);
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		final int index = Integer.parseInt(event) - 1;
		if (LOCS.length > index)
		{
			final Location loc = LOCS[index];
			player.teleToLocation(loc, false);
		}
		return super.onEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (npc.isInsideRadius2D(173147, -173762, 0, Npc.INTERACTION_DISTANCE))
		{
			return "32652-01.htm";
		}
		
		if (npc.isInsideRadius2D(181941, -174614, 0, Npc.INTERACTION_DISTANCE))
		{
			return "32652-02.htm";
		}
		
		if (npc.isInsideRadius2D(179560, -182956, 0, Npc.INTERACTION_DISTANCE))
		{
			return "32652-03.htm";
		}
		return super.onFirstTalk(npc, player);
	}
	
	public static void main(String[] args)
	{
		new MithrilMinesTeleporter();
	}
}
