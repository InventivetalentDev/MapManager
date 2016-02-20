package org.inventivetalent.mapmanager;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

public interface MapController {

	void addViewer(Player player);

	void removeViewer(OfflinePlayer player);

	void clearViewers();

	boolean isViewing(OfflinePlayer player);

	short getMapId(OfflinePlayer player);

	void update(ArrayImage content);

	void sendContent(Player player);

	void showInInventory(Player player, int slot, boolean force);

	void showInInventory(Player player, int slot);

	void showInHand(Player player, boolean force);

	void showInHand(Player player);

	void showInFrame(Player player, ItemFrame frame, boolean force);

	void showInFrame(Player player, ItemFrame frame);

}
