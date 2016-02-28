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
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import java.util.HashSet;
import java.util.Set;

import static org.inventivetalent.mapmanager.manager.MapManager.Options.*;

public class MapManagerPlugin extends JavaPlugin {

	protected static MapManagerPlugin instance;

	protected MapManager mapManagerInstance;

	private   PacketListener packetListener;
	protected MapListener    mapListener;

	protected static NMSClassResolver nmsClassResolver = new NMSClassResolver();
	protected static OBCClassResolver obcClassResolver = new OBCClassResolver();

	private static FieldResolver  CraftWorldFieldResolver;
	private static FieldResolver  WorldFieldResolver;
	private static MethodResolver IntHashMapMethodResolver;
	private static MethodResolver EntityMethodResolver;

	public MapManagerPlugin() {
		instance = this;
	}

	@Override
	public void onEnable() {
		packetListener = new PacketListener(this);
		Bukkit.getPluginManager().registerEvents(mapListener = new MapListener(this), this);

		mapManagerInstance = new DefaultMapManager();

		saveDefaultConfig();
		reload();

		PluginCommand command = getCommand("mapmanager");
		CommandHandler commandHandler = new CommandHandler();
		command.setExecutor(commandHandler);
		command.setTabCompleter(commandHandler);

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
	}

	@Override
	public void onDisable() {
		this.packetListener.disable();
	}

	void reload() {
		FileConfiguration config = getConfig();

		ALLOW_VANILLA = config.getBoolean("allowVanilla", ALLOW_VANILLA);
		CHECK_DUPLICATES = config.getBoolean("checkDuplicates", CHECK_DUPLICATES);
		CACHE_DATA = getConfig().getBoolean("cacheData", CACHE_DATA);
		Sender.DELAY = getConfig().getInt("sender.delay", Sender.DELAY);
		Sender.AMOUNT = getConfig().getInt("sender.amount", Sender.AMOUNT);
		Sender.ALLOW_QUEUE_BYPASS = getConfig().getBoolean("sender.allowQueueBypass", Sender.ALLOW_QUEUE_BYPASS);
	}

	/**
	 * @return The {@link MapManager} instance
	 */
	public MapManager getMapManager() {
		if (mapManagerInstance == null) { throw new IllegalStateException("Manager not yet initialized"); }
		return mapManagerInstance;
	}

	/**
	 * Helper method to find an {@link ItemFrame} by its entity ID
	 *
	 * @param world    {@link World} the frame is located in
	 * @param entityId the frame's entity ID
	 * @return the {@link ItemFrame} or <code>null</code>
	 */
	public static ItemFrame getItemFrameById(World world, int entityId) {
		try {
			if (CraftWorldFieldResolver == null) {
				CraftWorldFieldResolver = new FieldResolver(MapManagerPlugin.obcClassResolver.resolve("CraftWorld"));
			}
			if (WorldFieldResolver == null) {
				WorldFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("World"));
			}
			if (IntHashMapMethodResolver == null) {
				IntHashMapMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("IntHashMap"));
			}
			if (EntityMethodResolver == null) {
				EntityMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("Entity"));
			}

			Object entitiesById = WorldFieldResolver.resolve("entitiesById").get(CraftWorldFieldResolver.resolve("world").get(world));
			Object entity = IntHashMapMethodResolver.resolve(new ResolverQuery("get", int.class)).invoke(entitiesById, entityId);
			if (entity == null) { return null; }
			Entity bukkitEntity = (Entity) EntityMethodResolver.resolve("getBukkitEntity").invoke(entity);
			if (bukkitEntity != null && EntityType.ITEM_FRAME == bukkitEntity.getType()) {
				return (ItemFrame) bukkitEntity;
			}

			//				for (ItemFrame itemFrame : world.getEntitiesByClass(ItemFrame.class)) {
			//					if (itemFrame.getEntityId() == entityId) {
			//						return itemFrame;
			//					}
			//				}
			//				return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
