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
				if (image.equals(wrapper.getContent())) { return wrapper; }
			}
		}
		return wrapNewImage(image);
	}

	@Override
	public MapWrapper wrapMultiImage(BufferedImage image, int rows, int columns) {
		//Don't add the wrapper to the MANAGED_MAPS, since we're already registering all the single wrapped maps
		return new MultiMapWrapper(image, rows, columns);
	}

	@Override
	public MapWrapper wrapMultiImage(ArrayImage image, int rows, int columns) {
		//Don't add the wrapper to the MANAGED_MAPS, since we're already registering all the single wrapped maps
		return new MultiMapWrapper(image, rows, columns);
	}

	@Override
	public MapWrapper wrapMultiImage(ArrayImage[][] images) {
		return new MultiMapWrapper(images);
	}

	public MapWrapper wrapNewImage(ArrayImage image) {
		MapWrapper wrapper = new DefaultMapWrapper(image);
		MANAGED_MAPS.add(wrapper);
		return wrapper;
	}

	@Override
	public void unwrapImage(MapWrapper wrapper) {
		if (wrapper instanceof DefaultMapWrapper) {
			for (short s : ((DefaultMapWrapper) wrapper).viewers.values()) {
				MapSender.cancelIDs(new short[] { s });
			}
		}
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

		int largest = Options.FORCED_OFFSET;
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

	public MapWrapper getDuplicate(ArrayImage image) {
			for (int i = 0; i < MANAGED_MAPS.size(); i++) {
				MapWrapper wrapper = MANAGED_MAPS.get(i);
				if (image.equals(wrapper.getContent())) { return wrapper; }
			}
			return null;
	}

}
