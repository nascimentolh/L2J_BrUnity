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
package org.l2jbr_unity.gameserver.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.util.IXmlReader;
import org.l2jbr_unity.gameserver.data.xml.NpcData;
import org.l2jbr_unity.gameserver.instancemanager.DayNightSpawnManager;
import org.l2jbr_unity.gameserver.instancemanager.ZoneManager;
import org.l2jbr_unity.gameserver.model.Location;
import org.l2jbr_unity.gameserver.model.Spawn;
import org.l2jbr_unity.gameserver.model.StatSet;
import org.l2jbr_unity.gameserver.model.World;
import org.l2jbr_unity.gameserver.model.actor.templates.NpcTemplate;
import org.l2jbr_unity.gameserver.model.zone.ZoneForm;
import org.l2jbr_unity.gameserver.model.zone.form.ZoneCuboid;
import org.l2jbr_unity.gameserver.model.zone.form.ZoneCylinder;
import org.l2jbr_unity.gameserver.model.zone.form.ZoneNPoly;
import org.l2jbr_unity.gameserver.model.zone.type.NpcSpawnTerritory;

/**
 * Spawn data retriever.
 * @author Zoey76, Mobius
 */
public class SpawnTable implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(SpawnTable.class.getName());
	private static final String OTHER_XML_FOLDER = "data/spawns/Others";
	private static final Map<Integer, Set<Spawn>> _spawnTable = new ConcurrentHashMap<>();
	private static final Map<Integer, String> _spawnTemplates = new ConcurrentHashMap<>();
	private int _spawnCount = 0;
	
	@Override
	public void load()
	{
		_spawnTemplates.put(0, "None");
		if (!Config.ALT_DEV_NO_SPAWNS)
		{
			LOGGER.info(getClass().getSimpleName() + ": Initializing spawns...");
			parseDatapackDirectory("data/spawns", true);
			LOGGER.info(getClass().getSimpleName() + ": " + _spawnCount + " spawns have been initialized!");
		}
	}
	
	/**
	 * Verifies if the template exists and it's spawnable.
	 * @param npcId the NPC ID
	 * @return {@code true} if the NPC ID belongs to an spawnable tempalte, {@code false} otherwise
	 */
	private boolean checkTemplate(int npcId)
	{
		final NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(npcId);
		if (npcTemplate == null)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Data missing in NPC table for ID: " + npcId + ".");
			return false;
		}
		
		if (npcTemplate.isType("SiegeGuard") || npcTemplate.isType("RaidBoss") || (!Config.ALLOW_CLASS_MASTERS && npcTemplate.isType("ClassMaster")))
		{
			// Don't spawn
			return false;
		}
		
		if (!Config.FAKE_PLAYERS_ENABLED && npcTemplate.isFakePlayer())
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		NamedNodeMap attrs;
		for (Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
		{
			if (list.getNodeName().equalsIgnoreCase("list"))
			{
				attrs = list.getAttributes();
				// skip disabled spawnlists
				if (!Boolean.parseBoolean(attrs.getNamedItem("enabled").getNodeValue()))
				{
					continue;
				}
				for (Node param = list.getFirstChild(); param != null; param = param.getNextSibling())
				{
					attrs = param.getAttributes();
					if (param.getNodeName().equalsIgnoreCase("spawn"))
					{
						String territoryName = null;
						String spawnName = null;
						Map<String, Integer> map = null;
						
						// Check, if spawn name specified
						if (attrs.getNamedItem("name") != null)
						{
							spawnName = parseString(attrs, "name");
						}
						// Check, if spawn territory specified
						if (attrs.getNamedItem("zone") != null)
						{
							territoryName = parseString(attrs, "zone");
						}
						
						for (Node npctag = param.getFirstChild(); npctag != null; npctag = npctag.getNextSibling())
						{
							attrs = npctag.getAttributes();
							// Check if there are any AI parameters
							if (npctag.getNodeName().equalsIgnoreCase("AIData"))
							{
								attrs = npctag.getAttributes();
								if (map == null)
								{
									map = new HashMap<>();
								}
								for (Node c = npctag.getFirstChild(); c != null; c = c.getNextSibling())
								{
									// Skip odd nodes
									if (c.getNodeName().equals("#text"))
									{
										continue;
									}
									int val;
									switch (c.getNodeName())
									{
										case "disableRandomAnimation":
										case "disableRandomWalk":
										{
											val = Boolean.parseBoolean(c.getTextContent()) ? 1 : 0;
											break;
										}
										default:
										{
											val = Integer.parseInt(c.getTextContent());
										}
									}
									map.put(c.getNodeName(), val);
								}
							}
							// Check for NPC spawn territories
							else if (npctag.getNodeName().equalsIgnoreCase("territory"))
							{
								if (ZoneManager.getInstance().spawnTerritoryExists(territoryName))
								{
									continue;
								}
								
								final int minZ = parseInteger(attrs, "minZ");
								final int maxZ = parseInteger(attrs, "maxZ");
								final String zoneShape = parseString(attrs, "shape", "NPoly");
								
								final List<int[]> rs = new ArrayList<>();
								int[][] coords;
								ZoneForm zoneForm = null;
								try
								{
									for (Node c = npctag.getFirstChild(); c != null; c = c.getNextSibling())
									{
										if ("node".equalsIgnoreCase(c.getNodeName()))
										{
											attrs = c.getAttributes();
											final int[] point = new int[2];
											point[0] = parseInteger(attrs, "x");
											point[1] = parseInteger(attrs, "y");
											rs.add(point);
										}
									}
									
									coords = rs.toArray(new int[rs.size()][2]);
									rs.clear();
									
									if ((coords == null) || (coords.length == 0))
									{
										LOGGER.warning(getClass().getSimpleName() + ": SpawnTable: missing data for spawn territory: " + territoryName + " XML file: " + f.getName());
										continue;
									}
									
									// Create this zone. Parsing for cuboids is a bit different than for other polygons cuboids need exactly 2 points to be defined.
									// Other polygons need at least 3 (one per vertex)
									if (zoneShape.equalsIgnoreCase("Cuboid"))
									{
										if (coords.length == 2)
										{
											zoneForm = new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ);
										}
										else
										{
											LOGGER.warning(getClass().getSimpleName() + ": SpawnTable: Missing cuboid vertex data for territory: " + territoryName + " in file: " + f.getName());
											continue;
										}
									}
									else if (zoneShape.equalsIgnoreCase("NPoly"))
									{
										// nPoly needs to have at least 3 vertices
										if (coords.length > 2)
										{
											final int[] aX = new int[coords.length];
											final int[] aY = new int[coords.length];
											for (int i = 0; i < coords.length; i++)
											{
												aX[i] = coords[i][0];
												aY[i] = coords[i][1];
											}
											zoneForm = new ZoneNPoly(aX, aY, minZ, maxZ);
										}
										else
										{
											LOGGER.warning(getClass().getSimpleName() + ": SpawnTable: Bad data for territory: " + territoryName + " in file: " + f.getName());
											continue;
										}
									}
									else if (zoneShape.equalsIgnoreCase("Cylinder"))
									{
										// A Cylinder zone requires a center point at x,y and a radius
										final int zoneRad = Integer.parseInt(attrs.getNamedItem("rad").getNodeValue());
										if ((coords.length == 1) && (zoneRad > 0))
										{
											zoneForm = new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad);
										}
										else
										{
											LOGGER.warning(getClass().getSimpleName() + ": SpawnTable: Bad data for territory: " + territoryName + " in file: " + f.getName());
											continue;
										}
									}
									else
									{
										LOGGER.warning(getClass().getSimpleName() + ": SpawnTable: Unknown shape: \"" + zoneShape + "\"  for territory: " + territoryName + " in file: " + f.getName());
										continue;
									}
								}
								catch (Exception e)
								{
									LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": SpawnTable: Failed to load territory " + territoryName + " coordinates: " + e.getMessage(), e);
								}
								
								ZoneManager.getInstance().addSpawnTerritory(territoryName, new NpcSpawnTerritory(territoryName, zoneForm));
							}
							// Check for NPC banned spawn territories
							else if (npctag.getNodeName().equalsIgnoreCase("banned_territory"))
							{
								if (!ZoneManager.getInstance().spawnTerritoryExists(territoryName))
								{
									LOGGER.warning(getClass().getSimpleName() + ": SpawnTable: banned_territory " + territoryName + " must be set after normal territory.");
									continue;
								}
								
								final int minZ = parseInteger(attrs, "minZ");
								final int maxZ = parseInteger(attrs, "maxZ");
								final String zoneShape = parseString(attrs, "shape", "NPoly");
								
								final List<int[]> rs = new ArrayList<>();
								int[][] coords;
								ZoneForm zoneForm = null;
								try
								{
									for (Node c = npctag.getFirstChild(); c != null; c = c.getNextSibling())
									{
										if ("node".equalsIgnoreCase(c.getNodeName()))
										{
											attrs = c.getAttributes();
											final int[] point = new int[2];
											point[0] = parseInteger(attrs, "x");
											point[1] = parseInteger(attrs, "y");
											rs.add(point);
										}
									}
									
									coords = rs.toArray(new int[rs.size()][2]);
									rs.clear();
									
									if ((coords == null) || (coords.length == 0))
									{
										LOGGER.warning(getClass().getSimpleName() + ": SpawnTable: missing data for spawn banned_territory: " + territoryName + " XML file: " + f.getName());
										continue;
									}
									
									// Create this zone. Parsing for cuboids is a bit different than for other polygons cuboids need exactly 2 points to be defined.
									// Other polygons need at least 3 (one per vertex)
									if (zoneShape.equalsIgnoreCase("Cuboid"))
									{
										if (coords.length == 2)
										{
											zoneForm = new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ);
										}
										else
										{
											LOGGER.warning(getClass().getSimpleName() + ": SpawnTable: Missing cuboid vertex data for banned_territory: " + territoryName + " in file: " + f.getName());
											continue;
										}
									}
									else if (zoneShape.equalsIgnoreCase("NPoly"))
									{
										// nPoly needs to have at least 3 vertices
										if (coords.length > 2)
										{
											final int[] aX = new int[coords.length];
											final int[] aY = new int[coords.length];
											for (int i = 0; i < coords.length; i++)
											{
												aX[i] = coords[i][0];
												aY[i] = coords[i][1];
											}
											zoneForm = new ZoneNPoly(aX, aY, minZ, maxZ);
										}
										else
										{
											LOGGER.warning(getClass().getSimpleName() + ": SpawnTable: Bad data for banned_territory: " + territoryName + " in file: " + f.getName());
											continue;
										}
									}
									else if (zoneShape.equalsIgnoreCase("Cylinder"))
									{
										// A Cylinder zone requires a center point at x,y and a radius
										final int zoneRad = Integer.parseInt(attrs.getNamedItem("rad").getNodeValue());
										if ((coords.length == 1) && (zoneRad > 0))
										{
											zoneForm = new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad);
										}
										else
										{
											LOGGER.warning(getClass().getSimpleName() + ": SpawnTable: Bad data for banned_territory: " + territoryName + " in file: " + f.getName());
											continue;
										}
									}
									else
									{
										LOGGER.warning(getClass().getSimpleName() + ": SpawnTable: Unknown shape: \"" + zoneShape + "\"  for banned_territory: " + territoryName + " in file: " + f.getName());
										continue;
									}
								}
								catch (Exception e)
								{
									LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": SpawnTable: Failed to load banned_territory " + territoryName + " coordinates: " + e.getMessage(), e);
								}
								
								ZoneManager.getInstance().getSpawnTerritory(territoryName).addBannedTerritory(zoneForm);
							}
							// Check for NPC spawns
							else if (npctag.getNodeName().equalsIgnoreCase("npc"))
							{
								// mandatory
								final int templateId = parseInteger(attrs, "id");
								
								// avoid spawning unwanted spawns
								if (!checkTemplate(templateId))
								{
									continue;
								}
								
								// coordinates are optional, if territory is specified; mandatory otherwise
								int x = 0;
								int y = 0;
								int z = 0;
								
								try
								{
									x = parseInteger(attrs, "x");
									y = parseInteger(attrs, "y");
									z = parseInteger(attrs, "z");
								}
								catch (NullPointerException npe)
								{
									// x, y, z can be unspecified, if this spawn is territory based, do nothing
								}
								
								if ((x == 0) && (y == 0) && (territoryName == null)) // Both coordinates and zone are unspecified
								{
									LOGGER.warning("XML Spawnlist: Spawn could not be initialized, both coordinates and zone are unspecified for ID " + templateId);
									continue;
								}
								
								final Node countNode = attrs.getNamedItem("count");
								final int count = countNode == null ? 1 : parseInteger(attrs, "count");
								
								for (int i = 0; i < count; i++)
								{
									final StatSet spawnInfo = new StatSet();
									spawnInfo.set("npcTemplateid", templateId);
									spawnInfo.set("x", x);
									spawnInfo.set("y", y);
									spawnInfo.set("z", z);
									spawnInfo.set("territoryName", territoryName);
									spawnInfo.set("spawnName", spawnName);
									
									// trying to read optional parameters
									if (attrs.getNamedItem("heading") != null)
									{
										spawnInfo.set("heading", parseInteger(attrs, "heading"));
									}
									
									if (attrs.getNamedItem("respawnDelay") != null)
									{
										spawnInfo.set("respawnDelay", parseInteger(attrs, "respawnDelay"));
									}
									
									if (attrs.getNamedItem("respawnRandom") != null)
									{
										spawnInfo.set("respawnRandom", parseInteger(attrs, "respawnRandom"));
									}
									
									if (attrs.getNamedItem("chaseRange") != null)
									{
										spawnInfo.set("chaseRange", parseInteger(attrs, "chaseRange"));
									}
									
									if (attrs.getNamedItem("periodOfDay") != null)
									{
										final String period = attrs.getNamedItem("periodOfDay").getNodeValue();
										if (period.equalsIgnoreCase("day") || period.equalsIgnoreCase("night"))
										{
											spawnInfo.set("periodOfDay", period.equalsIgnoreCase("day") ? 1 : 2);
										}
									}
									
									spawnInfo.set("fileName", f.getPath());
									_spawnCount += addSpawn(spawnInfo, map);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Creates NPC spawn
	 * @param spawnInfo StatSet of spawn parameters
	 * @param aiData Map of specific AI parameters for this spawn
	 * @return count NPC instances, spawned by this spawn
	 */
	private int addSpawn(StatSet spawnInfo, Map<String, Integer> aiData)
	{
		Spawn spawnDat;
		int ret = 0;
		try
		{
			spawnDat = new Spawn(spawnInfo.getInt("npcTemplateid"));
			spawnDat.setAmount(spawnInfo.getInt("count", 1));
			spawnDat.setXYZ(spawnInfo.getInt("x", 0), spawnInfo.getInt("y", 0), spawnInfo.getInt("z", 0));
			spawnDat.setHeading(spawnInfo.getInt("heading", -1));
			spawnDat.setRespawnDelay(spawnInfo.getInt("respawnDelay", 0), spawnInfo.getInt("respawnRandom", 0));
			spawnDat.setChaseRange(spawnInfo.getInt("chaseRange", 0));
			spawnDat.setLocationId(spawnInfo.getInt("locId", 0));
			final String territoryName = spawnInfo.getString("territoryName", "");
			final String spawnName = spawnInfo.getString("spawnName", "");
			if (!spawnName.isEmpty())
			{
				spawnDat.setName(spawnName);
			}
			if (!territoryName.isEmpty())
			{
				spawnDat.setSpawnTerritory(ZoneManager.getInstance().getSpawnTerritory(territoryName));
			}
			// Register AI Data for this spawn
			NpcPersonalAIData.getInstance().storeData(spawnDat, aiData);
			switch (spawnInfo.getInt("periodOfDay", 0))
			{
				case 0: // default
				{
					ret += spawnDat.init();
					break;
				}
				case 1: // Day
				{
					DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
					ret = 1;
					break;
				}
				case 2: // Night
				{
					DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
					ret = 1;
					break;
				}
			}
			
			final String fileName = spawnInfo.getString("fileName", "None");
			if (_spawnTemplates.values().contains(fileName))
			{
				for (Entry<Integer, String> entry : _spawnTemplates.entrySet())
				{
					if (entry.getValue().equals(fileName))
					{
						spawnDat.setSpawnTemplateId(entry.getKey());
						break;
					}
				}
			}
			else
			{
				final int newId = _spawnTemplates.size();
				_spawnTemplates.put(newId, fileName);
				spawnDat.setSpawnTemplateId(newId);
			}
			
			addSpawn(spawnDat);
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			LOGGER.log(Level.WARNING, "Spawn could not be initialized: " + e.getMessage(), e);
		}
		
		return ret;
	}
	
	/**
	 * Gets the spawn data.
	 * @return the spawn data
	 */
	public Map<Integer, Set<Spawn>> getSpawnTable()
	{
		return _spawnTable;
	}
	
	/**
	 * Gets the spawns for the NPC Id.
	 * @param npcId the NPC Id
	 * @return the spawn set for the given npcId
	 */
	public Set<Spawn> getSpawns(int npcId)
	{
		return _spawnTable.getOrDefault(npcId, Collections.emptySet());
	}
	
	/**
	 * Gets the spawn count for the given NPC ID.
	 * @param npcId the NPC Id
	 * @return the spawn count
	 */
	public int getSpawnCount(int npcId)
	{
		return getSpawns(npcId).size();
	}
	
	/**
	 * Gets a spawn for the given NPC ID.
	 * @param npcId the NPC Id
	 * @return a spawn for the given NPC ID or {@code null}
	 */
	public Spawn getAnySpawn(int npcId)
	{
		return getSpawns(npcId).stream().findFirst().orElse(null);
	}
	
	/**
	 * Adds a new spawn to the spawn table.
	 * @param spawn the spawn to add
	 * @param store if {@code true} it'll be saved in the spawn XML files
	 */
	public synchronized void addNewSpawn(Spawn spawn, boolean store)
	{
		addSpawn(spawn);
		
		if (store)
		{
			// Create output directory if it doesn't exist
			final File outputDirectory = new File(OTHER_XML_FOLDER);
			if (!outputDirectory.exists())
			{
				boolean result = false;
				try
				{
					outputDirectory.mkdir();
					result = true;
				}
				catch (SecurityException se)
				{
					// empty
				}
				if (result)
				{
					LOGGER.info(getClass().getSimpleName() + ": Created directory: " + OTHER_XML_FOLDER);
				}
			}
			
			// XML file for spawn
			final int x = ((spawn.getX() - World.WORLD_X_MIN) >> 15) + World.TILE_X_MIN;
			final int y = ((spawn.getY() - World.WORLD_Y_MIN) >> 15) + World.TILE_Y_MIN;
			final File spawnFile = new File(OTHER_XML_FOLDER + "/" + x + "_" + y + ".xml");
			
			// Write info to XML
			final String spawnId = String.valueOf(spawn.getId());
			final String spawnCount = String.valueOf(spawn.getAmount());
			final String spawnX = String.valueOf(spawn.getX());
			final String spawnY = String.valueOf(spawn.getY());
			final String spawnZ = String.valueOf(spawn.getZ());
			final String spawnHeading = String.valueOf(spawn.getHeading());
			final String spawnDelay = String.valueOf(spawn.getRespawnDelay() / 1000);
			if (spawnFile.exists()) // update
			{
				final File tempFile = new File(spawnFile.getAbsolutePath().substring(Config.DATAPACK_ROOT.getAbsolutePath().length() + 1).replace('\\', '/') + ".tmp");
				try
				{
					final BufferedReader reader = new BufferedReader(new FileReader(spawnFile));
					final BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
					String currentLine;
					while ((currentLine = reader.readLine()) != null)
					{
						if (currentLine.contains("</spawn>"))
						{
							final NpcTemplate template = NpcData.getInstance().getTemplate(spawn.getId());
							final String title = template.getTitle();
							final String name = title.isEmpty() ? template.getName() : template.getName() + " - " + title;
							writer.write("		<npc id=\"" + spawnId + (spawn.getAmount() > 1 ? "\" count=\"" + spawnCount : "") + "\" x=\"" + spawnX + "\" y=\"" + spawnY + "\" z=\"" + spawnZ + (spawn.getHeading() > 0 ? "\" heading=\"" + spawnHeading : "") + "\" respawnDelay=\"" + spawnDelay + "\" /> <!-- " + name + " -->" + System.lineSeparator());
							writer.write(currentLine + System.lineSeparator());
							continue;
						}
						writer.write(currentLine + System.lineSeparator());
					}
					writer.close();
					reader.close();
					spawnFile.delete();
					tempFile.renameTo(spawnFile);
				}
				catch (Exception e)
				{
					LOGGER.warning(getClass().getSimpleName() + ": Could not store spawn in the spawn XML files: " + e);
				}
			}
			else // new file
			{
				try
				{
					final NpcTemplate template = NpcData.getInstance().getTemplate(spawn.getId());
					final String title = template.getTitle();
					final String name = title.isEmpty() ? template.getName() : template.getName() + " - " + title;
					final BufferedWriter writer = new BufferedWriter(new FileWriter(spawnFile));
					writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator());
					writer.write("<list enabled=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"../../xsd/spawns.xsd\">" + System.lineSeparator());
					writer.write("	<spawn name=\"" + x + "_" + y + "\">" + System.lineSeparator());
					writer.write("		<npc id=\"" + spawnId + (spawn.getAmount() > 1 ? "\" count=\"" + spawnCount : "") + "\" x=\"" + spawnX + "\" y=\"" + spawnY + "\" z=\"" + spawnZ + (spawn.getHeading() > 0 ? "\" heading=\"" + spawnHeading : "") + "\" respawnDelay=\"" + spawnDelay + "\" /> <!-- " + name + " -->" + System.lineSeparator());
					writer.write("	</spawn>" + System.lineSeparator());
					writer.write("</list>" + System.lineSeparator());
					writer.close();
					LOGGER.info(getClass().getSimpleName() + ": Created file: " + OTHER_XML_FOLDER + "/" + x + "_" + y + ".xml");
				}
				catch (Exception e)
				{
					LOGGER.warning(getClass().getSimpleName() + ": Spawn " + spawn + " could not be added to the spawn XML files: " + e);
				}
			}
		}
	}
	
	/**
	 * Delete an spawn from the spawn table.
	 * @param spawn the spawn to delete
	 * @param update if {@code true} the spawn XML files will be updated
	 */
	public synchronized void deleteSpawn(Spawn spawn, boolean update)
	{
		if (!removeSpawn(spawn) && !update)
		{
			return;
		}
		
		if (update)
		{
			final int x = ((spawn.getX() - World.WORLD_X_MIN) >> 15) + World.TILE_X_MIN;
			final int y = ((spawn.getY() - World.WORLD_Y_MIN) >> 15) + World.TILE_Y_MIN;
			final int npcSpawnTemplateId = spawn.getNpcSpawnTemplateId();
			final Location spawnLocation = spawn.getSpawnLocation();
			final File spawnFile = npcSpawnTemplateId > 0 ? new File(_spawnTemplates.get(npcSpawnTemplateId)) : new File(OTHER_XML_FOLDER + "/" + x + "_" + y + ".xml");
			final File tempFile = new File(spawnFile.getAbsolutePath().substring(Config.DATAPACK_ROOT.getAbsolutePath().length() + 1).replace('\\', '/') + ".tmp");
			try
			{
				final BufferedReader reader = new BufferedReader(new FileReader(spawnFile));
				final BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
				
				boolean found = false; // in XML you can have more than one spawn with same coords
				boolean isMultiLine = false; // in case spawn has more stats
				boolean lastLineFound = false; // used to check for empty file
				int lineCount = 0;
				String currentLine;
				
				final NpcSpawnTerritory npcSpawnTerritory = spawn.getSpawnTerritory();
				if (npcSpawnTerritory == null)
				{
					final String spawnId = String.valueOf(spawn.getId());
					final String spawnX = String.valueOf(spawnLocation != null ? spawnLocation.getX() : spawn.getX());
					final String spawnY = String.valueOf(spawnLocation != null ? spawnLocation.getY() : spawn.getY());
					final String spawnZ = String.valueOf(spawnLocation != null ? spawnLocation.getX() : spawn.getZ());
					
					while ((currentLine = reader.readLine()) != null)
					{
						if (!found)
						{
							if (isMultiLine)
							{
								if (currentLine.contains("</npc>"))
								{
									found = true;
								}
								continue;
							}
							if (currentLine.contains(spawnId) && currentLine.contains(spawnX) && currentLine.contains(spawnY) && currentLine.contains(spawnZ))
							{
								if (!currentLine.contains("/>") && !currentLine.contains("</npc>"))
								{
									isMultiLine = true;
								}
								else
								{
									found = true;
								}
								continue;
							}
						}
						writer.write(currentLine + System.lineSeparator());
						if (currentLine.contains("</list>"))
						{
							lastLineFound = true;
						}
						if (!lastLineFound)
						{
							lineCount++;
						}
					}
				}
				else
				{
					SEARCH: while ((currentLine = reader.readLine()) != null)
					{
						if (!found)
						{
							if (isMultiLine)
							{
								if (currentLine.contains("</spawn>"))
								{
									found = true;
								}
								continue;
							}
							if (currentLine.contains('"' + npcSpawnTerritory.getName() + '"'))
							{
								isMultiLine = true;
								continue SEARCH;
							}
						}
						writer.write(currentLine + System.lineSeparator());
						if (currentLine.contains("</list>"))
						{
							lastLineFound = true;
						}
						if (!lastLineFound)
						{
							lineCount++;
						}
					}
				}
				
				writer.close();
				reader.close();
				spawnFile.delete();
				tempFile.renameTo(spawnFile);
				// Delete empty file
				if (lineCount < 5)
				{
					LOGGER.info(getClass().getSimpleName() + ": Deleted empty file: " + spawnFile.getAbsolutePath().substring(Config.DATAPACK_ROOT.getAbsolutePath().length() + 1).replace('\\', '/'));
					spawnFile.delete();
				}
			}
			catch (Exception e)
			{
				LOGGER.warning(getClass().getSimpleName() + ": Spawn " + spawn + " could not be removed from the spawn XML files: " + e);
			}
		}
	}
	
	/**
	 * Add a spawn to the spawn set if present, otherwise add a spawn set and add the spawn to the newly created spawn set.
	 * @param spawn the NPC spawn to add
	 */
	private void addSpawn(Spawn spawn)
	{
		_spawnTable.computeIfAbsent(spawn.getId(), k -> ConcurrentHashMap.newKeySet(1)).add(spawn);
	}
	
	/**
	 * Remove a spawn from the spawn set, if the spawn set is empty, remove it as well.
	 * @param spawn the NPC spawn to remove
	 * @return {@code true} if the spawn was successfully removed, {@code false} otherwise
	 */
	private boolean removeSpawn(Spawn spawn)
	{
		final Set<Spawn> set = _spawnTable.get(spawn.getId());
		if (set != null)
		{
			final boolean removed = set.remove(spawn);
			if (set.isEmpty())
			{
				_spawnTable.remove(spawn.getId());
			}
			return removed;
		}
		return false;
	}
	
	/**
	 * Execute a procedure over all spawns.<br>
	 * <font size="4" color="red">Do not use it!</font>
	 * @param function the function to execute
	 * @return {@code true} if all procedures were executed, {@code false} otherwise
	 */
	public boolean forEachSpawn(Function<Spawn, Boolean> function)
	{
		for (Set<Spawn> set : _spawnTable.values())
		{
			for (Spawn spawn : set)
			{
				if (!function.apply(spawn))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public String getSpawnFile(int templateId)
	{
		return _spawnTemplates.get(templateId);
	}
	
	public static SpawnTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SpawnTable INSTANCE = new SpawnTable();
	}
}
