package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.theplugin.commons.remoteapi.ServerData;

public interface CrucibleUserCache {
	User getUser(ServerData server, String userId, boolean fetchIfNotExist);

	void addUser(ServerData server, User user);
}
