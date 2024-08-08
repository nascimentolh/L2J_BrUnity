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
package org.l2jbr_unity.gameserver.enums;

public enum InstanceType
{
	WorldObject(null),
	Item(WorldObject),
	Creature(WorldObject),
	Npc(Creature),
	Playable(Creature),
	Summon(Playable),
	Player(Playable),
	Folk(Npc),
	Merchant(Folk),
	Warehouse(Folk),
	StaticObject(Creature),
	Door(Creature),
	TerrainObject(Npc),
	EffectPoint(Npc),
	// Summons, Pets, Decoys and Traps
	Servitor(Summon),
	Pet(Summon),
	BabyPet(Pet),
	Decoy(Creature),
	Trap(Npc),
	// Attackable
	Attackable(Npc),
	Guard(Attackable),
	QuestGuard(Guard),
	Monster(Attackable),
	Chest(Monster),
	ControllableMob(Monster),
	FeedableBeast(Monster),
	TamedBeast(FeedableBeast),
	FriendlyMob(Attackable),
	RiftInvader(Monster),
	RaidBoss(Monster),
	GrandBoss(RaidBoss),
	// FlyMobs
	FlyNpc(Folk),
	FlyMonster(Monster),
	FlyRaidBoss(RaidBoss),
	FlyTerrainObject(Npc),
	// Sepulchers
	SepulcherNpc(Folk),
	SepulcherMonster(Monster),
	// Festival
	FestivalGuide(Npc),
	FestivalMonster(Monster),
	// Vehicles
	Vehicle(Creature),
	Boat(Vehicle),
	AirShip(Vehicle),
	ControllableAirShip(AirShip),
	// Siege
	Defender(Attackable),
	Artefact(Folk),
	ControlTower(Npc),
	FlameTower(Npc),
	SiegeFlag(Npc),
	// Fort Siege
	FortCommander(Defender),
	// Fort NPCs
	FortLogistics(Merchant),
	FortManager(Merchant),
	// Seven Signs
	SignsPriest(Npc),
	DawnPriest(SignsPriest),
	DuskPriest(SignsPriest),
	DungeonGatekeeper(Npc),
	// City NPCs
	Adventurer(Folk),
	Auctioneer(Npc),
	BroadcastingTower(Npc),
	ClanHallManager(Merchant),
	Fisherman(Merchant),
	OlympiadManager(Npc),
	PetManager(Merchant),
	RaceManager(Npc),
	Teleporter(Npc),
	Trainer(Folk),
	VillageMaster(Folk),
	// Doormen
	Doorman(Folk),
	CastleDoorman(Doorman),
	FortDoorman(Doorman),
	ClanHallDoorman(Doorman),
	// Custom
	ClassMaster(Folk),
	SchemeBuffer(Npc),
	EventMob(Npc),
	UCManagerInstance(Folk);
	
	private final InstanceType _parent;
	private final long _typeL;
	private final long _typeH;
	private final long _maskL;
	private final long _maskH;
	
	private InstanceType(InstanceType parent)
	{
		_parent = parent;
		
		final int high = ordinal() - (Long.SIZE - 1);
		if (high < 0)
		{
			_typeL = 1L << ordinal();
			_typeH = 0;
		}
		else
		{
			_typeL = 0;
			_typeH = 1L << high;
		}
		
		if ((_typeL < 0) || (_typeH < 0))
		{
			throw new Error("Too many instance types, failed to load " + name());
		}
		
		if (parent != null)
		{
			_maskL = _typeL | parent._maskL;
			_maskH = _typeH | parent._maskH;
		}
		else
		{
			_maskL = _typeL;
			_maskH = _typeH;
		}
	}
	
	public InstanceType getParent()
	{
		return _parent;
	}
	
	public boolean isType(InstanceType it)
	{
		return ((_maskL & it._typeL) > 0) || ((_maskH & it._typeH) > 0);
	}
	
	public boolean isTypes(InstanceType... it)
	{
		for (InstanceType i : it)
		{
			if (isType(i))
			{
				return true;
			}
		}
		return false;
	}
}
