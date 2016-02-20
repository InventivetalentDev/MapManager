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
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.ConstructorResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;

import java.util.*;

class DefaultMapWrapper implements MapWrapper{

	protected ArrayImage content;
	protected final Map<UUID, Short> viewers = new HashMap<>();

	private static FieldResolver       PacketEntityMetadataFieldResolver;
	private static FieldResolver       EntityHumanFieldResolver;
	private static FieldResolver       ContainerFieldResolver;
	private static ConstructorResolver WatchableObjectConstructorResolver;
	private static ConstructorResolver PacketPlayOutSlotConstructorResolver;
	private static MethodResolver      CraftItemStackMethodResolver;

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
			return viewers.containsKey(player.getUniqueId());
		}

		@Override
		public short getMapId(OfflinePlayer player) {
			if (isViewing(player)) {
				return viewers.get(player.getUniqueId());
			}
			return -1;
		}

		@Override
		public void update(ArrayImage content) {
			DefaultMapWrapper.this.content = content;
			for (UUID id : viewers.keySet()) {
				sendContent(Bukkit.getPlayer(id));
			}
		}

		@Override
		public void sendContent(Player player) {
			if (!isViewing(player)) { return; }
			MapSender.addToQueue(getMapId(player), DefaultMapWrapper.this.content, player);
		}

		@Override
		public void showInInventory(Player player, int slot, boolean force) {
			if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.MAP) {
				if (!force) {//Player is not holding a map
					return;
				}
			}
			if (player.getGameMode() == GameMode.CREATIVE) {
				//Clients in creative mode will send a 'PacketPlayInCreativeSlot' which tells the server there's a new item in the inventory and creates a new map
				if (!force) {
					return;
				}
			}

			//Adjust the slot ID
			if (slot < 9) { slot += 36; } else if (slot > 35) { slot = 8 - (slot - 36); }

			try {
				if (PacketPlayOutSlotConstructorResolver == null) {
					PacketPlayOutSlotConstructorResolver = new ConstructorResolver(MapManagerPlugin.nmsClassResolver.resolve("PacketPlayOutSetSlot"));
				}
				if (EntityHumanFieldResolver == null) {
					EntityHumanFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("EntityHuman"));
				}
				if (ContainerFieldResolver == null) {
					ContainerFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("Container"));
				}
				if (CraftItemStackMethodResolver == null) {
					CraftItemStackMethodResolver = new MethodResolver(MapManagerPlugin.obcClassResolver.resolve("inventory.CraftItemStack"));
				}

				Object entityPlayer = Minecraft.getHandle(player);
				Object defaultContainer = EntityHumanFieldResolver.resolve("defaultContainer").get(entityPlayer);
				Object windowId = ContainerFieldResolver.resolve("windowId").get(defaultContainer);

				//Create the ItemStack with the player's map ID
				Object itemStack = CraftItemStackMethodResolver.resolve(new ResolverQuery("asNMSCopy", ItemStack.class)).invoke(null, new ItemStack(Material.MAP, 1, getMapId(player)));

				Object setSlot = PacketPlayOutSlotConstructorResolver.resolve(new Class[] {
						int.class,
						int.class,
						MapManagerPlugin.nmsClassResolver.resolve("ItemStack") }).newInstance(windowId, slot, itemStack);

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
			showInInventory(player, player.getInventory().getHeldItemSlot(), force);
		}

		@Override
		public void showInHand(Player player) {
			showInHand(player, false);
		}

		@Override
		public void showInFrame(Player player, ItemFrame frame, boolean force) {
			if (frame.getItem() == null || frame.getItem().getType() != Material.MAP) {
				if (!force) {//There's no map in the item frame: don't do anything
					return;
				}
			}

			try {
				if (PacketEntityMetadataFieldResolver == null) {
					PacketEntityMetadataFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("PacketPlaOutEntityMetadata"));
				}
				if (WatchableObjectConstructorResolver == null) {
					WatchableObjectConstructorResolver = new ConstructorResolver(MapManagerPlugin.nmsClassResolver.resolve("WatchableObject", "DataWatcher$WatchableObject"));
				}
				if (CraftItemStackMethodResolver == null) {
					CraftItemStackMethodResolver = new MethodResolver(MapManagerPlugin.obcClassResolver.resolve("inventory.CraftItemStack"));
				}

				Object meta = MapManagerPlugin.nmsClassResolver.resolve("PacketPlayOutEntityMetadata").newInstance();

				//Set the Entity ID of the frame
				PacketEntityMetadataFieldResolver.resolve("a").set(meta, frame.getEntityId());

				List list = new ArrayList();

				// 0 = Byte
				// 1 = Short
				// 2 = Int
				// 3 = Float
				// 4 = String
				// 5 = ItemStack
				// 6 = BlockPosition / ChunkCoordinates
				// 7 = Vector3f / Vector(?)

				//Create the ItemStack with the player's map ID
				Object itemStack = CraftItemStackMethodResolver.resolve(new ResolverQuery("asNMSCopy", ItemStack.class)).invoke(null, new ItemStack(Material.MAP, 1, getMapId(player)));

				list.add(WatchableObjectConstructorResolver.resolve(new Class[] {
						int.class,
						int.class,
						Object.class }).newInstance(5, 8, itemStack));
				list.add(WatchableObjectConstructorResolver.resolve(new Class[] {
						int.class,
						int.class,
						Object.class }).newInstance(5, 2, itemStack));

				PacketEntityMetadataFieldResolver.resolve("b").set(meta, list);

				//Send the completed packet
				sendPacket(player, meta);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void showInFrame(Player player, ItemFrame frame) {
			showInFrame(player, frame, false);
		}

	};

	 DefaultMapWrapper(ArrayImage content) {
		this.content = content;
	}

	public MapController getController() {
		return controller;
	}

	protected void sendPacket(Player player, Object packet) {
		try {
			MapSender.sendPacket(packet, player);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
