package org.inventivetalent.mapmanager;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.inventivetalent.mapmanager.event.CreativeInventoryMapUpdateEvent;
import org.inventivetalent.mapmanager.event.MapCancelEvent;
import org.inventivetalent.mapmanager.event.MapInteractEvent;
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.packetlistener.handler.PacketHandler;
import org.inventivetalent.packetlistener.handler.PacketOptions;
import org.inventivetalent.packetlistener.handler.ReceivedPacket;
import org.inventivetalent.packetlistener.handler.SentPacket;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.minecraft.MinecraftVersion;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;

class PacketListener {

	private final PacketHandler packetHandler;

	private static FieldResolver Vec3DFieldResolver              = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolveSilent("Vec3D", "world.phys.Vec3D"));
	private static FieldResolver PacketUseEntityFieldResolver    = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolveSilent("PacketPlayInUseEntity", "network.protocol.game.PacketPlayInUseEntity"));
	private static FieldResolver PacketCreativeSlotFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolveSilent("PacketPlayInSetCreativeSlot", "network.protocol.game.PacketPlayInSetCreativeSlot"));

	private static MethodResolver CraftItemStackMethodResolver = new MethodResolver(MapManagerPlugin.obcClassResolver.resolveSilent("inventory.CraftItemStack"));

	public PacketListener(final MapManagerPlugin plugin) {
		this.packetHandler = new PacketHandler(plugin) {
			@Override
			@PacketOptions(forcePlayer = true)
			public void onSend(SentPacket sentPacket) {
				if (sentPacket.hasPlayer()) {
					if ("PacketPlayOutMap".equals(sentPacket.getPacketName())) {
						int id = (int) sentPacket.getPacketValue("a");

						if (id < 0) {
							//It's one of our maps, invert the id and let it through
							int newId = -id;
							sentPacket.setPacketValue("a", newId);
						} else {
							boolean async = !plugin.getServer().isPrimaryThread();
							MapCancelEvent mapCancelEvent = new MapCancelEvent(sentPacket.getPlayer(), id, async);
							if (!MapManager.Options.ALLOW_VANILLA) {//Vanilla maps not allowed, so we can just cancel all maps
								mapCancelEvent.setCancelled(true);
							} else {
								boolean isPluginMap = id > MapManager.Options.FORCED_OFFSET;
								if (MapManager.Options.FORCED_OFFSET <= 0) {//Less efficient method: check if the ID is used by the player
									isPluginMap = plugin.getMapManager().isIdUsedBy(sentPacket.getPlayer(), (int) id);
								}

								if (isPluginMap) {//It's the ID of one of our maps, so cancel it for this player
									mapCancelEvent.setCancelled(true);
								}
							}
							if (mapCancelEvent.getHandlers().getRegisteredListeners().length > 0) {
								Bukkit.getPluginManager().callEvent(mapCancelEvent);
							}
							sentPacket.setCancelled(mapCancelEvent.isCancelled());
						}
					}
				}
			}

			@Override
			@PacketOptions(forcePlayer = true)
			public void onReceive(ReceivedPacket receivedPacket) {
				if (receivedPacket.hasPlayer()) {
					if ("PacketPlayInUseEntity".equals(receivedPacket.getPacketName())) {
						try {
							int a = (int) receivedPacket.getPacketValue("a");
							Object b = PacketUseEntityFieldResolver.resolveSilent("action", "b").get(receivedPacket.getPacket());
							Object c = MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_8_R1) ? receivedPacket.getPacketValue("c") : null;
							Object d = MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_9_R1) ? receivedPacket.getPacketValue("d") : null;

							boolean async = !plugin.getServer().isPrimaryThread();
							MapInteractEvent event = new MapInteractEvent(receivedPacket.getPlayer(), a, ((Enum) b).ordinal(), vec3DtoVector(c), d == null ? 0 : ((Enum) d).ordinal(), async);
							if (event.getItemFrame() != null) {
								if (event.getMapWrapper() != null) {
									Bukkit.getPluginManager().callEvent(event);
									if (event.isCancelled()) {
										receivedPacket.setCancelled(true);
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if ("PacketPlayInSetCreativeSlot".equals(receivedPacket.getPacketName())) {
						try {
							int a = (int) PacketCreativeSlotFieldResolver.resolveSilent("slot", "a").get(receivedPacket.getPacket());
							Object b = receivedPacket.getPacketValue("b");
							ItemStack itemStack = b == null ? null : (ItemStack) CraftItemStackMethodResolver.resolve(new ResolverQuery("asBukkitCopy", MapManagerPlugin.nmsClassResolver.resolve("ItemStack"))).invoke(null, b);

							boolean async = !plugin.getServer().isPrimaryThread();
							CreativeInventoryMapUpdateEvent event = new CreativeInventoryMapUpdateEvent(receivedPacket.getPlayer(), a, itemStack, async);
							if (event.getMapWrapper() != null) {
								Bukkit.getPluginManager().callEvent(event);
								if (event.isCancelled()) {
									receivedPacket.setCancelled(true);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		PacketHandler.addHandler(this.packetHandler);
	}

	protected Vector vec3DtoVector(Object vec3D) {
		if (vec3D == null) { return null; }
		try {
			double a = (double) Vec3DFieldResolver.resolveAccessor("x"/*1.9*/, "a").get(vec3D);
			double b = (double) Vec3DFieldResolver.resolveAccessor("y"/*1.9*/, "b").get(vec3D);
			double c = (double) Vec3DFieldResolver.resolveAccessor("z"/*1.9*/, "c").get(vec3D);
			return new Vector(a, b, c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Vector(0, 0, 0);
	}

	protected void disable() {
		PacketHandler.removeHandler(this.packetHandler);
	}

}
