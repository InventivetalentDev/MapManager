package org.inventivetalent.mapmanager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

class CommandHandler implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if (args.length == 0) {
			if (sender.hasPermission("mapmanager.reload")) {
				sender.sendMessage("§7/mapmanager reload");
				sender.sendMessage("§eReload the configuration");
			}
			return true;
		}
		if ("reload".equalsIgnoreCase(args[0])) {
			if (!sender.hasPermission("mapmanager.reload")) {
				sender.sendMessage("§cNo permission");
				return false;
			}
			sender.sendMessage("§7Reloading...");
			MapManagerPlugin.instance.reload();
			sender.sendMessage("§aConfiguration reloaded.");
			return true;
		}
//		if ("test".equalsIgnoreCase(args[0])) {
//			try {
//				BufferedImage bufferedImage = ImageIO.read(new URL("https://i.imgur.com/iJU3GUq.png"));
//				MapManager mapManager = ((MapManagerPlugin) Bukkit.getPluginManager().getPlugin("MapManager")).getMapManager();
//				MapWrapper wrapper = mapManager.wrapImage(bufferedImage);
//				MapController controller = wrapper.getController();
//
//				controller.addViewer((Player) sender);
//				controller.sendContent((Player) sender);
//
//				controller.showInHand((Player) sender,true);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		return null;
	}
}
