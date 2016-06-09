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
		 * Cache the packet data in the image object (less CPU intensive for a lot of players, but probably a bit more memory intensive depending on the image size)
		 */
		public static boolean CACHE_DATA = true;

		public static boolean TIMINGS = false;

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

			public static boolean TIMINGS = false;

		}

	}

}
