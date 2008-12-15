package com.atlassian.theplugin.commons.remoteapi.rest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;

import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.util.HttpClientFactory;

/**
 * Default implementation of the {@link HttpSessionCallback}
 * 
 * @author Shawn Minto
 */
public class HttpSessionCallbackImpl implements HttpSessionCallback {

	public HttpClient getHttpClient(Server server) throws HttpProxySettingsException {
		return HttpClientFactory.getClient();
	}
	
	public void configureHttpMethod(AbstractHttpSession session, HttpMethod method) {
		session.adjustHttpHeader(method);
	}

}
