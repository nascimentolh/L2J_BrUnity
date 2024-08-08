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
package conquerablehalls.BanditStronghold;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jbr_unity.commons.database.DatabaseFactory;
import org.l2jbr_unity.commons.threads.ThreadPool;
import org.l2jbr_unity.gameserver.ai.CtrlIntention;
import org.l2jbr_unity.gameserver.ai.SpecialSiegeGuardAI;
import org.l2jbr_unity.gameserver.cache.HtmCache;
import org.l2jbr_unity.gameserver.data.sql.ClanHallTable;
import org.l2jbr_unity.gameserver.data.sql.ClanTable;
import org.l2jbr_unity.gameserver.data.xml.NpcData;
import org.l2jbr_unity.gameserver.enums.SiegeClanType;
import org.l2jbr_unity.gameserver.enums.TeleportWhereType;
import org.l2jbr_unity.gameserver.instancemanager.ZoneManager;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.World;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.templates.NpcTemplate;
import org.l2jbr_unity.gameserver.model.clan.Clan;
import org.l2jbr_unity.gameserver.model.clan.ClanMember;
import org.l2jbr_unity.gameserver.model.siege.SiegeClan;
import org.l2jbr_unity.gameserver.model.siege.clanhalls.ClanHallSiegeEngine;
import org.l2jbr_unity.gameserver.model.siege.clanhalls.SiegeStatus;
import org.l2jbr_unity.gameserver.model.zone.type.ResidenceHallTeleportZone;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jbr_unity.gameserver.network.serverpackets.SystemMessage;
import org.l2jbr_unity.gameserver.util.Broadcast;

/**
 * @author LordWinter
 */
public class BanditStronghold extends ClanHallSiegeEngine
{
	private static final String SQL_LOAD_ATTACKERS = "SELECT * FROM siegable_hall_flagwar_attackers WHERE hall_id = ?";
	private static final String SQL_SAVE_ATTACKER = "INSERT INTO siegable_hall_flagwar_attackers_members VALUES (?,?,?)";
	private static final String SQL_LOAD_MEMEBERS = "SELECT object_id FROM siegable_hall_flagwar_attackers_members WHERE clan_id = ?";
	private static final String SQL_SAVE_CLAN = "INSERT INTO siegable_hall_flagwar_attackers VALUES(?,?,?,?)";
	private static final String SQL_SAVE_NPC = "UPDATE siegable_hall_flagwar_attackers SET npc = ? WHERE clan_id = ?";
	private static final String SQL_CLEAR_CLAN = "DELETE FROM siegable_hall_flagwar_attackers WHERE hall_id = ?";
	private static final String SQL_CLEAR_CLAN_ATTACKERS = "DELETE FROM siegable_hall_flagwar_attackers_members WHERE hall_id = ?";
	
	private static final int ROYAL_FLAG = 35422;
	private static final int FLAG_RED = 35423;
	
	private static final int ALLY_1 = 35428;
	private static final int ALLY_2 = 35429;
	private static final int ALLY_3 = 35430;
	private static final int ALLY_4 = 35431;
	private static final int ALLY_5 = 35432;
	
	private static final int TELEPORT_1 = 35560;
	
	private static final int MESSENGER = 35437;
	
	protected static final int[] OUTTER_DOORS_TO_OPEN =
	{
		22170001,
		22170002
	};
	protected static final int[] INNER_DOORS_TO_OPEN =
	{
		22170003,
		22170004
	};
	private static final Location[] FLAG_COORDS =
	{
		new Location(83699, -17468, -1774, 19048),
		new Location(82053, -17060, -1784, 5432),
		new Location(82142, -15528, -1799, 58792),
		new Location(83544, -15266, -1770, 44976),
		new Location(84609, -16041, -1769, 35816),
		new Location(81981, -15708, -1858, 60392),
		new Location(84375, -17060, -1860, 27712)
	};
	
	private static final ResidenceHallTeleportZone[] TELE_ZONES = new ResidenceHallTeleportZone[6];
	static
	{
		final Collection<ResidenceHallTeleportZone> zoneList = ZoneManager.getInstance().getAllZones(ResidenceHallTeleportZone.class);
		for (ResidenceHallTeleportZone teleZone : zoneList)
		{
			if (teleZone.getResidenceId() != BANDIT_STRONGHOLD)
			{
				continue;
			}
			
			final int id = teleZone.getResidenceZoneId();
			if ((id < 0) || (id >= 6))
			{
				continue;
			}
			
			TELE_ZONES[id] = teleZone;
		}
	}
	
	private static final int QUEST_REWARD = 5009;
	private static final int TARLK_AMULET = 4332;
	
	private static final Location CENTER = new Location(82882, -16280, -1894, 0);
	
	protected static final Map<Integer, ClanData> _data = new HashMap<>();
	private Clan _winner;
	private boolean _firstPhase;
	
	public BanditStronghold()
	{
		super(BANDIT_STRONGHOLD);
		
		addStartNpc(MESSENGER);
		addFirstTalkId(MESSENGER);
		addTalkId(MESSENGER);
		
		for (int i = 0; i < 6; i++)
		{
			addFirstTalkId(TELEPORT_1 + i);
		}
		
		addKillId(ALLY_1);
		addKillId(ALLY_2);
		addKillId(ALLY_3);
		addKillId(ALLY_4);
		addKillId(ALLY_5);
		
		addSpawnId(ALLY_1);
		addSpawnId(ALLY_2);
		addSpawnId(ALLY_3);
		addSpawnId(ALLY_4);
		addSpawnId(ALLY_5);
		
		_winner = ClanTable.getInstance().getClan(_hall.getOwnerId());
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final Clan clan = player.getClan();
		
		if (event.startsWith("Register"))
		{
			if (!_hall.isRegistering())
			{
				if (_hall.isInSiege())
				{
					htmltext = "35437-02.htm";
				}
				else
				{
					sendRegistrationPageDate(player);
					return null;
				}
			}
			else if ((clan == null) || !player.isClanLeader())
			{
				htmltext = "35437-03.htm";
			}
			else if (getAttackers().size() >= 5)
			{
				htmltext = "35437-04.htm";
			}
			else if (checkIsAttacker(clan))
			{
				htmltext = "35437-05.htm";
			}
			else if (_hall.getOwnerId() == clan.getId())
			{
				htmltext = "35437-06.htm";
			}
			else
			{
				final String[] arg = event.split(" ");
				if (arg.length >= 2)
				{
					if (arg[1].equals("wQuest"))
					{
						if (player.destroyItemByItemId("Bandit Strong Hall Siege", QUEST_REWARD, 1, player, true))
						{
							registerClan(clan);
							htmltext = getFlagHtml(_data.get(clan.getId()).flag);
						}
						else
						{
							htmltext = "35437-07.htm";
						}
					}
					else if (arg[1].equals("wFee") && canPayRegistration())
					{
						if (player.reduceAdena(getClass().getSimpleName() + " Siege", 200000, player, true))
						{
							registerClan(clan);
							htmltext = getFlagHtml(_data.get(clan.getId()).flag);
						}
						else
						{
							htmltext = "35437-08.htm";
						}
					}
				}
			}
		}
		else if (event.startsWith("Select_NPC"))
		{
			if (!player.isClanLeader())
			{
				htmltext = "35437-09.htm";
			}
			else if (!_data.containsKey(clan.getId()))
			{
				htmltext = "35437-10.htm";
			}
			else
			{
				final String[] var = event.split(" ");
				if (var.length >= 2)
				{
					int id = 0;
					try
					{
						id = Integer.parseInt(var[1]);
					}
					catch (Exception e)
					{
						LOGGER.warning(getClass().getSimpleName() + "->select_clan_npc->Wrong mahum warrior id: " + var[1]);
					}
					if ((id > 0) && ((htmltext = getAllyHtml(id)) != null))
					{
						_data.get(clan.getId()).npc = id;
						saveNpc(id, clan.getId());
					}
				}
				else
				{
					LOGGER.warning(getClass().getSimpleName() + " Siege: Not enough parameters to save clan npc for clan: " + clan.getName());
				}
			}
		}
		else if (event.startsWith("View"))
		{
			ClanData cd = null;
			if (clan == null)
			{
				htmltext = "35437-10.htm";
			}
			else if ((cd = _data.get(clan.getId())) == null)
			{
				htmltext = "35437-03.htm";
			}
			else if (cd.npc == 0)
			{
				htmltext = "35437-11.htm";
			}
			else
			{
				htmltext = getAllyHtml(cd.npc);
			}
		}
		else if (event.startsWith("RegisterMember"))
		{
			if (clan == null)
			{
				htmltext = "35437-10.htm";
			}
			else if (!_hall.isRegistering())
			{
				htmltext = "35437-02.htm";
			}
			else if (!_data.containsKey(clan.getId()))
			{
				htmltext = "35437-03.htm";
			}
			else if (_data.get(clan.getId()).players.size() >= 18)
			{
				htmltext = "35437-12.htm";
			}
			else
			{
				final ClanData data = _data.get(clan.getId());
				data.players.add(player.getObjectId());
				saveMember(clan.getId(), player.getObjectId());
				if (data.npc == 0)
				{
					htmltext = "35437-11.htm";
				}
				else
				{
					htmltext = "35437-05.htm";
				}
			}
		}
		else if (event.startsWith("Attackers"))
		{
			if (_hall.isRegistering())
			{
				sendRegistrationPageDate(player);
				return null;
			}
			
			htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/conquerablehalls/BanditStronghold/35437-13.htm");
			int i = 1;
			for (Entry<Integer, ClanData> clanData : _data.entrySet())
			{
				final Clan attacker = ClanTable.getInstance().getClan(clanData.getKey());
				if (attacker == null)
				{
					continue;
				}
				
				htmltext = htmltext.replaceAll("%clan" + i + "%", clan.getName());
				htmltext = htmltext.replaceAll("%clanMem" + i + "%", String.valueOf(clanData.getValue().players.size()));
				i++;
			}
			
			if (_data.size() < 5)
			{
				for (int c = i; c < 5; c++)
				{
					htmltext = htmltext.replaceAll("%clan" + c + "%", "Empty pos.");
					htmltext = htmltext.replaceAll("%clanMem" + c + "%", String.valueOf(0));
				}
			}
		}
		else if (event.startsWith("CheckQuest"))
		{
			if ((clan == null) || (clan.getLevel() < 4))
			{
				htmltext = "35437-22.htm";
			}
			else if (!player.isClanLeader())
			{
				htmltext = "35437-23.htm";
			}
			else if ((clan.getHideoutId() > 0) || (clan.getFortId() > 0) || (clan.getCastleId() > 0))
			{
				htmltext = "35437-24.htm";
			}
			else if (!_hall.isWaitingBattle())
			{
				sendRegistrationPageDate(player);
				return null;
			}
			else if (player.getInventory().getItemByItemId(QUEST_REWARD) != null)
			{
				htmltext = "35437-25.htm";
			}
			else
			{
				if (player.getInventory().getInventoryItemCount(TARLK_AMULET, -1) >= 30)
				{
					htmltext = "35437-21a.htm";
				}
				else
				{
					htmltext = "35437-21.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (npc.getId() == MESSENGER)
		{
			if (!checkIsAttacker(player.getClan()))
			{
				final Clan clan = ClanTable.getInstance().getClan(_hall.getOwnerId());
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player, "data/scripts/conquerablehalls/BanditStronghold/35437-00.htm");
				html.replace("%clanName%", clan == null ? "no owner" : clan.getName());
				player.sendPacket(html);
			}
			else
			{
				return "35437-01.htm";
			}
		}
		else
		{
			final int index = npc.getId() - TELEPORT_1;
			if ((index == 0) && _firstPhase)
			{
				return "35560-00.htm";
			}
			
			TELE_ZONES[index].checkTeleportTask();
			return "35560-01.htm";
		}
		
		return "";
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (_hall.isInSiege())
		{
			final int npcId = npc.getId();
			for (int keys : _data.keySet())
			{
				if (_data.get(keys).npc == npcId)
				{
					removeParticipant(keys, true);
				}
			}
			
			synchronized (this)
			{
				final List<Integer> clanIds = new ArrayList<>(_data.keySet());
				if (_firstPhase)
				{
					if (((clanIds.size() == 1) && (_hall.getOwnerId() <= 0)) || (_data.get(clanIds.get(0)).npc == 0))
					{
						_missionAccomplished = true;
						cancelSiegeTask();
						endSiege();
					}
					else if ((_data.size() == 2) && (_hall.getOwnerId() > 0))
					{
						cancelSiegeTask();
						_firstPhase = false;
						_hall.getSiegeZone().setActive(false);
						for (int doorId : INNER_DOORS_TO_OPEN)
						{
							_hall.openCloseDoor(doorId, true);
						}
						
						for (ClanData data : _data.values())
						{
							doUnSpawns(data);
						}
						
						ThreadPool.schedule(() ->
						{
							for (int doorId : INNER_DOORS_TO_OPEN)
							{
								_hall.openCloseDoor(doorId, false);
							}
							
							for (Entry<Integer, ClanData> e : _data.entrySet())
							{
								doSpawns(e.getKey(), e.getValue());
							}
							
							_hall.getSiegeZone().setActive(true);
						}, 300000);
					}
				}
				else
				{
					_missionAccomplished = true;
					_winner = ClanTable.getInstance().getClan(clanIds.get(0));
					removeParticipant(clanIds.get(0), false);
					endSiege();
				}
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CENTER, 0);
		return null;
	}
	
	@Override
	public Clan getWinner()
	{
		return _winner;
	}
	
	@Override
	public void prepareOwner()
	{
		if (_hall.getOwnerId() > 0)
		{
			registerClan(ClanTable.getInstance().getClan(_hall.getOwnerId()));
		}
		
		_hall.banishForeigners();
		final SystemMessage msg = new SystemMessage(SystemMessageId.THE_REGISTRATION_TERM_FOR_S1_HAS_ENDED);
		msg.addString(ClanHallTable.getInstance().getClanHallById(_hall.getId()).getName());
		Broadcast.toAllOnlinePlayers(msg);
		_hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
		
		_siegeTask = ThreadPool.schedule(new SiegeStarts(), 3600000);
	}
	
	@Override
	public void startSiege()
	{
		if (getAttackers().size() < 2)
		{
			onSiegeEnds();
			getAttackers().clear();
			_hall.updateNextSiege();
			final SystemMessage sm = new SystemMessage(SystemMessageId.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
			sm.addString(ClanHallTable.getInstance().getClanHallById(_hall.getId()).getName());
			Broadcast.toAllOnlinePlayers(sm);
			return;
		}
		
		for (int door : OUTTER_DOORS_TO_OPEN)
		{
			_hall.openCloseDoor(door, true);
		}
		
		if (_hall.getOwnerId() > 0)
		{
			final Clan owner = ClanTable.getInstance().getClan(_hall.getOwnerId());
			final Location loc = _hall.getZone().getSpawns().get(0);
			for (ClanMember member : owner.getMembers())
			{
				if (member != null)
				{
					final Player player = member.getPlayer();
					if ((player != null) && player.isOnline())
					{
						player.teleToLocation(loc, false);
					}
				}
			}
		}
		
		ThreadPool.schedule(() ->
		{
			for (int door : OUTTER_DOORS_TO_OPEN)
			{
				_hall.openCloseDoor(door, false);
			}
			
			_hall.getZone().banishNonSiegeParticipants();
			
			startSiege();
		}, 300000);
	}
	
	@Override
	public void onSiegeStarts()
	{
		for (Entry<Integer, ClanData> clan : _data.entrySet())
		{
			try
			{
				final ClanData data = clan.getValue();
				doSpawns(clan.getKey(), data);
				fillPlayerList(data);
			}
			catch (Exception e)
			{
				endSiege();
				LOGGER.warning(getClass().getSimpleName() + ": Problems in siege initialization! " + e.getMessage());
			}
		}
	}
	
	@Override
	public void endSiege()
	{
		if (_hall.getOwnerId() > 0)
		{
			final Clan clan = ClanTable.getInstance().getClan(_hall.getOwnerId());
			clan.setHideoutId(0);
			_hall.free();
		}
		super.endSiege();
	}
	
	@Override
	public void onSiegeEnds()
	{
		if (_data.size() > 0)
		{
			for (int clanId : _data.keySet())
			{
				if (_hall.getOwnerId() == clanId)
				{
					removeParticipant(clanId, false);
				}
				else
				{
					removeParticipant(clanId, true);
				}
			}
		}
		clearTables();
	}
	
	@Override
	public final Location getInnerSpawnLoc(Player player)
	{
		Location loc = null;
		if (player.getId() == _hall.getOwnerId())
		{
			loc = _hall.getZone().getSpawns().get(0);
		}
		else
		{
			final ClanData cd = _data.get(player.getId());
			if (cd != null)
			{
				final int index = cd.flag - FLAG_RED;
				if ((index >= 0) && (index <= 4))
				{
					loc = _hall.getZone().getChallengerSpawns().get(index);
				}
				else
				{
					throw new ArrayIndexOutOfBoundsException();
				}
			}
		}
		return loc;
	}
	
	@Override
	public final boolean canPlantFlag()
	{
		return false;
	}
	
	@Override
	public final boolean doorIsAutoAttackable()
	{
		return false;
	}
	
	void doSpawns(int clanId, ClanData data)
	{
		try
		{
			final NpcTemplate mahumTemplate = NpcData.getInstance().getTemplate(data.npc);
			final NpcTemplate flagTemplate = NpcData.getInstance().getTemplate(data.flag);
			if (flagTemplate == null)
			{
				LOGGER.warning(getClass().getSimpleName() + ": Flag NpcTemplate[" + data.flag + "] does not exist!");
				throw new NullPointerException();
			}
			else if (mahumTemplate == null)
			{
				LOGGER.warning(getClass().getSimpleName() + ": Ally NpcTemplate[" + data.npc + "] does not exist!");
				throw new NullPointerException();
			}
			
			int index = 0;
			if (_firstPhase)
			{
				index = data.flag - FLAG_RED;
			}
			else
			{
				index = clanId == _hall.getOwnerId() ? 5 : 6;
			}
			final Location loc = FLAG_COORDS[index];
			
			data.flagInstance = addSpawn(flagTemplate.getId(), loc);
			data.flagInstance.getSpawn().setRespawnDelay(10000);
			data.flagInstance.getSpawn().startRespawn();
			
			data.warrior = addSpawn(mahumTemplate.getId(), loc);
			data.warrior.getSpawn().setRespawnDelay(10000);
			data.warrior.getSpawn().startRespawn();
			
			((SpecialSiegeGuardAI) data.warrior.getSpawn().getLastSpawn().getAI()).getAlly().addAll(data.players);
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Could not make clan spawns: " + e.getMessage());
		}
	}
	
	private void fillPlayerList(ClanData data)
	{
		for (int objId : data.players)
		{
			final Player player = World.getInstance().getPlayer(objId);
			if (player != null)
			{
				data.playersInstance.add(player);
			}
		}
	}
	
	private void registerClan(Clan clan)
	{
		final int clanId = clan.getId();
		final SiegeClan sc = new SiegeClan(clanId, SiegeClanType.ATTACKER);
		getAttackers().put(clanId, sc);
		
		final ClanData data = new ClanData();
		data.flag = ROYAL_FLAG + _data.size();
		data.players.add(clan.getLeaderId());
		_data.put(clanId, data);
		
		saveClan(clanId, data.flag);
		saveMember(clanId, clan.getLeaderId());
	}
	
	private void doUnSpawns(ClanData data)
	{
		if (data.flagInstance != null)
		{
			data.flagInstance.getSpawn().stopRespawn();
			data.flagInstance.getSpawn().getLastSpawn().deleteMe();
		}
		if (data.warrior != null)
		{
			data.warrior.getSpawn().stopRespawn();
			data.warrior.getSpawn().getLastSpawn().deleteMe();
		}
	}
	
	private void removeParticipant(int clanId, boolean teleport)
	{
		final ClanData dat = _data.remove(clanId);
		if (dat != null)
		{
			if (dat.flagInstance != null)
			{
				dat.flagInstance.getSpawn().stopRespawn();
				if (dat.flagInstance.getSpawn().getLastSpawn() != null)
				{
					dat.flagInstance.getSpawn().getLastSpawn().deleteMe();
				}
			}
			
			if (dat.warrior != null)
			{
				dat.warrior.getSpawn().stopRespawn();
				if (dat.warrior.getSpawn().getLastSpawn() != null)
				{
					dat.warrior.getSpawn().getLastSpawn().deleteMe();
				}
			}
			
			dat.players.clear();
			
			if (teleport)
			{
				for (Player player : dat.playersInstance)
				{
					if (player != null)
					{
						player.teleToLocation(TeleportWhereType.TOWN);
					}
				}
			}
			
			dat.playersInstance.clear();
		}
	}
	
	public boolean canPayRegistration()
	{
		return true;
	}
	
	private void sendRegistrationPageDate(Player player)
	{
		final NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(player, "data/scripts/conquerablehalls/BanditStronghold/35437-14.htm");
		msg.replace("%nextSiege%", _hall.getSiegeDate().getTime().toString());
		player.sendPacket(msg);
	}
	
	public String getFlagHtml(int flag)
	{
		String result = "35437-14a.htm";
		switch (flag)
		{
			case 35423:
			{
				result = "35437-15.htm";
				break;
			}
			case 35424:
			{
				result = "35437-16.htm";
				break;
			}
			case 35425:
			{
				result = "35437-17.htm";
				break;
			}
			case 35426:
			{
				result = "35437-18.htm";
				break;
			}
			case 35427:
			{
				result = "35437-19.htm";
				break;
			}
		}
		return result;
	}
	
	public String getAllyHtml(int ally)
	{
		String result = null;
		switch (ally)
		{
			case 35428:
			{
				result = "35437-15a.htm";
				break;
			}
			case 35429:
			{
				result = "35437-16a.htm";
				break;
			}
			case 35430:
			{
				result = "35437-17a.htm";
				break;
			}
			case 35431:
			{
				result = "35437-18a.htm";
				break;
			}
			case 35432:
			{
				result = "35437-19a.htm";
				break;
			}
		}
		return result;
	}
	
	@Override
	public void loadAttackers()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(SQL_LOAD_ATTACKERS);
			statement.setInt(1, _hall.getId());
			final ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				final int clanId = rset.getInt("clan_id");
				if (ClanTable.getInstance().getClan(clanId) == null)
				{
					LOGGER.warning(getClass().getSimpleName() + ": Loaded an unexistent clan as attacker! Clan Id: " + clanId);
					continue;
				}
				
				final int flag = rset.getInt("flag");
				final int npc = rset.getInt("npc");
				final SiegeClan sc = new SiegeClan(clanId, SiegeClanType.ATTACKER);
				getAttackers().put(clanId, sc);
				
				final ClanData data = new ClanData();
				data.flag = flag;
				data.npc = npc;
				_data.put(clanId, data);
				
				loadAttackerMembers(clanId);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ".loadAttackers()->" + e.getMessage());
		}
	}
	
	private void loadAttackerMembers(int clanId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final ArrayList<Integer> listInstance = _data.get(clanId).players;
			if (listInstance == null)
			{
				LOGGER.warning(getClass().getSimpleName() + ": Tried to load unregistered clan: " + clanId + "[clan Id]");
				return;
			}
			
			final PreparedStatement statement = con.prepareStatement(SQL_LOAD_MEMEBERS);
			statement.setInt(1, clanId);
			final ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				listInstance.add(rset.getInt("object_id"));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ".loadAttackerMembers()->" + e.getMessage());
		}
	}
	
	private void saveClan(int clanId, int flag)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(SQL_SAVE_CLAN);
			statement.setInt(1, _hall.getId());
			statement.setInt(2, flag);
			statement.setInt(3, 0);
			statement.setInt(4, clanId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ".saveClan()->" + e.getMessage());
		}
	}
	
	private void saveNpc(int npc, int clanId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(SQL_SAVE_NPC);
			statement.setInt(1, npc);
			statement.setInt(2, clanId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ".saveNpc()->" + e.getMessage());
		}
	}
	
	private void saveMember(int clanId, int objectId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(SQL_SAVE_ATTACKER);
			statement.setInt(1, _hall.getId());
			statement.setInt(2, clanId);
			statement.setInt(3, objectId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ".saveMember()->" + e.getMessage());
		}
	}
	
	private void clearTables()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement stat1 = con.prepareStatement(SQL_CLEAR_CLAN);
			stat1.setInt(1, _hall.getId());
			stat1.execute();
			stat1.close();
			
			final PreparedStatement stat2 = con.prepareStatement(SQL_CLEAR_CLAN_ATTACKERS);
			stat2.setInt(1, _hall.getId());
			stat2.execute();
			stat2.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ".clearTables()->" + e.getMessage());
		}
	}
	
	protected class ClanData
	{
		int flag = 0;
		int npc = 0;
		ArrayList<Integer> players = new ArrayList<>(18);
		ArrayList<Player> playersInstance = new ArrayList<>(18);
		Npc warrior = null;
		Npc flagInstance = null;
	}
	
	public static void main(String[] args)
	{
		new BanditStronghold();
	}
}
