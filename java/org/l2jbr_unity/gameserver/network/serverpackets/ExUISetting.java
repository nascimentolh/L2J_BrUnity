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

import java.util.List;

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.model.ActionKey;
import org.l2jbr_unity.gameserver.model.UIKeysSettings;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author mrTJO
 */
public class ExUISetting extends ServerPacket
{
	private final UIKeysSettings _uiSettings;
	private int buffsize;
	private int categories;
	
	public ExUISetting(Player player)
	{
		_uiSettings = player.getUISettings();
		calcSize();
	}
	
	private void calcSize()
	{
		int size = 16; // initial header and footer
		int category = 0;
		final int numKeyCt = _uiSettings.getKeys().size();
		for (int i = 0; i < numKeyCt; i++)
		{
			size++;
			if (_uiSettings.getCategories().containsKey(category))
			{
				final List<Integer> catElList1 = _uiSettings.getCategories().get(category);
				size += catElList1.size();
			}
			category++;
			size++;
			if (_uiSettings.getCategories().containsKey(category))
			{
				final List<Integer> catElList2 = _uiSettings.getCategories().get(category);
				size += catElList2.size();
			}
			category++;
			size += 4;
			if (_uiSettings.getKeys().containsKey(i))
			{
				final List<ActionKey> keyElList = _uiSettings.getKeys().get(i);
				size += (keyElList.size() * 20);
			}
		}
		buffsize = size;
		categories = category;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UI_SETTING.writeId(this, buffer);
		buffer.writeInt(buffsize);
		buffer.writeInt(categories);
		int category = 0;
		final int numKeyCt = _uiSettings.getKeys().size();
		buffer.writeInt(numKeyCt);
		for (int i = 0; i < numKeyCt; i++)
		{
			if (_uiSettings.getCategories().containsKey(category))
			{
				final List<Integer> catElList1 = _uiSettings.getCategories().get(category);
				buffer.writeByte(catElList1.size());
				for (int cmd : catElList1)
				{
					buffer.writeByte(cmd);
				}
			}
			else
			{
				buffer.writeByte(0);
			}
			category++;
			if (_uiSettings.getCategories().containsKey(category))
			{
				final List<Integer> catElList2 = _uiSettings.getCategories().get(category);
				buffer.writeByte(catElList2.size());
				for (int cmd : catElList2)
				{
					buffer.writeByte(cmd);
				}
			}
			else
			{
				buffer.writeByte(0);
			}
			category++;
			if (_uiSettings.getKeys().containsKey(i))
			{
				final List<ActionKey> keyElList = _uiSettings.getKeys().get(i);
				buffer.writeInt(keyElList.size());
				for (ActionKey akey : keyElList)
				{
					buffer.writeInt(akey.getCommandId());
					buffer.writeInt(akey.getKeyId());
					buffer.writeInt(akey.getToogleKey1());
					buffer.writeInt(akey.getToogleKey2());
					buffer.writeInt(akey.getShowStatus());
				}
			}
			else
			{
				buffer.writeInt(0);
			}
		}
		buffer.writeInt(0x11);
		buffer.writeInt(16);
	}
}
