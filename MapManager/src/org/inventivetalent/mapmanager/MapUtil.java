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
