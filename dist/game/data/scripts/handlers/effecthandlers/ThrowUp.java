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
import org.l2jbr_unity.gameserver.model.effects.EffectFlag;
import org.l2jbr_unity.gameserver.model.skill.Skill;
import org.l2jbr_unity.gameserver.network.serverpackets.FlyToLocation;
import org.l2jbr_unity.gameserver.network.serverpackets.ValidateLocation;

/**
 * Throw Up effect implementation.
 */
public class ThrowUp extends AbstractEffect
{
	public ThrowUp(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.STUNNED.getMask();
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		// Get current position of the Creature.
		final int curX = effected.getX();
		final int curY = effected.getY();
		final int curZ = effected.getZ();
		
		// Calculate distance between effector and effected current position.
		final double dx = effector.getX() - curX;
		final double dy = effector.getY() - curY;
		final double dz = effector.getZ() - curZ;
		final double distance = Math.sqrt((dx * dx) + (dy * dy));
		if (distance > 2000)
		{
			LOGGER.info("EffectThrow was going to use invalid coordinates for characters, getEffected: " + curX + "," + curY + " and getEffector: " + effector.getX() + "," + effector.getY());
			return;
		}
		int offset = Math.min((int) distance + skill.getFlyRadius(), 1400);
		double cos;
		double sin;
		
		// Approximation for moving futher when z coordinates are different.
		// TODO: Handle Z axis movement better.
		offset += Math.abs(dz);
		if (offset < 5)
		{
			offset = 5;
		}
		
		// If no distance.
		if (distance < 1)
		{
			return;
		}
		
		// Calculate movement angles needed.
		sin = dy / distance;
		cos = dx / distance;
		
		// Calculate the new destination with offset included.
		final int x = effector.getX() - (int) (offset * cos);
		final int y = effector.getY() - (int) (offset * sin);
		final int z = effected.getZ();
		final Location destination = GeoEngine.getInstance().getValidLocation(effected.getX(), effected.getY(), effected.getZ(), x, y, z, effected.getInstanceId());
		effected.broadcastPacket(new FlyToLocation(effected, destination, FlyType.THROW_UP));
		effected.setXYZ(destination);
		effected.broadcastPacket(new ValidateLocation(effected));
	}
}
