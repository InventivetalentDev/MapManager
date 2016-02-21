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

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.mapmanager.wrapper.MapWrapper;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class MapManagerPlugin extends JavaPlugin {

	protected static MapManagerPlugin instance;

	protected MapManager mapManagerInstance;

	private   PacketListener packetListener;
	protected MapListener    mapListener;

	protected static NMSClassResolver nmsClassResolver = new NMSClassResolver();
	protected static OBCClassResolver obcClassResolver = new OBCClassResolver();

	public MapManagerPlugin() {
		instance = this;
	}

	@Override
	public void onEnable() {
		packetListener = new PacketListener(this);
		Bukkit.getPluginManager().registerEvents(mapListener = new MapListener(this), this);

		mapManagerInstance = new DefaultMapManager();

		if (MapManager.Options.ALLOW_VANILLA) {
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
				getMapManager().registerOccupiedID(s);
			}
			getLogger().fine("These IDs will not be used: " + occupied);
		}

		//Test code
		MapWrapper wrapper0;
		try {
			wrapper0 = getMapManager().wrapImage(new ArrayImage(ImageIO.read(new File("D://Desktop/map_test.png"))));
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
					//					int rnd = new Random().nextInt(2);
					//					System.out.println("Image #" + rnd);
					//					MapWrapper wrapper1 = getMapManager().wrapImage(new ArrayImage(ImageIO.read(new File("D://Desktop/map_test" + rnd + ".png"))));
					//
					//					System.out.println("Add " + event.getPlayer().getName() + " to viewers");
					//					wrapper1.getController().addViewer(event.getPlayer());
					//
					//					System.out.println("Send content to " + event.getPlayer().getName());
					//					wrapper1.getController().sendContent(event.getPlayer());
					//
					//					System.out.println("Map ID for " + event.getPlayer().getName() + " is #" + wrapper1.getController().getMapId(event.getPlayer()));
					//
					//					wrapper1.getController().showInHand(event.getPlayer(), true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}, this);
	}

	@Override
	public void onDisable() {
		this.packetListener.disable();
	}

	public MapManager getMapManager() {
		if (mapManagerInstance == null) { throw new IllegalStateException("Manager not yet initialized"); }
		return mapManagerInstance;
	}
}
