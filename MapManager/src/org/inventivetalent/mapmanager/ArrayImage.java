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

import org.inventivetalent.mapmanager.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.Arrays;

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
}
