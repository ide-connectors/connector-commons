package com.atlassian.theplugin.commons.crucible.api.content;

/**
 * User: mwent
 * Date: Mar 13, 2009
 * Time: 11:55:23 AM
 */
public class ReviewFileContent {
	private final byte[] content;
    private boolean revisionOnLocalFS = false;

    public ReviewFileContent(final byte[] content, final boolean revisionOnLocalFS) {
		this.content = content;
        this.revisionOnLocalFS = revisionOnLocalFS;
	}

	public byte[] getContent() {
		return content;
	}

    public boolean isRevisionOnLocalFS() {
        return revisionOnLocalFS;
    }
}