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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class MapSender {

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

		runSender();
	}

	protected static void runSender() {
		if (Bukkit.getScheduler().isQueued(senderID) || Bukkit.getScheduler().isCurrentlyRunning(senderID) || sendQueue.size() == 0) { return; }

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

		Bukkit.getScheduler().runTaskAsynchronously(MapManagerPlugin.instance, new Runnable() {
			@Override
			public void run() {
				if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_8_R1)) {
					byte[][] dataArray = (byte[][]) image.generatePacketData();
					for (int x = 0; x < 128; x++) {
						//						byte[] bytes = new byte[131];
						byte[] bytes = dataArray[x];

						//						bytes[1] = (byte) x;
						//						for (int y = 0; y < 128; y++) {
						//							bytes[y + 3] = getColor(image, x, y);
						//						}

						try {
							Object packet = constructPacket_1_7(id, bytes);
							sendPacket(packet, receiver);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1)) {//1.8 & 1.9
					//					byte[] data = new byte[128 * 128];
					//					Arrays.fill(data, (byte) 0);
					//					for (int x = 0; x < 128; x++) {
					//						for (int y = 0; y < 128; y++) {
					//							data[y * 128 + x] = getColor(image, x, y);
					//						}
					//					}
					try {
						Object packet = constructPacket(id, image);
						sendPacket(packet, receiver);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private static Class<?> nmsPacketPlayOutMap;

	static {
		nmsPacketPlayOutMap = MapManagerPlugin.nmsClassResolver.resolveSilent("PacketPlayOutMap");
	}

	private static Object constructPacket(int id, ArrayImage data) {
		Object packet = null;

		/*if (Minecraft.getVersion().contains("1_7")) {
			try {
				packet = constructPacket_1_7(id, data);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		} else*/
		if (Minecraft.getVersion().contains("1_8")) {
			try {
				packet = constructPacket_1_8(id, data);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		} else if (Minecraft.getVersion().contains("1_9")) {
			try {
				packet = constructPacket_1_9(id, data);
			} catch (ReflectiveOperationException e) {
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

	private static Object constructPacket_1_8(int id, ArrayImage data) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		Object packet = nmsPacketPlayOutMap//
				.getConstructor(int.class, byte.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class)//
				.newInstance(id,// ID
						(byte) 0,// Scale
						new ArrayList<>(),// Icons
						data.generatePacketData(),// Data
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
						data.generatePacketData(),//Data
						data.minX,// X-position
						data.minY,// Y-position
						data.maxX,// X-Size (or 2nd X-position)
						data.maxY// Y-Size (or 2nd Y-position)
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
		if (color.getAlpha() < 128) { return 0; }
		int index = 0;
		double best = -1.0D;
		for (int i = 4; i < MAP_COLORS.length; i++) {
			double distance = getDistance(color, MAP_COLORS[i]);
			if (distance < best || best == -1.0D) {
				best = distance;
				index = i;
			}
		}

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

	protected static final Color[] MAP_COLORS = new Color[] {
			c(0, 0, 0),
			c(0, 0, 0),
			c(0, 0, 0),
			c(0, 0, 0),
			c(89, 125, 39),
			c(109, 153, 48),
			c(127, 178, 56),
			c(67, 94, 29),
			c(174, 164, 115),
			c(213, 201, 140),
			c(247, 233, 163),
			c(130, 123, 86),
			c(140, 140, 140),
			c(171, 171, 171),
			c(199, 199, 199),
			c(105, 105, 105),
			c(180, 0, 0),
			c(220, 0, 0),
			c(255, 0, 0),
			c(135, 0, 0),
			c(112, 112, 180),
			c(138, 138, 220),
			c(160, 160, 255),
			c(84, 84, 135),
			c(117, 117, 117),
			c(144, 144, 144),
			c(167, 167, 167),
			c(88, 88, 88),
			c(0, 87, 0),
			c(0, 106, 0),
			c(0, 124, 0),
			c(0, 65, 0),
			c(180, 180, 180),
			c(220, 220, 220),
			c(255, 255, 255),
			c(135, 135, 135),
			c(115, 118, 129),
			c(141, 144, 158),
			c(164, 168, 184),
			c(86, 88, 97),
			c(106, 76, 54),
			c(130, 94, 66),
			c(151, 109, 77),
			c(79, 57, 40),
			c(79, 79, 79),
			c(96, 96, 96),
			c(112, 112, 112),
			c(59, 59, 59),
			c(45, 45, 180),
			c(55, 55, 220),
			c(64, 64, 255),
			c(33, 33, 135),
			c(100, 84, 50),
			c(123, 102, 62),
			c(143, 119, 72),
			c(75, 63, 38),
			c(180, 177, 172),
			c(220, 217, 211),
			c(255, 252, 245),
			c(135, 133, 129),
			c(152, 89, 36),
			c(186, 109, 44),
			c(216, 127, 51),
			c(114, 67, 27),
			c(125, 53, 152),
			c(153, 65, 186),
			c(178, 76, 216),
			c(94, 40, 114),
			c(72, 108, 152),
			c(88, 132, 186),
			c(102, 153, 216),
			c(54, 81, 114),
			c(161, 161, 36),
			c(197, 197, 44),
			c(229, 229, 51),
			c(121, 121, 27),
			c(89, 144, 17),
			c(109, 176, 21),
			c(127, 204, 25),
			c(67, 108, 13),
			c(170, 89, 116),
			c(208, 109, 142),
			c(242, 127, 165),
			c(128, 67, 87),
			c(53, 53, 53),
			c(65, 65, 65),
			c(76, 76, 76),
			c(40, 40, 40),
			c(108, 108, 108),
			c(132, 132, 132),
			c(153, 153, 153),
			c(81, 81, 81),
			c(53, 89, 108),
			c(65, 109, 132),
			c(76, 127, 153),
			c(40, 67, 81),
			c(89, 44, 125),
			c(109, 54, 153),
			c(127, 63, 178),
			c(67, 33, 94),
			c(36, 53, 125),
			c(44, 65, 153),
			c(51, 76, 178),
			c(27, 40, 94),
			c(72, 53, 36),
			c(88, 65, 44),
			c(102, 76, 51),
			c(54, 40, 27),
			c(72, 89, 36),
			c(88, 109, 44),
			c(102, 127, 51),
			c(54, 67, 27),
			c(108, 36, 36),
			c(132, 44, 44),
			c(153, 51, 51),
			c(81, 27, 27),
			c(17, 17, 17),
			c(21, 21, 21),
			c(25, 25, 25),
			c(13, 13, 13),
			c(176, 168, 54),
			c(215, 205, 66),
			c(250, 238, 77),
			c(132, 126, 40),
			c(64, 154, 150),
			c(79, 188, 183),
			c(92, 219, 213),
			c(48, 115, 112),
			c(52, 90, 180),
			c(63, 110, 220),
			c(74, 128, 255),
			c(39, 67, 135),
			c(0, 153, 40),
			c(0, 187, 50),
			c(0, 217, 58),
			c(0, 114, 30),
			c(91, 60, 34),
			c(111, 74, 42),
			c(129, 86, 49),
			c(68, 45, 25),
			c(79, 1, 0),
			c(96, 1, 0),
			c(112, 2, 0),
			c(59, 1, 0) };

	private static Color c(int r, int g, int b) {
		return new Color(r, g, b);
	}

}