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

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.network.WritableBuffer;
import org.l2jbr_unity.gameserver.data.xml.ExperienceData;
import org.l2jbr_unity.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jbr_unity.gameserver.instancemanager.TerritoryWarManager;
import org.l2jbr_unity.gameserver.model.Elementals;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.skill.AbnormalVisualEffect;
import org.l2jbr_unity.gameserver.model.zone.ZoneId;
import org.l2jbr_unity.gameserver.network.GameClient;
import org.l2jbr_unity.gameserver.network.ServerPackets;

public class UserInfo extends ServerPacket
{
	private final Player _player;
	private int _relation;
	private int _airShipHelm;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double _moveMultiplier;
	
	public UserInfo(Player player)
	{
		_player = player;
		final int _territoryId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(player);
		_relation = _player.isClanLeader() ? 0x40 : 0;
		if (_player.getSiegeState() == 1)
		{
			if (_territoryId == 0)
			{
				_relation |= 0x180;
			}
			else
			{
				_relation |= 0x1000;
			}
		}
		if (_player.getSiegeState() == 2)
		{
			_relation |= 0x80;
		}
		// _isDisguised = TerritoryWarManager.getInstance().isDisguised(character.getObjectId());
		if (_player.isInAirShip() && _player.getAirShip().isCaptain(_player))
		{
			_airShipHelm = _player.getAirShip().getHelmItemId();
		}
		else
		{
			_airShipHelm = 0;
		}
		_moveMultiplier = player.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(player.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(player.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(player.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(player.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = player.isFlying() ? _runSpd : 0;
		_flyWalkSpd = player.isFlying() ? _walkSpd : 0;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.USER_INFO.writeId(this, buffer);
		buffer.writeInt(_player.getX());
		buffer.writeInt(_player.getY());
		buffer.writeInt(_player.getZ());
		buffer.writeInt(_player.getVehicle() != null ? _player.getVehicle().getObjectId() : 0);
		buffer.writeInt(_player.getObjectId());
		buffer.writeString(_player.getAppearance().getVisibleName());
		buffer.writeInt(_player.getRace().ordinal());
		buffer.writeInt(_player.getAppearance().isFemale());
		buffer.writeInt(_player.getBaseClass());
		buffer.writeInt(_player.getLevel());
		buffer.writeLong(_player.getExp());
		buffer.writeDouble((float) (_player.getExp() - ExperienceData.getInstance().getExpForLevel(_player.getLevel())) / (ExperienceData.getInstance().getExpForLevel(_player.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(_player.getLevel()))); // High Five exp %
		buffer.writeInt(_player.getSTR());
		buffer.writeInt(_player.getDEX());
		buffer.writeInt(_player.getCON());
		buffer.writeInt(_player.getINT());
		buffer.writeInt(_player.getWIT());
		buffer.writeInt(_player.getMEN());
		buffer.writeInt(_player.getMaxHp());
		buffer.writeInt((int) Math.round(_player.getCurrentHp()));
		buffer.writeInt(_player.getMaxMp());
		buffer.writeInt((int) Math.round(_player.getCurrentMp()));
		buffer.writeInt((int) _player.getSp());
		buffer.writeInt(_player.getCurrentLoad());
		buffer.writeInt(_player.getMaxLoad());
		buffer.writeInt(_player.getActiveWeaponItem() != null ? 40 : 20); // 20 no weapon, 40 weapon equipped
		
		for (int slot : getPaperdollOrder())
		{
			buffer.writeInt(_player.getInventory().getPaperdollObjectId(slot));
		}
		
		for (int slot : getPaperdollOrder())
		{
			buffer.writeInt(_player.getInventory().getPaperdollItemDisplayId(slot));
		}
		
		for (int slot : getPaperdollOrder())
		{
			buffer.writeInt(_player.getInventory().getPaperdollAugmentationId(slot));
		}
		
		buffer.writeInt(_player.getInventory().getTalismanSlots());
		buffer.writeInt(_player.getInventory().canEquipCloak());
		buffer.writeInt((int) _player.getPAtk(null));
		buffer.writeInt((int) _player.getPAtkSpd());
		buffer.writeInt((int) _player.getPDef(null));
		buffer.writeInt(_player.getEvasionRate(null));
		buffer.writeInt(_player.getAccuracy());
		buffer.writeInt(_player.getCriticalHit(null, null));
		buffer.writeInt((int) _player.getMAtk(null, null));
		buffer.writeInt(_player.getMAtkSpd());
		buffer.writeInt((int) _player.getPAtkSpd());
		buffer.writeInt((int) _player.getMDef(null, null));
		buffer.writeInt(_player.getPvpFlag());
		buffer.writeInt(_player.getKarma());
		buffer.writeInt(_runSpd);
		buffer.writeInt(_walkSpd);
		buffer.writeInt(_swimRunSpd);
		buffer.writeInt(_swimWalkSpd);
		buffer.writeInt(_flyRunSpd);
		buffer.writeInt(_flyWalkSpd);
		buffer.writeInt(_flyRunSpd);
		buffer.writeInt(_flyWalkSpd);
		buffer.writeDouble(_moveMultiplier);
		buffer.writeDouble(_player.getAttackSpeedMultiplier());
		buffer.writeDouble(_player.getCollisionRadius());
		buffer.writeDouble(_player.getCollisionHeight());
		buffer.writeInt(_player.getAppearance().getHairStyle());
		buffer.writeInt(_player.getAppearance().getHairColor());
		buffer.writeInt(_player.getAppearance().getFace());
		buffer.writeInt(_player.isGM()); // builder level
		
		String title = _player.getTitle();
		if (_player.isGM() && _player.isInvisible())
		{
			title = "[Invisible]";
		}
		buffer.writeString(title);
		
		buffer.writeInt(_player.getClanId());
		buffer.writeInt(_player.getClanCrestId());
		buffer.writeInt(_player.getAllyId());
		buffer.writeInt(_player.getAllyCrestId()); // ally crest id
		// 0x40 leader rights
		// siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
		buffer.writeInt(_relation);
		buffer.writeByte(_player.getMountType().ordinal()); // mount type
		buffer.writeByte(_player.getPrivateStoreType().getId());
		buffer.writeByte(_player.hasDwarvenCraft());
		buffer.writeInt(_player.getPkKills());
		buffer.writeInt(_player.getPvpKills());
		
		buffer.writeShort(_player.getCubics().size());
		for (int cubicId : _player.getCubics().keySet())
		{
			buffer.writeShort(cubicId);
		}
		
		buffer.writeByte(_player.isInPartyMatchRoom());
		buffer.writeInt(_player.isInvisible() ? _player.getAbnormalVisualEffects() | AbnormalVisualEffect.STEALTH.getMask() : _player.getAbnormalVisualEffects());
		buffer.writeByte(_player.isInsideZone(ZoneId.WATER) ? 1 : _player.isFlyingMounted() ? 2 : 0);
		buffer.writeInt(_player.getClanPrivileges().getBitmask());
		buffer.writeShort(_player.getRecomLeft()); // c2 recommendations remaining
		buffer.writeShort(_player.getRecomHave()); // c2 recommendations received
		buffer.writeInt(_player.getMountNpcId() > 0 ? _player.getMountNpcId() + 1000000 : 0);
		buffer.writeShort(_player.getInventoryLimit());
		buffer.writeInt(_player.getClassId().getId());
		buffer.writeInt(0); // special effects? circles around player...
		buffer.writeInt(_player.getMaxCp());
		buffer.writeInt((int) _player.getCurrentCp());
		buffer.writeByte(_player.isMounted() || (_airShipHelm != 0) ? 0 : _player.getEnchantEffect());
		buffer.writeByte(_player.getTeam().getId());
		buffer.writeInt(_player.getClanCrestLargeId());
		buffer.writeByte(_player.isNoble()); // 1: symbol on char menu ctrl+I
		buffer.writeByte(_player.isHero() || (_player.isGM() && Config.GM_HERO_AURA)); // 1: Hero Aura
		
		buffer.writeByte(_player.isFishing()); // Fishing Mode
		buffer.writeInt(_player.getFishX()); // fishing x
		buffer.writeInt(_player.getFishY()); // fishing y
		buffer.writeInt(_player.getFishZ()); // fishing z
		
		buffer.writeInt(_player.getAppearance().getNameColor());
		// new c5
		buffer.writeByte(_player.isRunning()); // changes the Speed display on Status Window
		buffer.writeInt(_player.getPledgeClass()); // changes the text above CP on Status Window
		buffer.writeInt(_player.getPledgeType());
		buffer.writeInt(_player.getAppearance().getTitleColor());
		buffer.writeInt(_player.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_player.getCursedWeaponEquippedId()) : 0);
		// T1 Starts
		buffer.writeInt(_player.getTransformationDisplayId());
		final byte attackAttribute = _player.getAttackElement();
		buffer.writeShort(attackAttribute);
		buffer.writeShort(_player.getAttackElementValue(attackAttribute));
		buffer.writeShort(_player.getDefenseElementValue(Elementals.FIRE));
		buffer.writeShort(_player.getDefenseElementValue(Elementals.WATER));
		buffer.writeShort(_player.getDefenseElementValue(Elementals.WIND));
		buffer.writeShort(_player.getDefenseElementValue(Elementals.EARTH));
		buffer.writeShort(_player.getDefenseElementValue(Elementals.HOLY));
		buffer.writeShort(_player.getDefenseElementValue(Elementals.DARK));
		buffer.writeInt(_player.getAgathionId());
		// T2 Starts
		buffer.writeInt(_player.getFame()); // Fame
		buffer.writeInt(_player.isMinimapAllowed()); // Minimap on Hellbound
		buffer.writeInt(_player.getVitalityPoints()); // Vitality Points
		buffer.writeInt(_player.getAbnormalVisualEffectSpecial());
		// buffer.writeInt(_territoryId); // CT2.3
		// buffer.writeInt((_isDisguised ? 1: 0)); // CT2.3
		// buffer.writeInt(_territoryId); // CT2.3
	}
}
