package com.atlassian.theplugin.commons.cfg;

public class PrivateBambooServerCfgInfo extends PrivateServerCfgInfo {
	private final int timezoneOffset;

	public PrivateBambooServerCfgInfo(final ServerId serverId, final boolean enabled, final boolean useDefaultCredentials,
			final String username,
			final String password, final int timezoneOffset) {
		super(serverId, enabled, useDefaultCredentials, username, password);
		this.timezoneOffset = timezoneOffset;
	}

	public int getTimezoneOffset() {
		return timezoneOffset;
	}
}
