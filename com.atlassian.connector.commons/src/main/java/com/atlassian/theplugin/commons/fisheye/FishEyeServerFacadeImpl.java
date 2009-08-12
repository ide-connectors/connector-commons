package com.atlassian.theplugin.commons.fisheye;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.fisheye.FishEyeServerFacade2;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.fisheye.api.rest.FishEyeRestSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;

import java.util.Collection;

/**
 * User: pmaruszak
 */
public class FishEyeServerFacadeImpl implements FishEyeServerFacade2 {
	private static FishEyeServerFacadeImpl instance;
	private final HttpSessionCallback callback;

	public FishEyeServerFacadeImpl(HttpSessionCallback callback) {
		this.callback = callback;
	}

	public void testServerConnection(ConnectionCfg serverCfg) throws RemoteApiException {
		FishEyeSession fishEyeSession = getSession(serverCfg);
		fishEyeSession.login(serverCfg.getUserName(), serverCfg.getPassword().toCharArray());

		// well, we need to call _something_ to see if it worked, in case of anonymous access
		fishEyeSession.getRepositories();

		fishEyeSession.logout();
	}

	public ServerType getServerType() {
		return ServerType.FISHEYE_SERVER;
	}

	public static synchronized FishEyeServerFacadeImpl getInstance(HttpSessionCallback callback) {
		if (instance == null) {
			instance = new FishEyeServerFacadeImpl(callback);
		}

		return instance;
	}

	public FishEyeSession getSession(ConnectionCfg server) throws RemoteApiMalformedUrlException {
		return new FishEyeRestSession(server, callback);

	}

	public Collection<String> getRepositories(final ConnectionCfg server) throws RemoteApiException {
		FishEyeSession fishEyeSession = getSession(server);
		Collection<String> repositories;

		fishEyeSession.login(server.getUserName(), server.getPassword().toCharArray());
		repositories = fishEyeSession.getRepositories();
		fishEyeSession.logout();
		return repositories;
	}
}
