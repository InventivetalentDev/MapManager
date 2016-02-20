package org.inventivetalent.mapmanager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.MapInitializeEvent;

public class MapListener implements Listener {

	private MapManagerPlugin plugin;

	public MapListener(MapManagerPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {

	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		MapManager.clearAllMapsFor(event.getPlayer());
	}

	@EventHandler
	public void onMapInitialize(MapInitializeEvent event) {
		System.out.println("Initialize Map #" + event.getMap().getId());
		if (MapManager.ALLOW_VANILLA) {
			plugin.getLogger().info("Adding new Map #" + event.getMap().getId() + " to occupied IDs.");
			MapManager.registerOccupiedID(event.getMap().getId());
		}
	}

}
