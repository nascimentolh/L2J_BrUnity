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
package ai.areas.Gracia.AI.NPC.FortuneTelling;

import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.itemcontainer.Inventory;

import ai.AbstractNpcAI;

/**
 * Fortune Telling AI.<br>
 * Original Jython script by Kerberos.
 * @author Nyaran
 */
public class FortuneTelling extends AbstractNpcAI
{
	// NPC
	private static final int MINE = 32616;
	// Misc
	private static final int COST = 1000;
	
	public FortuneTelling()
	{
		addStartNpc(MINE);
		addTalkId(MINE);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext;
		if (player.getAdena() < COST)
		{
			htmltext = "lowadena.htm";
		}
		else
		{
			takeItems(player, Inventory.ADENA_ID, COST);
			htmltext = getHtm(player, "fortune.htm").replace("%fortune%", String.valueOf(getRandom(1800309, 1800695)));
		}
		return htmltext;
	}
}