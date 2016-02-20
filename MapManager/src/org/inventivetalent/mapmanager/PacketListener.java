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
