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
package handlers.bypasshandlers;

import org.l2jbr_unity.gameserver.handler.IBypassHandler;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.instance.Adventurer;
import org.l2jbr_unity.gameserver.network.serverpackets.ExShowQuestInfo;

public class QuestList implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"questlist"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (!(target instanceof Adventurer))
		{
			return false;
		}
		
		player.sendPacket(ExShowQuestInfo.STATIC_PACKET);
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
