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

import java.util.StringTokenizer;

import org.l2jbr_unity.commons.util.Rnd;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.templates.NpcTemplate;

public class UCHelper extends Folk
{
	private static final Location[][] LOCATIONS = new Location[][]
	{
		{
			new Location(-84451, -45452, -10728),
			new Location(-84580, -45587, -10728)
		},
		{
			new Location(-86154, -50429, -10728),
			new Location(-86118, -50624, -10728)
		},
		{
			new Location(-82009, -53652, -10728),
			new Location(-81802, -53665, -10728)
		},
		{
			new Location(-77603, -50673, -10728),
			new Location(-77586, -50503, -10728)
		},
		{
			new Location(-79186, -45644, -10728),
			new Location(-79309, -45561, -10728)
		}
	};
	
	public UCHelper(NpcTemplate template)
	{
		super(template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final StringTokenizer token = new StringTokenizer(command, " ");
		final String actualCommand = token.nextToken();
		if (actualCommand.startsWith("coliseum"))
		{
			final int index = Integer.parseInt(token.nextToken());
			final Location[] locs = LOCATIONS[index];
			player.teleToLocation(locs[Rnd.get(locs.length)], true);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/undergroundColiseum/" + pom + ".htm";
	}
}
