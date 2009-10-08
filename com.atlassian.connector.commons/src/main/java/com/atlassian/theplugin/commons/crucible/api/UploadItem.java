package com.atlassian.theplugin.commons.crucible.api;

// @todo ideally byte arrays should be replaced by input streams
public class UploadItem {
	private final String fileName;
	private final byte[] oldContent;
	private final byte[] newContent;
	private final String oldRevision;

	/**
	 *
	 * @param fileName
	 * @param oldContent
	 * @param newContent
	 * @param oldRevision
	 *            ignored now by ACC and Crucible as well
	 */
	public UploadItem(final String fileName, final byte[] oldContent, final byte[] newContent, final String oldRevision) {
		this.fileName = fileName;
		this.oldContent = oldContent;
		this.newContent = newContent;
		this.oldRevision = oldRevision;
	}

	public String getFileName() {
		return fileName;
	}

	public byte[] getOldContent() {
		return oldContent;
	}

	public byte[] getNewContent() {
		return newContent;
	}

	public String getOldRevision() {
		return oldRevision;
	}
}
