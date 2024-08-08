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
package handlers.effecthandlers;

import org.l2jbr_unity.gameserver.enums.FlyType;
import org.l2jbr_unity.gameserver.geoengine.GeoEngine;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.conditions.Condition;
import org.l2jbr_unity.gameserver.model.effects.AbstractEffect;
import org.l2jbr_unity.gameserver.model.skill.Skill;
import org.l2jbr_unity.gameserver.network.serverpackets.FlyToLocation;
import org.l2jbr_unity.gameserver.network.serverpackets.ValidateLocation;

/**
 * Enemy Charge effect implementation.
 */
public class EnemyCharge extends AbstractEffect
{
	public EnemyCharge(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		if (effector.isMovementDisabled())
		{
			return;
		}
		
		// Get current position of the Creature
		final int curX = effector.getX();
		final int curY = effector.getY();
		final int curZ = effector.getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		final double dx = effected.getX() - curX;
		final double dy = effected.getY() - curY;
		final double dz = effected.getZ() - curZ;
		final double distance = Math.sqrt((dx * dx) + (dy * dy));
		if (distance > 2000)
		{
			LOGGER.info("EffectEnemyCharge was going to use invalid coordinates for characters, getEffector: " + curX + "," + curY + " and getEffected: " + effected.getX() + "," + effected.getY());
			return;
		}
		
		int offset = Math.max(skill.getFlyRadius(), 30);
		
		// approximation for moving closer when z coordinates are different
		// TODO: handle Z axis movement better
		offset -= Math.abs(dz);
		if (offset < 5)
		{
			offset = 5;
		}
		
		// If no distance
		if ((distance < 1) || ((distance - offset) <= 0))
		{
			return;
		}
		
		// Calculate movement angles needed
		final double sin = dy / distance;
		final double cos = dx / distance;
		
		// Calculate the new destination with offset included
		final int x = curX + (int) ((distance - offset) * cos);
		final int y = curY + (int) ((distance - offset) * sin);
		final int z = effected.getZ();
		final Location destination = GeoEngine.getInstance().getValidLocation(effector.getX(), effector.getY(), effector.getZ(), x, y, z, effector.getInstanceId());
		effector.broadcastPacket(new FlyToLocation(effector, destination, FlyType.CHARGE));
		effector.setXYZ(destination);
		effector.broadcastPacket(new ValidateLocation(effector));
	}
}
