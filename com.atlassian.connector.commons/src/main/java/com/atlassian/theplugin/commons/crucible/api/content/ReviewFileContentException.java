package com.atlassian.theplugin.commons.crucible.api.content;

/**
 * User: mwent
 * Date: Mar 13, 2009
 * Time: 12:57:37 PM
 */
public class ReviewFileContentException extends Exception {
	public ReviewFileContentException() {
	}

	public ReviewFileContentException(final String message) {
		super(message);
	}

	public ReviewFileContentException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ReviewFileContentException(final Throwable cause) {
		super(cause);
	}
}
