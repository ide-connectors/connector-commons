package com.atlassian.theplugin.commons.fisheye;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.fisheye.api.rest.FishEyeRestSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;

import java.util.Collection;

/**
 * User: pmaruszak
 */
public class FishEyeServerFacadeImpl implements FishEyeServerFacade {
	private static FishEyeServerFacadeImpl instance;
	private HttpSessionCallback callback;

	protected FishEyeServerFacadeImpl() {
		this.callback = new HttpSessionCallbackImpl();
	}

	public void testServerConnection(ServerData serverCfg) throws RemoteApiException {
		FishEyeSession fishEyeSession = getSession(serverCfg);
		fishEyeSession.login(serverCfg.getUserName(), serverCfg.getPassword().toCharArray());

		// well, we need to call _something_ to see if it worked, in case of anonymous access
		fishEyeSession.getRepositories();

		fishEyeSession.logout();
	}

	public ServerType getServerType() {
		return ServerType.FISHEYE_SERVER;
	}

	public static synchronized FishEyeServerFacadeImpl getInstance() {
		if (instance == null) {
			instance = new FishEyeServerFacadeImpl();
		}

		return instance;
	}

	/**
	 * For testing Only
	 *
	 * @param url
	 * @return
	 * @throws RemoteApiMalformedUrlException
	 */
	public FishEyeSession getSession(String url) throws RemoteApiMalformedUrlException {
		ServerData serverCfg = new ServerData((new ServerId()).toString(), "", "", "", url);
		return new FishEyeRestSession(serverCfg, callback);

	}

	public FishEyeSession getSession(ServerData server) throws RemoteApiMalformedUrlException {
		return new FishEyeRestSession(server, callback);

	}

	public Collection<String> getRepositories(final ServerData server) throws RemoteApiException {
		FishEyeSession fishEyeSession = getSession(server);
		Collection<String> repositories;

		fishEyeSession.login(server.getUserName(), server.getPassword().toCharArray());
		repositories = fishEyeSession.getRepositories();
		fishEyeSession.logout();
		return repositories;
	}

	public void setCallback(HttpSessionCallback callback) {
		this.callback = callback;
	}
}
