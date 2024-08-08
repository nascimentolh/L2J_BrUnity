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

import org.l2jbr_unity.Config;
import org.l2jbr_unity.gameserver.ai.CtrlIntention;
import org.l2jbr_unity.gameserver.data.xml.DoorData;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.events.EventDispatcher;
import org.l2jbr_unity.gameserver.model.events.EventType;
import org.l2jbr_unity.gameserver.model.events.impl.creature.player.OnPlayerMoveRequest;
import org.l2jbr_unity.gameserver.model.events.returns.TerminateReturn;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.ActionFailed;

public class MoveBackwardToLocation extends ClientPacket
{
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _movementMode;
	
	@Override
	protected void readImpl()
	{
		_targetX = readInt();
		_targetY = readInt();
		_targetZ = readInt();
		_originX = readInt();
		_originY = readInt();
		_originZ = readInt();
		_movementMode = readInt(); // is 0 if cursor keys are used 1 if mouse is used
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if ((Config.PLAYER_MOVEMENT_BLOCK_TIME > 0) && !player.isGM() && (player.getNotMoveUntil() > System.currentTimeMillis()))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC_ONE_MOMENT_PLEASE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((_targetX == _originX) && (_targetY == _originY) && (_targetZ == _originZ))
		{
			player.stopMove(player.getLocation());
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check for possible door logout and move over exploit. Also checked at ValidatePosition.
		if (DoorData.getInstance().checkIfDoorsBetween(player.getLastServerPosition(), player.getLocation(), player.getInstanceId()))
		{
			player.stopMove(player.getLastServerPosition());
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_movementMode == 1)
		{
			player.setCursorKeyMovement(false);
			if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_MOVE_REQUEST, player))
			{
				final TerminateReturn terminate = EventDispatcher.getInstance().notifyEvent(new OnPlayerMoveRequest(player, new Location(_targetX, _targetY, _targetZ)), player, TerminateReturn.class);
				if ((terminate != null) && terminate.terminate())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		else // 0
		{
			if (!Config.ENABLE_KEYBOARD_MOVEMENT)
			{
				return;
			}
			player.setCursorKeyMovement(true);
			player.setLastServerPosition(player.getX(), player.getY(), player.getZ());
		}
		
		// Correcting targetZ from floor level to head level.
		// Client is giving floor level as targetZ, but that floor level doesn't match our current geodata and teleport coordinates as good as head level!
		// L2J uses floor, not head level as char coordinates. This is some sort of incompatibility fix. Validate position packets sends head level.
		_targetZ += player.getTemplate().getCollisionHeight();
		
		final int teleMode = player.getTeleMode();
		if (teleMode > 0)
		{
			if (teleMode == 1)
			{
				player.setTeleMode(0);
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.teleToLocation(new Location(_targetX, _targetY, _targetZ));
			return;
		}
		
		final double dx = _targetX - player.getX();
		final double dy = _targetY - player.getY();
		// Can't move if character is confused, or trying to move a huge distance
		if (player.isOutOfControl() || (((dx * dx) + (dy * dy)) > 98010000)) // 9900*9900
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_targetX, _targetY, _targetZ));
		
		// Mobius: Check spawn protections.
		player.onActionRequest();
	}
}
