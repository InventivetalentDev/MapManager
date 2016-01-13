package org.inventivetalent.mapmanager;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface MapController {

	void addViewer(Player player);

	void removeViewer(OfflinePlayer player);

	boolean isViewing(OfflinePlayer player);

	short getMapId(OfflinePlayer player);

	void update(ArrayImage content);

	void sendContent(Player player);

}
