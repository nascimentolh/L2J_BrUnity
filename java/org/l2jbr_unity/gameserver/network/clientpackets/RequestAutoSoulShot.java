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
package org.l2jbr_unity.gameserver.network.clientpackets;

import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.item.instance.Item;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.ExAutoSoulShot;
import org.l2jbr_unity.gameserver.network.serverpackets.SystemMessage;

/**
 * @version $Revision: 1.0.0.0 $ $Date: 2005/07/11 15:29:30 $
 */
public class RequestAutoSoulShot extends ClientPacket
{
	// format cd
	private int _itemId;
	private int _type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		_itemId = readInt();
		_type = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!player.isInStoreMode() && (player.getActiveRequester() == null) && !player.isDead())
		{
			final Item item = player.getInventory().getItemByItemId(_itemId);
			if (item == null)
			{
				return;
			}
			
			if (_type == 1)
			{
				if (!player.getInventory().canManipulateWithItemId(item.getId()))
				{
					player.sendMessage("Cannot use this item.");
					return;
				}
				
				// Fishingshots are not automatic on retail
				if ((_itemId < 6535) || (_itemId > 6540))
				{
					// Attempt to charge first shot on activation
					if ((_itemId == 6645) || (_itemId == 6646) || (_itemId == 6647) || (_itemId == 20332) || (_itemId == 20333) || (_itemId == 20334))
					{
						if (player.hasSummon())
						{
							if (item.getEtcItem().getHandlerName().equals("BeastSoulShot"))
							{
								if (player.getSummon().getSoulShotsPerHit() > item.getCount())
								{
									player.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR);
									return;
								}
							}
							else
							{
								if (player.getSummon().getSpiritShotsPerHit() > item.getCount())
								{
									player.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR);
									return;
								}
							}
							player.addAutoSoulShot(_itemId);
							player.sendPacket(new ExAutoSoulShot(_itemId, _type));
							
							// start the auto soulshot use
							final SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
							sm.addItemName(item);
							player.sendPacket(sm);
							
							player.rechargeShots(true, true);
							player.getSummon().rechargeShots(true, true);
						}
						else
						{
							player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_SERVITOR_OR_PET_AND_THEREFORE_CANNOT_USE_THE_AUTOMATIC_USE_FUNCTION);
						}
					}
					else
					{
						if ((player.getActiveWeaponItem() != player.getFistsWeaponItem()) && (item.getTemplate().getCrystalType() == player.getActiveWeaponItem().getCrystalTypePlus()))
						{
							player.addAutoSoulShot(_itemId);
							player.sendPacket(new ExAutoSoulShot(_itemId, _type));
						}
						else
						{
							if (((_itemId >= 2509) && (_itemId <= 2514)) || ((_itemId >= 3947) && (_itemId <= 3952)) || (_itemId == 5790) || ((_itemId >= 22072) && (_itemId <= 22081)))
							{
								player.sendPacket(SystemMessageId.YOUR_SPIRITSHOT_DOES_NOT_MATCH_THE_WEAPON_S_GRADE);
							}
							else
							{
								player.sendPacket(SystemMessageId.THE_SOULSHOT_YOU_ARE_ATTEMPTING_TO_USE_DOES_NOT_MATCH_THE_GRADE_OF_YOUR_EQUIPPED_WEAPON);
							}
							
							player.addAutoSoulShot(_itemId);
							player.sendPacket(new ExAutoSoulShot(_itemId, _type));
						}
						
						// start the auto soulshot use
						final SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
						sm.addItemName(item);
						player.sendPacket(sm);
						
						player.rechargeShots(true, true);
					}
				}
			}
			else if (_type == 0)
			{
				player.removeAutoSoulShot(_itemId);
				player.sendPacket(new ExAutoSoulShot(_itemId, _type));
				
				// cancel the auto soulshot use
				final SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED);
				sm.addItemName(item);
				player.sendPacket(sm);
			}
		}
	}
}