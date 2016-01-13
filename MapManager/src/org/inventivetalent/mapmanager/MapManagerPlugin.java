package org.inventivetalent.mapmanager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MapManagerPlugin extends JavaPlugin  {

	public static MapManagerPlugin instance;

	private PacketListener packetListener;
	protected MapListener mapListener;

	public MapManagerPlugin() {
		instance = this;
		Bukkit.getPluginManager().registerEvents(mapListener = new MapListener(this), this);
	}

	@Override
	public void onEnable() {
		packetListener = new PacketListener(this);
	}

	@Override
	public void onDisable() {
		this.packetListener.disable();
	}
}
