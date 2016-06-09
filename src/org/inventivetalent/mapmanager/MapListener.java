package org.inventivetalent.mapmanager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.inventivetalent.mapmanager.manager.MapManager;

class MapListener implements Listener {

	private MapManagerPlugin plugin;

	public MapListener(MapManagerPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		plugin.getMapManager().clearAllMapsFor(event.getPlayer());
	}

	@EventHandler
	public void onMapInitialize(MapInitializeEvent event) {
		if (MapManager.Options.ALLOW_VANILLA) {
			plugin.getLogger().info("Adding new Map #" + event.getMap().getId() + " to occupied IDs.");
			plugin.getMapManager().registerOccupiedID(event.getMap().getId());
		}
	}

}
