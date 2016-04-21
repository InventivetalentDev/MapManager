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

package org.inventivetalent.mapmanager.controller;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.inventivetalent.mapmanager.ArrayImage;

import java.awt.image.BufferedImage;

/**
 * Controller for multiple/split maps
 *
 * @see org.inventivetalent.mapmanager.manager.MapManager#wrapMultiImage(BufferedImage, int, int)
 * @see org.inventivetalent.mapmanager.manager.MapManager#wrapMultiImage(ArrayImage, int, int)
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
