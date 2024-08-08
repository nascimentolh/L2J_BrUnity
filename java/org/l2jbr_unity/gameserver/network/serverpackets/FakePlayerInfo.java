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
import org.l2jbr_unity.gameserver.data.sql.ClanTable;
import org.l2jbr_unity.gameserver.data.xml.FakePlayerData;
import org.l2jbr_unity.gameserver.enums.Sex;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.clan.Clan;
import org.l2jbr_unity.gameserver.model.holders.FakePlayerHolder;
import org.l2jbr_unity.gameserver.model.zone.ZoneId;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class FakePlayerInfo extends ServerPacket
{
	private final Npc _npc;
	private final int _objId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _mAtkSpd;
	private final int _pAtkSpd;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double _moveMultiplier;
	private final float _attackSpeedMultiplier;
	private final FakePlayerHolder _fpcHolder;
	private final Clan _clan;
	
	public FakePlayerInfo(Npc npc)
	{
		_npc = npc;
		_objId = npc.getObjectId();
		_x = npc.getX();
		_y = npc.getY();
		_z = npc.getZ();
		_heading = npc.getHeading();
		_mAtkSpd = npc.getMAtkSpd();
		_pAtkSpd = (int) npc.getPAtkSpd();
		_attackSpeedMultiplier = npc.getAttackSpeedMultiplier();
		_moveMultiplier = npc.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(npc.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(npc.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(npc.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(npc.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = npc.isFlying() ? _runSpd : 0;
		_flyWalkSpd = npc.isFlying() ? _walkSpd : 0;
		_fpcHolder = FakePlayerData.getInstance().getInfo(npc.getId());
		_clan = ClanTable.getInstance().getClan(_fpcHolder.getClanId());
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CHAR_INFO.writeId(this, buffer);
		buffer.writeInt(_x);
		buffer.writeInt(_y);
		buffer.writeInt(_z);
		buffer.writeInt(0); // vehicleId
		buffer.writeInt(_objId);
		buffer.writeString(_npc.getName());
		buffer.writeInt(_npc.getRace().ordinal());
		buffer.writeInt(_npc.getTemplate().getSex() == Sex.FEMALE);
		buffer.writeInt(_fpcHolder.getClassId());
		buffer.writeInt(0); // Inventory.PAPERDOLL_UNDER
		buffer.writeInt(_fpcHolder.getEquipHead());
		buffer.writeInt(_fpcHolder.getEquipRHand());
		buffer.writeInt(_fpcHolder.getEquipLHand());
		buffer.writeInt(_fpcHolder.getEquipGloves());
		buffer.writeInt(_fpcHolder.getEquipChest());
		buffer.writeInt(_fpcHolder.getEquipLegs());
		buffer.writeInt(_fpcHolder.getEquipFeet());
		buffer.writeInt(_fpcHolder.getEquipCloak());
		buffer.writeInt(_fpcHolder.getEquipRHand()); // dual hand
		buffer.writeInt(_fpcHolder.getEquipHair());
		buffer.writeInt(_fpcHolder.getEquipHair2());
		buffer.writeInt(0); // Inventory.PAPERDOLL_RBRACELET
		buffer.writeInt(0); // Inventory.PAPERDOLL_LBRACELET
		buffer.writeInt(0); // Inventory.PAPERDOLL_DECO1
		buffer.writeInt(0); // Inventory.PAPERDOLL_DECO2
		buffer.writeInt(0); // Inventory.PAPERDOLL_DECO3
		buffer.writeInt(0); // Inventory.PAPERDOLL_DECO4
		buffer.writeInt(0); // Inventory.PAPERDOLL_DECO5
		buffer.writeInt(0); // Inventory.PAPERDOLL_DECO6
		buffer.writeInt(0); // Inventory.PAPERDOLL_BELT
		for (int i = 0; i < 21; i++)
		{
			buffer.writeInt(0);
		}
		buffer.writeInt(0); // getTalismanSlots
		buffer.writeInt(1); // canEquipCloak
		buffer.writeInt(_npc.getScriptValue()); // getPvpFlag()
		buffer.writeInt(_npc.getKarma());
		buffer.writeInt(_mAtkSpd);
		buffer.writeInt(_pAtkSpd);
		buffer.writeInt(0); // ?
		buffer.writeInt(_runSpd);
		buffer.writeInt(_walkSpd);
		buffer.writeInt(_swimRunSpd);
		buffer.writeInt(_swimWalkSpd);
		buffer.writeInt(_flyRunSpd);
		buffer.writeInt(_flyWalkSpd);
		buffer.writeInt(_flyRunSpd);
		buffer.writeInt(_flyWalkSpd);
		buffer.writeDouble(_moveMultiplier);
		buffer.writeDouble(_attackSpeedMultiplier);
		buffer.writeDouble(_npc.getCollisionRadius());
		buffer.writeDouble(_npc.getCollisionHeight());
		buffer.writeInt(_fpcHolder.getHair());
		buffer.writeInt(_fpcHolder.getHairColor());
		buffer.writeInt(_fpcHolder.getFace());
		buffer.writeString(_npc.getTemplate().getTitle());
		if (_clan != null)
		{
			buffer.writeInt(_clan.getId());
			buffer.writeInt(_clan.getCrestId());
			buffer.writeInt(_clan.getAllyId());
			buffer.writeInt(_clan.getAllyCrestId());
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
		buffer.writeByte(1); // !isSitting() (at some initial tests it worked)
		buffer.writeByte(_npc.isRunning());
		buffer.writeByte(_npc.isInCombat());
		buffer.writeByte(_npc.isAlikeDead());
		buffer.writeByte(_npc.isInvisible());
		buffer.writeByte(0); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		buffer.writeByte(0); // getPrivateStoreType().getId()
		buffer.writeShort(0); // getCubics().size()
		// getCubics().keySet().forEach(packet::writeH);
		buffer.writeByte(0); // isInPartyMatchRoom
		buffer.writeInt(_npc.getAbnormalVisualEffects());
		buffer.writeByte(_npc.isInsideZone(ZoneId.WATER));
		buffer.writeShort(_fpcHolder.getRecommends()); // Blue value for name (0 = white, 255 = pure blue)
		buffer.writeInt(0); // getMountNpcId() == 0 ? 0 : getMountNpcId() + 1000000
		buffer.writeInt(_fpcHolder.getClassId());
		buffer.writeInt(0); // ?
		buffer.writeByte(_fpcHolder.getWeaponEnchantLevel()); // isMounted() ? 0 : _enchantLevel
		buffer.writeByte(_npc.getTeam().getId());
		buffer.writeInt(_clan != null ? _clan.getCrestLargeId() : 0);
		buffer.writeByte(_fpcHolder.getNobleLevel());
		buffer.writeByte(_fpcHolder.isHero());
		buffer.writeByte(_fpcHolder.isFishing());
		buffer.writeInt(_fpcHolder.getBaitLocationX());
		buffer.writeInt(_fpcHolder.getBaitLocationY());
		buffer.writeInt(_fpcHolder.getBaitLocationZ());
		buffer.writeInt(_fpcHolder.getNameColor());
		buffer.writeInt(_heading);
		buffer.writeInt(_fpcHolder.getPledgeStatus());
		buffer.writeInt(0); // getPledgeType()
		buffer.writeInt(_fpcHolder.getTitleColor());
		buffer.writeInt(0); // isCursedWeaponEquipped
		buffer.writeInt(0); // getClanId() > 0 ? getClan().getReputationScore() : 0
		// T1
		buffer.writeInt(0); // getTransformationDisplayId()
		buffer.writeInt(_fpcHolder.getAgathionId());
		// T2
		buffer.writeInt(1);
		// T2.3
		buffer.writeInt(_npc.getAbnormalVisualEffectSpecial());
	}
}
