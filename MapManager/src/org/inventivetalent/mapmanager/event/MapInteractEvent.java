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

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.inventivetalent.mapmanager.MapManagerPlugin;
import org.inventivetalent.mapmanager.wrapper.MapWrapper;

import java.util.List;

/**
 * Event called when a player interacts with a {@link org.inventivetalent.mapmanager.manager.MapManager} map in an {@link ItemFrame}
 */
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

	/**
	 * @return the {@link Player} that interacted
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @return the Entity-ID of the clicked ItemFrame
	 */
	public int getEntityID() {
		return entityID;
	}

	/**
	 * @return 0 = INTERACT; 1 = ATTACK; 2 = INTERACT_AT
	 */
	public int getActionID() {
		return action;
	}

	/**
	 * Only returns if {@link #getActionID()} == INTERACT_AT
	 * @return the {@link Vector}-Position where the player clicked, or <code>null</code> if the action is not INTERACT_AT
	 */
	public Vector getVector() {
		return vector;
	}

	/**
	 * @return the clicked {@link ItemFrame}
	 */
	public ItemFrame getItemFrame() {
		if (this.itemFrame != null) { return this.itemFrame; }
		return this.itemFrame = MapManagerPlugin.getItemFrameById(getPlayer().getWorld(), getEntityID());
	}

	/**
	 * @return the {@link MapWrapper} of the clicked frame
	 */
	public MapWrapper getMapWrapper() {
		if (this.mapWrapper != null) { return this.mapWrapper; }
		System.out.println("getMapWrapper");
		ItemFrame itemFrame = getItemFrame();
		if (itemFrame != null) {
			if (itemFrame.hasMetadata("MAP_WRAPPER_REF")) {
				List<MetadataValue> metadataValues = itemFrame.getMetadata("MAP_WRAPPER_REF");
				for (MetadataValue value : metadataValues) {
					MapWrapper wrapper = (MapWrapper) value.value();
					System.out.println(wrapper);
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
