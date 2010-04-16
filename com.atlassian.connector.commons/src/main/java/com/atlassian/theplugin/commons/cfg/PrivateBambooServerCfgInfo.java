package com.atlassian.theplugin.commons.cfg;

public class PrivateBambooServerCfgInfo extends PrivateServerCfgInfo {
	private final int timezoneOffset;

	public PrivateBambooServerCfgInfo(final ServerIdImpl serverId, final boolean enabled, final boolean useDefaultCredentials,
			final String username,
			final String password, final int timezoneOffset,
            final boolean useHttpBasic,
            final String basicUsername,
            final String basicPassword) {
		super(serverId, enabled, useDefaultCredentials, username, password, useHttpBasic, basicUsername, basicPassword);
		this.timezoneOffset = timezoneOffset;
	}

	public int getTimezoneOffset() {
		return timezoneOffset;
	}
}
