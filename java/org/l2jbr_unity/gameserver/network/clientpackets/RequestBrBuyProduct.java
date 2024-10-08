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

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.commons.database.DatabaseFactory;
import org.l2jbr_unity.gameserver.data.xml.ItemData;
import org.l2jbr_unity.gameserver.data.xml.PrimeShopData;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.holders.PrimeShopProductHolder;
import org.l2jbr_unity.gameserver.model.item.ItemTemplate;
import org.l2jbr_unity.gameserver.network.PacketLogger;
import org.l2jbr_unity.gameserver.network.serverpackets.ExBrBuyProduct;
import org.l2jbr_unity.gameserver.network.serverpackets.ExBrGamePoint;
import org.l2jbr_unity.gameserver.network.serverpackets.StatusUpdate;

/**
 * @author Mobius
 */
public class RequestBrBuyProduct extends ClientPacket
{
	private int _productId;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_productId = readInt();
		_count = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if ((_count > 99) || (_count < 0))
		{
			return;
		}
		
		final PrimeShopProductHolder product = PrimeShopData.getInstance().getProduct(_productId);
		if (product == null)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}
		
		final long totalPoints = product.getPrice() * _count;
		if (totalPoints < 0)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}
		
		final long gamePointSize = Config.PRIME_SHOP_ITEM_ID == -1 ? player.getGamePoints() : player.getInventory().getInventoryItemCount(Config.PRIME_SHOP_ITEM_ID, -1);
		if (totalPoints > gamePointSize)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_NOT_ENOUGH_POINTS));
			return;
		}
		
		final ItemTemplate item = ItemData.getInstance().getTemplate(product.getItemId());
		if (item == null)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}
		
		final int totalWeight = product.getItemWeight() * product.getItemCount() * _count;
		int totalCount = 0;
		totalCount += item.isStackable() ? 1 : product.getItemCount() * _count;
		if (!player.getInventory().validateCapacity(totalCount) || !player.getInventory().validateWeight(totalWeight))
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_INVENTORY_FULL));
			return;
		}
		
		// Pay for Item
		if (Config.PRIME_SHOP_ITEM_ID == -1)
		{
			player.setGamePoints(player.getGamePoints() - totalPoints);
		}
		else
		{
			player.getInventory().destroyItemByItemId("Buy Product" + _productId, Config.PRIME_SHOP_ITEM_ID, totalPoints, player, null);
		}
		
		// Buy Item
		player.addItem("PrimeShop: " + _productId, product.getItemId(), product.getItemCount() * _count, player, true);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		player.sendPacket(new ExBrGamePoint(player));
		player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_OK));
		player.broadcastUserInfo();
		
		// Save transaction info at SQL table prime_shop_transactions
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO prime_shop_transactions (charId, productId, quantity) values (?,?,?)"))
		{
			statement.setLong(1, player.getObjectId());
			statement.setInt(2, product.getProductId());
			statement.setLong(3, _count);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			PacketLogger.warning("Could not save Item Mall transaction: " + e.getMessage());
		}
	}
}
