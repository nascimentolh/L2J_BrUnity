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

import org.l2jbr_unity.commons.util.Rnd;
import org.l2jbr_unity.gameserver.enums.ShotType;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.conditions.Condition;
import org.l2jbr_unity.gameserver.model.effects.AbstractEffect;
import org.l2jbr_unity.gameserver.model.effects.EffectType;
import org.l2jbr_unity.gameserver.model.item.Weapon;
import org.l2jbr_unity.gameserver.model.item.type.WeaponType;
import org.l2jbr_unity.gameserver.model.skill.Skill;
import org.l2jbr_unity.gameserver.model.stats.BaseStat;
import org.l2jbr_unity.gameserver.model.stats.Formulas;
import org.l2jbr_unity.gameserver.model.stats.Stat;

/**
 * Energy Attack effect implementation.
 * @author NosBit
 */
public class EnergyAttack extends AbstractEffect
{
	private final double _power;
	private final int _criticalChance;
	private final boolean _ignoreShieldDefence;
	
	public EnergyAttack(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
		_criticalChance = params.getInt("criticalChance", 0);
		_ignoreShieldDefence = params.getBoolean("ignoreShieldDefence", false);
	}
	
	@Override
	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		// TODO: Verify this on retail
		return !Formulas.calcPhysicalSkillEvasion(effector, effected, skill);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.PHYSICAL_ATTACK;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		final Player attacker = effector.isPlayer() ? (Player) effector : null;
		if (attacker == null)
		{
			return;
		}
		
		double attack = attacker.getPAtk(effected);
		double defence = effected.getPDef(attacker);
		if (!_ignoreShieldDefence)
		{
			final byte shield = Formulas.calcShldUse(attacker, effected, skill, true);
			switch (shield)
			{
				case Formulas.SHIELD_DEFENSE_FAILED:
				{
					break;
				}
				case Formulas.SHIELD_DEFENSE_SUCCEED:
				{
					defence += effected.getShldDef();
					break;
				}
				case Formulas.SHIELD_DEFENSE_PERFECT_BLOCK:
				{
					defence = -1;
					break;
				}
			}
		}
		
		double damage = 1;
		boolean critical = false;
		if (defence != -1)
		{
			final double damageMultiplier = Formulas.calcWeaponTraitBonus(attacker, effected) * Formulas.calcAttributeBonus(attacker, effected, skill) * Formulas.calcGeneralTraitBonus(attacker, effected, skill.getTraitType(), true);
			final boolean ss = skill.useSoulShot() && attacker.isChargedShot(ShotType.SOULSHOTS);
			final double ssBoost = ss ? 2 : 1.0;
			double weaponTypeBoost;
			final Weapon weapon = attacker.getActiveWeaponItem();
			if ((weapon != null) && ((weapon.getItemType() == WeaponType.BOW) || (weapon.getItemType() == WeaponType.CROSSBOW)))
			{
				weaponTypeBoost = 70;
			}
			else
			{
				weaponTypeBoost = 77;
			}
			
			// charge count should be the count before casting the skill but since its reduced before calling effects
			// we add skill consume charges to current charges
			final double energyChargesBoost = (((attacker.getCharges() + skill.getChargeConsume()) - 1) * 0.2) + 1;
			attack += _power;
			attack *= ssBoost;
			attack *= energyChargesBoost;
			attack *= weaponTypeBoost;
			if (effected.isPlayer())
			{
				defence *= effected.getStat().calcStat(Stat.PVP_PHYS_SKILL_DEF, 1.0);
			}
			
			damage = attack / defence;
			damage *= damageMultiplier;
			if (effected.isPlayer())
			{
				damage *= attacker.getStat().calcStat(Stat.PVP_PHYS_SKILL_DMG, 1.0);
				damage = attacker.getStat().calcStat(Stat.PHYSICAL_SKILL_POWER, damage);
			}
			
			critical = (BaseStat.STR.calcBonus(attacker) * _criticalChance) > (Rnd.nextDouble() * 100);
			if (critical)
			{
				damage *= 2;
			}
		}
		
		if (damage > 0)
		{
			attacker.sendDamageMessage(effected, (int) damage, false, critical, false);
			effected.reduceCurrentHp(damage, attacker, skill);
			effected.notifyDamageReceived(damage, attacker, skill, critical, false);
			
			// Check if damage should be reflected
			Formulas.calcDamageReflected(attacker, effected, skill, critical);
		}
	}
}