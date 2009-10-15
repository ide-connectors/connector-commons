package com.atlassian.theplugin.commons.crucible.api;

// @todo ideally byte arrays should be replaced by input streams
public class UploadItem {
	private final String fileName;
	private final byte[] oldContent;
	private final byte[] newContent;

	/**
	 *
	 * @param fileName
	 * @param oldContent
	 * @param newContent
	 */
	public UploadItem(final String fileName, final byte[] oldContent, final byte[] newContent) {
		this.fileName = fileName;
		this.oldContent = oldContent;
		this.newContent = newContent;
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
}
