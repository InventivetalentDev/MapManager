package org.inventivetalent.mapmanager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.mapmanager.metrics.Metrics;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.minecraft.MinecraftVersion;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import static org.inventivetalent.mapmanager.manager.MapManager.Options.*;

/**
 * MapManager-Plugin
 * <p>
 * use <code>Bukkit.getPluginManager().getPlugin("MapManager")</code> to access the plugin instance or <code>Bukkit.getPluginManager().getPlugin("MapManager").getMapManager()</code> to access the {@link MapManager} instance
 */
public class MapManagerPlugin extends JavaPlugin {

    protected static MapManagerPlugin instance;

    protected MapManager mapManagerInstance;

    private PacketListener packetListener;
    protected MapListener mapListener;

    protected static NMSClassResolver nmsClassResolver = new NMSClassResolver();
    protected static OBCClassResolver obcClassResolver = new OBCClassResolver();

    private static FieldResolver CraftWorldFieldResolver;
    private static FieldResolver WorldFieldResolver;
    private static FieldResolver WorldServerFieldResolver;
    private static MethodResolver IntHashMapMethodResolver;
    private static MethodResolver EntityMethodResolver;
    private static MethodResolver WorldServerMethodResolver;

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

            Set<Integer> occupied = new HashSet<>();
            for (int s = 0; s < Short.MAX_VALUE; s++) { // Integer.max is just too much
                try {
                    MapView view = Bukkit.getMap(s);
                    if (view != null) {
                        occupied.add(s);
                    }
                } catch (Exception e) {
                    if (!e.getMessage().toLowerCase().contains("invalid map dimension")) {
                        getLogger().log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            getLogger().info("Found " + occupied.size() + " occupied IDs.");

            for (int s : occupied) {
                getMapManager().registerOccupiedID(s);
            }
            getLogger().fine("These IDs will not be used: " + occupied);
        }

        new Metrics(this);

        SpigetUpdate updater = new SpigetUpdate(this, 19198);
        updater.setUserAgent("MapManager/" + getDescription().getVersion()).setVersionComparator(VersionComparator.SEM_VER_SNAPSHOT);
        updater.checkForUpdate(new UpdateCallback() {
            @Override
            public void updateAvailable(String s, String s1, boolean b) {
                getLogger().info("A new version is available: https://r.spiget.org/19198");
            }

            @Override
            public void upToDate() {
                getLogger().info("Plugin is up-to-date");
            }
        });
    }

    @Override
    public void onDisable() {
        this.packetListener.disable();
    }

    void reload() {
        FileConfiguration config = getConfig();

        ALLOW_VANILLA = config.getBoolean("allowVanilla", ALLOW_VANILLA);
        FORCED_OFFSET = config.getInt("forcedOffset", FORCED_OFFSET);
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
        if (mapManagerInstance == null) {throw new IllegalStateException("Manager not yet initialized");}
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
                WorldFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("world.level.World"));
            }
            if (WorldServerFieldResolver == null) {
                WorldServerFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("server.level.WorldServer"));
            }
            if (EntityMethodResolver == null) {
                EntityMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("world.entity.Entity"));
            }
            if (WorldServerMethodResolver == null) {
                WorldServerMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("server.level.WorldServer"));
            }

            Object nmsWorld = CraftWorldFieldResolver.resolveAccessor("world").get(world);

            Object entity;
            if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_18_R1)) {
                entity = world.getEntitiesByClass(ItemFrame.class).stream().filter(i -> i.getEntityId() == entityId).findFirst().orElse(null);
                if (entity != null) {
                    entity = Minecraft.getHandle(entity);
                }
            } else if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_17_R1)) {
                // no more entitiesById in 1.17
                entity = WorldServerMethodResolver.resolve(new ResolverQuery("getEntity", int.class)).invoke(nmsWorld, entityId);
            } else {
                Object entitiesById;
                // NOTE: this check can be false, if the v1_14_R1 doesn't exist (stupid java), i.e. in old ReflectionHelper versions
                if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_8_R1)
                        && MinecraftVersion.VERSION.olderThan(Minecraft.Version.v1_14_R1)) { /* seriously?! between 1.8 and 1.14 entitiesyId was moved to World */
                    entitiesById = WorldFieldResolver.resolveAccessor("entitiesById").get(nmsWorld);
                } else {
                    entitiesById = WorldServerFieldResolver.resolveAccessor("entitiesById").get(nmsWorld);
                }

                if (MinecraftVersion.VERSION.olderThan(Minecraft.Version.v1_14_R1)) {// < 1.14 uses IntHashMap
                    if (IntHashMapMethodResolver == null) {
                        IntHashMapMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("IntHashMap"));
                    }

                    entity = IntHashMapMethodResolver.resolve(new ResolverQuery("get", int.class)).invoke(entitiesById, entityId);
                } else {// > 1.14 uses Int2ObjectMap which implements Map
                    entity = ((Map) entitiesById).get(entityId);
                }
            }

            if (entity == null) {
                return null;
            }
            Entity bukkitEntity = (Entity) EntityMethodResolver.resolve("getBukkitEntity").invoke(entity);
            if (bukkitEntity instanceof ItemFrame) {
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
