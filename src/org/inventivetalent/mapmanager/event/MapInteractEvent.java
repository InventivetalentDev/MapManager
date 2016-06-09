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
	private int    hand;

	private ItemFrame  itemFrame;
	private MapWrapper mapWrapper;

	private boolean cancelled;

	public MapInteractEvent(Player who, int entityID, int action, Vector vector, int hand) {
		this.player = who;
		this.entityID = entityID;
		this.action = action;
		this.vector = vector;
		this.hand = hand;
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
	 *
	 * @return the {@link Vector}-Position where the player clicked, or <code>null</code> if the action is not INTERACT_AT
	 */
	public Vector getVector() {
		return vector;
	}

	public int getHandID() {
		return hand;
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
		ItemFrame itemFrame = getItemFrame();
		if (itemFrame != null) {
			if (itemFrame.hasMetadata("MAP_WRAPPER_REF")) {
				List<MetadataValue> metadataValues = itemFrame.getMetadata("MAP_WRAPPER_REF");
				for (MetadataValue value : metadataValues) {
					MapWrapper wrapper = (MapWrapper) value.value();
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
