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

import org.l2jbr_unity.gameserver.enums.TeleportWhereType;
import org.l2jbr_unity.gameserver.instancemanager.MapRegionManager;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.instance.Guard;
import org.l2jbr_unity.gameserver.model.conditions.Condition;
import org.l2jbr_unity.gameserver.model.effects.AbstractEffect;
import org.l2jbr_unity.gameserver.model.effects.EffectType;
import org.l2jbr_unity.gameserver.model.krateisCube.KrateiArena;
import org.l2jbr_unity.gameserver.model.skill.Skill;

/**
 * Escape effect implementation.
 * @author Adry_85
 */
public class Escape extends AbstractEffect
{
	private final TeleportWhereType _escapeType;
	
	public Escape(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_escapeType = params.getEnum("escapeType", TeleportWhereType.class, null);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.TELEPORT;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		if (_escapeType == null)
		{
			return;
		}
		
		if (effected.isPlayer())
		{
			final Player player = effected.getActingPlayer();
			final KrateiArena arena = player.getKrateiArena();
			if (arena != null)
			{
				arena.removePlayer(player);
			}
			else if (player.getUCState() != Player.UC_STATE_NONE)
			{
				return;
			}
		}
		
		if (effected instanceof Guard)
		{
			effected.teleToLocation(((Npc) effected).getSpawn());
			effected.setHeading(((Npc) effected).getSpawn().getHeading());
		}
		else
		{
			effected.teleToLocation(MapRegionManager.getInstance().getTeleToLocation(effected, _escapeType), true);
			effected.getActingPlayer().setIn7sDungeon(false);
			effected.setInstanceId(0);
		}
	}
}