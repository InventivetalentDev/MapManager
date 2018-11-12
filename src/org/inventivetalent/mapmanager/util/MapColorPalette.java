// From https://github.com/bergerhealer/BKCommonLib, modified by Joiubaxas#4650
package org.inventivetalent.mapmanager.util;

import org.bukkit.Bukkit;
import org.inventivetalent.mapmanager.util.mcsd.MCSDBubbleFormat;
import org.inventivetalent.mapmanager.util.mcsd.MCSDGenBukkit;

import java.awt.*;
import java.io.InputStream;
import java.util.Arrays;

public class MapColorPalette extends MapColorSpaceData {
    private static final MapColorSpaceData COLOR_MAP_DATA = new MapColorSpaceData();
    public static final byte[] COLOR_MAP_AVERAGE  = new byte[0x10000];
    public static final byte[] COLOR_MAP_ADD      = new byte[0x10000];
    public static final byte[] COLOR_MAP_SUBTRACT = new byte[0x10000];
    public static final byte[] COLOR_MAP_MULTIPLY = new byte[0x10000];
    public static final byte[] COLOR_MAP_SPECULAR = new byte[0x10000];

    public static final byte COLOR_TRANSPARENT = 0;

    static {
        {
            MCSDBubbleFormat bubbleData = new MCSDBubbleFormat();
            boolean success = false;
            try {
                String bub_path = "/org/inventivetalent/mapmanager/util/map/";

                if (Bukkit.getVersion().contains("1.12") || Bukkit.getVersion().contains("1.13")) {
                    bub_path += "map_1_12.ab";
                } else {
                    bub_path += "map_1_8_8.ab";
                }

                InputStream input = MapColorPalette.class.getResourceAsStream(bub_path);
                if (input == null) {
					System.err.println("Missing data file " + bub_path);
                } else {
                    bubbleData.readFrom(input);
					System.out.println(bubbleData);
                    success = true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            if (success) {
                COLOR_MAP_DATA.readFrom(bubbleData);
            } else {
                MCSDGenBukkit bukkitGen = new MCSDGenBukkit();
                bukkitGen.generate();
                COLOR_MAP_DATA.readFrom(bukkitGen);
            }
        }

        for (int a = 0; a < 256; a++) {
            int index = (a * 256);
            Color color_a = getRealColor((byte) a);
            if (color_a.getAlpha() < 128) {
                Arrays.fill(COLOR_MAP_SPECULAR, index, index + 256, COLOR_TRANSPARENT);
            } else {
                for (int b = 0; b < 256; b++) {
                    float f = (float) b / 128.0f;
                    int sr = (int) ((float) color_a.getRed() * f);
                    int sg = (int) ((float) color_a.getGreen() * f);
                    int sb = (int) ((float) color_a.getBlue() * f);
                    COLOR_MAP_SPECULAR[index++] = getColor(sr, sg, sb);
                }
            }
        }

        for (int c1 = 0; c1 < 256; c1++) {
            for (int c2 = 0; c2 < 256; c2++) {
                initTable((byte) c1, (byte) c2);
            }
        }
    }


    private static void initTable(byte color1, byte color2) {
        int index = getMapIndex(color1, color2);
        if (isTransparent(color1) || isTransparent(color2)) {
            initTransparent(index, color1, color2);
        } else {
            Color c1 = getRealColor(color1);
            Color c2 = getRealColor(color2);
            initColor(
                    index,
                    c1.getRed(), c1.getGreen(), c1.getBlue(),
                    c2.getRed(), c2.getGreen(), c2.getBlue()
            );
        }
    }

    private static void initTransparent(int index, byte color1, byte color2) {
        COLOR_MAP_AVERAGE[index] = color2;
        COLOR_MAP_ADD[index] = color2;
        COLOR_MAP_SUBTRACT[index] = color2;
        COLOR_MAP_MULTIPLY[index] = (byte) 0;
    }

    private static void initColor(int index, int r1, int g1, int b1, int r2, int g2, int b2) {
        initArray(COLOR_MAP_AVERAGE,  index, (r1 + r2) >> 1, (g1 + g2) >> 1, (b1 + b2) >> 1);
        initArray(COLOR_MAP_ADD,      index, (r1 + r2),      (g1 + g2),      (b1 + b2));
        initArray(COLOR_MAP_SUBTRACT, index, (r2 - r1),      (g2 - g1),      (b2 - b1));
        initArray(COLOR_MAP_MULTIPLY, index, (r1 * r2) / 255, (g1 * g2) / 255, (b1 * b2) / 255);
    }

    private static void initArray(byte[] array, int index, int r, int g, int b) {
        if (r < 0x00) r = 0x00;
        if (r > 0xFF) r = 0xFF;
        if (g < 0x00) g = 0x00;
        if (g > 0xFF) g = 0xFF;
        if (b < 0x00) b = 0x00;
        if (b > 0xFF) b = 0xFF;
        array[index] = getColor(r, g, b);
    }


    public static boolean isTransparent(byte color) {
        return (color & 0xFF) < 0x4;
    }

    public static byte getColor(Color color) {
        if ((color.getAlpha() & 0x80) == 0) {
            return COLOR_TRANSPARENT;
        } else {
            return COLOR_MAP_DATA.get(color.getRed(), color.getGreen(), color.getBlue());
        }
    }


    public static byte getColor(int r, int g, int b) {
        if (r < 0)
            r = 0;
        else if (r > 255)
            r = 255;
        if (g < 0)
            g = 0;
        else if (g > 255)
            g = 255;
        if (b < 0)
            b = 0;
        else if (b > 255)
            b = 255;

        return COLOR_MAP_DATA.get(r, g, b);
    }

    public static final int getMapIndex(byte color_a, byte color_b) {
        return (color_a & 0xFF) | ((color_b & 0xFF) << 8);
    }

    public static final Color getRealColor(byte color) {
        return COLOR_MAP_DATA.getColor(color);
    }

}