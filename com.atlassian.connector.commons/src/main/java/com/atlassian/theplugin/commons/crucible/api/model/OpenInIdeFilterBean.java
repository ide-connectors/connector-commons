package com.atlassian.theplugin.commons.crucible.api.model;

public class OpenInIdeFilterBean implements OpenInIdeFilter {
	public String getFilterName() {
		return "Open in IDE";
	}

	public String getFilterUrl() {
		return "";
	}
}
