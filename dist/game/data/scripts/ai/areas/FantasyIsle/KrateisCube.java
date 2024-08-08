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
package ai.areas.FantasyIsle;

import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.krateisCube.KrateiArena;

import ai.AbstractNpcAI;

/**
 * Kratei's Cube AI
 * @author Mobius
 */
public class KrateisCube extends AbstractNpcAI
{
	// NPCs
	private static final int[] MONSTERS =
	{
		18579,
		18580,
		18581,
		18582,
		18583,
		18584,
		18585,
		18586,
		18587,
		18588,
		18589,
		18590,
		18591,
		18592,
		18593,
		18594,
		18595,
		18596,
		18597,
		18598,
		18599,
		18600,
	};
	
	public KrateisCube()
	{
		addKillId(MONSTERS);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		if (player != null)
		{
			final KrateiArena arena = player.getKrateiArena();
			if (arena != null)
			{
				arena.addPoints(player, false);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new KrateisCube();
	}
}
