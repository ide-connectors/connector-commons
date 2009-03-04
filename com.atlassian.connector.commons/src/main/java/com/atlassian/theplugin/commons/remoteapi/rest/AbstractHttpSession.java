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

package com.atlassian.theplugin.commons.remoteapi.rest;

import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.util.UrlUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Communication stub for lightweight XML based APIs.
 * This method should be tread-safe (at least it is used in this manner), however
 * I think there are still some issues with thread-safety here [wseliga].
 * E.g. as Server is not immutable then this may be the cause of races
 */
public abstract class AbstractHttpSession {
	protected HttpClient client;
	@NotNull
	private final HttpSessionCallback callback;
	@NotNull
	private final Server server;

	protected String getUsername() {
		return server.getUsername();
	}

	protected String getPassword() {
		return server.getPassword();
	}

	private final Object clientLock = new Object();

	private static ThreadLocal<URL> url = new ThreadLocal<URL>();

	// TODO: replace this with a proper cache to ensure automatic purging. Responses can get quite large.
	private final Map<String, CacheRecord> cache =
			new HashMap<String, CacheRecord>();

	/**
	 * This class holds an HTTP response body, together with its last
	 * modification time and Etag.
	 */
	private final class CacheRecord {
		private final String document;
		private final String lastModified;
		private final String etag;

		private CacheRecord(String document, String lastModified, String etag) {
			if (document == null || lastModified == null || etag == null) {
				throw new IllegalArgumentException("null");
			} else {
				this.document = document;
				this.lastModified = lastModified;
				this.etag = etag;
			}
		}

		public String getDocument() {
			return document;
		}

		public String getLastModified() {
			return lastModified;
		}

		public String getEtag() {
			return etag;
		}
	}

	public static URL getUrl() {
		return url.get();
	}

	public static void setUrl(final URL urlString) {
		url.set(urlString);
	}

	public static void setUrl(final String urlString) throws MalformedURLException {
		setUrl(new URL(urlString));
	}

	protected String getBaseUrl() {
		return UrlUtil.removeUrlTrailingSlashes(server.getUrl());
	}

	/**
	 * Public constructor for AbstractHttpSession
	 *
	 * @param server   server params used by this session
	 * @param callback provider of HttpSession
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException
	 *          for malformed url
	 */
	public AbstractHttpSession(@NotNull Server server, @NotNull HttpSessionCallback callback)
			throws RemoteApiMalformedUrlException {
		this.server = server;
		this.callback = callback;
		String myurl = server.getUrl();
		try {
			UrlUtil.validateUrl(myurl);
		} catch (MalformedURLException e) {
			throw new RemoteApiMalformedUrlException("Malformed server URL: " + myurl, e);
		}
	}

	protected Document retrieveGetResponse(String urlString)
			throws IOException, JDOMException, RemoteApiSessionExpiredException {

		final SAXBuilder builder = new SAXBuilder();
		final Document doc = builder.build(new StringReader(doConditionalGet(urlString)));
//		XmlUtil.printXml(doc);
		preprocessResult(doc);
		return doc;
	}

	/**
	 * Use it only for retrieving text information (like XML or text files)
	 * This method could be refactored into one part returning GetMethod and another
	 * using such method to operate on strings.
	 *
	 * @param urlString URL to retrieve data from
	 * @return response encoded as String (following charset information send by remote party in HTTP header)
	 * @throws IOException in case of IO problem
	 */
	protected String doConditionalGet(String urlString) throws IOException {

		UrlUtil.validateUrl(urlString);
		setUrl(urlString);
		synchronized (clientLock) {
			if (client == null) {
				try {
					client = callback.getHttpClient(server);
				} catch (HttpProxySettingsException e) {
					throw createIOException("Connection error. Please set up HTTP Proxy settings", e);
				}
			}

			final GetMethod method = new GetMethod(urlString);

			CacheRecord cacheRecord = cache.get(urlString);
			if (cacheRecord != null) {
//                System.out.println(String.format("%s in cache, adding If-Modified-Since: %s and If-None-Match: %s headers.",
//                    urlString, cacheRecord.getLastModified(), cacheRecord.getEtag()));
				method.addRequestHeader("If-Modified-Since", cacheRecord.getLastModified());
				method.addRequestHeader("If-None-Match", cacheRecord.getEtag());
			}
			try {
				method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
				method.getParams().setSoTimeout(client.getParams().getSoTimeout());
				callback.configureHttpMethod(this, method);

				client.executeMethod(method);

				if (method.getStatusCode() == HttpStatus.SC_NOT_MODIFIED && cacheRecord != null) {
//					System.out.println("Cache record valid, using cached value: " + new String(cacheRecord.getDocument()));
					return cacheRecord.getDocument();
				} else if (method.getStatusCode() != HttpStatus.SC_OK) {
					throw new IOException(
							"HTTP " + method.getStatusCode() + " (" + HttpStatus.getStatusText(method.getStatusCode())
									+ ")\n" + method.getStatusText());
				} else {
					final String result = method.getResponseBodyAsString();
					final String lastModified = method.getResponseHeader("Last-Modified") == null ? null
							: method.getResponseHeader("Last-Modified").getValue();
					final String eTag = method.getResponseHeader("Etag") == null ? null
							: method.getResponseHeader("Etag").getValue();

					if (lastModified != null && eTag != null) {
						cacheRecord = new CacheRecord(result, lastModified, eTag);
						cache.put(urlString, cacheRecord);
//						System.out.println("Latest GET response document placed in cache: " + new String(result));
					}
					return result;
				}
			} catch (NullPointerException e) {
				throw createIOException("Connection error", e);
			} finally {
				method.releaseConnection();
			}
		}
	}


	/**
	 * Helper method needed because IOException in Java 1.5 does not have constructor taking "cause"
	 *
	 * @param message message
	 * @param cause   chained reason for this exception
	 * @return constructed exception
	 */
	private IOException createIOException(String message, Throwable cause) {
		final IOException ioException = new IOException(message);
		ioException.initCause(cause);
		return ioException;
	}

	protected Document retrievePostResponse(String urlString, Document request)
			throws IOException, JDOMException, RemoteApiSessionExpiredException {
		return retrievePostResponse(urlString, request, true);
	}

	protected Document retrievePostResponse(String urlString, Document request, boolean expectResponse)
			throws IOException, JDOMException, RemoteApiSessionExpiredException {
		XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
		String requestString = serializer.outputString(request);
		return retrievePostResponse(urlString, requestString, expectResponse);
	}

	protected Document retrievePostResponse(String urlString, String request, boolean expectResponse)
			throws IOException, JDOMException, RemoteApiSessionExpiredException {
		UrlUtil.validateUrl(urlString);
		setUrl(urlString);
		Document doc = null;
		synchronized (clientLock) {
			if (client == null) {
				try {
					client = callback.getHttpClient(server);
				} catch (HttpProxySettingsException e) {
					throw createIOException("Connection error. Please set up HTTP Proxy settings", e);
				}
			}

			PostMethod method = new PostMethod(urlString);

			try {
				method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
				method.getParams().setSoTimeout(client.getParams().getSoTimeout());
				callback.configureHttpMethod(this, method);

				if (request != null && !"".equals(request)) {
					method.setRequestEntity(
							new StringRequestEntity(request, "application/xml", "UTF-8"));
				}

				client.executeMethod(method);

				final int httpStatus = method.getStatusCode();
				if (httpStatus == HttpStatus.SC_NO_CONTENT) {
					return doc;
				} else if (httpStatus != HttpStatus.SC_OK
						&& httpStatus != HttpStatus.SC_CREATED) {

					Document document;
					SAXBuilder builder = new SAXBuilder();
					document = builder.build(method.getResponseBodyAsStream());

					throw new IOException(buildExceptionText(method.getStatusCode(), document));
				}

				if (expectResponse) {
					SAXBuilder builder = new SAXBuilder();
					doc = builder.build(method.getResponseBodyAsStream());
					preprocessResult(doc);
				}
			} catch (NullPointerException e) {
				throw createIOException("Connection error", e);
			} finally {
				method.releaseConnection();
			}
		}
		return doc;
	}

	/**
	 * This method will connect to server, and return the results of the push
	 * You must set Query first, which is the contents of your XML file
	 */
	protected Document retrievePostResponse(String urlString, Part[] parts, boolean expectResponse)
			throws IOException, JDOMException, RemoteApiSessionExpiredException {
		Document doc = null;

		synchronized (clientLock) {
			if (client == null) {
				try {
					client = callback.getHttpClient(server);
				} catch (HttpProxySettingsException e) {
					throw createIOException("Connection error. Please set up HTTP Proxy settings", e);
				}
			}

			PostMethod method = new PostMethod(urlString);

			try {
				//create new post method, and set parameters

				method.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE,
						true);

				//Create the multi-part request
				method.setRequestEntity(
						new MultipartRequestEntity(parts, method.getParams())
				);

				client.executeMethod(method);
				final int httpStatus = method.getStatusCode();
				if (httpStatus == HttpStatus.SC_NO_CONTENT) {
					return doc;
				} else if (httpStatus != HttpStatus.SC_OK
						&& httpStatus != HttpStatus.SC_CREATED) {

					Document document;
					SAXBuilder builder = new SAXBuilder();
					document = builder.build(method.getResponseBodyAsStream());

					throw new IOException(buildExceptionText(method.getStatusCode(), document));
				}

				if (expectResponse) {
					SAXBuilder builder = new SAXBuilder();
					doc = builder.build(method.getResponseBodyAsStream());
					preprocessResult(doc);
				}
			} catch (NullPointerException e) {
				throw createIOException("Connection error", e);
			} finally {
				method.releaseConnection();
			}
		}
		return doc;
	}

	private String buildExceptionText(final int statusCode, final Document document) throws JDOMException {
		String text = "Server returned HTTP " + statusCode + " (" + HttpStatus.getStatusText(statusCode) + ")\n"
				+ "Reason: ";

		XPath xpath = XPath.newInstance("error/code");
		@SuppressWarnings("unchecked")
		final List<Element> nodes = xpath.selectNodes(document);
		if (nodes != null && !nodes.isEmpty()) {
			text += nodes.get(0).getValue() + " ";
		}

		xpath = XPath.newInstance("error/message");
		@SuppressWarnings("unchecked")
		final List<Element> messages = xpath.selectNodes(document);
		if (messages != null && !messages.isEmpty()) {
			text += "\nMessage: " + messages.get(0).getValue();
		}


//		xpath = XPath.newInstance("error/stacktrace");
//		nodes = xpath.selectNodes(document);
//		if (nodes != null && !nodes.isEmpty()) {
////			System.out.println(nodes.get(0).getValue());
//		}

		return text;
	}


	protected Document retrieveDeleteResponse(String urlString, boolean expectResponse)
			throws IOException, JDOMException, RemoteApiSessionExpiredException {
		UrlUtil.validateUrl(urlString);

		Document doc = null;
		synchronized (clientLock) {
			if (client == null) {
				try {
					client = callback.getHttpClient(server);
				} catch (HttpProxySettingsException e) {
					throw createIOException("Connection error. Please set up HTTP Proxy settings", e);
				}
			}

			DeleteMethod method = new DeleteMethod(urlString);

			try {
				method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
				method.getParams().setSoTimeout(client.getParams().getSoTimeout());
				callback.configureHttpMethod(this, method);

				client.executeMethod(method);

				if (method.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
					return null;
				}
				if (method.getStatusCode() != HttpStatus.SC_OK) {
					throw new IOException("HTTP status code " + method.getStatusCode() + ": " + method.getStatusText());
				}

				if (expectResponse) {
					SAXBuilder builder = new SAXBuilder();
					doc = builder.build(method.getResponseBodyAsStream());
					preprocessResult(doc);
				}
			} catch (NullPointerException e) {
				throw createIOException("Connection error", e);
			} finally {
				method.releaseConnection();
			}
		}
		return doc;
	}


	protected abstract void adjustHttpHeader(HttpMethod method);

	protected abstract void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException;

	public static String getServerNameFromUrl(String urlString) {
		int pos = urlString.indexOf("://");
		if (pos != -1) {
			urlString = urlString.substring(pos + 1 + 2);
		}
		pos = urlString.indexOf("/");
		if (pos != -1) {
			urlString = urlString.substring(0, pos);
		}
		return urlString;
	}
}
