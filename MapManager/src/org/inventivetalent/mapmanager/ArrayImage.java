package org.inventivetalent.mapmanager;

import org.inventivetalent.mapmanager.util.ImageUtil;

import java.awt.image.BufferedImage;

public class ArrayImage {

	private int[] array;
	private int   width;
	private int   height;

	public ArrayImage(BufferedImage image) {
		this.width = image.getWidth();
		this.height = image.getHeight();
		int[][] intArray = ImageUtil.ImageToArray(image);
		int length = width * height;
		this.array = new int[length];
		for (int x = 0; x < intArray.length; x++) {
			for (int y = 0; y < intArray[x].length; y++) {
				array[y * image.getWidth() + x] = intArray[x][y];
			}
		}
	}

	public ArrayImage(int[][] data) {
		this.array = new int[data.length * data[0].length];
		for (int x = 0; x < data.length; x++) {
			for (int y = 0; y < data[x].length; y++) {
				array[y * data.length + x] = data[x][y];
			}
		}
	}

	public int getRGB(int x, int y) {
		return array[y * width + x];
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
