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
import org.bukkit.entity.Player;
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

public class MapSender {

	private static final List<QueuedMap> sendQueue = new ArrayList<>();
	private static       int             senderID  = -1;

	private static FieldResolver  EntityPlayerFieldResolver;
	private static MethodResolver PlayerConnectionMethodResolver;

	public static void cancelIDs(short[] ids) {
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
		if (sendQueue.contains(toSend)) { return; }
		sendQueue.add(toSend);
		System.out.println("Added #" + id + " to queue");

		runSender();
	}

	protected static void runSender() {
		if (Bukkit.getScheduler().isQueued(senderID) || Bukkit.getScheduler().isCurrentlyRunning(senderID) || sendQueue.size() == 0) { return; }

		System.out.println("running sender");

		senderID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MapManagerPlugin.instance, new Runnable() {

			@Override
			public void run() {
				if (sendQueue.isEmpty()) { return; }
				for (int i = 0; i < Math.min(sendQueue.size(), MapManager.Options.Sender.AMOUNT + 1); i++) {
					QueuedMap current = sendQueue.get(0);
					if (current == null) { return; }
					sendMap(current.id, current.image, current.player);
					if (!sendQueue.isEmpty()) {
						sendQueue.remove(0);
					}
				}
			}
		}, 0, MapManager.Options.Sender.DELAY);
	}

	protected static void sendMap(@Nonnull final int id0, @Nonnull final ArrayImage image, @Nonnull final Player receiver) {
		if (receiver == null || !receiver.isOnline()) {

			List<QueuedMap> toRemove = new ArrayList<>();
			for (QueuedMap qMap : sendQueue) {
				if (qMap == null) { continue; }
				if (qMap.player == null || !qMap.player.isOnline()) {
					toRemove.add(qMap);
				}
			}
			Bukkit.getScheduler().cancelTask(senderID);
			sendQueue.removeAll(toRemove);

			return;
		}

		final int id = -id0;

		System.out.println("Sending #" + id + "(" + id0 + ") to " + receiver.getName());

		Bukkit.getScheduler().runTaskAsynchronously(MapManagerPlugin.instance, new Runnable() {
			@Override
			public void run() {
				if (Minecraft.getVersion().contains("1_7")) {
					for (int x = 0; x < 128; x++) {
						byte[] bytes = new byte[131];

						bytes[1] = (byte) x;
						for (int y = 0; y < 128; y++) {
							bytes[y + 3] = getColor(image, x, y);
						}

						Object packet = consructPacket(id, bytes);
						try {
							sendPacket(packet, receiver);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				if (Minecraft.getVersion().contains("1_8")) {
					byte[] data = new byte[128 * 128];
					Arrays.fill(data, (byte) 0);
					for (int x = 0; x < 128; x++) {
						for (int y = 0; y < 128; y++) {
							data[y * 128 + x] = getColor(image, x, y);
						}
					}

					try {
						Object packet = constructPacket_1_8(id, data);
						try {
							sendPacket(packet, receiver);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
	}

	private static Class<?> nmsPacketPlayOutMap;

	static {
		nmsPacketPlayOutMap = MapManagerPlugin.nmsClassResolver.resolveSilent("PacketPlayOutMap");
	}

	private static Object consructPacket(int id, byte[] bytes) {
		Object packet = null;

		if (Minecraft.getVersion().contains("1_7")) {
			try {
				packet = constructPacket_1_7(id, bytes);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		} else if (Minecraft.getVersion().contains("1_8")) {
			try {
				packet = constructPacket_1_8(id, bytes);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
				e.printStackTrace();
			}
		}

		return packet;
	}

	private static Object constructPacket_1_7(int id, byte[] bytes) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Constructor constructor = null;
		try {
			constructor = nmsPacketPlayOutMap.getConstructor(int.class, byte[].class, byte.class);//Spigot protocol hack
		} catch (Exception e) {
			constructor = nmsPacketPlayOutMap.getConstructor(int.class, byte[].class);//default 1.7.10
		}
		try {//Spigot protocol hack
			return constructor.newInstance(id,// ID
					bytes,// Data
					(byte) 0// Scale
			);
		} catch (Exception e) {//default 1.7.10
			return constructor.newInstance(id,// ID
					bytes// Data
			);
		}
	}

	private static Object constructPacket_1_8(int id, byte[] bytes) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		Object packet = nmsPacketPlayOutMap//
				.getConstructor(int.class, byte.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class)//
				.newInstance(id,// ID
						(byte) 0,// Scale
						new ArrayList<>(),// Icons
						bytes,// Data
						0,// X-position
						0,// Y-position
						128,// X-Size (or 2nd X-position)
						128// Y-Size (or 2nd Y-position)
				);
		return packet;
	}

	protected static void sendPacket(Object packet, Player p) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
		if (EntityPlayerFieldResolver == null) {
			EntityPlayerFieldResolver = new FieldResolver(MapManagerPlugin.nmsClassResolver.resolve("EntityPlayer"));
		}
		if (PlayerConnectionMethodResolver == null) {
			PlayerConnectionMethodResolver = new MethodResolver(MapManagerPlugin.nmsClassResolver.resolve("PlayerConnection"));
		}

		Object handle = Minecraft.getHandle(p);
		final Object connection = EntityPlayerFieldResolver.resolve("playerConnection").get(handle);
		PlayerConnectionMethodResolver.resolve("sendPacket").invoke(connection, packet);
	}

	protected static byte getColor(ArrayImage image, int x, int y) {
		return matchColor(new Color(image.getRGB(x, y), true));
	}

	protected static byte getColor(BufferedImage image, int x, int y) {
		if (x > image.getWidth()) { return 0; }
		if (y > image.getHeight()) { return 0; }
		int color = image.getRGB(x, y);
		Color c = new Color(color, true);
		return matchColor(c);
	}

	protected static byte matchColor(Color color) {
		//		if (color.getAlpha() < 128) { return 0; }
		int index = 0;
		double best = -1.0D;
		for (int i = 4; i < MAP_COLORS.length; i++) {
			double distance = getDistance(color, MAP_COLORS[i]);
			if (distance < best || best == -1.0D) {
				best = distance;
				index = i;
			}
		}

		System.out.println("Closest Map Color for " + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha() + ": " + MAP_COLORS[index].getRed() + "," + MAP_COLORS[index].getGreen() + "," + MAP_COLORS[index].getBlue() + "," + MAP_COLORS[index].getAlpha());

		return (byte) (index < 128 ? index : -129 + index - 127);
	}

	protected static double getDistance(Color c1, Color c2) {
		double rmean = (c1.getRed() + c2.getRed()) / 2.0D;
		double r = c1.getRed() - c2.getRed();
		double g = c1.getGreen() - c2.getGreen();
		int b = c1.getBlue() - c2.getBlue();
		double weightR = 2.0D + rmean / 256.0D;
		double weightG = 4.0D;
		double weightB = 2.0D + (255.0D - rmean) / 256.0D;
		return weightR * r * r + weightG * g * g + weightB * b * b;
	}

	static class QueuedMap {

		final int        id;
		final ArrayImage image;
		final Player     player;

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
			if (this == obj) { return true; }
			if (obj == null) { return false; }
			if (this.getClass() != obj.getClass()) { return false; }
			QueuedMap other = (QueuedMap) obj;
			if (this.id != other.id) { return false; }
			if (this.image == null) {
				if (other.image != null) { return false; }
			} else if (!this.image.equals(other.image)) { return false; }
			if (this.player == null) {
				if (other.player != null) { return false; }
			} else if (!this.player.equals(other.player)) { return false; }
			return true;
		}

	}

	/*
	 * From https://github.com/LB--/MCModify/blob/java/src/main/java/com/lb_stuff/mcmodify/minecraft/Map.java (With updated values from http://minecraft.gamepedia.com/Map_item_format)
	 */

	protected static final Color[]         MAP_COLORS;
	protected static final IndexColorModel MAP_COLOR_MODEL;

	static {
		System.out.println("Generating Map Color Palette....");
		final Color[] BaseMapColors = new Color[] {
				new Color(0, 0, 0, 0),
				new Color(125, 176, 55),
				new Color(244, 230, 161),
				new Color(197, 197, 197),
				new Color(252, 0, 0),
				new Color(158, 158, 252),
				new Color(165, 165, 165),
				new Color(0, 123, 0),
				new Color(252, 252, 252),
				new Color(162, 166, 182),
				new Color(149, 108, 76),
				new Color(111, 111, 111),
				new Color(63, 63, 252),
				new Color(141, 118, 71),
				//new 1.7 colors (13w42a/13w42b)
				new Color(252, 249, 242),
				new Color(213, 125, 50),
				new Color(176, 75, 213),
				new Color(101, 151, 213),
				new Color(226, 226, 50),
				new Color(125, 202, 25),
				new Color(239, 125, 163),
				new Color(75, 75, 75),
				new Color(151, 151, 151),
				new Color(75, 125, 151),
				new Color(125, 62, 176),
				new Color(50, 75, 176),
				new Color(101, 75, 50),
				new Color(101, 125, 50),
				new Color(151, 50, 50),
				new Color(25, 25, 25),
				new Color(247, 235, 76),
				new Color(91, 216, 210),
				new Color(73, 129, 252),
				new Color(0, 214, 57),
				new Color(127, 85, 48),
				new Color(111, 2, 0),
				//new 1.8 colors
				new Color(126, 84, 48) };
		MAP_COLORS = new Color[BaseMapColors.length * 4];
		for (int i = 0; i < BaseMapColors.length; ++i) {
			Color bc = BaseMapColors[i];
			MAP_COLORS[i * 4 + 0] = new Color((int) (bc.getRed() * 180.0 / 255.0 + 0.5), (int) (bc.getGreen() * 180.0 / 255.0 + 0.5), (int) (bc.getBlue() * 180.0 / 255.0 + 0.5), bc.getAlpha());
			MAP_COLORS[i * 4 + 1] = new Color((int) (bc.getRed() * 220.0 / 255.0 + 0.5), (int) (bc.getGreen() * 220.0 / 255.0 + 0.5), (int) (bc.getBlue() * 220.0 / 255.0 + 0.5), bc.getAlpha());
			MAP_COLORS[i * 4 + 2] = bc;
			MAP_COLORS[i * 4 + 3] = new Color((int) (bc.getRed() * 135.0 / 255.0 + 0.5), (int) (bc.getGreen() * 135.0 / 255.0 + 0.5), (int) (bc.getBlue() * 135.0 / 255.0 + 0.5), bc.getAlpha());
		}
		byte[] r = new byte[MAP_COLORS.length],
				g = new byte[MAP_COLORS.length],
				b = new byte[MAP_COLORS.length],
				a = new byte[MAP_COLORS.length];
		for (int i = 0; i < MAP_COLORS.length; ++i) {
			Color mc = MAP_COLORS[i];
			r[i] = (byte) mc.getRed();
			g[i] = (byte) mc.getGreen();
			b[i] = (byte) mc.getBlue();
			a[i] = (byte) mc.getAlpha();
		}
		MAP_COLOR_MODEL = new IndexColorModel(8, MAP_COLORS.length, r, g, b, a);

		System.out.println("Generated " + MAP_COLORS.length + " Colors.");
	}

}