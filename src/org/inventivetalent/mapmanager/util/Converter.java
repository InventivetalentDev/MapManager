package org.inventivetalent.mapmanager.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Converter {

    public static byte[] imageToBytes(Image image) {

        BufferedImage temp = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = temp.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        int[] pixels = new int[temp.getWidth() * temp.getHeight()];
        temp.getRGB(0, 0, temp.getWidth(), temp.getHeight(), pixels, 0, temp.getWidth());

        byte[] result = new byte[temp.getWidth() * temp.getHeight()];
        for (int i = 0; i < pixels.length; i++) {
            result[i] = MapColorPalette.getColor(new Color(pixels[i], true));
        }

        return result;

    }
}
