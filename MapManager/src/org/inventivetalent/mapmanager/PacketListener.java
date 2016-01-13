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
						int id=((Integer)sentPacket.getPacketValue("a")).intValue();
						if(id<0) {
							Integer newId = Integer.valueOf(-id);
							sentPacket.setPacketValue("a", newId);
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
