package org.inventivetalent.mapmanager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class MapWrapper {

	protected ArrayImage content;
	protected final Map<UUID, Short> viewers = new HashMap<>();

	protected MapController controller = new MapController() {
		@Override
		public void addViewer(Player player) {
			if (!isViewing(player)) {
				viewers.put(player.getUniqueId(), MapManager.getNextFreeIdFor(player));
			}
		}

		@Override
		public void removeViewer(OfflinePlayer player) {
			viewers.remove(player.getUniqueId());
		}

		@Override
		public void clearViewers() {
			Set<UUID> uuids = new HashSet<>(viewers.keySet());
			for (UUID uuid : uuids) {
				viewers.remove(uuid);
			}
		}

		@Override
		public boolean isViewing(OfflinePlayer player) {
			return viewers.containsKey(player.getUniqueId());
		}

		@Override
		public short getMapId(OfflinePlayer player) {
			if (isViewing(player)) {
				return viewers.get(player.getUniqueId());
			}
			return -1;
		}

		@Override
		public void update(ArrayImage content) {
			MapWrapper.this.content = content;
			for (UUID id : viewers.keySet()) {
				sendContent(Bukkit.getPlayer(id));
			}
		}

		@Override
		public void sendContent(Player player) {
			if (!isViewing(player)) { return; }
			MapSender.addToQueue(getMapId(player), MapWrapper.this.content, player);
		}
	};

	public MapWrapper(ArrayImage content) {
		this.content = content;
	}

	public MapController getController() {
		return controller;
	}
}

