package org.inventivetalent.mapmanager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.minecraft.MinecraftVersion;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class MapSender {

    private static final List<QueuedMap> sendQueue = new ArrayList<>();
    private static int senderID = -1;

    private static Class EntityPlayer;
    private static Class PlayerConnection;

    private static FieldResolver EntityPlayerFieldResolver;
    private static MethodResolver PlayerConnectionMethodResolver;

    public static void cancelIDs(int[] ids) {
        Iterator<QueuedMap> iterator = sendQueue.iterator();
        while (iterator.hasNext()) {
            QueuedMap next = iterator.next();
            id:
            for (int i : ids) {
                if (next.id == -i) {
                    iterator.remove();
                    break id;
                }
            }
        }
    }

    public static void addToQueue(final int id, final ArrayImage image, final Player receiver) {
        QueuedMap toSend = new QueuedMap(id, image, receiver);
        if (sendQueue.contains(toSend)) {return;}
        sendQueue.add(toSend);

        runSender();
    }

    protected static void runSender() {
        if (Bukkit.getScheduler().isQueued(senderID) || Bukkit.getScheduler().isCurrentlyRunning(senderID) || sendQueue.size() == 0) {
            return;
        }

        senderID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MapManagerPlugin.instance, new Runnable() {

            @Override
            public void run() {
                if (sendQueue.isEmpty()) {return;}
                for (int i = 0; i < Math.min(sendQueue.size(), MapManager.Options.Sender.AMOUNT + 1); i++) {
                    QueuedMap current = sendQueue.get(0);
                    if (current == null) {return;}
                    sendMap(current.id, current.image, current.player);
                    if (!sendQueue.isEmpty()) {
                        sendQueue.remove(0);
                    }
                }
            }
        }, 0, MapManager.Options.Sender.DELAY);
    }

    protected static void sendMap(final int id0, final ArrayImage image, final Player receiver) {
        if (MapManager.Options.Sender.TIMINGS) {TimingsHelper.startTiming("MapManager:sender:sendMap");}
        if (receiver == null || !receiver.isOnline()) {

            List<QueuedMap> toRemove = new ArrayList<>();
            for (QueuedMap qMap : sendQueue) {
                if (qMap == null) {continue;}
                if (qMap.player == null || !qMap.player.isOnline()) {
                    toRemove.add(qMap);
                }
            }
            Bukkit.getScheduler().cancelTask(senderID);
            sendQueue.removeAll(toRemove);

            if (MapManager.Options.Sender.TIMINGS) {TimingsHelper.stopTiming("MapManager:sender:sendMap");}
            return;
        }

        final int id = -id0;

        Bukkit.getScheduler().runTaskAsynchronously(MapManagerPlugin.instance, new Runnable() {
            @Override
            public void run() {
                try {
                    Object packet = constructPacket(id, image);
                    sendPacket(packet, receiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        TimingsHelper.stopTiming("MapManager:sender:sendMap");
    }

    private static Class<?> nmsPacketPlayOutMap;
    private static Class<?> nmsWorldMap$UpdateData;

    static {
        nmsPacketPlayOutMap = MapManagerPlugin.nmsClassResolver.resolveSilent("PacketPlayOutMap", "network.protocol.game.PacketPlayOutMap");
        if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_17_R1)) {
            nmsWorldMap$UpdateData = MapManagerPlugin.nmsClassResolver.resolveSilent("world.level.saveddata.maps.WorldMap$b");
        }
    }

    private static Object constructPacket(int id, ArrayImage data) {
        Object packet = null;

        if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_17_R1)) {
            try {
                packet = constructPacket_1_17(id, data);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        } else if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_14_R1)) {
            try {
                packet = constructPacket_1_14(id, data);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        } else if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_9_R1)) {
            try {
                packet = constructPacket_1_9(id, data);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        } else if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_8_R1)) {
            try {
                packet = constructPacket_1_8(id, data);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        return packet;
    }

    private static Object constructPacket_1_8(int id, ArrayImage data) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
        Object packet = nmsPacketPlayOutMap//
                .getConstructor(int.class, byte.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class)//
                .newInstance(id,// ID
                        (byte) 0,// Scale
                        new ArrayList<>(),// Icons
                        data.array,// Data
                        data.minX,// X-position
                        data.minY,// Y-position
                        data.maxX,// X-Size (or 2nd X-position)
                        data.maxY// Y-Size (or 2nd Y-position)
                );
        return packet;
    }

    private static Object constructPacket_1_9(int id, ArrayImage data) throws ReflectiveOperationException {
        Object packet = nmsPacketPlayOutMap//
                .getConstructor(int.class, byte.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class)//
                .newInstance(id,//ID
                        (byte) 0,//Scale
                        false,//????
                        new ArrayList<>(),//Icons
                        data.array,//Data
                        data.minX,// X-position
                        data.minY,// Y-position
                        data.maxX,// X-Size (or 2nd X-position)
                        data.maxY// Y-Size (or 2nd Y-position)
                );
        return packet;
    }

    private static Object constructPacket_1_14(int id, ArrayImage data) throws ReflectiveOperationException {
        Object packet = nmsPacketPlayOutMap//
                .getConstructor(int.class, byte.class, boolean.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class)//
                .newInstance(id,//ID
                        (byte) 0,//Scale
                        false,// tracking position
                        false,// locked
                        new ArrayList<>(),//Icons
                        data.array,//Data
                        data.minX,// X-position
                        data.minY,// Y-position
                        data.maxX,// X-Size (or 2nd X-position)
                        data.maxY// Y-Size (or 2nd Y-position)
                );
        return packet;
    }

    private static Object constructPacket_1_17(int id, ArrayImage data) throws ReflectiveOperationException {
        Object updateData = nmsWorldMap$UpdateData
                .getConstructor(int.class, int.class, int.class, int.class, byte[].class)
                .newInstance(
                        data.minX,// X-position
                        data.minY,// Y-position
                        data.maxX,// X-Size (or 2nd X-position)
                        data.maxY,// Y-Size (or 2nd Y-position)
                        data.array//Data
                );
        Object packet = nmsPacketPlayOutMap//
                .getConstructor(int.class, byte.class, boolean.class, Collection.class, nmsWorldMap$UpdateData)//
                .newInstance(
                        id,//ID
                        (byte) 0,//Scale
                        false,// Show Icons
                        new ArrayList<>(),//Icons
                        updateData
                );
        return packet;
    }

    protected static void sendPacket(Object packet, Player p) throws IllegalArgumentException, ClassNotFoundException {
        if (EntityPlayer == null) {
            EntityPlayer = MapManagerPlugin.nmsClassResolver.resolve("EntityPlayer", "server.level.EntityPlayer");
        }
        if (PlayerConnection == null) {
            PlayerConnection = MapManagerPlugin.nmsClassResolver.resolve("PlayerConnection", "server.network.PlayerConnection");
        }
        if (EntityPlayerFieldResolver == null) {
            EntityPlayerFieldResolver = new FieldResolver(EntityPlayer);
        }
        if (PlayerConnectionMethodResolver == null) {
            PlayerConnectionMethodResolver = new MethodResolver(PlayerConnection);
        }

        try {
            Object handle = Minecraft.getHandle(p);
            final Object connection = EntityPlayerFieldResolver.resolveByFirstTypeAccessor(PlayerConnection).get(handle);
            Method sendPacket = PlayerConnectionMethodResolver.resolveSignature("void sendPacket(Packet)", "void a(Packet)");
            sendPacket.invoke(connection, packet);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static class QueuedMap {

        final int id;
        final ArrayImage image;
        final Player player;

        QueuedMap(int id, ArrayImage image, Player player) {
            this.id = id;
            this.image = image;
            this.player = player;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.id;
            result = prime * result + (this.image == null ? 0 : this.image.hashCode());
            result = prime * result + (this.player == null ? 0 : this.player.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {return true;}
            if (obj == null) {return false;}
            if (this.getClass() != obj.getClass()) {return false;}
            QueuedMap other = (QueuedMap) obj;
            if (this.id != other.id) {return false;}
            if (this.image == null) {
                if (other.image != null) {return false;}
            } else if (!this.image.equals(other.image)) {return false;}
            if (this.player == null) {
                if (other.player != null) {return false;}
            } else if (!this.player.equals(other.player)) {return false;}
            return true;
        }

    }

}
