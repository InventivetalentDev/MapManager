package org.inventivetalent.mapmanager.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.mapmanager.MapManagerPlugin;
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.mapmanager.wrapper.MapWrapper;

/**
 * Event called when a client sends a CreativeInventoryUpdate-Packet for a {@link MapManager} map
 * (usually after using {@link org.inventivetalent.mapmanager.controller.MapController#showInInventory(Player, int, boolean)})
 * <p>
 * Cancelled by default.
 */
public class CreativeInventoryMapUpdateEvent extends Event implements Cancellable {

	private Player    player;
	private int       slot;
	private ItemStack itemStack;

	private MapWrapper mapWrapper;

	private boolean cancelled = true;

	public CreativeInventoryMapUpdateEvent(Player player, int slot, ItemStack itemStack) {
		this.player = player;
		this.slot = slot;
		this.itemStack = itemStack;
	}

	/**
	 * @return the {@link Player} that sent the update
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @return the update item slot
	 */
	public int getSlot() {
		return slot;
	}

	/**
	 * @return the updated {@link ItemStack}
	 */
	public ItemStack getItemStack() {
		return itemStack;
	}

	/**
	 * @return the {@link MapWrapper} of the item
	 */
	public MapWrapper getMapWrapper() {
		if (this.mapWrapper != null) { return this.mapWrapper; }
		if (getItemStack() == null) { return null; }
		if (getItemStack().getType() != Material.MAP) { return null; }
		MapManager mapManager = ((MapManagerPlugin) Bukkit.getPluginManager().getPlugin("MapManager")).getMapManager();
		return this.mapWrapper = mapManager.getWrapperForId(getPlayer(), getItemStack().getDurability());
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
