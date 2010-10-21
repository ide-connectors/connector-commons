package com.atlassian.theplugin.commons.remoteapi.rest;

import com.atlassian.connector.commons.api.ConnectionCfg;
import org.apache.commons.httpclient.HttpMethod;

/**
 * Default implementation of the {@link HttpSessionCallback}
 *
 * @author Shawn Minto
 */
public abstract class HttpSessionCallbackImpl implements HttpSessionCallback {

	public void configureHttpMethod(AbstractHttpSession session, HttpMethod method) {
		session.adjustHttpHeader(method);
	}

    public void disposeClient(ConnectionCfg server) {

    }

}
