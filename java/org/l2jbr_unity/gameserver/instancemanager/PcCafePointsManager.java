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
package org.l2jbr_unity.gameserver.instancemanager;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.threads.ThreadPool;
import org.l2jbr_unity.commons.util.Rnd;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.zone.ZoneId;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.ExPCCafePointInfo;
import org.l2jbr_unity.gameserver.network.serverpackets.SystemMessage;

public class PcCafePointsManager
{
	public void run(Player player)
	{
		// PC-points only premium accounts
		if (!Config.PC_CAFE_ENABLED || !Config.PC_CAFE_RETAIL_LIKE || (!player.hasEnteredWorld()))
		{
			return;
		}
		
		ThreadPool.scheduleAtFixedRate(() -> giveRetailPcCafePont(player), Config.PC_CAFE_REWARD_TIME, Config.PC_CAFE_REWARD_TIME);
	}
	
	public void giveRetailPcCafePont(Player player)
	{
		if (!Config.PC_CAFE_ENABLED || !Config.PC_CAFE_RETAIL_LIKE || (player.isOnlineInt() == 0) || (!player.hasPremiumStatus() && Config.PC_CAFE_ONLY_PREMIUM) || player.isInOfflineMode())
		{
			return;
		}
		
		int points = Config.ACQUISITION_PC_CAFE_RETAIL_LIKE_POINTS;
		
		if (points >= Config.PC_CAFE_MAX_POINTS)
		{
			player.sendPacket(SystemMessageId.THE_MAXIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED_YOU_CAN_NO_LONGER_ACQUIRE_PC_CAFE_POINTS);
			return;
		}
		
		if (Config.PC_CAFE_RANDOM_POINT)
		{
			points = Rnd.get(points / 2, points);
		}
		
		SystemMessage message = null;
		if (Config.PC_CAFE_ENABLE_DOUBLE_POINTS && (Rnd.get(100) < Config.PC_CAFE_DOUBLE_POINTS_CHANCE))
		{
			points *= 2;
			message = new SystemMessage(SystemMessageId.DOUBLE_POINTS_YOU_ACQUIRED_S1_PC_BANG_POINT);
		}
		else
		{
			message = new SystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
		}
		
		if ((player.getPcCafePoints() + points) > Config.PC_CAFE_MAX_POINTS)
		{
			points = Config.PC_CAFE_MAX_POINTS - player.getPcCafePoints();
		}
		
		message.addLong(points);
		player.sendPacket(message);
		player.setPcCafePoints(player.getPcCafePoints() + points);
		player.sendPacket(new ExPCCafePointInfo(player.getPcCafePoints(), points, 1));
	}
	
	public void givePcCafePoint(Player player, double exp)
	{
		if (Config.PC_CAFE_RETAIL_LIKE || !Config.PC_CAFE_ENABLED || player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.PVP) || player.isInsideZone(ZoneId.SIEGE) || (player.isOnlineInt() == 0) || player.isJailed())
		{
			return;
		}
		
		// PC-points only premium accounts
		if (Config.PC_CAFE_ONLY_PREMIUM && !player.hasPremiumStatus())
		{
			return;
		}
		
		if (player.getPcCafePoints() >= Config.PC_CAFE_MAX_POINTS)
		{
			final SystemMessage message = new SystemMessage(SystemMessageId.THE_MAXIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED_YOU_CAN_NO_LONGER_ACQUIRE_PC_CAFE_POINTS);
			player.sendPacket(message);
			return;
		}
		
		int points = (int) (exp * 0.0001 * Config.PC_CAFE_POINT_RATE);
		if (Config.PC_CAFE_RANDOM_POINT)
		{
			points = Rnd.get(points / 2, points);
		}
		
		if ((points == 0) && (exp > 0) && Config.PC_CAFE_REWARD_LOW_EXP_KILLS && (Rnd.get(100) < Config.PC_CAFE_LOW_EXP_KILLS_CHANCE))
		{
			points = 1; // minimum points
		}
		
		if (points <= 0)
		{
			return;
		}
		
		SystemMessage message = null;
		if (Config.PC_CAFE_ENABLE_DOUBLE_POINTS && (Rnd.get(100) < Config.PC_CAFE_DOUBLE_POINTS_CHANCE))
		{
			points *= 2;
			message = new SystemMessage(SystemMessageId.DOUBLE_POINTS_YOU_ACQUIRED_S1_PC_BANG_POINT);
		}
		else
		{
			message = new SystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
		}
		if ((player.getPcCafePoints() + points) > Config.PC_CAFE_MAX_POINTS)
		{
			points = Config.PC_CAFE_MAX_POINTS - player.getPcCafePoints();
		}
		message.addLong(points);
		player.sendPacket(message);
		player.setPcCafePoints(player.getPcCafePoints() + points);
		player.sendPacket(new ExPCCafePointInfo(player.getPcCafePoints(), points, 0));
	}
	
	/**
	 * Gets the single instance of {@code PcCafePointsManager}.
	 * @return single instance of {@code PcCafePointsManager}
	 */
	public static PcCafePointsManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PcCafePointsManager INSTANCE = new PcCafePointsManager();
	}
}