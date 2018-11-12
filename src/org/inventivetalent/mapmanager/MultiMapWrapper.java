package org.inventivetalent.mapmanager;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.inventivetalent.mapmanager.controller.MultiMapController;
import org.inventivetalent.mapmanager.wrapper.MapWrapper;
import org.inventivetalent.mapmanager.wrapper.MultiWrapper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

class MultiMapWrapper extends DefaultMapWrapper implements MapWrapper, MultiWrapper {

	private ArrayImage     content;
	private MapWrapper[][] wrapperMatrix;
	private Set<UUID>      viewerIds = new HashSet<>();

	private MultiMapController controller = new MultiMapController() {
		@Override
		public void addViewer(final Player player) {
			if (!viewerIds.contains(player.getUniqueId())) {
				matrixIterator(new MatrixCallable() {
					@Override
					public void call(MapWrapper wrapper) {
						wrapper.getController().addViewer(player);
					}
				});
				viewerIds.add(player.getUniqueId());
			}
		}

		@Override
		public void removeViewer(final OfflinePlayer player) {
			matrixIterator(new MatrixCallable() {
				@Override
				public void call(MapWrapper wrapper) {
					wrapper.getController().removeViewer(player);
				}
			});
			viewerIds.remove(player.getUniqueId());
		}

		@Override
		public void clearViewers() {
			matrixIterator(new MatrixCallable() {
				@Override
				public void call(MapWrapper wrapper) {
					wrapper.getController().clearViewers();
				}
			});
			viewerIds.clear();
		}

		@Override
		public boolean isViewing(OfflinePlayer player) {
			return viewerIds.contains(player.getUniqueId());
			//			for (int x = 0; x < wrapperMatrix.length; x++) {
			//				for (int y = 0; y < wrapperMatrix[x].length; y++) {
			//					if (wrapperMatrix[x][y].getController().isViewing(player)) { return true; }
			//				}
			//			}
			//			return false;
		}

		@Override
		public short getMapId(OfflinePlayer player) {
			//We don't have a unique ID
			return -1;
		}

		@Override
		@Deprecated
		public void update(ArrayImage content) {
			ArrayImage[][] split = splitImage(content.toBuffered(), wrapperMatrix[0].length, wrapperMatrix.length);
			for (int x = 0; x < wrapperMatrix.length; x++) {
				for (int y = 0; y < wrapperMatrix[x].length; y++) {
					wrapperMatrix[x][y].getController().update(split[x][y]);
				}
			}
		}

		@Override
		public void update(BufferedImage content) {
			MultiMapWrapper.this.content = new ArrayImage(content);

			ArrayImage[][] split = splitImage(content, wrapperMatrix[0].length, wrapperMatrix.length);
			for (int x = 0; x < wrapperMatrix.length; x++) {
				for (int y = 0; y < wrapperMatrix[x].length; y++) {
					wrapperMatrix[x][y].getController().update(split[x][y]);
				}
			}
		}

		@Override
		public ArrayImage getContent() {
			return content;
		}

		@Override
		public void sendContent(Player player) {
			sendContent(player, false);
		}

		@Override
		public void sendContent(final Player player, final boolean withoutQueue) {
			matrixIterator(new MatrixCallable() {
				@Override
				public void call(MapWrapper wrapper) {
					wrapper.getController().sendContent(player, withoutQueue);
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

	public MultiMapWrapper(BufferedImage image, int rows, int columns) {
		this(splitImage(image, columns, rows));
		//		this.content = new ArrayImage(image);
	}

	@Deprecated
	public MultiMapWrapper(ArrayImage image, int rows, int columns) {
		this(splitImage(image.toBuffered(), columns, rows));
		//		this.content = image;
	}

	public MultiMapWrapper(ArrayImage[][] imageMatrix) {
		this((Object[][]) imageMatrix);
	}

	public MultiMapWrapper(BufferedImage[][] imageMatrix) {
		this((Object[][]) imageMatrix);
	}

	private MultiMapWrapper(Object[][] imageMatrix) {
		super(null);
		setContent(imageMatrix);
	}

	@Override
	public MultiMapController getController() {
		return this.controller;
	}

	@Override
	@Deprecated
	public ArrayImage getContent() {
		return this.content;
	}

	@Override
	public ArrayImage[][] getMultiContent() {
		ArrayImage[][] images = new ArrayImage[wrapperMatrix.length][wrapperMatrix[0].length];

		for (int x = 0; x < wrapperMatrix.length; x++) {
			if (wrapperMatrix[x].length != wrapperMatrix[0].length) { throw new IllegalArgumentException("image is not rectangular"); }
			for (int y = 0; y < wrapperMatrix[x].length; y++) {
				images[x][y] = wrapperMatrix[x][y].getContent();
			}
		}

		return images;
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

	/**
	 * Modified Method from http://kalanir.blogspot.de/2010/02/how-to-split-image-into-chunks-java.html
	 */
	static ArrayImage[][] splitImage(final BufferedImage image, final int columns, final int rows) {
		int chunkWidth = image.getWidth() / columns; // determines the chunk width and height
		int chunkHeight = image.getHeight() / rows;

		ArrayImage[][] images = new ArrayImage[rows][columns];
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				// Initialize the image array with image chunks
				BufferedImage raw = new BufferedImage(chunkWidth, chunkHeight, image.getType());

				// draws the image chunk
				Graphics2D gr = raw.createGraphics();
				gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
				gr.dispose();

				images[x][y] = new ArrayImage(raw);
				raw.flush();
			}
		}
		return images;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	interface MatrixCallable {
		void call(MapWrapper wrapper);
	}
}
