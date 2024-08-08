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
import org.l2jbr_unity.gameserver.model.actor.Summon;
import org.l2jbr_unity.gameserver.model.actor.instance.Pet;
import org.l2jbr_unity.gameserver.model.actor.instance.Servitor;
import org.l2jbr_unity.gameserver.model.zone.ZoneId;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class PetInfo extends ServerPacket
{
	private final Summon _summon;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final boolean _isSummoned;
	private final int _value;
	private final int _mAtkSpd;
	private final int _pAtkSpd;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double _moveMultiplier;
	private final int _maxHp;
	private final int _maxMp;
	private int _maxFed;
	private int _curFed;
	
	public PetInfo(Summon summon, int value)
	{
		_summon = summon;
		_isSummoned = summon.isShowSummonAnimation();
		_x = summon.getX();
		_y = summon.getY();
		_z = summon.getZ();
		_heading = summon.getHeading();
		_mAtkSpd = summon.getMAtkSpd();
		_pAtkSpd = (int) summon.getPAtkSpd();
		_moveMultiplier = summon.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(summon.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(summon.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(summon.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(summon.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = summon.isFlying() ? _runSpd : 0;
		_flyWalkSpd = summon.isFlying() ? _walkSpd : 0;
		_maxHp = summon.getMaxHp();
		_maxMp = summon.getMaxMp();
		_value = value;
		if (summon.isPet())
		{
			final Pet pet = (Pet) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); // max fed it can be
		}
		else if (summon.isServitor())
		{
			final Servitor sum = (Servitor) _summon;
			_curFed = sum.getLifeTimeRemaining();
			_maxFed = sum.getLifeTime();
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PET_INFO.writeId(this, buffer);
		buffer.writeInt(_summon.getSummonType());
		buffer.writeInt(_summon.getObjectId());
		buffer.writeInt(_summon.getTemplate().getDisplayId() + 1000000);
		buffer.writeInt(0); // 1=attackable
		buffer.writeInt(_x);
		buffer.writeInt(_y);
		buffer.writeInt(_z);
		buffer.writeInt(_heading);
		buffer.writeInt(0);
		buffer.writeInt(_mAtkSpd);
		buffer.writeInt(_pAtkSpd);
		buffer.writeInt(_runSpd);
		buffer.writeInt(_walkSpd);
		buffer.writeInt(_swimRunSpd);
		buffer.writeInt(_swimWalkSpd);
		buffer.writeInt(_flyRunSpd);
		buffer.writeInt(_flyWalkSpd);
		buffer.writeInt(_flyRunSpd);
		buffer.writeInt(_flyWalkSpd);
		buffer.writeDouble(_moveMultiplier);
		buffer.writeDouble(_summon.getAttackSpeedMultiplier()); // attack speed multiplier
		buffer.writeDouble(_summon.getTemplate().getFCollisionRadius());
		buffer.writeDouble(_summon.getTemplate().getFCollisionHeight());
		buffer.writeInt(_summon.getWeapon()); // right hand weapon
		buffer.writeInt(_summon.getArmor()); // body armor
		buffer.writeInt(0); // left hand weapon
		buffer.writeByte(_summon.getOwner() != null); // when pet is dead and player exit game, pet doesn't show master name
		buffer.writeByte(_summon.isRunning()); // running=1 (it is always 1, walking mode is calculated from multiplier)
		buffer.writeByte(_summon.isInCombat()); // attacking 1=true
		buffer.writeByte(_summon.isAlikeDead()); // dead 1=true
		buffer.writeByte(_isSummoned ? 2 : _value); // 0=teleported 1=default 2=summoned
		buffer.writeInt(-1); // High Five NPCString ID
		if (_summon.isPet())
		{
			buffer.writeString(_summon.getName()); // Pet name.
		}
		else
		{
			buffer.writeString(_summon.getTemplate().isUsingServerSideName() ? _summon.getName() : ""); // Summon name.
		}
		buffer.writeInt(-1); // High Five NPCString ID
		buffer.writeString(_summon.getTitle()); // owner name
		buffer.writeInt(1);
		buffer.writeInt(_summon.getPvpFlag()); // 0 = white,2= purpleblink, if it is greater then karma = purple
		buffer.writeInt(_summon.getKarma()); // karma
		buffer.writeInt(_curFed); // how fed it is
		buffer.writeInt(_maxFed); // max fed it can be
		buffer.writeInt((int) _summon.getCurrentHp()); // current hp
		buffer.writeInt(_maxHp); // max hp
		buffer.writeInt((int) _summon.getCurrentMp()); // current mp
		buffer.writeInt(_maxMp); // max mp
		buffer.writeInt((int) _summon.getStat().getSp()); // sp
		buffer.writeInt(_summon.getLevel()); // level
		buffer.writeLong(_summon.getStat().getExp());
		if (_summon.getExpForThisLevel() > _summon.getStat().getExp())
		{
			buffer.writeLong(_summon.getStat().getExp()); // 0% absolute value
		}
		else
		{
			buffer.writeLong(_summon.getExpForThisLevel()); // 0% absolute value
		}
		buffer.writeLong(_summon.getExpForNextLevel()); // 100% absoulte value
		buffer.writeInt(_summon.isPet() ? _summon.getInventory().getTotalWeight() : 0); // weight
		buffer.writeInt(_summon.getMaxLoad()); // max weight it can carry
		buffer.writeInt((int) _summon.getPAtk(null)); // patk
		buffer.writeInt((int) _summon.getPDef(null)); // pdef
		buffer.writeInt((int) _summon.getMAtk(null, null)); // matk
		buffer.writeInt((int) _summon.getMDef(null, null)); // mdef
		buffer.writeInt(_summon.getAccuracy()); // accuracy
		buffer.writeInt(_summon.getEvasionRate(null)); // evasion
		buffer.writeInt(_summon.getCriticalHit(null, null)); // critical
		buffer.writeInt((int) _summon.getMoveSpeed()); // speed
		buffer.writeInt((int) _summon.getPAtkSpd()); // atkspeed
		buffer.writeInt(_summon.getMAtkSpd()); // casting speed
		buffer.writeInt(_summon.getAbnormalVisualEffects()); // c2 abnormal visual effect... bleed=1; poison=2; poison & bleed=3; flame=4;
		buffer.writeShort(_summon.isMountable()); // c2 ride button
		buffer.writeByte(_summon.isInsideZone(ZoneId.WATER) ? 1 : _summon.isFlying() ? 2 : 0); // c2
		// Following all added in C4.
		buffer.writeShort(0); // ??
		buffer.writeByte(_summon.getTeam().getId());
		buffer.writeInt(_summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
		buffer.writeInt(_summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit
		buffer.writeInt(_summon.getFormId()); // CT1.5 Pet form and skills
		buffer.writeInt(_summon.getAbnormalVisualEffectSpecial());
	}
}
