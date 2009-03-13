package com.atlassian.theplugin.commons.crucible.api.content;

/**
 * User: mwent
 * Date: Mar 13, 2009
 * Time: 11:55:23 AM
 */
public class ReviewFileContent {
	private final byte[] content;

	public ReviewFileContent(final byte[] content) {
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}
}