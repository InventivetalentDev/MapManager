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

package org.inventivetalent.mapmanager;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.util.Set;

public interface MapManager {

	MapWrapper wrapImage(BufferedImage image);

	MapWrapper wrapImage(ArrayImage image);

	MapWrapper wrapMultiImage(BufferedImage image, int columns, int rows);

	MapWrapper wrapMultiImage(ArrayImage image, int columns, int rows);

	void unwrapImage(MapWrapper wrapper);

	Set<MapWrapper> getMapsVisibleTo(OfflinePlayer player);

	void registerOccupiedID(short id);

	void unregisterOccupiedID(short id);

	Set<Short> getOccupiedIdsFor(OfflinePlayer player);

	boolean isIdUsedBy(OfflinePlayer player, short id);

	short getNextFreeIdFor(Player player) throws MapLimitExceededException;

	void clearAllMapsFor(OfflinePlayer player);

	void updateContent(MapWrapper wrapper, ArrayImage content);

	class Options {

		//If vanilla maps should be allowed to be sent to the players (less efficient, since we need to check the id of every sent map)
		public static boolean ALLOW_VANILLA = false;

		//If the plugin checks for duplicate images before creating a new one (Less efficient when first creating a image, but more efficient overall)
		public static boolean CHECK_DUPLICATES = true;

		public static class Sender {

			//Delay between map packets (ticks)
			public static int DELAY = 2;

			//Maximum amount of map packets sent at once
			public static int AMOUNT = 10;

		}

	}

}
