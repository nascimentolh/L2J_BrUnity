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
package org.l2jbr_unity.gameserver.instancemanager.tasks;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.threads.ThreadPool;
import org.l2jbr_unity.gameserver.instancemanager.FourSepulchersManager;

/**
 * Four Sepulchers change attack time task.
 * @author xban1x
 */
public class FourSepulchersChangeAttackTimeTask implements Runnable
{
	@Override
	public void run()
	{
		final FourSepulchersManager manager = FourSepulchersManager.getInstance();
		manager.setEntryTime(false);
		manager.setWarmUpTime(false);
		manager.setAttackTime(true);
		manager.setCoolDownTime(false);
		
		manager.locationShadowSpawns();
		
		manager.spawnMysteriousBox(31921);
		manager.spawnMysteriousBox(31922);
		manager.spawnMysteriousBox(31923);
		manager.spawnMysteriousBox(31924);
		
		final long currentTime = System.currentTimeMillis();
		if (!manager.isFirstTimeRun())
		{
			manager.setWarmUpTimeEnd(currentTime);
		}
		
		long interval = 0;
		// say task
		if (manager.isFirstTimeRun())
		{
			for (double min = Calendar.getInstance().get(Calendar.MINUTE); min < manager.getCycleMin(); min++)
			{
				// looking for next shout time....
				if ((min % 5) == 0) // check if min can be divided by 5
				{
					final Calendar calendar = Calendar.getInstance();
					calendar.set(Calendar.MINUTE, (int) min);
					if (calendar.getTimeInMillis() < currentTime)
					{
						calendar.add(Calendar.MINUTE, 1);
					}
					ThreadPool.schedule(new FourSepulchersManagerSayTask(), calendar.getTimeInMillis() - currentTime);
					break;
				}
			}
		}
		else
		{
			ThreadPool.schedule(new FourSepulchersManagerSayTask(), 5 * 60400);
		}
		// searching time when attack time will be ended:
		// counting difference between time when attack time ends and
		// current time
		// and then launching change time task
		if (manager.isFirstTimeRun())
		{
			interval = manager.getAttackTimeEnd() - currentTime;
		}
		else
		{
			interval = Config.FS_TIME_ATTACK * 60000;
		}
		
		manager.setChangeCoolDownTimeTask(ThreadPool.schedule(new FourSepulchersChangeCoolDownTimeTask(), interval));
		final ScheduledFuture<?> changeAttackTimeTask = manager.getChangeAttackTimeTask();
		
		if (changeAttackTimeTask != null)
		{
			changeAttackTimeTask.cancel(true);
			manager.setChangeAttackTimeTask(null);
		}
	}
}
