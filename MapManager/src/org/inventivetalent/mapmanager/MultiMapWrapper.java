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

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;

class MultiMapWrapper extends DefaultMapManager implements MapWrapper {

	private ArrayImage     content;
	private MapWrapper[][] wrapperMatrix;

	private MultiMapController controller = new MultiMapController() {
		@Override
		public void addViewer(final Player player) {
			matrixIterator(new MatrixCallable() {
				@Override
				public void call(MapWrapper wrapper) {
					wrapper.getController().addViewer(player);
				}
			});
		}

		@Override
		public void removeViewer(final OfflinePlayer player) {
			matrixIterator(new MatrixCallable() {
				@Override
				public void call(MapWrapper wrapper) {
					wrapper.getController().removeViewer(player);
				}
			});
		}

		@Override
		public void clearViewers() {
			matrixIterator(new MatrixCallable() {
				@Override
				public void call(MapWrapper wrapper) {
					wrapper.getController().clearViewers();
				}
			});
		}

		@Override
		public boolean isViewing(OfflinePlayer player) {
			for (int x = 0; x < wrapperMatrix.length; x++) {
				for (int y = 0; y < wrapperMatrix[x].length; y++) {
					if (wrapperMatrix[x][y].getController().isViewing(player)) { return true; }
				}
			}
			return false;
		}

		@Override
		public short getMapId(OfflinePlayer player) {
			//We don't have a unique ID
			return -1;
		}

		@Override
		public void update(ArrayImage content) {
			setContent(splitImage(content.toBuffered(), wrapperMatrix[0].length, wrapperMatrix.length));
		}

		@Override
		public void sendContent(final Player player) {
			matrixIterator(new MatrixCallable() {
				@Override
				public void call(MapWrapper wrapper) {
					wrapper.getController().sendContent(player);
				}
			});
		}

		@Override
		public void showInFrames(Player player, int[][] entityIdMatrix) {
			for (int x = 0; x < entityIdMatrix.length; x++) {
				for (int y = 0; y < entityIdMatrix[x].length; y++) {
					wrapperMatrix[y][x].getController().showInFrame(player, entityIdMatrix[x][wrapperMatrix.length - 1 - y]);
				}
			}
		}

		@Override
		public void showInFrames(Player player, int[][] entityIdMatrix, DebugCallable callable) {
			for (int x = 0; x < entityIdMatrix.length; x++) {
				for (int y = 0; y < entityIdMatrix[x].length; y++) {
					wrapperMatrix[y][x].getController().showInFrame(player, entityIdMatrix[x][wrapperMatrix.length - 1 - y], callable.call(wrapperMatrix[y][x].getController(), x, y));
				}
			}
		}

		@Override
		public void showInFrames(Player player, ItemFrame[][] itemFrameMatrix, boolean force) {
			for (int x = 0; x < itemFrameMatrix.length; x++) {
				for (int y = 0; y < itemFrameMatrix[x].length; y++) {
					wrapperMatrix[y][x].getController().showInFrame(player, itemFrameMatrix[x][wrapperMatrix.length - 1 - y], force);
				}
			}
		}

		@Override
		public void clearFrames(Player player, int[][] entityIdMatrix) {
			for (int x = 0; x < entityIdMatrix.length; x++) {
				for (int y = 0; y < entityIdMatrix[x].length; y++) {
					wrapperMatrix[y][x].getController().clearFrame(player, entityIdMatrix[x][y]);
				}
			}
		}

		@Override
		public void clearFrames(Player player, ItemFrame[][] itemFrameMatrix) {
			for (int x = 0; x < itemFrameMatrix.length; x++) {
				for (int y = 0; y < itemFrameMatrix[x].length; y++) {
					wrapperMatrix[y][x].getController().clearFrame(player, itemFrameMatrix[x][y]);
				}
			}
		}

		@Override
		public void showInFrames(Player player, ItemFrame[][] itemFrameMatrix) {
			showInFrames(player, itemFrameMatrix, false);
		}

		@Override
		public void showInInventory(Player player, int slot, boolean force) {
			throw new UnsupportedOperationException("cannot show multi-map in inventory");
		}

		@Override
		public void showInInventory(Player player, int slot) {
			throw new UnsupportedOperationException("cannot show multi-map in inventory");
		}

		@Override
		public void showInHand(Player player, boolean force) {
			throw new UnsupportedOperationException("cannot show multi-map in inventory");
		}

		@Override
		public void showInHand(Player player) {
			throw new UnsupportedOperationException("cannot show multi-map in inventory");
		}

		@Override
		public void showInFrame(Player player, int entityId) {
			throw new UnsupportedOperationException("cannot show multi-map in single frame");
		}

		@Override
		public void showInFrame(Player player, ItemFrame frame, boolean force) {
			throw new UnsupportedOperationException("cannot show multi-map in single frame");
		}

		@Override
		public void showInFrame(Player player, ItemFrame frame) {
			throw new UnsupportedOperationException("cannot show multi-map in single frame");
		}

		@Override
		public void showInFrame(Player player, int entityId, String debugInfo) {
			throw new UnsupportedOperationException("cannot show multi-map in single frame");
		}

		@Override
		public void clearFrame(Player player, int entityId) {
			throw new UnsupportedOperationException("cannot clear multi-map in single frame");
		}

		@Override
		public void clearFrame(Player player, ItemFrame frame) {
			throw new UnsupportedOperationException("cannot clear multi-map in single frame");
		}
	};

	public MultiMapWrapper(BufferedImage image, int columns, int rows) {
		this(splitImage(image, rows, columns));
		this.content = new ArrayImage(image);
	}

	public MultiMapWrapper(ArrayImage image, int columns, int rows) {
		this(splitImage(image.toBuffered(), rows, columns));
		this.content = image;
	}

	public MultiMapWrapper(ArrayImage[][] imageMatrix) {
		this((Object[][]) imageMatrix);
	}

	public MultiMapWrapper(BufferedImage[][] imageMatrix) {
		this((Object[][]) imageMatrix);
	}

	private MultiMapWrapper(Object[][] imageMatrix) {
		setContent(imageMatrix);
	}

	@Override
	public MultiMapController getController() {
		return this.controller;
	}

	@Override
	public ArrayImage getContent() {
		return this.content;
	}

	public void unwrap() {
		matrixIterator(new MatrixCallable() {
			@Override
			public void call(MapWrapper wrapper) {
				MapManagerPlugin.instance.getMapManager().unwrapImage(wrapper);
			}
		});
	}

	protected void setContent(Object[][] imageMatrix) {
		wrapperMatrix = new MapWrapper[imageMatrix.length][imageMatrix[0].length];
		for (int x = 0; x < imageMatrix.length; x++) {
			if (imageMatrix[x].length != imageMatrix[0].length) { throw new IllegalArgumentException("image is not rectangular"); }
			for (int y = 0; y < imageMatrix[x].length; y++) {
				Object object = imageMatrix[x][y];
				if (object == null) {
					throw new IllegalArgumentException("null element in image array");
				} else if (object instanceof BufferedImage) {
					wrapperMatrix[x][y] = MapManagerPlugin.instance.getMapManager().wrapImage((BufferedImage) object);
				} else if (object instanceof ArrayImage) {
					wrapperMatrix[x][y] = MapManagerPlugin.instance.getMapManager().wrapImage((ArrayImage) object);
				}
			}
		}
	}

	protected void matrixIterator(MatrixCallable callable) {
		for (int x = 0; x < wrapperMatrix.length; x++) {
			for (int y = 0; y < wrapperMatrix[x].length; y++) {
				callable.call(wrapperMatrix[x][y]);
			}
		}
	}

	static ArrayImage[][] splitImage(final BufferedImage image, final int rows, final int columns) {
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

	interface MatrixCallable {
		void call(MapWrapper wrapper);
	}
}
