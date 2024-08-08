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
package handlers.chathandlers;

import org.l2jbr_unity.Config;
import org.l2jbr_unity.gameserver.enums.ChatType;
import org.l2jbr_unity.gameserver.enums.PlayerCondOverride;
import org.l2jbr_unity.gameserver.handler.IChatHandler;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.network.SystemMessageId;
import org.l2jbr_unity.gameserver.network.serverpackets.CreatureSay;

/**
 * Party Room All chat handler.
 * @author durgus
 */
public class ChatPartyRoomAll implements IChatHandler
{
	private static final ChatType[] CHAT_TYPES =
	{
		ChatType.PARTYROOM_ALL,
	};
	
	@Override
	public void handleChat(ChatType type, Player activeChar, String target, String text)
	{
		if (!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel() || !activeChar.getParty().isLeader(activeChar))
		{
			return;
		}
		if (activeChar.isChatBanned() && Config.BAN_CHAT_CHANNELS.contains(type))
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_INCREASE_EVEN_FURTHER);
			return;
		}
		if (Config.JAIL_DISABLE_CHAT && activeChar.isJailed() && !activeChar.canOverrideCond(PlayerCondOverride.CHAT_CONDITIONS))
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
			return;
		}
		activeChar.getParty().getCommandChannel().broadcastCreatureSay(new CreatureSay(activeChar, type, activeChar.getName(), text), activeChar);
	}
	
	@Override
	public ChatType[] getChatTypeList()
	{
		return CHAT_TYPES;
	}
}