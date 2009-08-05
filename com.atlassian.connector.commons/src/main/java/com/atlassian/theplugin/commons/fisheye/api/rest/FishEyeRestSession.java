/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.commons.fisheye.api.rest;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.UrlUtil;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class FishEyeRestSession extends AbstractHttpSession implements FishEyeSession {
	private static final String REST_BASE_URL = "/api/rest/";

	static final String LOGIN_ACTION = REST_BASE_URL + "login";

	static final String LOGOUT_ACTION = REST_BASE_URL + "logout";

	static final String LIST_REPOSITORIES_ACTION = REST_BASE_URL + "repositories";

	private String authToken;

	private boolean loggedIn;

	/**
	 * Public constructor for AbstractHttpSession
	 *
	 * @param server   The server configuration for this session
	 * @param callback The callback needed for preparing HttpClient calls
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException
	 *          for malformed url
	 */
	public FishEyeRestSession(ConnectionCfg server, HttpSessionCallback callback) throws RemoteApiMalformedUrlException {
		super(server, callback);
	}

	@Override
	protected void adjustHttpHeader(final HttpMethod method) {
		// TODO: may be use the same approach as in CrucibleSessionImpl?
	}

	@Override
	protected void preprocessResult(final Document doc) throws JDOMException, RemoteApiSessionExpiredException {
	}

	/**
	 * Login method - use empty both username and password for anonymous access (see PL-931)
	 *
	 * @param name
	 * @param aPassword
	 * @throws RemoteApiLoginException
	 */
	public void login(final String name, char[] aPassword) throws RemoteApiLoginException {

		// anonymous access - see PL-931
		if (StringUtils.isBlank(name) && (aPassword == null || aPassword.length == 0)) {
			loggedIn = true;
			authToken = null;
			return;
		}

		String loginUrl;

		if (name == null || aPassword == null) {
			throw new RemoteApiLoginException("Corrupted configuration. Username or Password null");
		}
		loginUrl = getBaseUrl() + LOGIN_ACTION + "?username=" + UrlUtil.encodeUrl(name) + "&password="
				+ UrlUtil.encodeUrl(String.valueOf(aPassword));

		try {
			Document doc = retrieveGetResponse(loginUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				throw new RemoteApiLoginFailedException(exception);
			}

			@SuppressWarnings("unchecked")
			final List<Element> elements = XPath.newInstance("/response/string").selectNodes(doc);
			if (elements == null || elements.size() == 0) {
				throw new RemoteApiLoginException("Server did not return any authentication token");
			}
			if (elements.size() != 1) {
				throw new RemoteApiLoginException("Server returned unexpected number of authentication tokens ("
						+ elements.size() + ")");
			}
			this.authToken = elements.get(0).getText();
			loggedIn = true;
		} catch (MalformedURLException e) {
			throw new RemoteApiLoginException("Malformed server URL: " + getBaseUrl(), e);
		} catch (UnknownHostException e) {
			throw new RemoteApiLoginException("Unknown host: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new RemoteApiLoginException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new RemoteApiLoginException("Server returned malformed response", e);
		} catch (RemoteApiSessionExpiredException e) {
			throw new RemoteApiLoginException("Session expired", e);
		} catch (IllegalArgumentException e) {
			throw new RemoteApiLoginException("Malformed server URL: " + getBaseUrl(), e);
		}
	}

	private static String getExceptionMessages(Document doc) throws JDOMException {
		if (doc.getRootElement() != null && doc.getRootElement().getName().equals("error")) {
			return doc.getRootElement().getText();
		}

		return null;
	}

	public void logout() {
		if (!isLoggedIn() || authToken == null) {
			return;
		}

		try {
			String logoutUrl = getBaseUrl() + LOGOUT_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);
			retrieveGetResponse(logoutUrl);
		} catch (IOException e) {
			LoggerImpl.getInstance().error("Exception encountered while logout:" + e.getMessage(), e);
		} catch (JDOMException e) {
			LoggerImpl.getInstance().error("Exception encountered while logout:" + e.getMessage(), e);
		} catch (RemoteApiSessionExpiredException e) {
			LoggerImpl.getInstance().debug("Exception encountered while logout:" + e.getMessage(), e);
		}

		authToken = null;
		loggedIn = false;
		client = null;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public List<String> getRepositories() throws RemoteApiException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Calling method without calling login() first");
		}

		String requestUrl = getBaseUrl() + LIST_REPOSITORIES_ACTION;
		try {
			Document doc = retrieveGetResponse(requestUrl);

			XPath xpath = XPath.newInstance("/response/string");
			@SuppressWarnings("unchecked")
			List<Element> elements = xpath.selectNodes(doc);
			List<String> myRepositories = new ArrayList<String>();

			if (elements != null && !elements.isEmpty()) {
				for (Element element : elements) {
					myRepositories.add(element.getText());
				}
			}
			return myRepositories;
		} catch (IOException e) {
			throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
		} catch (JDOMException e) {
			throw new RemoteApiException(getBaseUrl() + ": Server returned malformed response", e);
		}
	}

}
