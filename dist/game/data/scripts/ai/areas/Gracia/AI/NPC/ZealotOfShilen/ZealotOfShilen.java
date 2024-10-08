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
package ai.areas.Gracia.AI.NPC.ZealotOfShilen;

import org.l2jbr_unity.gameserver.ai.CtrlIntention;
import org.l2jbr_unity.gameserver.model.World;
import org.l2jbr_unity.gameserver.model.actor.Attackable;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.instance.Monster;

import ai.AbstractNpcAI;

/**
 * Zealot of Shilen AI.
 * @author nonom, Mobius
 */
public class ZealotOfShilen extends AbstractNpcAI
{
	// NPCs
	private static final int ZEALOT = 18782;
	private static final int[] GUARDS =
	{
		32628,
		32629
	};
	
	public ZealotOfShilen()
	{
		addSpawnId(ZEALOT);
		addSpawnId(GUARDS);
		addFirstTalkId(GUARDS);
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		if (npc == null)
		{
			return null;
		}
		
		if (!npc.isAttackingNow() && !npc.isAlikeDead())
		{
			Npc nearby = null;
			double maxDistance = Double.MAX_VALUE;
			for (Monster obj : World.getInstance().getVisibleObjects(npc, Monster.class))
			{
				final double distance = npc.calculateDistance2D(obj);
				if ((distance < maxDistance) && !obj.isDead() && !obj.isDecayed())
				{
					maxDistance = distance;
					nearby = obj;
				}
			}
			if (nearby != null)
			{
				npc.setRunning();
				((Attackable) npc).addDamageHate(nearby, 0, 999);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, nearby, null);
			}
		}
		
		return super.onEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return (npc.isAttackingNow()) ? "32628-01.html" : npc.getId() + ".html";
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		if (npc.getId() == ZEALOT)
		{
			npc.setRandomWalking(false);
		}
		else
		{
			npc.setInvul(true);
			((Attackable) npc).setCanReturnToSpawnPoint(false);
			cancelQuestTimer("WATCHING", npc, null);
			startQuestTimer("WATCHING", 10000, npc, null, true);
		}
		return super.onSpawn(npc);
	}
}
