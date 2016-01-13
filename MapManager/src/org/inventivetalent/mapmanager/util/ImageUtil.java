/*
 * Copyright 2013-2015 Marvin Schäfer (inventivetalent). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package org.inventivetalent.mapmanager.util;

import org.inventivetalent.mapmanager.ArrayImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * © Copyright 2015 inventivetalent
 *
 * @author inventivetalent
 */
public class ImageUtil {

	//	/**
	//	 * Modified Method from http://kalanir.blogspot.de/2010/02/how-to-split-image-into-chunks-java.html
	//	 */
	//	public static BufferedImage[][] splitImage(final BufferedImage image, final int rows, final int columns) throws IOException {
	//		int chunkWidth = image.getWidth() / rows; // determines the chunk width and height
	//		int chunkHeight = image.getHeight() / columns;
	//
	//		BufferedImage[][] images = new BufferedImage[columns][rows];
	//		for (int x = 0; x < columns; x++) {
	//			for (int y = 0; y < rows; y++) {
	//				// Initialize the image array with image chunks
	//				images[x][y] = new BufferedImage(chunkWidth, chunkHeight, image.getType());
	//
	//				// draws the image chunk
	//				Graphics2D gr = images[x][y].createGraphics();
	//				gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
	//				gr.dispose();
	//			}
	//		}
	//		return images;
	//	}

	/**
	 * Modified Method from http://kalanir.blogspot.de/2010/02/how-to-split-image-into-chunks-java.html
	 */
	public static ArrayImage[][] splitImage(final BufferedImage image, final int rows, final int columns) throws IOException {
		int chunkWidth = image.getWidth() / rows; // determines the chunk width and height
		int chunkHeight = image.getHeight() / columns;

		ArrayImage[][] images = new ArrayImage[columns][rows];
		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				// Initialize the image array with image chunks
				BufferedImage raw = new BufferedImage(chunkWidth, chunkHeight, image.getType());

				// draws the image chunk
				Graphics2D gr = raw.createGraphics();
				gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
				gr.dispose();

				images[x][y] = new ArrayImage(raw);
			}
		}
		return images;
	}

	public static boolean ImageContentEqual(BufferedImage b1, BufferedImage b2) {
		if (b1 == null || b2 == null) { return false; }
		// if (b1.equals(b2)) return true;
		if (b1.getWidth() != b2.getWidth()) { return false; }
		if (b1.getHeight() != b2.getHeight()) { return false; }
		for (int y = 0; y < b1.getHeight(); y++) {
			for (int x = 0; x < b1.getWidth(); x++) {
				if (b1.getRGB(x, y) != b2.getRGB(x, y)) { return false; }
			}
		}
		return true;
	}

	public static boolean ImageContentEqual(ArrayImage b1, ArrayImage b2) {
		if (b1 == null || b2 == null) { return false; }
		// if (b1.equals(b2)) return true;
		if (b1.getWidth() != b2.getWidth()) { return false; }
		if (b1.getHeight() != b2.getHeight()) { return false; }
		for (int y = 0; y < b1.getHeight(); y++) {
			for (int x = 0; x < b1.getWidth(); x++) {
				if (b1.getRGB(x, y) != b2.getRGB(x, y)) { return false; }
			}
		}
		return true;
	}

	public static boolean isValidImage(String name) {
		switch (name.substring(name.lastIndexOf("."))) {
			case ".gif":
				return true;
			case ".png":
				return true;
			case ".jpg":
				return true;
			case ".jpeg":
				return true;
			default:
				return false;
		}
	}

	public static int[][] ImageToArray(BufferedImage image) {
		int[][] array = new int[image.getWidth()][image.getHeight()];
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				array[x][y] = image.getRGB(x, y);
			}
		}
		return array;
	}

	public static BufferedImage ArrayToImage(int[][] array) {
		BufferedImage image = new BufferedImage(array.length, array[0].length, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				image.setRGB(x, y, array[x][y]);
			}
		}
		return image;
	}

}
