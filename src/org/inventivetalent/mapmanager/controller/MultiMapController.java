package org.inventivetalent.mapmanager.controller;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;

/**
 * Controller for multiple/split maps
 *
 * @see org.inventivetalent.mapmanager.manager.MapManager#wrapMultiImage(BufferedImage, int, int)
 */
public interface MultiMapController extends MapController {

	/**
	 * Show this {@link MultiMapController} in {@link ItemFrame}s
	 *
	 * @param player         {@link Player} that will be able to see the maps
	 * @param entityIdMatrix 2D-Array of entity-IDs of the {@link ItemFrame}s (<code>int[width][height]</code>)
	 * @see MapController#showInFrame(Player, int)
	 */
	void showInFrames(Player player, int[][] entityIdMatrix);

	/**
	 * Show this {@link MultiMapController} in {@link ItemFrame}s
	 *
	 * @param player         {@link Player} that will be able to see the maps
	 * @param entityIdMatrix 2D-Array of entity-IDs of the {@link ItemFrame}s (<code>int[width][height]</code>)
	 * @param callable       {@link org.inventivetalent.mapmanager.controller.MultiMapController.DebugCallable} which will be called to display debug information, or <code>null</code>
	 * @see MapController#showInFrame(Player, int, String)
	 */
	void showInFrames(Player player, int[][] entityIdMatrix, DebugCallable callable);

	/**
	 * Show this {@link MultiMapController} in {@link ItemFrame}s
	 *
	 * @param player          {@link Player} that will be able to see the maps
	 * @param itemFrameMatrix 2D-Array of {@link ItemFrame}s (<code>ItemFrame[width][height]</code>)
	 * @param force           if <code>false</code>, the map will not be shown if there is not Map-Item in the ItemFrames
	 * @see MapController#showInFrame(Player, ItemFrame, boolean)
	 */
	void showInFrames(Player player, ItemFrame[][] itemFrameMatrix, boolean force);

	/**
	 * Show this {@link MultiMapController} in {@link ItemFrame}s
	 *
	 * @param player          {@link Player} that will be able to see the maps
	 * @param itemFrameMatrix 2D-Array of {@link ItemFrame}s (<code>ItemFrame[width][height]</code>)
	 * @see MapController#showInFrame(Player, ItemFrame)
	 */
	void showInFrames(Player player, ItemFrame[][] itemFrameMatrix);

	/**
	 * Clear the frames
	 *
	 * @param player         {@link Player} that will be able to see the cleared frames
	 * @param entityIdMatrix 2D-Array of entity-IDs of the {@link ItemFrame}s (<code>int[width][height]</code>)
	 */
	void clearFrames(Player player, int[][] entityIdMatrix);

	/**
	 * Clear the frames
	 *
	 * @param player          {@link Player} that will be able to see the cleared frames
	 * @param itemFrameMatrix 2D-Array of {@link ItemFrame}s (<code>ItemFrame[width][height]</code>)
	 */
	void clearFrames(Player player, ItemFrame[][] itemFrameMatrix);

	void update(BufferedImage content);

	interface DebugCallable {
		/**
		 * Called to get debug information for a frame
		 *
		 * @param controller the {@link MapController}
		 * @param x          X-Position of the current frame
		 * @param y          Y-Position of the current frame
		 * @return {@link String} to show when a player looks at the map, or <code>null</code>
		 * @see MapController#showInFrame(Player, int, String)
		 */
		String call(MapController controller, int x, int y);
	}

}
