// From https://github.com/bergerhealer/BKCommonLib, modified by Joiubaxas#4650
package org.inventivetalent.mapmanager.util;

import java.awt.Color;
import java.util.Arrays;

public class MapColorSpaceData implements Cloneable {
    private final Color[] colors = new Color[256];
    private final byte[] data = new byte[1 << 24];

    public MapColorSpaceData() {
        Arrays.fill(this.colors, new Color(0, 0, 0, 0));
    }


    public final void clearRGBData() {
        Arrays.fill(this.data, (byte) 0);
    }


    public final void clear() {
        Arrays.fill(this.colors, new Color(0, 0, 0, 0));
        Arrays.fill(this.data, (byte) 0);
    }


    public void readFrom(MapColorSpaceData data) {
        System.arraycopy(data.data, 0, this.data, 0, this.data.length);
        System.arraycopy(data.colors, 0, this.colors, 0, this.colors.length);
    }


    public final void setColor(byte code, Color color) {
        this.colors[code & 0xFF] = color;
    }

    public final Color getColor(byte code) {
        return this.colors[code & 0xFF];
    }

    public final void set(int r, int g, int b, byte code) {
        this.data[getDataIndex(r, g, b)] = code;
    }

    public final byte get(int r, int g, int b) {
        return this.data[getDataIndex(r, g, b)];
    }

    public final void set(int index, byte code) {
        this.data[index] = code;
    }

    public final byte get(int index) {
        return this.data[index];
    }

    @Override
    public MapColorSpaceData clone() {
        MapColorSpaceData clone = new MapColorSpaceData();
        System.arraycopy(this.colors, 0, clone.colors, 0, this.colors.length);
        System.arraycopy(this.data, 0, clone.data, 0, this.data.length);
        return clone;
    }

    private static final int getDataIndex(int r, int g, int b) {
        return (r & 0xFF) + ((g & 0xFF) << 8) + ((b & 0xFF) << 16);
    }
}