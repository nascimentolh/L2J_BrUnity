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
package handlers.itemhandlers;

import java.util.List;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.util.Rnd;
import org.l2jbr_unity.gameserver.data.xml.ItemData;
import org.l2jbr_unity.gameserver.handler.IItemHandler;
import org.l2jbr_unity.gameserver.model.ExtractableProduct;
import org.l2jbr_unity.gameserver.model.actor.Playable;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.item.EtcItem;
import org.l2jbr_unity.gameserver.model.item.instance.Item;
import org.l2jbr_unity.gameserver.network.SystemMessageId;

/**
 * Extractable Items handler.
 * @author HorridoJoho
 */
public class ExtractableItems implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final Player player = playable.getActingPlayer();
		final EtcItem etcitem = (EtcItem) item.getTemplate();
		final List<ExtractableProduct> exitem = etcitem.getExtractableItems();
		if (exitem == null)
		{
			LOGGER.info("No extractable data defined for " + etcitem);
			return false;
		}
		
		// destroy item
		if (!player.destroyItem("Extract", item.getObjectId(), 1, player, true))
		{
			return false;
		}
		
		boolean created = false;
		for (ExtractableProduct expi : exitem)
		{
			if (Rnd.get(100000) <= expi.getChance())
			{
				final int min = (int) (expi.getMin() * Config.RATE_EXTRACTABLE);
				final int max = (int) (expi.getMax() * Config.RATE_EXTRACTABLE);
				int createItemAmount = (max == min) ? min : (Rnd.get((max - min) + 1) + min);
				if (createItemAmount == 0)
				{
					continue;
				}
				
				if (ItemData.getInstance().getTemplate(expi.getId()).isStackable() || (createItemAmount == 1))
				{
					player.addItem("Extract", expi.getId(), createItemAmount, player, true);
				}
				else
				{
					while (createItemAmount > 0)
					{
						player.addItem("Extract", expi.getId(), 1, player, true);
						createItemAmount--;
					}
				}
				created = true;
			}
		}
		
		if (!created)
		{
			player.sendPacket(SystemMessageId.THERE_WAS_NOTHING_FOUND_INSIDE);
		}
		return true;
	}
}
