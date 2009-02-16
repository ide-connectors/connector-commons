package com.atlassian.theplugin.commons.fisheye;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.FishEyeServer;
import com.atlassian.theplugin.commons.cfg.FishEyeServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.fisheye.api.rest.FishEyeRestSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
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
		
	public void testServerConnection(ServerCfg serverCfg) throws RemoteApiException {
		assert serverCfg instanceof FishEyeServerCfg;
		FishEyeSession fishEyeSession = getSession((FishEyeServerCfg) serverCfg);
		fishEyeSession.login(serverCfg.getUsername(), serverCfg.getPassword().toCharArray());

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
		FishEyeServerCfg serverCfg = new FishEyeServerCfg(url, new ServerId());
		serverCfg.setUrl(url);
		return new FishEyeRestSession(serverCfg, callback);

	}
	
	public FishEyeSession getSession(FishEyeServer server) throws RemoteApiMalformedUrlException {
		return new FishEyeRestSession(server, callback);

	}

	public Collection<String> getRepositories(final FishEyeServer server) throws RemoteApiException {
		FishEyeSession fishEyeSession = getSession(server);
		Collection<String> repositories;
		
		fishEyeSession.login(server.getUsername(), server.getPassword().toCharArray());
		repositories = fishEyeSession.getRepositories();
		fishEyeSession.logout();
		return repositories;
	}
	
	public void setCallback(HttpSessionCallback callback) {
		this.callback = callback;
	}
}
