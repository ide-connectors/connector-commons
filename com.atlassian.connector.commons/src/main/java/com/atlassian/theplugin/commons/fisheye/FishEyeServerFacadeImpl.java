package com.atlassian.theplugin.commons.fisheye;

import java.util.Collection;

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

/**
 * User: pmaruszak
 */
public class FishEyeServerFacadeImpl implements FishEyeServerFacade {
	private static FishEyeServerFacadeImpl instance;
	private HttpSessionCallback callback;
	
	protected FishEyeServerFacadeImpl() {
		this.callback = new HttpSessionCallbackImpl();
	}
		
	/**
     * For testing Only
     * @see com.atlassian.theplugin.commons.remoteapi.ProductServerFacade#testServerConnection(java.lang.String,
	 * java.lang.String, java.lang.String)
     */
    public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
    	FishEyeServerCfg serverCfg = new FishEyeServerCfg(url, new ServerId());
    	serverCfg.setUrl(url);
    	serverCfg.setUsername(userName);
    	serverCfg.setPassword(password);
    	testServerConnection(serverCfg);
    }
	
	public void testServerConnection(ServerCfg serverCfg) throws RemoteApiException {
		assert serverCfg instanceof FishEyeServerCfg;
		FishEyeSession fishEyeSession = getSession((FishEyeServerCfg) serverCfg);
		fishEyeSession.login(serverCfg.getUsername(), serverCfg.getPassword().toCharArray());
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
	 * @param server
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
