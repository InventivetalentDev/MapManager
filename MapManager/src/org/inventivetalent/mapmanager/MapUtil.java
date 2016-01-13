package org.inventivetalent.mapmanager;

import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MapUtil {

	public static boolean canPlayerSeeMap(Player player, int mapId) {
		return doesInventoryContainMap(player.getInventory(), mapId) || isPlayerNearbyMap(player, mapId);
	}

	public static boolean doesInventoryContainMap(Inventory inventory, int mapId) {
		for (ItemStack itemStack : inventory.getContents()) {
			if (itemStack == null) { continue; }
			if (itemStack.getType() == Material.MAP) {
				if (itemStack.getDurability() == mapId || itemStack.getData().getData() == mapId) { return true; }
			}
		}
		return false;
	}

	public static boolean isPlayerNearbyMap(Player player, int mapId) {
		for (ItemFrame frame : player.getWorld().getEntitiesByClass(ItemFrame.class)) {
			if (frame.getLocation().distanceSquared(player.getLocation()) < 1024/*Math.pow(32)*/) {
				if (frame.getItem() != null) {
					if (frame.getItem().getType() == Material.MAP) {
						if (frame.getItem().getDurability() == mapId || frame.getItem().getData().getData() == mapId) { return true; }
					}
				}
			}
		}
		return false;
	}

}
