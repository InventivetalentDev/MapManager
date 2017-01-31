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
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import java.util.HashSet;
import java.util.Set;

import static org.inventivetalent.mapmanager.manager.MapManager.Options.*;

/**
 * MapManager-Plugin
 * <p>
 * use <code>Bukkit.getPluginManager().getPlugin("MapManager")</code> to access the plugin instance
 * or <code>Bukkit.getPluginManager().getPlugin("MapManager").getMapManager()</code> to access the {@link MapManager} instance
 */
public class MapManagerPlugin extends JavaPlugin {

	protected static MapManagerPlugin instance;

	protected MapManager mapManagerInstance;

	private   PacketListener packetListener;
	protected MapListener    mapListener;

	protected static NMSClassResolver nmsClassResolver = new NMSClassResolver();
	protected static OBCClassResolver obcClassResolver = new OBCClassResolver();

	private static FieldResolver  CraftWorldFieldResolver;
	private static FieldResolver  WorldFieldResolver;
	private static FieldResolver  WorldServerFieldResolver;
	private static MethodResolver IntHashMapMethodResolver;
	private static MethodResolver EntityMethodResolver;

	public MapManagerPlugin() {
		instance = this;
	}

	@Override
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("PacketListenerApi")) {
			getLogger().severe("****************************************");
			getLogger().severe("This plugin depends on PacketListenerApi");
			getLogger().severe("Download it here: https://r.spiget.org/2930");
			getLogger().severe("****************************************");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

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
			if (WorldServerFieldResolver == null) {
				WorldServerFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("WorldServer"));
			}
			if (IntHashMapMethodResolver == null) {
				IntHashMapMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("IntHashMap"));
			}
			if (EntityMethodResolver == null) {
				EntityMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("Entity"));
			}

			Object nmsWorld = CraftWorldFieldResolver.resolve("world").get(world);
			Object entitiesById = Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1) ? WorldFieldResolver.resolve("entitiesById").get(nmsWorld) : WorldServerFieldResolver.resolve("entitiesById").get(nmsWorld);
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
