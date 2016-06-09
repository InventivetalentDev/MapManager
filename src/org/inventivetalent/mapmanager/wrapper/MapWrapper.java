package org.inventivetalent.mapmanager.wrapper;

import org.inventivetalent.mapmanager.ArrayImage;
import org.inventivetalent.mapmanager.controller.MapController;

public interface MapWrapper {

	/**
	 * Get this {@link MapWrapper}'s {@link MapController}
	 *
	 * @return the {@link MapController}
	 */
	MapController getController();

	/**
	 * Get the content of this wrapper
	 *
	 * @return the {@link ArrayImage} content
	 */
	ArrayImage getContent();

}

