package org.inventivetalent.mapmanager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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

}
