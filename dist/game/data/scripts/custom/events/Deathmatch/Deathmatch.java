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
package custom.events.Deathmatch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.time.SchedulingPattern;
import org.l2jbr_unity.commons.util.IXmlReader;
import org.l2jbr_unity.commons.util.TimeUtil;
import org.l2jbr_unity.gameserver.enums.SkillFinishType;
import org.l2jbr_unity.gameserver.instancemanager.AntiFeedManager;
import org.l2jbr_unity.gameserver.instancemanager.InstanceManager;
import org.l2jbr_unity.gameserver.instancemanager.ZoneManager;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.actor.Creature;
import org.l2jbr_unity.gameserver.model.actor.Npc;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.actor.Summon;
import org.l2jbr_unity.gameserver.model.events.EventType;
import org.l2jbr_unity.gameserver.model.events.annotations.RegisterEvent;
import org.l2jbr_unity.gameserver.model.events.impl.creature.OnCreatureDeath;
import org.l2jbr_unity.gameserver.model.events.impl.creature.player.OnPlayerLogout;
import org.l2jbr_unity.gameserver.model.events.listeners.AbstractEventListener;
import org.l2jbr_unity.gameserver.model.events.listeners.ConsumerEventListener;
import org.l2jbr_unity.gameserver.model.holders.ItemHolder;
import org.l2jbr_unity.gameserver.model.holders.SkillHolder;
import org.l2jbr_unity.gameserver.model.instancezone.Instance;
import org.l2jbr_unity.gameserver.model.instancezone.InstanceWorld;
import org.l2jbr_unity.gameserver.model.olympiad.OlympiadManager;
import org.l2jbr_unity.gameserver.model.quest.Event;
import org.l2jbr_unity.gameserver.model.quest.QuestTimer;
import org.l2jbr_unity.gameserver.model.zone.ZoneForm;
import org.l2jbr_unity.gameserver.model.zone.ZoneId;
import org.l2jbr_unity.gameserver.model.zone.ZoneType;
import org.l2jbr_unity.gameserver.network.serverpackets.ExPVPMatchCCRecord;
import org.l2jbr_unity.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jbr_unity.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jbr_unity.gameserver.util.Broadcast;
import org.l2jbr_unity.gameserver.util.Util;

/**
 * Deathmatch event.
 * @author NasSeKa
 */
public class Deathmatch extends Event
{
	// NPC
	private static final int MANAGER = 70011;
	// Skills
	private static final SkillHolder[] FIGHTER_BUFFS =
	{
		new SkillHolder(4322, 1), // Wind Walk
		new SkillHolder(4323, 1), // Shield
		new SkillHolder(5637, 1), // Magic Barrier
		new SkillHolder(4324, 1), // Bless the Body
		new SkillHolder(4325, 1), // Vampiric Rage
		new SkillHolder(4326, 1), // Regeneration
		new SkillHolder(5632, 1), // Haste
	};
	private static final SkillHolder[] MAGE_BUFFS =
	{
		new SkillHolder(4322, 1), // Wind Walk
		new SkillHolder(4323, 1), // Shield
		new SkillHolder(5637, 1), // Magic Barrier
		new SkillHolder(4328, 1), // Bless the Soul
		new SkillHolder(4329, 1), // Acumen
		new SkillHolder(4330, 1), // Concentration
		new SkillHolder(4331, 1), // Empower
	};
	private static final SkillHolder GHOST_WALKING = new SkillHolder(100000, 1); // Custom Ghost Walking
	// Others
	private static final int INSTANCE_ID = 3049;
	private static final int BLUE_DOOR_ID = 24190002;
	private static final int RED_DOOR_ID = 24190003;
	private static final Location MANAGER_SPAWN_LOC = new Location(83425, 148585, -3406, 32938);
	private static final ZoneForm SPAWN_1 = ZoneManager.getInstance().getZoneByName("colosseum_battle1").getZone();
	private static final ZoneForm SPAWN_2 = ZoneManager.getInstance().getZoneByName("colosseum_battle2").getZone();
	private static final ZoneForm SPAWN_3 = ZoneManager.getInstance().getZoneByName("colosseum_battle3").getZone();
	// Settings
	private static final int REGISTRATION_TIME = 1; // Minutes
	private static final int WAIT_TIME = 20; // Seconds
	private static final int FIGHT_TIME = 3; // Minutes
	private static final int INACTIVITY_TIME = 2; // Minutes
	private static final int MINIMUM_PARTICIPANT_LEVEL = 85;
	private static final int MAXIMUM_PARTICIPANT_LEVEL = 200;
	private static final int MINIMUM_PARTICIPANT_COUNT = 2;
	private static final int MAXIMUM_PARTICIPANT_COUNT = 24; // Scoreboard has 25 slots
	private static final ItemHolder REWARD = new ItemHolder(57, 1000000); // Adena
	// Misc
	private static final Map<Player, Integer> PLAYER_SCORES = new ConcurrentHashMap<>();
	private static final Set<Player> PLAYER_LIST = ConcurrentHashMap.newKeySet();
	private static InstanceWorld PVP_WORLD = null;
	private static Npc MANAGER_NPC_INSTANCE = null;
	private static boolean EVENT_ACTIVE = false;
	
	private Deathmatch()
	{
		addTalkId(MANAGER);
		addFirstTalkId(MANAGER);
		
		loadConfig();
	}
	
	private void loadConfig()
	{
		new IXmlReader()
		{
			@Override
			public void load()
			{
				parseDatapackFile("data/scripts/custom/events/Deathmatch/config.xml");
			}
			
			@Override
			public void parseDocument(Document doc, File f)
			{
				final AtomicInteger count = new AtomicInteger(0);
				forEach(doc, "event", eventNode ->
				{
					final StatSet att = new StatSet(parseAttributes(eventNode));
					final String name = att.getString("name");
					for (Node node = doc.getDocumentElement().getFirstChild(); node != null; node = node.getNextSibling())
					{
						switch (node.getNodeName())
						{
							case "schedule":
							{
								final StatSet attributes = new StatSet(parseAttributes(node));
								final String pattern = attributes.getString("pattern");
								final SchedulingPattern schedulingPattern = new SchedulingPattern(pattern);
								final StatSet params = new StatSet();
								params.set("Name", name);
								params.set("SchedulingPattern", pattern);
								final long delay = schedulingPattern.getDelayToNextFromNow();
								getTimers().addTimer("Schedule" + count.incrementAndGet(), params, delay + 5000, null, null); // Added 5 seconds to prevent overlapping.
								LOGGER.info("Event " + name + " scheduled at " + TimeUtil.getDateTimeString(System.currentTimeMillis() + delay));
								break;
							}
						}
					}
				});
			}
		}.load();
	}
	
	@Override
	public void onTimerEvent(String event, StatSet params, Npc npc, Player player)
	{
		if (event.startsWith("Schedule"))
		{
			eventStart(null);
			final SchedulingPattern schedulingPattern = new SchedulingPattern(params.getString("SchedulingPattern"));
			final long delay = schedulingPattern.getDelayToNextFromNow();
			getTimers().addTimer(event, params, delay + 5000, null, null); // Added 5 seconds to prevent overlapping.
			LOGGER.info("Event " + params.getString("Name") + " scheduled at " + TimeUtil.getDateTimeString(System.currentTimeMillis() + delay));
		}
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		if (!EVENT_ACTIVE)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "Participate":
			{
				if (canRegister(player))
				{
					if ((Config.DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP == 0) || AntiFeedManager.getInstance().tryAddPlayer(AntiFeedManager.L2EVENT_ID, player, Config.DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP))
					{
						PLAYER_LIST.add(player);
						PLAYER_SCORES.put(player, 0);
						player.setRegisteredOnEvent(true);
						addLogoutListener(player);
						htmltext = "registration-success.html";
					}
					else
					{
						htmltext = "registration-ip.html";
					}
				}
				else
				{
					htmltext = "registration-failed.html";
				}
				break;
			}
			case "CancelParticipation":
			{
				if (player.isOnEvent())
				{
					return null;
				}
				// Remove the player from the IP count
				if (Config.DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP > 0)
				{
					AntiFeedManager.getInstance().removePlayer(AntiFeedManager.L2EVENT_ID, player);
				}
				PLAYER_LIST.remove(player);
				PLAYER_SCORES.remove(player);
				removeListeners(player);
				player.setRegisteredOnEvent(false);
				htmltext = "registration-canceled.html";
				break;
			}
			case "BuffHeal":
			{
				if (player.isOnEvent() || player.isGM())
				{
					if (player.isInCombat())
					{
						htmltext = "manager-combat.html";
					}
					else
					{
						if (player.isMageClass())
						{
							for (SkillHolder skill : MAGE_BUFFS)
							{
								npc.setTarget(player);
								npc.doCast(skill.getSkill());
								// No animation.
								// skill.getSkill().applyEffects(npc, player);
							}
						}
						else
						{
							for (SkillHolder skill : FIGHTER_BUFFS)
							{
								npc.setTarget(player);
								npc.doCast(skill.getSkill());
								// No animation.
								// skill.getSkill().applyEffects(npc, player);
							}
						}
						player.setCurrentHp(player.getMaxHp());
						player.setCurrentMp(player.getMaxMp());
						player.setCurrentCp(player.getMaxCp());
					}
				}
				break;
			}
			case "TeleportToArena":
			{
				// Remove offline players.
				for (Player participant : PLAYER_LIST)
				{
					if ((participant == null) || (participant.isOnlineInt() != 1))
					{
						PLAYER_LIST.remove(participant);
						PLAYER_SCORES.remove(participant);
					}
				}
				// Check if there are enough players to start the event.
				if (PLAYER_LIST.size() < MINIMUM_PARTICIPANT_COUNT)
				{
					Broadcast.toAllOnlinePlayers("Deathmatch Event: Event was canceled, not enough participants.");
					for (Player participant : PLAYER_LIST)
					{
						removeListeners(participant);
						participant.setRegisteredOnEvent(false);
					}
					EVENT_ACTIVE = false;
					return null;
				}
				// Create the instance.
				final InstanceWorld world = new InstanceWorld();
				world.setInstance(InstanceManager.getInstance().createDynamicInstance(INSTANCE_ID));
				InstanceManager.getInstance().addWorld(world);
				PVP_WORLD = world;
				// Randomize player list.
				final List<Player> playerList = new ArrayList<>(PLAYER_LIST.size());
				playerList.addAll(PLAYER_LIST);
				Collections.shuffle(playerList);
				PLAYER_LIST.clear();
				PLAYER_LIST.addAll(playerList);
				
				for (Player participant : PLAYER_LIST)
				{
					participant.setOnEvent(true);
					participant.setOnSoloEvent(true);
					participant.setRegisteredOnEvent(false);
					PVP_WORLD.addAllowed(participant);
					participant.leaveParty();
					RANDOM: switch (getRandom(1, 3))
					{
						case 1:
						{
							participant.teleToLocation(SPAWN_1.getRandomPoint(), PVP_WORLD.getInstanceId(), 50);
							break RANDOM;
						}
						case 2:
						{
							participant.teleToLocation(SPAWN_2.getRandomPoint(), PVP_WORLD.getInstanceId(), 50);
							break RANDOM;
						}
						case 3:
						{
							participant.teleToLocation(SPAWN_3.getRandomPoint(), PVP_WORLD.getInstanceId(), 50);
							break RANDOM;
						}
					}
					participant.setInvul(true);
					participant.setImmobilized(true);
					participant.disableAllSkills();
					final Summon summon = participant.getSummon();
					if (summon != null)
					{
						summon.setInvul(true);
						summon.setImmobilized(true);
						summon.disableAllSkills();
					}
					addDeathListener(participant);
				}
				
				// Spawn managers.
				// addSpawn(MANAGER, BLUE_BUFFER_SPAWN_LOC, false, (WAIT_TIME + FIGHT_TIME) * 60000, false, PVP_WORLD.getId());
				// addSpawn(MANAGER, RED_BUFFER_SPAWN_LOC, false, (WAIT_TIME + FIGHT_TIME) * 60000, false, PVP_WORLD.getId());
				// Initialize scores.
				// BLUE_SCORE = 0;
				// RED_SCORE = 0;
				// Initialize scoreboard.
				PVP_WORLD.broadcastPacket(new ExPVPMatchCCRecord(ExPVPMatchCCRecord.INITIALIZE, Util.sortByValue(PLAYER_SCORES, true)));
				// Schedule start.
				startQuestTimer("5", (WAIT_TIME * 1000) - 5000, null, null);
				startQuestTimer("4", (WAIT_TIME * 1000) - 4000, null, null);
				startQuestTimer("3", (WAIT_TIME * 1000) - 3000, null, null);
				startQuestTimer("2", (WAIT_TIME * 1000) - 2000, null, null);
				startQuestTimer("1", (WAIT_TIME * 1000) - 1000, null, null);
				startQuestTimer("StartFight", WAIT_TIME * 1000, null, null);
				break;
			}
			case "StartFight":
			{
				// Open doors.
				// PVP_WORLD.openDoor(BLUE_DOOR_ID);
				// PVP_WORLD.openDoor(RED_DOOR_ID);
				// Send message.
				broadcastScreenMessageWithEffect("The fight has began!", 5);
				for (Player participant : PLAYER_LIST)
				{
					participant.setInvul(false);
					participant.setImmobilized(false);
					participant.enableAllSkills();
					final Summon summon = participant.getSummon();
					if (summon != null)
					{
						summon.setInvul(false);
						summon.setImmobilized(false);
						summon.enableAllSkills();
					}
				}
				// Schedule finish.
				startQuestTimer("10", (FIGHT_TIME * 60000) - 10000, null, null);
				startQuestTimer("9", (FIGHT_TIME * 60000) - 9000, null, null);
				startQuestTimer("8", (FIGHT_TIME * 60000) - 8000, null, null);
				startQuestTimer("7", (FIGHT_TIME * 60000) - 7000, null, null);
				startQuestTimer("6", (FIGHT_TIME * 60000) - 6000, null, null);
				startQuestTimer("5", (FIGHT_TIME * 60000) - 5000, null, null);
				startQuestTimer("4", (FIGHT_TIME * 60000) - 4000, null, null);
				startQuestTimer("3", (FIGHT_TIME * 60000) - 3000, null, null);
				startQuestTimer("2", (FIGHT_TIME * 60000) - 2000, null, null);
				startQuestTimer("1", (FIGHT_TIME * 60000) - 1000, null, null);
				startQuestTimer("EndFight", FIGHT_TIME * 60000, null, null);
				break;
			}
			case "EndFight":
			{
				// Close doors.
				PVP_WORLD.closeDoor(BLUE_DOOR_ID);
				PVP_WORLD.closeDoor(RED_DOOR_ID);
				// Disable players.
				for (Player participant : PLAYER_LIST)
				{
					participant.setInvul(true);
					participant.setImmobilized(true);
					participant.disableAllSkills();
					final Summon summon = participant.getSummon();
					if (summon != null)
					{
						summon.setInvul(true);
						summon.setImmobilized(true);
						summon.disableAllSkills();
					}
				}
				// Make sure noone is dead.
				for (Player participant : PLAYER_LIST)
				{
					if (participant.isDead())
					{
						participant.doRevive();
					}
				}
				
				Player winner = Collections.max(PLAYER_SCORES.entrySet(), Map.Entry.comparingByValue()).getKey();
				winner.addItem("Deathmatch winner", REWARD, player, true);
				
				startQuestTimer("ScoreBoard", 3500, null, null);
				startQuestTimer("TeleportOut", 7000, null, null);
				break;
			}
			case "ScoreBoard":
			{
				PVP_WORLD.broadcastPacket(new ExPVPMatchCCRecord(ExPVPMatchCCRecord.FINISH, Util.sortByValue(PLAYER_SCORES, true)));
				break;
			}
			case "TeleportOut":
			{
				// Remove event listeners.
				for (Player participant : PLAYER_LIST)
				{
					removeListeners(participant);
					participant.setOnEvent(false);
					participant.setOnSoloEvent(false);
					participant.leaveParty();
					PVP_WORLD.ejectPlayer(participant);
				}
				// Destroy world.
				if (PVP_WORLD != null)
				{
					final Instance instance = InstanceManager.getInstance().getInstance(PVP_WORLD.getInstanceId());
					if (instance != null)
					{
						instance.setDuration(60000);
						instance.setEmptyDestroyTime(0);
					}
					PVP_WORLD = null;
				}
				// Enable players.
				for (Player participant : PLAYER_LIST)
				{
					participant.setInvul(false);
					participant.setImmobilized(false);
					participant.enableAllSkills();
					final Summon summon = participant.getSummon();
					if (summon != null)
					{
						summon.setInvul(true);
						summon.setImmobilized(true);
						summon.disableAllSkills();
					}
				}
				EVENT_ACTIVE = false;
				break;
			}
			case "ResurrectPlayer":
			{
				if (player.isDead() && player.isOnEvent())
				{
					player.setIsPendingRevive(true);
					RANDOM: switch (getRandom(1, 3))
					{
						case 1:
						{
							player.teleToLocation(SPAWN_1.getRandomPoint(), PVP_WORLD.getInstanceId(), 50);
							break RANDOM;
						}
						case 2:
						{
							player.teleToLocation(SPAWN_2.getRandomPoint(), PVP_WORLD.getInstanceId(), 50);
							break RANDOM;
						}
						case 3:
						{
							player.teleToLocation(SPAWN_3.getRandomPoint(), PVP_WORLD.getInstanceId(), 50);
							break RANDOM;
						}
					}
					resetActivityTimers(player);
				}
				break;
			}
			case "10":
			case "9":
			case "8":
			case "7":
			case "6":
			case "5":
			case "4":
			case "3":
			case "2":
			case "1":
			{
				broadcastScreenMessage(event, 4);
				break;
			}
			case "manager-cancel":
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player, "data/scripts/custom/events/Deathmatch/manager-cancel.html");
				html.replace("%player_numbers%", String.valueOf(PLAYER_LIST.size()));
				player.sendPacket(html);
				break;
			}
			case "manager-register":
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player, "data/scripts/custom/events/Deathmatch/manager-register.html");
				html.replace("%player_numbers%", String.valueOf(PLAYER_LIST.size()));
				player.sendPacket(html);
				break;
			}
		}
		// Activity timer.
		if (event.startsWith("KickPlayer") && (player != null) && (player.getInstanceId() == PVP_WORLD.getInstanceId()))
		{
			if (event.contains("Warning"))
			{
				sendScreenMessage(player, "You have been marked as inactive!", 10);
			}
			else
			{
				PVP_WORLD.ejectPlayer(player);
				PLAYER_LIST.remove(player);
				PLAYER_SCORES.remove(player);
				player.setOnEvent(false);
				player.setOnSoloEvent(false);
				removeListeners(player);
				player.sendMessage("You have been kicked for been inactive.");
				if (PVP_WORLD != null)
				{
					broadcastScreenMessageWithEffect("Player " + player.getName() + " was kicked for been inactive!", 7);
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		// Event not active.
		if (!EVENT_ACTIVE)
		{
			return null;
		}
		
		// Player has already registered.
		if (PLAYER_LIST.contains(player))
		{
			// Npc is in instance.
			if (npc.getInstanceId() > 0)
			{
				return "manager-buffheal.html";
			}
			startQuestTimer("manager-cancel", 5, npc, player);
			return "manager-cancel.html";
		}
		// Player is not registered.
		startQuestTimer("manager-register", 5, npc, player);
		return "manager-register.html";
	}
	
	@Override
	public String onExitZone(Creature creature, ZoneType zone)
	{
		if (creature.isPlayer() && creature.getActingPlayer().isOnEvent())
		{
			final Player player = creature.getActingPlayer();
			cancelQuestTimer("KickPlayer" + creature.getObjectId(), null, player);
			cancelQuestTimer("KickPlayerWarning" + creature.getObjectId(), null, player);
			// Removed invulnerability shield.
			if (player.isAffectedBySkill(GHOST_WALKING.getSkillId()))
			{
				player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, GHOST_WALKING.getSkill());
			}
		}
		return super.onExitZone(creature, zone);
	}
	
	private boolean canRegister(Player player)
	{
		if (PLAYER_LIST.contains(player))
		{
			player.sendMessage("You are already registered on this event.");
			return false;
		}
		if (player.getLevel() < MINIMUM_PARTICIPANT_LEVEL)
		{
			player.sendMessage("Your level is too low to participate.");
			return false;
		}
		if (player.getLevel() > MAXIMUM_PARTICIPANT_LEVEL)
		{
			player.sendMessage("Your level is too high to participate.");
			return false;
		}
		if (player.isRegisteredOnEvent() || (player.getBlockCheckerArena() > -1))
		{
			player.sendMessage("You are already registered on an event.");
			return false;
		}
		if (PLAYER_LIST.size() >= MAXIMUM_PARTICIPANT_COUNT)
		{
			player.sendMessage("There are too many players registered on the event.");
			return false;
		}
		if (player.isFlyingMounted())
		{
			player.sendMessage("You cannot register on the event while flying.");
			return false;
		}
		if (player.isTransformed())
		{
			player.sendMessage("You cannot register on the event while on a transformed state.");
			return false;
		}
		if (!player.isInventoryUnder80(false))
		{
			player.sendMessage("There are too many items in your inventory.");
			player.sendMessage("Try removing some items.");
			return false;
		}
		if ((player.getWeightPenalty() != 0))
		{
			player.sendMessage("Your invetory weight has exceeded the normal limit.");
			player.sendMessage("Try removing some items.");
			return false;
		}
		if (player.isCursedWeaponEquipped() || (player.getKarma() > 0))
		{
			player.sendMessage("People with bad reputation can't register.");
			return false;
		}
		if (player.isInDuel())
		{
			player.sendMessage("You cannot register while on a duel.");
			return false;
		}
		if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendMessage("You cannot participate while registered on the Olympiad.");
			return false;
		}
		if (player.getInstanceId() > 0)
		{
			player.sendMessage("You cannot register while in an instance.");
			return false;
		}
		if (player.isInSiege() || player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendMessage("You cannot register while on a siege.");
			return false;
		}
		if (player.isFishing())
		{
			player.sendMessage("You cannot register while fishing.");
			return false;
		}
		return true;
	}
	
	private void sendScreenMessage(Player player, String message, int duration)
	{
		player.sendPacket(new ExShowScreenMessage(message, ExShowScreenMessage.TOP_CENTER, duration * 1000, 0, true, false));
	}
	
	private void broadcastScreenMessage(String message, int duration)
	{
		PVP_WORLD.broadcastPacket(new ExShowScreenMessage(message, ExShowScreenMessage.TOP_CENTER, duration * 1000, 0, true, false));
	}
	
	private void broadcastScreenMessageWithEffect(String message, int duration)
	{
		PVP_WORLD.broadcastPacket(new ExShowScreenMessage(message, ExShowScreenMessage.TOP_CENTER, duration * 1000, 0, true, true));
	}
	
	private void addLogoutListener(Player player)
	{
		player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_LOGOUT, (OnPlayerLogout event) -> onPlayerLogout(event), this));
	}
	
	private void addDeathListener(Player player)
	{
		player.addListener(new ConsumerEventListener(player, EventType.ON_CREATURE_DEATH, (OnCreatureDeath event) -> onPlayerDeath(event), this));
	}
	
	private void removeListeners(Player player)
	{
		for (AbstractEventListener listener : player.getListeners(EventType.ON_PLAYER_LOGOUT))
		{
			if (listener.getOwner() == this)
			{
				listener.unregisterMe();
			}
		}
		for (AbstractEventListener listener : player.getListeners(EventType.ON_CREATURE_DEATH))
		{
			if (listener.getOwner() == this)
			{
				listener.unregisterMe();
			}
		}
	}
	
	private void resetActivityTimers(Player player)
	{
		cancelQuestTimer("KickPlayer" + player.getObjectId(), null, player);
		cancelQuestTimer("KickPlayerWarning" + player.getObjectId(), null, player);
		startQuestTimer("KickPlayer" + player.getObjectId(), PVP_WORLD.getDoor(BLUE_DOOR_ID).isOpen() ? INACTIVITY_TIME * 60000 : (INACTIVITY_TIME * 60000) + (WAIT_TIME * 1000), null, player);
		startQuestTimer("KickPlayerWarning" + player.getObjectId(), PVP_WORLD.getDoor(BLUE_DOOR_ID).isOpen() ? (INACTIVITY_TIME / 2) * 60000 : ((INACTIVITY_TIME / 2) * 60000) + (WAIT_TIME * 1000), null, player);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGOUT)
	private void onPlayerLogout(OnPlayerLogout event)
	{
		final Player player = event.getPlayer();
		// Remove player from lists.
		PLAYER_LIST.remove(player);
		PLAYER_SCORES.remove(player);
	}
	
	@RegisterEvent(EventType.ON_CREATURE_DEATH)
	public void onPlayerDeath(OnCreatureDeath event)
	{
		if (event.getTarget().isPlayer())
		{
			final Player killedPlayer = event.getTarget().getActingPlayer();
			final Player killer = event.getAttacker().getActingPlayer();
			// Confirm player kill.
			PLAYER_SCORES.put(killer, PLAYER_SCORES.get(killer) + 1);
			PVP_WORLD.broadcastPacket(new ExPVPMatchCCRecord(ExPVPMatchCCRecord.UPDATE, Util.sortByValue(PLAYER_SCORES, true)));
			// Auto release after 10 seconds.
			startQuestTimer("ResurrectPlayer", 10000, null, killedPlayer);
		}
	}
	
	@Override
	public boolean eventStart(Player eventMaker)
	{
		if (EVENT_ACTIVE)
		{
			return false;
		}
		EVENT_ACTIVE = true;
		
		// Cancel timers. (In case event started immediately after another event was canceled.)
		for (List<QuestTimer> timers : getQuestTimers().values())
		{
			for (QuestTimer timer : timers)
			{
				timer.cancel();
			}
		}
		// Register the event at AntiFeedManager and clean it for just in case if the event is already registered
		if (Config.DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP > 0)
		{
			AntiFeedManager.getInstance().registerEvent(AntiFeedManager.L2EVENT_ID);
			AntiFeedManager.getInstance().clear(AntiFeedManager.L2EVENT_ID);
		}
		// Clear player lists.
		PLAYER_LIST.clear();
		PLAYER_SCORES.clear();
		// Spawn event manager.
		MANAGER_NPC_INSTANCE = addSpawn(MANAGER, MANAGER_SPAWN_LOC, false, REGISTRATION_TIME * 60000);
		MANAGER_NPC_INSTANCE.setTitle("Deathmatch Event");
		MANAGER_NPC_INSTANCE.broadcastStatusUpdate();
		startQuestTimer("TeleportToArena", REGISTRATION_TIME * 60000, null, null);
		// Send message to players.
		Broadcast.toAllOnlinePlayers("Deathmatch Event: Registration opened for " + REGISTRATION_TIME + " minutes.");
		Broadcast.toAllOnlinePlayers("Deathmatch Event: You can register at Giran Event Manager.");
		return true;
	}
	
	@Override
	public boolean eventStop()
	{
		if (!EVENT_ACTIVE)
		{
			return false;
		}
		EVENT_ACTIVE = false;
		
		// Despawn event manager.
		MANAGER_NPC_INSTANCE.deleteMe();
		// Cancel timers.
		for (List<QuestTimer> timers : getQuestTimers().values())
		{
			for (QuestTimer timer : timers)
			{
				timer.cancel();
			}
		}
		// Remove participants.
		for (Player participant : PLAYER_LIST)
		{
			removeListeners(participant);
			participant.setRegisteredOnEvent(false);
			participant.setOnEvent(false);
			participant.setOnSoloEvent(false);
			participant.setInvul(false);
			participant.setImmobilized(false);
			participant.enableAllSkills();
			final Summon summon = participant.getSummon();
			if (summon != null)
			{
				summon.setInvul(false);
				summon.setImmobilized(false);
				summon.enableAllSkills();
			}
		}
		if (PVP_WORLD != null)
		{
			final Instance instance = InstanceManager.getInstance().getInstance(PVP_WORLD.getInstanceId());
			if (instance != null)
			{
				instance.setDuration(60000);
				instance.setEmptyDestroyTime(0);
			}
			PVP_WORLD = null;
		}
		// Send message to players.
		Broadcast.toAllOnlinePlayers("Deathmatch Event: Event was canceled.");
		return true;
	}
	
	@Override
	public boolean eventBypass(Player player, String bypass)
	{
		return false;
	}
	
	public static void main(String[] args)
	{
		new Deathmatch();
	}
}
