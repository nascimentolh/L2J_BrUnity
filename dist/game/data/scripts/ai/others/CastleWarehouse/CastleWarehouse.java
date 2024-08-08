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
package ai.others.CastleWarehouse;

import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Castle Warehouse Keeper AI.
 * @author malyelfik
 */
public class CastleWarehouse extends AbstractNpcAI
{
	// NPCs
	private static final int[] NPCS =
	{
		35099, // Warehouse Keeper (Gludio)
		35141, // Warehouse Keeper (Dion)
		35183, // Warehouse Keeper (Giran)
		35225, // Warehouse Keeper (Oren)
		35273, // Warehouse Keeper (Aden)
		35315, // Warehouse Keeper (Inadril)
		35362, // Warehouse Keeper (Goddard)
		35508, // Warehouse Keeper (Rune)
		35554, // Warehouse Keeper (Schuttgart)
	};
	// Items
	private static final int BLOOD_OATH = 9910;
	private static final int BLOOD_ALLIANCE = 9911;
	
	private CastleWarehouse()
	{
		addStartNpc(NPCS);
		addTalkId(NPCS);
		addFirstTalkId(NPCS);
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		switch (event)
		{
			case "warehouse-01.html":
			case "warehouse-02.html":
			case "warehouse-03.html":
			{
				break;
			}
			case "warehouse-04.html":
			{
				htmltext = !npc.isMyLord(player) ? "warehouse-no.html" : getHtm(player, "warehouse-04.html").replace("%blood%", Integer.toString(player.getClan().getBloodAllianceCount()));
				break;
			}
			case "Receive":
			{
				if (!npc.isMyLord(player))
				{
					htmltext = "warehouse-no.html";
				}
				else if (player.getClan().getBloodAllianceCount() == 0)
				{
					htmltext = "warehouse-05.html";
				}
				else
				{
					giveItems(player, BLOOD_ALLIANCE, player.getClan().getBloodAllianceCount());
					player.getClan().resetBloodAllianceCount();
					htmltext = "warehouse-06.html";
				}
				break;
			}
			case "Exchange":
			{
				if (!npc.isMyLord(player))
				{
					htmltext = "warehouse-no.html";
				}
				else if (!hasQuestItems(player, BLOOD_ALLIANCE))
				{
					htmltext = "warehouse-08.html";
				}
				else
				{
					takeItems(player, BLOOD_ALLIANCE, 1);
					giveItems(player, BLOOD_OATH, 30);
					htmltext = "warehouse-07.html";
				}
				break;
			}
			default:
			{
				htmltext = null;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "warehouse-01.html";
	}
	
	public static void main(String[] args)
	{
		new CastleWarehouse();
	}
}