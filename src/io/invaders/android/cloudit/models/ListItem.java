/**
 * ListItem
 *
 * Custom CloudApp's serializable list item
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package io.invaders.android.cloudit.models;

import java.io.Serializable;
import java.util.Date;

import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;

public class ListItem implements Serializable, CloudAppItem {

	private static final long serialVersionUID = 1L;
	
	enum Type {
	    AUDIO, BOOKMARK, IMAGE, UNKNOWN, VIDEO;
	}
	
	String href;
	public String name;
	public String url;
	public String contentUrl;
	boolean isPrivate;
	boolean isSubscribed;
	boolean isTrashed;
	Type itemType;
	long viewCounter;
	String iconUrl;
	String remoteUrl;
	String redirectUrl;
	String thumbnailUrl;
	String source;
	Date createdAt;
	Date updatedAt;
	Date deletedAt;
	
	public ListItem(CloudAppItem item) throws CloudAppException {
		href = item.getHref();
		name = item.getName();
		url = item.getUrl();
		contentUrl = item.getContentUrl();
		isPrivate = item.isPrivate();
		isSubscribed = item.isSubscribed();
		isTrashed = item.isTrashed();
		try {
			itemType =  Type.valueOf(item.getItemType().toString().toUpperCase());
	    } catch (IllegalArgumentException e) {
	    	itemType = Type.UNKNOWN;
	    }
		viewCounter = item.getViewCounter();
		iconUrl = item.getIconUrl();
		remoteUrl = item.getRemoteUrl();
		redirectUrl = item.getRedirectUrl();
		thumbnailUrl = item.getThumbnailUrl();
		source = item.getSource();
		createdAt = item.getCreatedAt();
		//updatedAt = item.getUpdatedAt();
		//deletedAt = item.getDeletedAt();
	}

	@Override
	public String getContentUrl() throws CloudAppException {
		return contentUrl;
	}

	@Override
	public Date getCreatedAt() throws CloudAppException {
		return createdAt;
	}

	@Override
	public Date getDeletedAt() throws CloudAppException {
		return deletedAt;
	}

	@Override
	public String getHref() throws CloudAppException {
		return href;
	}

	@Override
	public String getIconUrl() throws CloudAppException {
		return iconUrl;
	}

	@Override
	public com.cloudapp.api.model.CloudAppItem.Type getItemType() {
		try {
			return CloudAppItem.Type.valueOf(itemType.toString().toUpperCase());
	    } catch (IllegalArgumentException e) {
	    	return CloudAppItem.Type.UNKNOWN;
	    }
	}

	@Override
	public String getName() throws CloudAppException {
		return name;
	}

	@Override
	public String getRedirectUrl() throws CloudAppException {
		return redirectUrl;
	}

	@Override
	public String getRemoteUrl() throws CloudAppException {
		return remoteUrl;
	}

	@Override
	public String getSource() throws CloudAppException {
		return source;
	}

	@Override
	public String getThumbnailUrl() throws CloudAppException {
		return thumbnailUrl;
	}

	@Override
	public Date getUpdatedAt() throws CloudAppException {
		return updatedAt;
	}

	@Override
	public String getUrl() throws CloudAppException {
		return url;
	}

	@Override
	public long getViewCounter() throws CloudAppException {
		return viewCounter;
	}

	@Override
	public boolean isPrivate() throws CloudAppException {
		return isPrivate;
	}

	@Override
	public boolean isSubscribed() throws CloudAppException {
		return isSubscribed;
	}

	@Override
	public boolean isTrashed() throws CloudAppException {
		return isTrashed;
	}
	
	
	
	

}
