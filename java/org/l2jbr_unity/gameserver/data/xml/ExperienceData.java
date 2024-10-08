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
package org.l2jbr_unity.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.util.IXmlReader;

/**
 * This class holds the Experience points for each level for players and pets.
 * @author mrTJO
 */
public class ExperienceData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ExperienceData.class.getName());
	
	private final Map<Integer, Long> _expTable = new HashMap<>();
	
	private byte MAX_LEVEL;
	private byte MAX_PET_LEVEL;
	
	/**
	 * Instantiates a new experience table.
	 */
	protected ExperienceData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_expTable.clear();
		parseDatapackFile("data/stats/experience.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _expTable.size() + " levels.");
		LOGGER.info(getClass().getSimpleName() + ": Max Player Level is " + (MAX_LEVEL - 1) + ".");
		LOGGER.info(getClass().getSimpleName() + ": Max Pet Level is " + (MAX_PET_LEVEL - 1) + ".");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		final Node table = doc.getFirstChild();
		final NamedNodeMap tableAttr = table.getAttributes();
		MAX_LEVEL = (byte) (Byte.parseByte(tableAttr.getNamedItem("maxLevel").getNodeValue()) + 1);
		MAX_PET_LEVEL = (byte) (Byte.parseByte(tableAttr.getNamedItem("maxPetLevel").getNodeValue()) + 1);
		if (MAX_LEVEL > Config.PLAYER_MAXIMUM_LEVEL)
		{
			MAX_LEVEL = Config.PLAYER_MAXIMUM_LEVEL;
		}
		if (MAX_PET_LEVEL > (MAX_LEVEL + 1))
		{
			MAX_PET_LEVEL = (byte) (MAX_LEVEL + 1); // Pet level should not exceed owner level.
		}
		
		int maxLevel = 0;
		for (Node n = table.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("experience".equals(n.getNodeName()))
			{
				final NamedNodeMap attrs = n.getAttributes();
				maxLevel = parseInteger(attrs, "level");
				if (maxLevel > Config.PLAYER_MAXIMUM_LEVEL)
				{
					break;
				}
				_expTable.put(maxLevel, parseLong(attrs, "tolevel"));
			}
		}
	}
	
	/**
	 * Gets the exp for level.
	 * @param level the level required.
	 * @return the experience points required to reach the given level.
	 */
	public long getExpForLevel(int level)
	{
		if (level > Config.PLAYER_MAXIMUM_LEVEL)
		{
			return _expTable.get((int) Config.PLAYER_MAXIMUM_LEVEL);
		}
		return _expTable.get(level);
	}
	
	/**
	 * Gets the max level.
	 * @return the maximum level acquirable by a player.
	 */
	public byte getMaxLevel()
	{
		return MAX_LEVEL;
	}
	
	/**
	 * Gets the max pet level.
	 * @return the maximum level acquirable by a pet.
	 */
	public byte getMaxPetLevel()
	{
		return MAX_PET_LEVEL;
	}
	
	/**
	 * Gets the single instance of ExperienceTable.
	 * @return single instance of ExperienceTable
	 */
	public static ExperienceData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ExperienceData INSTANCE = new ExperienceData();
	}
}
