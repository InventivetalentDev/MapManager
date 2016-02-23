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

package org.inventivetalent.mapmanager.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.inventivetalent.mapmanager.MapManagerPlugin;
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.mapmanager.wrapper.MapWrapper;

import java.util.List;

public class MapInteractEvent extends Event implements Cancellable {

	private Player player;
	private int    entityID;
	private int    action;
	private Vector vector;

	private ItemFrame  itemFrame;
	private MapWrapper mapWrapper;

	private boolean cancelled;

	public MapInteractEvent(Player who, int entityID, int action, Vector vector) {
		this.player = who;
		this.entityID = entityID;
		this.action = action;
		this.vector = vector;
	}

	public Player getPlayer() {
		return player;
	}

	public int getEntityID() {
		return entityID;
	}

	/**
	 * @return 0 = INTERACT; 1 = ATTACK; 2 = INTERACT_AT
	 */
	public int getActionID() {
		return action;
	}

	public Vector getVector() {
		return vector;
	}

	public ItemFrame getItemFrame() {
		if (this.itemFrame != null) { return this.itemFrame; }
		//		for (ItemFrame itemFrame : getPlayer().getWorld().getEntitiesByClass(ItemFrame.class)) {
		//			if (itemFrame.getEntityId() == getEntityID()) {
		//				return this.itemFrame = itemFrame;
		//			}
		//		}
		return this.itemFrame = MapManagerPlugin.getItemFrameById(getPlayer().getWorld(), getEntityID());
	}

	public MapWrapper getMapWrapper() {
		if (this.mapWrapper != null) { return this.mapWrapper; }
		ItemFrame itemFrame = getItemFrame();
		if (itemFrame != null) {
			if (itemFrame.hasMetadata("MAP_WRAPPER_ID_REF")) {
				MapManager mapManager = ((MapManagerPlugin) Bukkit.getPluginManager().getPlugin("MapManager")).getMapManager();
				List<MetadataValue> metadataValues = itemFrame.getMetadata("MAP_WRAPPER_ID_REF");
				for (MetadataValue value : metadataValues) {
					MapWrapper wrapper = mapManager.getWrapperForId(getPlayer(), value.asShort());
					if (wrapper != null) {
						return this.mapWrapper = wrapper;
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean b) {
		cancelled = b;
	}

	private static HandlerList handlerList = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}
}
