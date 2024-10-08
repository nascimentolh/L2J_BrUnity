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
package org.l2jbr_unity.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.data.xml.EnchantSkillGroupsData;
import org.l2jbr_unity.gameserver.model.EnchantSkillGroup.EnchantSkillHolder;
import org.l2jbr_unity.gameserver.model.EnchantSkillLearn;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class ExEnchantSkillInfo extends ServerPacket
{
	private final List<Integer> _routes = new ArrayList<>(); // skill levels for each route
	private final int _id;
	private final int _level;
	private boolean _maxEnchanted = false;
	
	public ExEnchantSkillInfo(int id, int level)
	{
		_id = id;
		_level = level;
		final EnchantSkillLearn enchantLearn = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_id);
		// do we have this skill?
		if (enchantLearn != null)
		{
			// skill already enchanted?
			if (_level > 100)
			{
				_maxEnchanted = enchantLearn.isMaxEnchant(_level);
				// get detail for next level
				final EnchantSkillHolder esd = enchantLearn.getEnchantSkillHolder(_level);
				// if it exists add it
				if (esd != null)
				{
					_routes.add(_level); // current enchant add firts
				}
				final int skillLevel = (_level % 100);
				for (int route : enchantLearn.getAllRoutes())
				{
					if (((route * 100) + skillLevel) == _level)
					{
						continue;
					}
					// add other levels of all routes - same level as enchanted
					// level
					_routes.add((route * 100) + skillLevel);
				}
			}
			else
			// not already enchanted
			{
				for (int route : enchantLearn.getAllRoutes())
				{
					// add first level (+1) of all routes
					_routes.add((route * 100) + 1);
				}
			}
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_SKILL_INFO.writeId(this, buffer);
		buffer.writeInt(_id);
		buffer.writeInt(_level);
		buffer.writeInt(!_maxEnchanted);
		buffer.writeInt(_level > 100); // enchanted?
		buffer.writeInt(_routes.size());
		for (int level : _routes)
		{
			buffer.writeInt(level);
		}
	}
}