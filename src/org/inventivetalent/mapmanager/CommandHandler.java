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
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		return null;
	}
}
