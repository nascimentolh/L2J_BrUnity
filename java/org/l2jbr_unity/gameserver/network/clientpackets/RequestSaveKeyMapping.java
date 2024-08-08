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
package org.l2jbr_unity.gameserver.network.clientpackets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.gameserver.data.xml.UIData;
import org.l2jbr_unity.gameserver.model.ActionKey;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.network.ConnectionState;

/**
 * Request Save Key Mapping client
 * @author mrTJO, Zoey76
 */
public class RequestSaveKeyMapping extends ClientPacket
{
	private final Map<Integer, List<ActionKey>> _keyMap = new HashMap<>();
	private final Map<Integer, List<Integer>> _catMap = new HashMap<>();
	
	@Override
	protected void readImpl()
	{
		int category = 0;
		readInt(); // Unknown
		readInt(); // Unknown
		final int _tabNum = readInt();
		for (int i = 0; i < _tabNum; i++)
		{
			final int cmd1Size = readByte();
			for (int j = 0; j < cmd1Size; j++)
			{
				UIData.addCategory(_catMap, category, readByte());
			}
			category++;
			
			final int cmd2Size = readByte();
			for (int j = 0; j < cmd2Size; j++)
			{
				UIData.addCategory(_catMap, category, readByte());
			}
			category++;
			
			final int cmdSize = readInt();
			for (int j = 0; j < cmdSize; j++)
			{
				final int cmd = readInt();
				final int key = readInt();
				final int tgKey1 = readInt();
				final int tgKey2 = readInt();
				final int show = readInt();
				UIData.addKey(_keyMap, i, new ActionKey(i, cmd, key, tgKey1, tgKey2, show));
			}
		}
		readInt();
		readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (!Config.STORE_UI_SETTINGS || (player == null) || (getClient().getConnectionState() != ConnectionState.IN_GAME))
		{
			return;
		}
		
		player.getUISettings().storeAll(_catMap, _keyMap);
	}
}
