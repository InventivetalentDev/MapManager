package org.inventivetalent.mapmanager.controller;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.inventivetalent.mapmanager.ArrayImage;

/**
 * Default MapController
 */
public interface MapController {

	/**
	 * Add a viewer to this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper}
	 *
	 * @param player {@link Player} to add
	 */
	void addViewer(Player player);

	/**
	 * Remove a viewer from this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper}
	 *
	 * @param player {@link OfflinePlayer} to remove
	 */
	void removeViewer(OfflinePlayer player);

	/**
	 * Remove all viewers
	 */
	void clearViewers();

	/**
	 * Check if a player is viewing this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper}
	 *
	 * @param player {@link OfflinePlayer} to check
	 * @return <code>true</code> if the player is viewing
	 */
	boolean isViewing(OfflinePlayer player);

	/**
	 * Get this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper}'s map ID for a player
	 *
	 * @param player {@link OfflinePlayer} to get the ID for
	 * @return the ID, or <code>-1</code> if no ID exists (i.e. the player is not viewing)
	 */
	short getMapId(OfflinePlayer player);

	/**
	 * Update the image in this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper}
	 *
	 * @param content new {@link ArrayImage} content
	 */
	void update(ArrayImage content);

	ArrayImage getContent();

	/**
	 * Send the content of this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper} to a player
	 *
	 * @param player {@link Player} receiver of the content
	 */
	void sendContent(Player player);

	/**
	 * Send the content of this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper} to a player
	 *
	 * @param player       {@link Player} receiver of the content
	 * @param withoutQueue if <code>true</code>, the content will be sent immediately
	 */
	void sendContent(Player player, boolean withoutQueue);

	/**
	 * Show this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper} in a player's inventory
	 *
	 * @param player {@link Player}
	 * @param slot   slot to show the map in
	 * @param force  if <code>false</code>, the map will not be shown if the player is in creative mode
	 * @see org.inventivetalent.mapmanager.event.CreativeInventoryMapUpdateEvent
	 */
	void showInInventory(Player player, int slot, boolean force);

	/**
	 * Show this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper} in a player's inventory
	 *
	 * @param player {@link Player}
	 * @param slot   slot to show the map in
	 */
	void showInInventory(Player player, int slot);

	/**
	 * Show this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper} in a player's hand
	 *
	 * @param player {@link Player}
	 * @param force  if <code>false</code>, the map will not be shown if the player is not holding a map, or is in createive mode
	 * @see #showInFrame(Player, ItemFrame, boolean)
	 * @see org.inventivetalent.mapmanager.event.CreativeInventoryMapUpdateEvent
	 */
	void showInHand(Player player, boolean force);

	/**
	 * Show this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper} in a player's hand
	 *
	 * @param player {@link Player}
	 */
	void showInHand(Player player);

	/**
	 * Show this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper} in an {@link ItemFrame}
	 *
	 * @param player {@link Player} that will be able to see the map
	 * @param frame  {@link ItemFrame} to show the map in
	 */
	void showInFrame(Player player, ItemFrame frame);

	/**
	 * Show this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper} in an {@link ItemFrame}
	 *
	 * @param player {@link Player} that will be able to see the map
	 * @param frame  {@link ItemFrame} to show the map in
	 * @param force  if <code>false</code>, the map will not be shown if there is not Map-Item in the ItemFrame
	 */
	void showInFrame(Player player, ItemFrame frame, boolean force);

	/**
	 * Show this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper} in an {@link ItemFrame}
	 *
	 * @param player   {@link Player} that will be able to see the map
	 * @param entityId Entity-ID of the {@link ItemFrame} to show the map in
	 */
	void showInFrame(Player player, int entityId);

	/**
	 * Show this {@link org.inventivetalent.mapmanager.wrapper.MapWrapper} in an {@link ItemFrame}
	 *
	 * @param player    {@link Player} that will be able to see the map
	 * @param entityId  Entity-ID of the {@link ItemFrame} to show the map in
	 * @param debugInfo {@link String} to show when a player looks at the map, or <code>null</code>
	 */
	void showInFrame(Player player, int entityId, String debugInfo);

	/**
	 * Clear a frame
	 *
	 * @param player   {@link Player} that will be able to see the cleared frame
	 * @param entityId Entity-ID of the {@link ItemFrame} to clear
	 */
	void clearFrame(Player player, int entityId);

	/**
	 * Clear a frame
	 *
	 * @param player {@link Player} that will be able to see the cleared frame
	 * @param frame  {@link ItemFrame} to clear
	 */
	void clearFrame(Player player, ItemFrame frame);

}
