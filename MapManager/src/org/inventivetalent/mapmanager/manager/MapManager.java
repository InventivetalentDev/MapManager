/*
 * Copyright 2015-2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.mapmanager.manager;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.inventivetalent.mapmanager.ArrayImage;
import org.inventivetalent.mapmanager.MapLimitExceededException;
import org.inventivetalent.mapmanager.wrapper.MapWrapper;

import java.awt.image.BufferedImage;
import java.util.Set;

public interface MapManager {

	/**
	 * Get the {@link MapWrapper} for a {@link BufferedImage}
	 *
	 * @param image the image to wrap
	 * @return the wrapper of the image
	 */
	MapWrapper wrapImage(BufferedImage image);

	/**
	 * Get the {@link MapWrapper} for an {@link ArrayImage}
	 *
	 * @param image the image to wrap
	 * @return the wrapper of the image
	 */
	MapWrapper wrapImage(ArrayImage image);

	/**
	 * Wrap an image and split it into multiple maps
	 *
	 * @param image   the image to wrap
	 * @param rows    rows of the split (i.e. height)
	 * @param columns columns of the split (i.e. width)
	 * @return the wrapper of the image
	 */
	MapWrapper wrapMultiImage(BufferedImage image, int rows, int columns);

	/**
	 * Wrap an image and split it into multiple maps
	 *
	 * @param image   the image to wrap
	 * @param rows    rows of the split (i.e. height)
	 * @param columns columns of the split (i.e. width)
	 * @return the wrapper of the image
	 */
	MapWrapper wrapMultiImage(ArrayImage image, int rows, int columns);

	/**
	 * Remove a wrapper
	 *
	 * @param wrapper the {@link MapWrapper} to remove
	 */
	void unwrapImage(MapWrapper wrapper);

	/**
	 * Get all {@link MapWrapper}s visible to a player
	 *
	 * @param player {@link OfflinePlayer} to check
	 * @return a set of visible maps
	 */
	Set<MapWrapper> getMapsVisibleTo(OfflinePlayer player);

	/**
	 * Get the MapWrapper for a {@link OfflinePlayer} and a map ID
	 *
	 * @param player {@link OfflinePlayer} to get the wrapper for
	 * @param id     ID of the map
	 * @return the {@link MapWrapper} or <code>null</code>
	 */
	MapWrapper getWrapperForId(OfflinePlayer player, short id);

	/**
	 * Registers an occupied ID (which will not be used as a map ID)
	 *
	 * @param id the ID to register
	 */
	void registerOccupiedID(short id);

	/**
	 * Unregisters an occupied ID
	 *
	 * @param id the ID to unregister
	 * @see #registerOccupiedID(short)
	 */
	void unregisterOccupiedID(short id);

	/**
	 * Get the IDs which are used for a player
	 *
	 * @param player the {@link OfflinePlayer} to get the IDs for
	 * @return Set of IDs
	 */
	Set<Short> getOccupiedIdsFor(OfflinePlayer player);

	/**
	 * Check if an map ID is used by a player
	 *
	 * @param player {@link OfflinePlayer} to check
	 * @param id     Map ID to check
	 * @return <code>true</code> if the ID is used
	 */
	boolean isIdUsedBy(OfflinePlayer player, short id);

	/**
	 * Get the next available (non-occupied) map ID for a player
	 *
	 * @param player {@link Player} to get the ID for
	 * @return the next available ID
	 * @throws MapLimitExceededException if there are no more IDs available (i.e. all IDs up to {@link Short#MAX_VALUE} are occupied by the player)
	 */
	short getNextFreeIdFor(Player player) throws MapLimitExceededException;

	/**
	 * Removes all {@link MapWrapper}s for a player
	 *
	 * @param player {@link OfflinePlayer} to clear the maps for
	 */
	void clearAllMapsFor(OfflinePlayer player);

	/**
	 * MapManger Options
	 */
	class Options {

		/**
		 * If vanilla maps should be allowed to be sent to the players (less efficient, since we need to check the id of every sent map)
		 */
		public static boolean ALLOW_VANILLA = false;

		/**
		 * If the plugin checks for duplicate images before creating a new one (Less efficient when first creating a image, but more efficient overall)
		 */
		public static boolean CHECK_DUPLICATES = true;

		/**
		 * Cached the packet data in the image object (less CPU intensive for a lot of players, but probably a bit more memory intensive depending on the image size)
		 */
		public static boolean CACHE_DATA = true;

		/**
		 * Options for Map-sending
		 */
		public static class Sender {

			/**
			 * Delay between map packets (ticks)
			 */
			public static int DELAY = 2;

			/**
			 * Maximum amount of map packets sent at once
			 */
			public static int AMOUNT = 10;

			/**
			 * Allow immediate sending of map data
			 */
			public static boolean ALLOW_QUEUE_BYPASS = true;

		}

	}

}
