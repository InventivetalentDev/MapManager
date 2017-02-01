package org.inventivetalent.mapmanager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.reflection.resolver.ClassResolver;
import org.spigotmc.CustomTimingsHandler;

import java.util.HashMap;
import java.util.Map;

public class TimingsHelper {

	static ClassResolver classResolver = new ClassResolver();


	public static final boolean             PAPER_SPIGOT = Bukkit.getVersion().contains("Paper") || (MapManagerPlugin.instance != null && MapManagerPlugin.instance.getConfig().getBoolean("paperSpigot", false));
	static final        Map<String, Object> HANDLER_MAP  = new HashMap<>();

	public static void startTiming(String name) {
		if (!HANDLER_MAP.containsKey(name)) {
			HANDLER_MAP.put(name, createHandler(name));
		}
		Object handler = HANDLER_MAP.get(name);
		try {
			classResolver.resolveSilent("co.aikar.timings.Timing","org.spigotmc.CustomTimingsHandler").getDeclaredMethod("startTiming").invoke(handler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void stopTiming(String name) {
		if (HANDLER_MAP.containsKey(name)) {
			Object handler = HANDLER_MAP.get(name);
			try {
				classResolver.resolveSilent("co.aikar.timings.Timing","org.spigotmc.CustomTimingsHandler").getDeclaredMethod("stopTiming").invoke(handler);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static Object createHandler(String name) {
		if (!PAPER_SPIGOT) {
			return new CustomTimingsHandler(name);
		} else {
			try {
				Class<?> clazz = Class.forName("co.aikar.timings.Timings");
				return clazz.getDeclaredMethod("of", Plugin.class, String.class).invoke(null, MapManagerPlugin.instance, name);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
