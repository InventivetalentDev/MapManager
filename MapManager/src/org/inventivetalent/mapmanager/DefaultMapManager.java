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
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.mapmanager.wrapper.MapWrapper;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

class DefaultMapManager implements MapManager {

	protected final Set<Short>       OCCUPIED_IDS = new HashSet<>();
	private final   List<MapWrapper> MANAGED_MAPS = new CopyOnWriteArrayList<>();

	@Override
	public MapWrapper wrapImage(BufferedImage image) {
		return wrapImage(new ArrayImage(image));
	}

	@Override
	public MapWrapper wrapImage(ArrayImage image) {
		if (Options.CHECK_DUPLICATES) {
			for (int i = 0; i < MANAGED_MAPS.size(); i++) {
				MapWrapper wrapper = MANAGED_MAPS.get(i);
				if (ArrayImage.ImageContentEqual(wrapper.getContent(), image)) { return wrapper; }
			}
		}
		return wrapNewImage(image);
	}

	@Override
	public MapWrapper wrapMultiImage(BufferedImage image, int columns, int rows) {
		//Don't add the wrapper to the MANAGED_MAPS, since we're already registering all the single wrapped maps
		return new MultiMapWrapper(image, columns, rows);
	}

	@Override
	public MapWrapper wrapMultiImage(ArrayImage image, int columns, int rows) {
		//Don't add the wrapper to the MANAGED_MAPS, since we're already registering all the single wrapped maps
		return new MultiMapWrapper(image, columns, rows);
	}

	public MapWrapper wrapNewImage(ArrayImage image) {
		MapWrapper wrapper = new DefaultMapWrapper(image);
		MANAGED_MAPS.add(wrapper);
		return wrapper;
	}

	@Override
	public void unwrapImage(MapWrapper wrapper) {
		wrapper.getController().clearViewers();
		MANAGED_MAPS.remove(wrapper);
		if (wrapper instanceof MultiMapWrapper) {
			((MultiMapWrapper) wrapper).unwrap();
		}
	}

	@Override
	public Set<MapWrapper> getMapsVisibleTo(OfflinePlayer player) {
		Set<MapWrapper> visible = new HashSet<>();
		for (MapWrapper wrapper : MANAGED_MAPS) {
			if (wrapper.getController().isViewing(player)) {
				visible.add(wrapper);
			}
		}
		return visible;
	}

	@Override
	public MapWrapper getWrapperForId(OfflinePlayer player, short id) {
		for (MapWrapper wrapper : getMapsVisibleTo(player)) {
			if (wrapper.getController().getMapId(player) == id) { return wrapper; }
		}
		return null;
	}

	@Override
	public void registerOccupiedID(short id) {
		if (!OCCUPIED_IDS.contains(id)) { OCCUPIED_IDS.add(id); }
	}

	@Override
	public void unregisterOccupiedID(short id) {
		OCCUPIED_IDS.remove(id);
	}

	@Override
	public Set<Short> getOccupiedIdsFor(OfflinePlayer player) {
		Set<Short> ids = new HashSet<>();
		for (MapWrapper wrapper : MANAGED_MAPS) {
			short s;
			if ((s = wrapper.getController().getMapId(player)) >= 0) {
				ids.add(s);
			}
		}
		return ids;
	}

	@Override
	public boolean isIdUsedBy(OfflinePlayer player, short id) {
		return getOccupiedIdsFor(player).contains(id);
	}

	@Override
	public short getNextFreeIdFor(Player player) throws MapLimitExceededException {
		Set<Short> occupied = getOccupiedIdsFor(player);
		//Add the 'default' occupied IDs
		occupied.addAll(OCCUPIED_IDS);

		int largest = 0;
		for (Short s : occupied) {
			if (s > largest) { largest = s; }
		}

		//Simply increase the maximum id if it's still small enough
		if (largest + 1 < Short.MAX_VALUE) { return (short) (largest + 1); }

		//Otherwise iterate through all options until there is an unused id
		for (short s = 0; s < Short.MAX_VALUE; s++) {
			if (!occupied.contains(s)) {
				return s;
			}
		}

		//If we end up here, this player has no more free ids. Let's hope nobody uses this many Maps.
		throw new MapLimitExceededException("'" + player + "' reached the maximum amount of available Map-IDs");
	}

	@Override
	public void clearAllMapsFor(OfflinePlayer player) {
		for (MapWrapper wrapper : getMapsVisibleTo(player)) {
			wrapper.getController().removeViewer(player);
		}
	}

	@Override
	public void updateContent(MapWrapper wrapper, ArrayImage content) {
	}
}
