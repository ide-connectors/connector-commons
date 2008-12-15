package com.atlassian.theplugin.commons.remoteapi.rest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;

import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;

/**
 * Interface for the callback used by AbstractHttpSession for HttpClient setup
 * 
 * @author Shawn Minto
 *
 */
public interface HttpSessionCallback {

	HttpClient getHttpClient(Server server) throws HttpProxySettingsException;

	void configureHttpMethod(AbstractHttpSession session, HttpMethod method);
	
}
