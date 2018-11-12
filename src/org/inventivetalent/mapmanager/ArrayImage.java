package org.inventivetalent.mapmanager;

import com.google.common.primitives.Ints;
import org.inventivetalent.mapmanager.util.Converter;
import org.inventivetalent.mapmanager.util.MapColorPalette;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Container class for images
 * <p>
 * Stores colors as an integer array
 */
public class ArrayImage {

	protected byte[] array;

	private int width;
	private int height;

	protected int minX = 0;
	protected int minY = 0;
	protected int maxX = 128;
	protected int maxY = 128;

	private int imageType = BufferedImage.TYPE_4BYTE_ABGR;

	protected ArrayImage(byte[] data) {
		this.array = data;
	}

	/**
	 * Convert a {@link BufferedImage} to an ArrayImage
	 *
	 * @param image image to convert
	 */
	public ArrayImage(BufferedImage image) {
		this.imageType = image.getType();

		this.width = image.getWidth();
		this.height = image.getHeight();

		this.array = Converter.imageToBytes(image);
	}

	/**
	 * @return the width of the image
	 */
	@Deprecated
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height of the image
	 */
	@Deprecated
	public int getHeight() {
		return height;
	}

	/**
	 * Convert this image back to a {@link BufferedImage}
	 *
	 * @return new {@link BufferedImage}
	 */
	@Deprecated
	public BufferedImage toBuffered() {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), this.imageType);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, MapColorPalette.getRealColor(array[y * getWidth() + x]).getRGB());
			}
		}
		return image;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		ArrayImage that = (ArrayImage) o;

		if (width != that.width) { return false; }
		if (height != that.height) { return false; }
		return Arrays.equals(array, that.array);

	}

	@Override
	public int hashCode() {
		int result = array != null ? Arrays.hashCode(array) : 0;
		result = 31 * result + width;
		result = 31 * result + height;
		return result;
	}

	public static void writeToStream(ArrayImage image, OutputStream outputStream) throws IOException {
		outputStream.write(Ints.toByteArray(image.array.length));

		outputStream.write(image.array);
	}

	public static ArrayImage readFromStream(InputStream inputStream) throws IOException {
		byte[] lengthBytes = new byte[4];
		inputStream.read(lengthBytes, 0, 4);

		int length = Ints.fromByteArray(lengthBytes);

		byte[] data = new byte[length];
		inputStream.read(data);

		ArrayImage image = new ArrayImage(data);

		return image;
	}

	public static void writeMultiToSream(ArrayImage[][] images, OutputStream outputStream) throws IOException {
		outputStream.write(Ints.toByteArray(images.length));// width
		outputStream.write(Ints.toByteArray(images[0].length));// height

		for (int x = 0; x < images.length; x++) {
			if (images[x].length != images[0].length) { throw new IllegalArgumentException("image is not rectangular"); }
			for (int y = 0; y < images[x].length; y++) {
				outputStream.write(Ints.toByteArray(x));
				outputStream.write(Ints.toByteArray(y));

				writeToStream(images[x][y], outputStream);
			}
		}
	}

	public static ArrayImage[][] readMultiFromStream(InputStream inputStream) throws IOException {
		byte[] widthBytes = new byte[4];
		byte[] heightBytes = new byte[4];
		inputStream.read(widthBytes, 0, 4);
		inputStream.read(heightBytes, 0, 4);

		int width = Ints.fromByteArray(widthBytes);
		int height = Ints.fromByteArray(heightBytes);

		ArrayImage[][] images = new ArrayImage[width][height];

		byte[] xBytes = new byte[4];
		byte[] yBytes = new byte[4];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				inputStream.read(xBytes, 0, 4);
				inputStream.read(yBytes, 0, 4);

				int actualX = Ints.fromByteArray(xBytes);
				int actualY = Ints.fromByteArray(yBytes);

				images[actualX][actualY] = readFromStream(inputStream);
			}
		}

		return images;
	}

}
