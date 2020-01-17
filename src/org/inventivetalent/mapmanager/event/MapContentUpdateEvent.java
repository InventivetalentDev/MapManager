package org.inventivetalent.mapmanager.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.inventivetalent.mapmanager.ArrayImage;
import org.inventivetalent.mapmanager.wrapper.MapWrapper;

/**
 * Event called when the content of a {@link MapWrapper} is updated
 */
public class MapContentUpdateEvent extends Event {

	private MapWrapper mapWrapper;
	private ArrayImage content;
	private boolean    sendContent;

	public MapContentUpdateEvent(MapWrapper mapWrapper, ArrayImage content) {
		this.mapWrapper = mapWrapper;
		this.content = content;
		this.sendContent = true;
	}

	public MapContentUpdateEvent(MapWrapper mapWrapper, ArrayImage content, boolean async) {
		super(async);
		this.mapWrapper = mapWrapper;
		this.content = content;
		this.sendContent = true;
	}


	/**
	 * @return the updated {@link MapWrapper}
	 */
	public MapWrapper getMapWrapper() {
		return mapWrapper;
	}

	/**
	 * @return the {@link ArrayImage} content
	 */
	public ArrayImage getContent() {
		return content;
	}

	/**
	 * Change the updated content
	 *
	 * @param content new image content
	 */
	public void setContent(ArrayImage content) {
		this.content = content;
	}

	/**
	 * <code>true</code> by default
	 *
	 * @return <code>true</code> if the content will be sent to the {@link org.inventivetalent.mapmanager.manager.MapManager} viewers
	 */
	public boolean isSendContent() {
		return sendContent;
	}

	/**
	 * Change if the content is sent to the viewers
	 *
	 * @param sendContent if <code>true</code>, the content will be sent; if <code>false</code>, the content will be update without sending
	 * @see #isSendContent()
	 */
	public void setSendContent(boolean sendContent) {
		this.sendContent = sendContent;
	}

	private static HandlerList handlerList = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}
}
