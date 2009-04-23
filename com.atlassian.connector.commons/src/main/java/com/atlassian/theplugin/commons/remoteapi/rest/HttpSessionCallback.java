package com.atlassian.theplugin.commons.remoteapi.rest;

import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;

/**
 * Interface for the callback used by AbstractHttpSession for HttpClient setup
 *
 * @author Shawn Minto
 */
public interface HttpSessionCallback {

	HttpClient getHttpClient(ServerData server) throws HttpProxySettingsException;

	void configureHttpMethod(AbstractHttpSession session, HttpMethod method);

}
