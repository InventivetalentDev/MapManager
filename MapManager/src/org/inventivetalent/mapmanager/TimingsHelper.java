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

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.spigotmc.CustomTimingsHandler;

import java.util.HashMap;
import java.util.Map;

public class TimingsHelper {

	public static final boolean             PAPER_SPIGOT = Bukkit.getVersion().contains("PaperSpigot");
	static final        Map<String, Object> HANDLER_MAP  = new HashMap<>();

	public static void startTiming(String name) {
		if (!HANDLER_MAP.containsKey(name)) {
			HANDLER_MAP.put(name, createHandler(name));
		}
		Object handler = HANDLER_MAP.get(name);
		try {
			handler.getClass().getDeclaredMethod("startTiming").invoke(handler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void stopTiming(String name) {
		if (HANDLER_MAP.containsKey(name)) {
			Object handler = HANDLER_MAP.get(name);
			try {
				handler.getClass().getDeclaredMethod("stopTiming").invoke(handler);
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
