package org.inventivetalent.mapmanager;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MapManagerPlugin extends JavaPlugin {

	public static MapManagerPlugin instance;

	private   PacketListener packetListener;
	protected MapListener    mapListener;

	public MapManagerPlugin() {
		instance = this;
	}

	@Override
	public void onEnable() {
		packetListener = new PacketListener(this);
		Bukkit.getPluginManager().registerEvents(mapListener = new MapListener(this), this);

		//TODO: Remove, change to config
		MapManager.ALLOW_VANILLA = true;

		if (MapManager.ALLOW_VANILLA) {
			getLogger().info("Vanilla Maps are allowed. Trying to discover occupied Map IDs...");

			Set<Short> occupied = new HashSet<>();
			for (short s = 0; s < Short.MAX_VALUE; s++) {
				MapView view = Bukkit.getMap(s);
				if (view != null) {
					occupied.add(s);
				}
			}
			getLogger().info("Found " + occupied.size() + " occupied IDs.");

			for (short s : occupied) {
				MapManager.registerOccupiedID(s);
			}
			getLogger().fine("These IDs will not be used: " + occupied);
		}

		//Test code
		MapWrapper wrapper0;
		try {
			wrapper0 = MapManager.wrapImage(new ArrayImage(ImageIO.read(new File("D://Desktop/map_test.png"))));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		final MapWrapper wrapper = wrapper0;
		Bukkit.getPluginManager().registerEvents(new Listener() {

			@EventHandler
			public void onJoin(PlayerJoinEvent event) {

				System.out.println("Add " + event.getPlayer().getName() + " to viewers");
				wrapper.getController().addViewer(event.getPlayer());

				System.out.println("Send content to " + event.getPlayer().getName());
				wrapper.getController().sendContent(event.getPlayer());

				System.out.println("Map ID for " + event.getPlayer().getName() + " is #" + wrapper.getController().getMapId(event.getPlayer()));
			}

			@EventHandler
			public void onQuit(PlayerQuitEvent event) {
				System.out.println("Remove " + event.getPlayer().getName() + " from viewers");
				wrapper.getController().removeViewer(event.getPlayer());
			}

			@EventHandler
			public void onInteract(PlayerInteractEvent event) {
				try {
					int rnd = new Random().nextInt(2);
					System.out.println("Image #" + rnd);
					MapWrapper wrapper1 = MapManager.wrapImage(new ArrayImage(ImageIO.read(new File("D://Desktop/map_test" + rnd + ".png"))));

					System.out.println("Add " + event.getPlayer().getName() + " to viewers");
					wrapper1.getController().addViewer(event.getPlayer());

					System.out.println("Send content to " + event.getPlayer().getName());
					wrapper1.getController().sendContent(event.getPlayer());

					System.out.println("Map ID for " + event.getPlayer().getName() + " is #" + wrapper1.getController().getMapId(event.getPlayer()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@EventHandler
			public void on(MapInitializeEvent event) {
				System.out.println("Initialize Map #" + event.getMap().getId());
			}

		}, this);
	}

	@Override
	public void onDisable() {
		this.packetListener.disable();
	}
}
