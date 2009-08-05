package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.connector.commons.api.ConnectionCfg;

public interface CrucibleUserCache {
	User getUser(ConnectionCfg server, String userId, boolean fetchIfNotExist);

	void addUser(ConnectionCfg server, User user);
}
