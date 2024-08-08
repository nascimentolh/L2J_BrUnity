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

import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.item.Henna;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author Zoey76
 */
public class HennaItemDrawInfo extends ServerPacket
{
	private final Player _player;
	private final Henna _henna;
	
	public HennaItemDrawInfo(Henna henna, Player player)
	{
		_henna = henna;
		_player = player;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.HENNA_ITEM_INFO.writeId(this, buffer);
		buffer.writeInt(_henna.getDyeId()); // symbol Id
		buffer.writeInt(_henna.getDyeItemId()); // item id of dye
		buffer.writeLong(_henna.getWearCount()); // total amount of dye require
		buffer.writeLong(_henna.getWearFee()); // total amount of Adena require to draw symbol
		buffer.writeInt(_henna.isAllowedClass(_player.getClassId())); // able to draw or not 0 is false and 1 is true
		buffer.writeLong(_player.getAdena());
		buffer.writeInt(_player.getINT()); // current INT
		buffer.writeByte(_player.getINT() + _henna.getStatINT()); // equip INT
		buffer.writeInt(_player.getSTR()); // current STR
		buffer.writeByte(_player.getSTR() + _henna.getStatSTR()); // equip STR
		buffer.writeInt(_player.getCON()); // current CON
		buffer.writeByte(_player.getCON() + _henna.getStatCON()); // equip CON
		buffer.writeInt(_player.getMEN()); // current MEN
		buffer.writeByte(_player.getMEN() + _henna.getStatMEN()); // equip MEN
		buffer.writeInt(_player.getDEX()); // current DEX
		buffer.writeByte(_player.getDEX() + _henna.getStatDEX()); // equip DEX
		buffer.writeInt(_player.getWIT()); // current WIT
		buffer.writeByte(_player.getWIT() + _henna.getStatWIT()); // equip WIT
	}
}
