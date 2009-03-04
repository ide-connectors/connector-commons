package com.atlassian.theplugin.commons.crucible.api;

public class UploadItem {
	private final String fileName;
	private final String oldContent;
	private final String newContent;
	private final String oldRevision;

	public UploadItem(final String fileName, final String oldContent, final String newContent, final String oldRevision) {
		this.fileName = fileName;
		this.oldContent = oldContent;
		this.newContent = newContent;
		this.oldRevision = oldRevision;
	}

	public String getFileName() {
		return fileName;
	}

	public String getOldContent() {
		return oldContent;
	}

	public String getNewContent() {
		return newContent;
	}

	public String getOldRevision() {
		return oldRevision;
	}
}
