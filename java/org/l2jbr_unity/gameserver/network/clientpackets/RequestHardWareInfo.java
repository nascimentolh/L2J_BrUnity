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

import org.l2jbr_unity.gameserver.model.holders.ClientHardwareInfoHolder;

/**
 * @author Mobius
 */
public class RequestHardWareInfo extends ClientPacket
{
	private String _macAddress;
	private int _windowsPlatformId;
	private int _windowsMajorVersion;
	private int _windowsMinorVersion;
	private int _windowsBuildNumber;
	private int _directxVersion;
	private int _directxRevision;
	private String _cpuName;
	private int _cpuSpeed;
	private int _cpuCoreCount;
	private int _vgaCount;
	private int _vgaPcxSpeed;
	private int _physMemorySlot1;
	private int _physMemorySlot2;
	private int _physMemorySlot3;
	private int _videoMemory;
	private int _vgaVersion;
	private String _vgaName;
	private String _vgaDriverVersion;
	
	@Override
	protected void readImpl()
	{
		_macAddress = readString();
		_windowsPlatformId = readInt();
		_windowsMajorVersion = readInt();
		_windowsMinorVersion = readInt();
		_windowsBuildNumber = readInt();
		_directxVersion = readInt();
		_directxRevision = readInt();
		readBytes(16);
		_cpuName = readString();
		_cpuSpeed = readInt();
		_cpuCoreCount = readByte();
		readInt();
		_vgaCount = readInt();
		_vgaPcxSpeed = readInt();
		_physMemorySlot1 = readInt();
		_physMemorySlot2 = readInt();
		_physMemorySlot3 = readInt();
		readByte();
		_videoMemory = readInt();
		readInt();
		_vgaVersion = readShort();
		_vgaName = readString();
		_vgaDriverVersion = readString();
	}
	
	@Override
	protected void runImpl()
	{
		getClient().setHardwareInfo(new ClientHardwareInfoHolder(_macAddress, _windowsPlatformId, _windowsMajorVersion, _windowsMinorVersion, _windowsBuildNumber, _directxVersion, _directxRevision, _cpuName, _cpuSpeed, _cpuCoreCount, _vgaCount, _vgaPcxSpeed, _physMemorySlot1, _physMemorySlot2, _physMemorySlot3, _videoMemory, _vgaVersion, _vgaName, _vgaDriverVersion));
	}
}