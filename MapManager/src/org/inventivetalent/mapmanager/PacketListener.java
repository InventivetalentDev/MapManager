package org.inventivetalent.mapmanager;

import de.inventivegames.packetlistener.handler.PacketHandler;
import de.inventivegames.packetlistener.handler.PacketOptions;
import de.inventivegames.packetlistener.handler.ReceivedPacket;
import de.inventivegames.packetlistener.handler.SentPacket;
import org.bukkit.plugin.Plugin;

public class PacketListener {

	private final PacketHandler packetHandler;

	public PacketListener(Plugin plugin) {
		this.packetHandler = new PacketHandler(plugin) {
			@Override
			@PacketOptions(forcePlayer = true)
			public void onSend(SentPacket sentPacket) {
				if (sentPacket.hasPlayer()) {
					if ("PacketPlayOutMap".equals(sentPacket.getPacketName())) {
						int id = ((Integer) sentPacket.getPacketValue("a")).intValue();

						if (id < 0) {
							//It's one of our maps, invert the id and let it through
							Integer newId = Integer.valueOf(-id);
							sentPacket.setPacketValue("a", newId);
						} else {
							if (!MapManager.ALLOW_VANILLA) {//Vanilla maps not allowed, so we can just cancel all maps
								sentPacket.setCancelled(true);
							} else {
								boolean isPluginMap = !MapManager.ALLOW_VANILLA;
								if (MapManager.ALLOW_VANILLA) {//Less efficient method: check if the ID is used by the player
									isPluginMap = MapManager.isIdUsedBy(sentPacket.getPlayer(), (short) id);
								}

								if (isPluginMap) {//It's the ID of one of our maps, so cancel it for this player
									sentPacket.setCancelled(true);
								}
							}
						}
					}
				}
			}

			@Override
			public void onReceive(ReceivedPacket receivedPacket) {
			}
		};
		PacketHandler.addHandler(this.packetHandler);
	}

	protected void disable() {
		PacketHandler.removeHandler(this.packetHandler);
	}

}
