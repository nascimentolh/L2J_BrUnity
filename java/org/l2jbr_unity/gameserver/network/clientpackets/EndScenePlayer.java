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

import org.l2jbr_unity.gameserver.enums.Movie;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.model.holders.MovieHolder;

/**
 * @author JIV
 */
public class EndScenePlayer extends ClientPacket
{
	private int _movieId;
	
	@Override
	protected void readImpl()
	{
		_movieId = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if ((player == null) || (_movieId == 0))
		{
			return;
		}
		final MovieHolder movieHolder = player.getMovieHolder();
		if (movieHolder == null)
		{
			return;
		}
		final Movie movie = movieHolder.getMovie();
		if (movie.getClientId() != _movieId)
		{
			return;
		}
		player.stopMovie();
		player.setTeleporting(true, false); // avoid to get player removed from World
		player.decayMe();
		player.spawnMe(player.getX(), player.getY(), player.getZ());
		player.setTeleporting(false, false);
	}
}
