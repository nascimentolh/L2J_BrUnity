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
package org.l2jbr_unity.loginserver.network.gameserverpackets;

import java.util.logging.Logger;

import org.l2jbr_unity.commons.network.base.BaseReadablePacket;
import org.l2jbr_unity.loginserver.GameServerThread;

/**
 * @author -Wooden-
 */
public class PlayerLogout extends BaseReadablePacket
{
	protected static final Logger LOGGER = Logger.getLogger(PlayerLogout.class.getName());
	
	public PlayerLogout(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		final String account = readString();
		server.removeAccountOnGameServer(account);
	}
}