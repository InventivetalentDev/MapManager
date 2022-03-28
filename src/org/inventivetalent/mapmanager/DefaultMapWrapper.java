package org.inventivetalent.mapmanager;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.inventivetalent.mapmanager.controller.MapController;
import org.inventivetalent.mapmanager.event.MapContentUpdateEvent;
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.mapmanager.wrapper.MapWrapper;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.minecraft.MinecraftVersion;
import org.inventivetalent.reflection.resolver.ConstructorResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

class DefaultMapWrapper implements MapWrapper {

    static int ID_COUNTER = 1;
    protected final int id = ID_COUNTER++;

    protected ArrayImage content;
    protected final Map<UUID, Integer> viewers = new HashMap<>();

    private static Class<?> Entity;
    private static Class<?> DataWatcher;
    private static Class<?> PacketPlayOutEntityMetadata;

    private static FieldResolver PacketEntityMetadataFieldResolver;
    private static FieldResolver EntityHumanFieldResolver;
    private static FieldResolver ContainerFieldResolver;
    private static ConstructorResolver WatchableObjectConstructorResolver;
    private static ConstructorResolver PacketPlayOutSlotConstructorResolver;
    private static MethodResolver CraftItemStackMethodResolver;
    private static MethodResolver ItemStackMethodResolver;
    private static MethodResolver NBTTagMethodResolver;

    //1.9
    private static FieldResolver DataWatcherRegistryFieldResolver;
    private static ConstructorResolver DataWatcherItemConstructorResolver;
    private static ConstructorResolver DataWatcherObjectConstructorResolver;
    private static FieldResolver EntityItemFrameFieldResolver;

    static Material MAP_MATERIAL;

    static {
        if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_13_R1)) {
            MAP_MATERIAL = Material.FILLED_MAP;
        } else {
            MAP_MATERIAL = Material.MAP;
        }
    }

    protected MapController controller = new MapController() {
        @Override
        public void addViewer(Player player) {
            if (!isViewing(player)) {
                viewers.put(player.getUniqueId(), MapManagerPlugin.instance.getMapManager().getNextFreeIdFor(player));
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
            if (player == null) {return false;}
            return viewers.containsKey(player.getUniqueId());
        }

        @Override
        public int getMapId(OfflinePlayer player) {
            if (isViewing(player)) {
                return viewers.get(player.getUniqueId());
            }
            return -1;
        }

        @Override
        public void update(ArrayImage content) {
            MapContentUpdateEvent event = new MapContentUpdateEvent(DefaultMapWrapper.this, content);
            Bukkit.getPluginManager().callEvent(event);

            if (event.getContent() != null) {
                if (MapManager.Options.CHECK_DUPLICATES) {
                    MapWrapper duplicate = ((DefaultMapManager) ((MapManagerPlugin) Bukkit.getPluginManager().getPlugin("MapManager")).getMapManager()).getDuplicate(event.getContent());
                    if (duplicate != null) {
                        DefaultMapWrapper.this.content = duplicate.getContent();
                        return;
                    }
                }
                DefaultMapWrapper.this.content = event.getContent();
            }

            if (event.isSendContent()) {
                for (UUID id : viewers.keySet()) {
                    sendContent(Bukkit.getPlayer(id));
                }
            }
        }

        @Override
        public ArrayImage getContent() {
            return content;
        }

        @Override
        public void sendContent(Player player) {
            sendContent(player, false);
        }

        @Override
        public void sendContent(Player player, boolean withoutQueue) {
            if (!isViewing(player)) {return;}
            int id = getMapId(player);
            if (withoutQueue && MapManager.Options.Sender.ALLOW_QUEUE_BYPASS) {
                MapSender.sendMap(id, DefaultMapWrapper.this.content, player);
            } else {
                MapSender.addToQueue(id, DefaultMapWrapper.this.content, player);
            }
        }

        @Override
        public void showInInventory(Player player, int slot, boolean force) {
            if (!isViewing(player)) {
                return;
            }

            if (player.getGameMode() == GameMode.CREATIVE) {
                //Clients in creative mode will send a 'PacketPlayInCreativeSlot' which tells the server there's a new item in the inventory and creates a new map
                if (!force) {
                    return;
                }
            }

            //Adjust the slot ID
            if (slot < 9) {slot += 36;} else if (slot > 35 && slot != 45) {slot = 8 - (slot - 36);}

            try {
                if (PacketPlayOutSlotConstructorResolver == null) {
                    PacketPlayOutSlotConstructorResolver = new ConstructorResolver(MapManagerPlugin.nmsClassResolver.resolve("PacketPlayOutSetSlot", "network.protocol.game.PacketPlayOutSetSlot"));
                }
                if (EntityHumanFieldResolver == null) {
                    EntityHumanFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("EntityHuman", "world.entity.player.EntityHuman"));
                }
                if (ContainerFieldResolver == null) {
                    ContainerFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("Container", "world.inventory.Container"));
                }
                if (CraftItemStackMethodResolver == null) {
                    CraftItemStackMethodResolver = new MethodResolver(MapManagerPlugin.obcClassResolver.resolve("inventory.CraftItemStack"));
                }

                Object entityPlayer = Minecraft.getHandle(player);
                Object defaultContainer = EntityHumanFieldResolver.resolveAccessor("defaultContainer").get(entityPlayer);
                Object windowId = ContainerFieldResolver.resolveAccessor("windowId").get(defaultContainer);

                //Create the ItemStack with the player's map ID
                ItemStack itemStack;
                if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_13_R1)) {
                    itemStack = new ItemStack(MAP_MATERIAL, 1);
                } else {
                    itemStack = new ItemStack(MAP_MATERIAL, 1, (short) getMapId(player));
                }
                Object craftItemStack = createCraftItemStack(itemStack, getMapId(player));

                Object setSlot = PacketPlayOutSlotConstructorResolver.resolve(new Class[]{
                        int.class,
                        int.class,
                        MapManagerPlugin.nmsClassResolver.resolve("world.item.ItemStack")
                }).newInstance(windowId, slot, craftItemStack);

                //Send the packet
                sendPacket(player, setSlot);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void showInInventory(Player player, int slot) {
            showInInventory(player, slot, false);
        }

        @Override
        public void showInHand(Player player, boolean force) {
            if (player.getItemInHand() == null || player.getItemInHand().getType() != MAP_MATERIAL) {
                if (!force) {//Player is not holding a map
                    return;
                }
            }
            showInInventory(player, player.getInventory().getHeldItemSlot(), force);
        }

        @Override
        public void showInHand(Player player) {
            showInHand(player, false);
        }

        @Override
        public void showInFrame(Player player, ItemFrame frame, boolean force) {
            if (frame.getItem() == null || frame.getItem().getType() != MAP_MATERIAL) {
                if (!force) {//There's no map in the item frame: don't do anything
                    return;
                }
            }
            showInFrame(player, frame.getEntityId());
        }

        @Override
        public void showInFrame(Player player, int entityId, String debugInfo) {
            if (!isViewing(player)) {
                return;
            }
            //Create the ItemStack with the player's map ID
            ItemStack itemStack = new ItemStack(MAP_MATERIAL, 1);
            if (debugInfo != null) {
                //Add the debug info to the display
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(debugInfo);
                itemStack.setItemMeta(itemMeta);
            }

            Bukkit.getScheduler().runTask(MapManagerPlugin.instance, () -> {
                ItemFrame itemFrame = MapManagerPlugin.getItemFrameById(player.getWorld(), entityId);
                if (itemFrame != null) {
                    //Add a reference to this MapWrapper (can be used in MapWrapper#getWrapperForId)
                    itemFrame.removeMetadata("MAP_WRAPPER_REF", MapManagerPlugin.instance);
                    itemFrame.setMetadata("MAP_WRAPPER_REF", new FixedMetadataValue(MapManagerPlugin.instance, DefaultMapWrapper.this));
                }

                sendItemFramePacket(player, entityId, itemStack, getMapId(player));
            });
        }

        @Override
        public void showInFrame(Player player, int entityId) {
            showInFrame(player, entityId, null);
        }

        @Override
        public void showInFrame(Player player, ItemFrame frame) {
            showInFrame(player, frame, false);
        }

        @Override
        public void clearFrame(Player player, int entityId) {
            sendItemFramePacket(player, entityId, null, -1);
            Bukkit.getScheduler().runTask(MapManagerPlugin.instance, () -> {
                ItemFrame itemFrame = MapManagerPlugin.getItemFrameById(player.getWorld(), entityId);
                if (itemFrame != null) {
                    //Remove the reference
                    itemFrame.removeMetadata("MAP_WRAPPER_REF", MapManagerPlugin.instance);
                }
            });
        }

        @Override
        public void clearFrame(Player player, ItemFrame frame) {
            clearFrame(player, frame.getEntityId());
        }

    };

    DefaultMapWrapper(ArrayImage content) {
        this.content = content;
    }

    public MapController getController() {
        return controller;
    }

    @Override
    public ArrayImage getContent() {
        return content;
    }

    protected void sendPacket(Player player, Object packet) {
        try {
            MapSender.sendPacket(packet, player);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Object createCraftItemStack(ItemStack itemStack, int mapId) throws ReflectiveOperationException {
        Object craftItemStack = CraftItemStackMethodResolver.resolve(new ResolverQuery("asNMSCopy", ItemStack.class)).invoke(null, itemStack);
        if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_16_R1)) {
            MapMeta meta = (MapMeta) itemStack.getItemMeta();
            meta.setMapId(mapId); //TODO
            itemStack.setItemMeta(meta);
            craftItemStack = CraftItemStackMethodResolver.resolve(new ResolverQuery("asNMSCopy", ItemStack.class)).invoke(null, itemStack);
        } else if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_13_R1)) {
            if (ItemStackMethodResolver == null) {
                ItemStackMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("ItemStack", "world.item.ItemStack"));
            }
            if (NBTTagMethodResolver == null) {
                NBTTagMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("NBTTagCompound", "nbt.NBTTagCompound"));
            }
            if (itemStack != null && craftItemStack != null && mapId >= 0) {
                Object nbtTag = ItemStackMethodResolver.resolveSignature("NBTTagCompound getOrCreateTag()", "NBTTagCompound u()", "NBTTagCompound getTag()").invoke(craftItemStack);
                Method setInt = NBTTagMethodResolver.resolveSignature("void setInt(String, int)", "void a(String, int)");
                setInt.invoke(nbtTag, "map", mapId);
            }
        }

        return craftItemStack;
    }

    public void sendItemFramePacket(Player player, int entityId, ItemStack itemStack, int mapId) {
        try {
            if (Entity == null) {
                Entity = MapManagerPlugin.nmsClassResolver.resolve("world.entity.Entity", "Entity");
            }
            if (DataWatcher == null) {
                DataWatcher = MapManagerPlugin.nmsClassResolver.resolve("network.syncher.DataWatcher", "network.syncer.DataWatcher", "DataWatcher");
            }
            if (PacketPlayOutEntityMetadata == null) {
                PacketPlayOutEntityMetadata = MapManagerPlugin.nmsClassResolver
                        .resolve("PacketPlayOutEntityMetadata", "network.protocol.game.PacketPlayOutEntityMetadata");
            }
            if (PacketEntityMetadataFieldResolver == null) {
                PacketEntityMetadataFieldResolver = new FieldResolver(PacketPlayOutEntityMetadata);
            }
            if (WatchableObjectConstructorResolver == null) {
                WatchableObjectConstructorResolver = new ConstructorResolver(MapManagerPlugin.nmsClassResolver.resolve("network.syncher.DataWatcher$Item", "network.syncer.DataWatcher$Item", "WatchableObject", "DataWatcher$WatchableObject", "DataWatcher$Item"/*1.9*/));
            }
            if (CraftItemStackMethodResolver == null) {
                CraftItemStackMethodResolver = new MethodResolver(MapManagerPlugin.obcClassResolver.resolve("inventory.CraftItemStack"));
            }

            //1.9
            if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_9_R1)) {
                if (DataWatcherRegistryFieldResolver == null) {
                    DataWatcherRegistryFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("network.syncher.DataWatcherRegistry", "network.syncer.DataWatcherRegistry", "DataWatcherRegistry"));
                }
                if (DataWatcherItemConstructorResolver == null) {
                    DataWatcherItemConstructorResolver = new ConstructorResolver(MapManagerPlugin.nmsClassResolver.resolve("network.syncher.DataWatcher$Item", "network.syncer.DataWatcher$Item", "DataWatcher$Item"));
                }
                if (DataWatcherObjectConstructorResolver == null) {
                    DataWatcherObjectConstructorResolver = new ConstructorResolver(MapManagerPlugin.nmsClassResolver.resolve("network.syncher.DataWatcherObject", "network.syncer.DataWatcherObject", "DataWatcherObject"));
                }
                if (EntityItemFrameFieldResolver == null) {
                    EntityItemFrameFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("EntityItemFrame", "world.entity.decoration.EntityItemFrame"));
                }
            }

            if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_13_R1)) {
                if (ItemStackMethodResolver == null) {
                    ItemStackMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("ItemStack", "world.item.ItemStack"));
                }
                if (NBTTagMethodResolver == null) {
                    NBTTagMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("NBTTagCompound", "nbt.NBTTagCompound"));
                }
            }


            Object meta;
            try {
                Constructor<?> noArgConstructor = PacketPlayOutEntityMetadata.getConstructor();
                meta = noArgConstructor.newInstance();
            } catch (ReflectiveOperationException e) {
                Object dummyDataWatcher = DataWatcher.getConstructor(Entity).newInstance((Object) null);
                meta = PacketPlayOutEntityMetadata.getConstructor(int.class, DataWatcher, boolean.class)
                        .newInstance(entityId, dummyDataWatcher, true);
            }

            //Set the Entity ID of the frame
            PacketEntityMetadataFieldResolver.resolveAccessor("a").set(meta, entityId);

            Object craftItemStack = createCraftItemStack(itemStack, mapId);

            List list = new ArrayList();

            //<= 1.8
            if (MinecraftVersion.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
                // 0 = Byte
                // 1 = Short
                // 2 = Int
                // 3 = Float
                // 4 = String
                // 5 = ItemStack
                // 6 = BlockPosition / ChunkCoordinates
                // 7 = Vector3f / Vector(?)
                list.add(WatchableObjectConstructorResolver.resolve(new Class[]{
                        int.class,
                        int.class,
                        Object.class
                }).newInstance(5, 8, craftItemStack));

                //<= 1.7
                if (MinecraftVersion.VERSION.olderThan(Minecraft.Version.v1_8_R1)) {
                    list.add(WatchableObjectConstructorResolver.resolve(new Class[]{
                            int.class,
                            int.class,
                            Object.class
                    }).newInstance(5, 2, craftItemStack));
                }
            } else {
                Object dataWatcherObject;
                if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_17_R1)) {
                    if (MinecraftVersion.VERSION.equal(Minecraft.Version.v1_18_R1)) {
                        dataWatcherObject = EntityItemFrameFieldResolver.resolveAccessor("ITEM", "ap").get(null);
                    } else {
                        dataWatcherObject = EntityItemFrameFieldResolver.resolveAccessor("ITEM", "ao").get(null);
                    }
                } else if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_13_R1)) {
                    dataWatcherObject = EntityItemFrameFieldResolver.resolveAccessor("ITEM", "e").get(null);
                } else {
                    dataWatcherObject = EntityItemFrameFieldResolver.resolveAccessor("c").get(null);
                }

                Constructor constructor = DataWatcherItemConstructorResolver.resolveFirstConstructor();
                Object dataWatcherItem;
                if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_11_R1)) {
                    // For some reason, it doesn't like Optionals anymore in 1.11...
                    dataWatcherItem = constructor.newInstance(dataWatcherObject, craftItemStack);
                } else {
                    dataWatcherItem = constructor.newInstance(dataWatcherObject, com.google.common.base.Optional.fromNullable(craftItemStack));
                }

                list.add(dataWatcherItem);
            }

            PacketEntityMetadataFieldResolver.resolveAccessor("b").set(meta, list);


            //Send the completed packet
            sendPacket(player, meta);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}

        DefaultMapWrapper that = (DefaultMapWrapper) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

}
