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
package handlers.admincommandhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.l2jbr_unity.commons.util.StringUtil;
import org.l2jbr_unity.gameserver.data.xml.ItemData;
import org.l2jbr_unity.gameserver.handler.IAdminCommandHandler;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.item.ItemTemplate;
import org.l2jbr_unity.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jbr_unity.gameserver.util.MathUtil;

/**
 * @author CostyKiller
 */
public class AdminSearch implements IAdminCommandHandler
{
	private static final int PAGE_LIMIT = 15;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_search"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_search"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			if (!st.hasMoreTokens())
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile(activeChar, "data/html/admin/search.htm");
				html.replace("%items%", "");
				html.replace("%pages%", "");
				activeChar.sendPacket(html);
			}
			else
			{
				final String item = st.nextToken();
				int page = 1;
				if (st.hasMoreTokens())
				{
					try
					{
						page = Integer.parseInt(st.nextToken());
					}
					catch (NumberFormatException e)
					{
						page = 1;
					}
				}
				results(activeChar, item, page);
			}
		}
		return true;
	}
	
	private void results(Player player, String text, int page)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(player, "data/html/admin/search.htm");
		
		List<ItemTemplate> items = new ArrayList<>();
		for (ItemTemplate itemName : ItemData.getInstance().getAllItems())
		{
			if ((itemName != null) && itemName.getName().toLowerCase().contains(text.toLowerCase()))
			{
				items.add(itemName);
			}
		}
		
		if (items.isEmpty())
		{
			html.replace("%items%", "<tr><td>No items found with word " + text + ".</td></tr>");
			html.replace("%pages%", "");
			player.sendPacket(html);
			return;
		}
		
		final int max = Math.min(100, MathUtil.countPagesNumber(items.size(), PAGE_LIMIT));
		items = items.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, items.size()));
		
		final StringBuilder sb = new StringBuilder();
		for (ItemTemplate item : items)
		{
			StringUtil.append(sb, "<tr><td align=center><button value=\\\"Add\\\" action=\\\"bypass admin_create_item " //
				, String.valueOf(item.getId()) //
				, "\" width=32 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td><img src=\\\"" //
				, item.getIcon() //
				, "\" width=32 height=32></td><td>"//
				, String.valueOf(item.getDisplayId()) //
				, "</td><td>" //
				, (item.isStackable() ? "<font name=\"hs8\" color=008000 size=-1>ST</font>" : "<font name=\"hs8\" color=ff0000 size=-1>ST</font>") //
				, "</td><td align=center>"//
				, getItemGrade(item) //
				, "</td><td>" //
				, getFontedWord(text, item.getName()) //
				, "</td></tr>");
		}
		html.replace("%items%", sb.toString());
		
		sb.setLength(0);
		for (int i = 0; i < max; i++)
		{
			final int pagenr = i + 1;
			if (page == pagenr)
			{
				StringUtil.append(sb, +pagenr + "&nbsp;");
			}
			else
			{
				StringUtil.append(sb, "<a action=\"bypass -h admin_search " + text + " " + pagenr + "\">" + pagenr + "</a>&nbsp;");
			}
		}
		
		html.replace("%pages%", sb.toString());
		player.sendPacket(html);
	}
	
	private String getItemGrade(ItemTemplate item)
	{
		switch (item.getCrystalType())
		{
			case NONE:
			{
				return "<font name=\"hs8\" color=ae9977 size=-1>NG</font>";
			}
			case D:
			{
				return "<font name=\\\"hs8\\\" color=ae9977 size=-1>D</font>";
			}
			case C:
			{
				return "<font name=\\\"hs8\\\" color=ae9977 size=-1>C</font>";
			}
			case B:
			{
				return "<font name=\\\"hs8\\\" color=ae9977 size=-1>B</font>";
			}
			case A:
			{
				return "<font name=\\\"hs8\\\" color=ae9977 size=-1>A</font>";
			}
			case S:
			{
				return "<font name=\\\"hs8\\\" color=ae9977 size=-1>S</font>";
			}
			case S80:
			{
				return "<font name=\\\"hs8\\\" color=ae9977 size=-1>S80</font>";
			}
			case S84:
			{
				return "<font name=\\\"hs8\\\" color=ae9977 size=-1>S84</font>";
			}
			default:
			{
				return String.valueOf(item.getItemGrade());
			}
		}
	}
	
	private String getFontedWord(String text, String name)
	{
		final int position = name.toLowerCase().indexOf(text.toLowerCase());
		final StringBuilder str = new StringBuilder(name);
		final String font = "<FONT COLOR=\"LEVEL\">";
		str.insert(position, font);
		str.insert(position + (font.length() + text.length()), "</FONT>");
		return str.toString();
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}