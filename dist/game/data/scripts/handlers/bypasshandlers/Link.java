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

import java.util.HashSet;
import java.util.Set;

import org.l2jbr_unity.gameserver.cache.HtmCache;
import org.l2jbr_unity.gameserver.handler.IBypassHandler;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.instance.Teleporter;
import org.l2jbr_unity.gameserver.network.serverpackets.NpcHtmlMessage;

public class Link implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Link"
	};
	
	private static final Set<String> VALID_LINKS = new HashSet<>();
	static
	{
		VALID_LINKS.add("adventurer_guildsman/AboutHighLevelGuilds.htm");
		VALID_LINKS.add("adventurer_guildsman/AboutNewLifeCrystals.htm");
		VALID_LINKS.add("clanHallDoorman/evolve.htm");
		VALID_LINKS.add("common/augmentation_01.htm");
		VALID_LINKS.add("common/augmentation_02.htm");
		VALID_LINKS.add("common/crafting_01.htm");
		VALID_LINKS.add("common/duals_01.htm");
		VALID_LINKS.add("common/duals_02.htm");
		VALID_LINKS.add("common/duals_03.htm");
		VALID_LINKS.add("common/g_cube_warehouse001.htm");
		VALID_LINKS.add("common/skill_enchant_help.htm");
		VALID_LINKS.add("common/skill_enchant_help_01.htm");
		VALID_LINKS.add("common/skill_enchant_help_02.htm");
		VALID_LINKS.add("common/skill_enchant_help_03.htm");
		VALID_LINKS.add("common/weapon_sa_01.htm");
		VALID_LINKS.add("common/welcomeback002.htm");
		VALID_LINKS.add("common/welcomeback003.htm");
		VALID_LINKS.add("default/BlessingOfProtection.htm");
		VALID_LINKS.add("default/SupportMagic.htm");
		VALID_LINKS.add("default/SupportMagicServitor.htm");
		VALID_LINKS.add("fisherman/fishing_championship.htm");
		VALID_LINKS.add("fortress/foreman.htm");
		VALID_LINKS.add("guard/kamaloka_help.htm");
		VALID_LINKS.add("guard/kamaloka_level.htm");
		VALID_LINKS.add("olympiad/hero_main2.htm");
		VALID_LINKS.add("petmanager/evolve.htm");
		VALID_LINKS.add("petmanager/exchange.htm");
		VALID_LINKS.add("petmanager/instructions.htm");
		VALID_LINKS.add("seven_signs/blkmrkt_1.htm");
		VALID_LINKS.add("seven_signs/blkmrkt_2.htm");
		VALID_LINKS.add("seven_signs/mammblack_1a.htm");
		VALID_LINKS.add("seven_signs/mammblack_1b.htm");
		VALID_LINKS.add("seven_signs/mammblack_1c.htm");
		VALID_LINKS.add("seven_signs/mammblack_2a.htm");
		VALID_LINKS.add("seven_signs/mammblack_2b.htm");
		VALID_LINKS.add("seven_signs/mammmerch_1.htm");
		VALID_LINKS.add("seven_signs/mammmerch_1a.htm");
		VALID_LINKS.add("seven_signs/mammmerch_1b.htm");
		VALID_LINKS.add("teleporter/separatedsoul.htm");
		VALID_LINKS.add("warehouse/clanwh.htm");
		VALID_LINKS.add("warehouse/privatewh.htm");
	}
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		final String htmlPath = command.substring(4).trim();
		if (htmlPath.isEmpty())
		{
			LOGGER.warning(player + " sent empty link html!");
			return false;
		}
		
		if (htmlPath.contains(".."))
		{
			LOGGER.warning(player + " sent invalid link html: " + htmlPath);
			return false;
		}
		
		String content = VALID_LINKS.contains(htmlPath) ? HtmCache.getInstance().getHtm(player, "data/html/" + htmlPath) : null;
		// Precaution.
		if (htmlPath.startsWith("teleporter/") && !(player.getTarget() instanceof Teleporter))
		{
			content = null;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(target != null ? target.getObjectId() : 0);
		if (content != null)
		{
			html.setHtml(content.replace("%objectId%", String.valueOf(target != null ? target.getObjectId() : 0)));
		}
		player.sendPacket(html);
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
