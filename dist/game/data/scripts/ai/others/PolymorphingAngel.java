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
package ai.others;

import java.util.HashMap;
import java.util.Map;

import org.l2jbr_unity.gameserver.model.actor.Attackable;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Angel spawns...when one of the angels in the keys dies, the other angel will spawn.
 */
public class PolymorphingAngel extends AbstractNpcAI
{
	private static final Map<Integer, Integer> ANGELSPAWNS = new HashMap<>();
	static
	{
		ANGELSPAWNS.put(20830, 20859);
		ANGELSPAWNS.put(21067, 21068);
		ANGELSPAWNS.put(21062, 21063);
		ANGELSPAWNS.put(20831, 20860);
		ANGELSPAWNS.put(21070, 21071);
	}
	
	private PolymorphingAngel()
	{
		addKillId(ANGELSPAWNS.keySet());
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Attackable newNpc = (Attackable) addSpawn(ANGELSPAWNS.get(npc.getId()), npc);
		newNpc.setRunning();
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new PolymorphingAngel();
	}
}
