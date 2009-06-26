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

package com.atlassian.theplugin.commons.bamboo.api;

import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.commons.BambooFileInfoImpl;
import com.atlassian.theplugin.commons.bamboo.*;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.remoteapi.*;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.UrlUtil;
import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Communication stub for Bamboo REST API.
 */
public class BambooSessionImpl extends AbstractHttpSession implements BambooSession {
	private static final String LOGIN_ACTION = "/api/rest/login.action";

	private static final String LOGOUT_ACTION = "/api/rest/logout.action";

	private static final String LIST_PROJECT_ACTION = "/api/rest/listProjectNames.action";

	private static final String LIST_PLAN_ACTION = "/api/rest/listBuildNames.action";

	private static final String LATEST_BUILD_FOR_PLAN_ACTION = "/api/rest/getLatestBuildResults.action";

	private static final String RECENT_BUILDS_FOR_PLAN_ACTION = "/api/rest/getRecentlyCompletedBuildResultsForBuild.action";

	private static final String RECENT_BUILDS_FOR_USER_ACTION = "/api/rest/getLatestBuildsByUser.action";

	private static final String LATEST_USER_BUILDS_ACTION = "/api/rest/getLatestUserBuilds.action";

	private static final String GET_BUILD_DETAILS_ACTION = "/api/rest/getBuildResultsDetails.action";

	private static final String ADD_LABEL_ACTION = "/api/rest/addLabelToBuildResults.action";

	private static final String ADD_COMMENT_ACTION = "/api/rest/addCommentToBuildResults.action";

	private static final String EXECUTE_BUILD_ACTION = "/api/rest/executeBuild.action";

	private static final String GET_BAMBOO_BUILD_NUMBER_ACTION = "/api/rest/getBambooBuildNumber.action";

	private String authToken;

	private static final String AUTHENTICATION_ERROR_MESSAGE = "User not authenticated yet, or session timed out";

	private static final String BUILD_COMPLETED_DATE_ELEM = "buildCompletedDate";

	private static final String BUILD_SUCCESSFUL = "Successful";

	private static final String BUILD_FAILED = "Failed";

	private final ServerData serverData;

	/**
	 * For testing purposes, shouldn't be public
	 *
	 * @param url bamboo server url
	 * @throws RemoteApiMalformedUrlException malformed url
	 */
	BambooSessionImpl(String url) throws RemoteApiMalformedUrlException {
		this(new ServerData("unknown", new ServerIdImpl(), "", "", url), new HttpSessionCallbackImpl());
	}

	/**
	 * Public constructor for BambooSessionImpl.
	 *
	 * @param serverData The server configuration for this session
	 * @param callback   The callback needed for preparing HttpClient calls
	 * @throws RemoteApiMalformedUrlException malformed url
	 */
	public BambooSessionImpl(ServerData serverData, HttpSessionCallback callback) throws RemoteApiMalformedUrlException {
		super(serverData, callback);
		this.serverData = serverData;
	}

	/**
	 * Connects to Bamboo server instance. On successful login authentication token is returned from server and stored
	 * in Bamboo session for subsequent calls.
	 * <p/>
	 * The exception returned may have the getCause() examined for to get the actual exception reason.<br>
	 * If the exception is caused by a valid error response from the server (no IOEXception, UnknownHostException,
	 * MalformedURLException or JDOMException), the
	 * {@link com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException} is actually thrown. This may be
	 * used as a hint that the password is invalid.
	 *
	 * @param name	  username defined on Bamboo server instance
	 * @param aPassword for username
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException
	 *          on connection or authentication errors
	 */
	public void login(String name, char[] aPassword) throws RemoteApiLoginException {
		String loginUrl;

		if (name == null || aPassword == null) {
			throw new RemoteApiLoginException("Corrupted configuration. Username or Password null");
		}
		String pass = String.valueOf(aPassword);
		loginUrl = getBaseUrl() + LOGIN_ACTION + "?username=" + UrlUtil.encodeUrl(name) + "&password="
				+ UrlUtil.encodeUrl(pass) + "&os_username=" + UrlUtil.encodeUrl(name) + "&os_password="
				+ UrlUtil.encodeUrl(pass);

		try {
			Document doc = retrieveGetResponse(loginUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				throw new RemoteApiLoginFailedException(exception);
			}

			@SuppressWarnings("unchecked")
			final List<Element> elements = XPath.newInstance("/response/auth").selectNodes(doc);
			if (elements == null || elements.size() == 0) {
				throw new RemoteApiLoginException("Server did not return any authentication token");
			}
			if (elements.size() != 1) {
				throw new RemoteApiLoginException("Server returned unexpected number of authentication tokens ("
						+ elements.size() + ")");
			}
			this.authToken = elements.get(0).getText();
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

	public void logout() {
		if (!isLoggedIn()) {
			return;
		}

		try {
			String logoutUrl = getBaseUrl() + LOGOUT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
			retrieveGetResponse(logoutUrl);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URLEncoding problem", e);
		} catch (IOException e) {
			/* ignore errors on logout */
		} catch (JDOMException e) {
			/* ignore errors on logout */
		} catch (RemoteApiSessionExpiredException e) {
			/* ignore errors on logout */
		}

		authToken = null;
		client = null;
	}

	public int getBamboBuildNumber() throws RemoteApiException {
		String queryUrl = getBaseUrl() + GET_BAMBOO_BUILD_NUMBER_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);

		try {
			Document doc = retrieveGetResponse(queryUrl);

			String exception = getExceptionMessages(doc);
			if (null != exception) {
				// error - method does nt exists (session errors handled in retrieveGetReponse
				return -1;
			}

			XPath xpath = XPath.newInstance("/response/bambooBuildNumber");
			@SuppressWarnings("unchecked")
			Element element = (Element) xpath.selectSingleNode(doc);
			if (element != null) {
				String bNo = element.getText();
				return Integer.parseInt(bNo);
			}
			return -1;
		} catch (JDOMException e) {
			throw new RemoteApiException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new RemoteApiException(e.getMessage(), e);
		}
	}

	@NotNull
	public List<BambooProject> listProjectNames() throws RemoteApiException {
		String buildResultUrl = getBaseUrl() + LIST_PROJECT_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);

		List<BambooProject> projects = new ArrayList<BambooProject>();
		try {
			Document doc = retrieveGetResponse(buildResultUrl);
			XPath xpath = XPath.newInstance("/response/project");
			@SuppressWarnings("unchecked")
			List<Element> elements = xpath.selectNodes(doc);
			if (elements != null) {
				for (Element element : elements) {
					String name = element.getChild("name").getText();
					String key = element.getChild("key").getText();
					projects.add(new BambooProjectInfo(name, key));
				}
			}
		} catch (JDOMException e) {
			throw new RemoteApiException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new RemoteApiException(e.getMessage(), e);
		}

		return projects;
	}

	@NotNull
	public List<BambooPlan> listPlanNames() throws RemoteApiException {
		String buildResultUrl = getBaseUrl() + LIST_PLAN_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);

		List<BambooPlan> plans = new ArrayList<BambooPlan>();
		try {
			Document doc = retrieveGetResponse(buildResultUrl);
			XPath xpath = XPath.newInstance("/response/build");
			@SuppressWarnings("unchecked")
			List<Element> elements = xpath.selectNodes(doc);
			if (elements != null) {
				for (Element element : elements) {
					String enabledValue = element.getAttributeValue("enabled");
					boolean enabled = true;
					if (enabledValue != null) {
						enabled = Boolean.parseBoolean(enabledValue);
					}
					String name = element.getChild("name").getText();
					String key = element.getChild("key").getText();
					BambooPlan plan = new BambooPlan(name, key, enabled);
					plans.add(plan);
				}
			}
		} catch (JDOMException e) {
			throw new RemoteApiException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new RemoteApiException(e.getMessage(), e);
		}

		return plans;
	}

	/**
	 * Returns a {@link com.atlassian.theplugin.commons.bamboo.BambooBuild} information about the latest build in a
	 * plan.
	 * <p/>
	 * Returned structure contains either the information about the build or an error message if the connection fails.
	 *
	 * @param planKey ID of the plan to get info about
	 * @return Information about the last build or error message
	 */
	@NotNull
	public BambooBuild getLatestBuildForPlan(@NotNull String planKey, final int timezoneOffset)
			throws RemoteApiException {
		final List<BambooPlan> planList = listPlanNames();
		final Boolean isEnabled = isPlanEnabled(planList, planKey);
		return getLatestBuildForPlan(planKey, isEnabled != null ? isEnabled : true, timezoneOffset);
	}

	@Nullable
	public static Boolean isPlanEnabled(@NotNull Collection<BambooPlan> allPlans, @NotNull String planKey) {
		for (BambooPlan bambooPlan : allPlans) {
			if (planKey.equals(bambooPlan.getPlanKey())) {
				return bambooPlan.isEnabled();
			}
		}
		return null;
	}

	@NotNull
	public BambooBuild getLatestBuildForPlan(@NotNull final String planKey, final boolean isPlanEnabled,
			final int timezoneOffset) throws RemoteApiException {
		String buildResultUrl = getBaseUrl() + LATEST_BUILD_FOR_PLAN_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken)
				+ "&buildKey=" + UrlUtil.encodeUrl(planKey);

		try {
			Document doc = retrieveGetResponse(buildResultUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				return constructBuildErrorInfo(planKey, exception, new Date());
			}

			@SuppressWarnings("unchecked")
			final List<Element> elements = XPath.newInstance("/response").selectNodes(doc);
			if (elements != null && !elements.isEmpty()) {
				Element e = elements.iterator().next();
				final Set<String> commiters = constructBuildCommiters(e);
				return constructBuildItem(e, new Date(), planKey, isPlanEnabled, commiters, timezoneOffset);
			} else {
				return constructBuildErrorInfo(planKey, "Malformed server reply: no response element", new Date());
			}
		} catch (IOException e) {
			return constructBuildErrorInfo(planKey, e.getMessage(), e, new Date());
		} catch (JDOMException e) {
			return constructBuildErrorInfo(planKey, "Server returned malformed response", e, new Date());
		} catch (RemoteApiException e) {
			return constructBuildErrorInfo(planKey, e.getMessage(), e, new Date());
		}
	}

	public Collection<BambooBuild> getRecentBuildsForPlan(@NotNull final String planKey, final int timezoneOffset)
			throws RemoteApiException {
		String buildResultUrl = getBaseUrl() + RECENT_BUILDS_FOR_PLAN_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken)
				+ "&buildKey=" + UrlUtil.encodeUrl(planKey);
		return getBuildsCollection(buildResultUrl, planKey, timezoneOffset);
	}

	public Collection<BambooBuild> getRecentBuildsForUser(final int timezoneOffset) throws RemoteApiException {
		String buildResultUrl = getBaseUrl() + RECENT_BUILDS_FOR_USER_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken)
				+ "&username=" + UrlUtil.encodeUrl(getUsername());
		return getBuildsCollection(buildResultUrl, getUsername(), timezoneOffset);
	}

	private Collection<BambooBuild> getBuildsCollection(@NotNull final String url, @NotNull final String planKey,
			final int timezoneOffset) throws RemoteApiException {

		final Date pollingTime = new Date();
		final List<BambooBuild> builds = new ArrayList<BambooBuild>();
		try {
			Document doc = retrieveGetResponse(url);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				builds.add(constructBuildErrorInfo(url, exception, new Date()));
				return builds;
			}

			@SuppressWarnings("unchecked")
			final List<Element> elements = XPath.newInstance("/response/build").selectNodes(doc);
			if (elements == null || elements.isEmpty()) {
				builds.add(constructBuildErrorInfo(url, "Malformed server reply: no response element", new Date()));
			} else {
				for (Element element : elements) {
					final Set<String> commiters = constructBuildCommiters(element);
					builds.add(constructBuildItem(element, pollingTime, planKey, true, commiters, timezoneOffset));
				}
			}
		} catch (IOException e) {
			builds.add(constructBuildErrorInfo(planKey, e.getMessage(), e, pollingTime));
		} catch (JDOMException e) {
			builds.add(constructBuildErrorInfo(planKey, "Server returned malformed response", e, pollingTime));
		} catch (RemoteApiException e) {
			builds.add(constructBuildErrorInfo(planKey, e.getMessage(), e, pollingTime));
		}
		return builds;
	}

	private Set<String> constructBuildCommiters(final Element element) throws JDOMException {

		Set<String> commiters = new HashSet<String>();
		@SuppressWarnings("unchecked")
		final List<Element> commitElements = XPath.newInstance("commits/commit").selectNodes(element);
		if (!commitElements.isEmpty()) {
			for (Element commiter : commitElements) {
				commiters.add(commiter.getAttributeValue("author"));
			}
		}
		return commiters;
	}

	@NotNull
	public List<String> getFavouriteUserPlans() throws RemoteApiSessionExpiredException {
		List<String> builds = new ArrayList<String>();
		String buildResultUrl = getBaseUrl() + LATEST_USER_BUILDS_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);

		try {
			Document doc = retrieveGetResponse(buildResultUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				return builds;
			}

			final XPath xpath = XPath.newInstance("/response/build");
			@SuppressWarnings("unchecked")
			final List<Element> elements = xpath.selectNodes(doc);
			if (elements != null) {
				for (Element element : elements) {
					builds.add(element.getChildText("key"));
				}
				return builds;
			} else {
				return builds;
			}
		} catch (IOException e) {
			return builds;
		} catch (JDOMException e) {
			return builds;
		}
	}

	@NotNull
	public BuildDetails getBuildResultDetails(@NotNull String planKey, int buildNumber) throws RemoteApiException {
		final String buildResultUrl = getBaseUrl() + GET_BUILD_DETAILS_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken)
				+ "&buildKey=" + UrlUtil.encodeUrl(planKey) + "&buildNumber=" + buildNumber;

		try {
			BuildDetailsInfo build = new BuildDetailsInfo();
			Document doc = retrieveGetResponse(buildResultUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				throw new RemoteApiException(exception);
			}

			@SuppressWarnings("unchecked")
			final List<Element> responseElements = XPath.newInstance("/response").selectNodes(doc);
			for (Element element : responseElements) {
				String vcsRevisionKey = element.getAttributeValue("vcsRevisionKey");
				if (vcsRevisionKey != null) {
					build.setVcsRevisionKey(vcsRevisionKey);
				}
			}

			@SuppressWarnings("unchecked")
			final List<Element> commitElements = XPath.newInstance("/response/commits/commit").selectNodes(doc);
			if (!commitElements.isEmpty()) {
				int i = 1;
				for (Element element : commitElements) {
					BambooChangeSetImpl cInfo = new BambooChangeSetImpl();
					cInfo.setAuthor(element.getAttributeValue("author"));
					cInfo.setCommitDate(parseCommitTime(element.getAttributeValue("date")));
					cInfo.setComment(getChildText(element, "comment"));

					String path = "/response/commits/commit[" + i++ + "]/files/file";
					XPath filesPath = XPath.newInstance(path);
					@SuppressWarnings("unchecked")
					final List<Element> fileElements = filesPath.selectNodes(doc);
					for (Element file : fileElements) {
						BambooFileInfo fileInfo = new BambooFileInfoImpl(file.getAttributeValue("name"),
								file.getAttributeValue("revision"));
						cInfo.addCommitFile(fileInfo);
					}
					build.addCommitInfo(cInfo);
				}
			}

			@SuppressWarnings("unchecked")
			final List<Element> sucTestResElements = XPath.newInstance("/response/successfulTests/testResult")
					.selectNodes(doc);
			for (Element element : sucTestResElements) {
				TestDetailsInfo tInfo = new TestDetailsInfo();
				tInfo.setTestClassName(element.getAttributeValue("testClass"));
				tInfo.setTestMethodName(element.getAttributeValue("testMethod"));
				double duration;
				try {
					duration = Double.valueOf(element.getAttributeValue("duration"));
				} catch (NumberFormatException e) {
					// leave 0
					duration = 0;
				}
				tInfo.setTestDuration(duration);
				tInfo.setTestResult(TestResult.TEST_SUCCEED);
				build.addSuccessfulTest(tInfo);
			}

			@SuppressWarnings("unchecked")
			final List<Element> failedTestResElements = XPath.newInstance("/response/failedTests/testResult")
					.selectNodes(doc);
			if (!failedTestResElements.isEmpty()) {
				int i = 1;
				for (Element element : failedTestResElements) {
					TestDetailsInfo tInfo = new TestDetailsInfo();
					tInfo.setTestClassName(element.getAttributeValue("testClass"));
					tInfo.setTestMethodName(element.getAttributeValue("testMethod"));
					double duration;
					try {
						duration = Double.valueOf(element.getAttributeValue("duration"));
					} catch (NumberFormatException e) {
						// leave 0
						duration = 0;
					}
					tInfo.setTestDuration(duration);
					tInfo.setTestResult(TestResult.TEST_FAILED);

					String path = "/response/failedTests/testResult[" + i++ + "]/errors/error";
					XPath errorPath = XPath.newInstance(path);
					@SuppressWarnings("unchecked")
					final List<Element> errorElements = errorPath.selectNodes(doc);
					for (Element error : errorElements) {
						tInfo.setTestErrors(error.getText());
					}
					build.addFailedTest(tInfo);
				}
			}

			return build;
		} catch (JDOMException e) {
			throw new RemoteApiException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new RemoteApiException(e.getMessage(), e);
		}
	}

	/**
	 * Currently length of the comment is limited by poor implementation which uses GET HTTP method (sic!) to post a new
	 * comment and the comment becomes part of URL, which is typically truncated by web servers.
	 */
	public void addLabelToBuild(@NotNull String planKey, int buildNumber, String buildLabel) throws RemoteApiException {
		String buildResultUrl = getBaseUrl() + ADD_LABEL_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken)
				+ "&buildKey=" + UrlUtil.encodeUrl(planKey) + "&buildNumber=" + buildNumber + "&label="
				+ UrlUtil.encodeUrl(buildLabel);

		try {
			Document doc = retrieveGetResponse(buildResultUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				throw new RemoteApiException(exception);
			}
		} catch (JDOMException e) {
			throw new RemoteApiException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new RemoteApiException(e.getMessage(), e);
		}
	}

	/**
	 * Currently length of the comment is limited by poor implementation which uses GET HTTP method (sic!) to post a new
	 * comment and the comment becomes part of URL, which is typically truncated by web servers.
	 */
	public void addCommentToBuild(@NotNull String planKey, int buildNumber, String buildComment)
			throws RemoteApiException {
		String buildResultUrl = getBaseUrl() + ADD_COMMENT_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken)
				+ "&buildKey=" + UrlUtil.encodeUrl(planKey) + "&buildNumber=" + buildNumber + "&content="
				+ UrlUtil.encodeUrl(buildComment);

		try {
			Document doc = retrieveGetResponse(buildResultUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				throw new RemoteApiException(exception);
			}
		} catch (JDOMException e) {
			throw new RemoteApiException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new RemoteApiException(e.getMessage(), e);
		}
	}

	public void executeBuild(@NotNull String planKey) throws RemoteApiException {
		String buildResultUrl;

		buildResultUrl = getBaseUrl() + EXECUTE_BUILD_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken) + "&buildKey="
				+ UrlUtil.encodeUrl(planKey);

		try {
			Document doc = retrieveGetResponse(buildResultUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				throw new RemoteApiException(exception);
			}
		} catch (JDOMException e) {
			throw new RemoteApiException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new RemoteApiException(e.getMessage(), e);
		}
	}

	BambooBuildInfo constructBuildErrorInfo(String planKey, String message, Date lastPollingTime) {
		return new BambooBuildInfo.Builder(planKey, null, serverData, null, null, BuildStatus.UNKNOWN).pollingTime(
				lastPollingTime).errorMessage(message).build();
	}

	BambooBuildInfo constructBuildErrorInfo(String planKey, String message, Throwable exception, Date lastPollingTime) {
		return new BambooBuildInfo.Builder(planKey, null, serverData, null, null, BuildStatus.UNKNOWN).pollingTime(
				lastPollingTime).errorMessage(message, exception).build();
	}

	private int parseInt(String number) throws RemoteApiException {
		try {
			return Integer.parseInt(number);
		} catch (NumberFormatException ex) {
			throw new RemoteApiException("Invalid number", ex);
		}
	}

	private BambooBuildInfo constructBuildItem(Element buildItemNode, Date lastPollingTime, final String aPlanKey,
			boolean isEnabled, @Nullable Set<String> commiters, final int timezoneOffset) throws RemoteApiException {

		// for never executed build we actually have no data here (no children)
		if (!buildItemNode.getChildren().iterator().hasNext()) {
			return new BambooBuildInfo.Builder(aPlanKey, serverData, BuildStatus.UNKNOWN).enabled(isEnabled)
					.pollingTime(lastPollingTime)
					.reason("Never built")
					.build();

		}

		final String planKey = getChildText(buildItemNode, "buildKey");
		final String buildName = getChildText(buildItemNode, "buildName");
		final String projectName = getChildText(buildItemNode, "projectName");
		final int buildNumber = parseInt(getChildText(buildItemNode, "buildNumber"));
		final String relativeBuildDate = getChildText(buildItemNode, "buildRelativeBuildDate");
		final Date startTime = parseBuildDate(getChildText(buildItemNode, "buildTime"), "Cannot parse buildTime.",
				timezoneOffset);
		final String buildCompletedDateStr = getChildText(buildItemNode, BUILD_COMPLETED_DATE_ELEM);
		final Date completionTime = (buildCompletedDateStr != null && buildCompletedDateStr.length() > 0) ? parseDateUniversal(
				buildCompletedDateStr, BUILD_COMPLETED_DATE_ELEM, timezoneOffset)
				//older Bamboo versions do not generate buildCompletedDate so we set it as buildTime
				: startTime;
		final String durationDescription = getChildText(buildItemNode, "buildDurationDescription");

		final String stateStr = getChildText(buildItemNode, "buildState");
		return new BambooBuildInfo.Builder(planKey, buildName, serverData, projectName, buildNumber,
				getStatus(stateStr)).enabled(isEnabled)
				.pollingTime(lastPollingTime)
				.reason(getChildText(buildItemNode, "buildReason"))
				.startTime(startTime)
				.testSummary(getChildText(buildItemNode, "buildTestSummary"))
				.commitComment(getChildText(buildItemNode, "buildCommitComment"))
				.testsPassedCount(parseInt(getChildText(buildItemNode, "successfulTestCount")))
				.testsFailedCount(parseInt(getChildText(buildItemNode, "failedTestCount")))
				.completionTime(completionTime)
				.relativeBuildDate(relativeBuildDate)
				.durationDescription(durationDescription)
				.commiters(commiters)
				.build();
	}

	@NotNull
	private BuildStatus getStatus(@Nullable String stateStr) {
		if (BUILD_SUCCESSFUL.equalsIgnoreCase(stateStr)) {
			return BuildStatus.SUCCESS;
		} else if (BUILD_FAILED.equalsIgnoreCase(stateStr)) {
			return BuildStatus.FAILURE;
		} else {
			return BuildStatus.UNKNOWN;
		}
	}

	private Date parseDateUniversal(@Nullable String dateStr, @NotNull String element, final int timezoneOffset)
			throws RemoteApiException {
		if (dateStr != null) {
			if (dateStr.indexOf('T') != -1) {
				// new format
				return parseCommitTime(dateStr);
			} else {
				// old format
				return parseBuildDate(dateStr, "Cannot parse " + element, timezoneOffset);
			}
		}
		throw new RemoteApiException(element + " cannot be found");
	}

	private static DateTimeFormatter buildDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

	private static DateTimeFormatter commitDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");

	/**
	 * Parses date without timezone info
	 * <p/>
	 * wseliga: I have no idea why this method silently returns null in case of parsing problem. For now, I am going to
	 * leave it as it is to avoid hell of the problems, should it be really necessary (and I am now a few days before
	 * 2.0.0 final release)
	 *
	 * @param date		 string to parse
	 * @param errorMessage message used during logging
	 * @return parsed date
	 */
	@Nullable
	private Date parseBuildDate(String date, String errorMessage, final int timezoneOffset) {
		try {
			final DateTime dateTime = buildDateFormat.parseDateTime(date);
			// now adjust the time for local caller time, as Bamboo servers always serves its local time
			// without the timezone info
			return dateTime.plusHours(timezoneOffset).toDate();
		} catch (IllegalArgumentException e) {
			LoggerImpl.getInstance().debug("Cannot parse build date: " + errorMessage);
			return null;
		}
	}

	private Date parseCommitTime(String date) {
		return commitDateFormat.parseDateTime(date).toDate();
	}

	private String getChildText(Element node, String childName) {
		final Element child = node.getChild(childName);
		if (child == null) {
			return "";
		}
		return child.getText();
	}

	private static String getExceptionMessages(Document doc) throws JDOMException {
		XPath xpath = XPath.newInstance("/errors/error");
		@SuppressWarnings("unchecked")
		List<Element> elements = xpath.selectNodes(doc);

		if (elements != null && elements.size() > 0) {
			StringBuffer exceptionMsg = new StringBuffer();
			for (Element e : elements) {
				exceptionMsg.append(e.getText());
				exceptionMsg.append("\n");
			}
			return exceptionMsg.toString();
		} else {
			/* no exception */
			return null;
		}
	}

	public boolean isLoggedIn() {
		return authToken != null;
	}

	public String getBuildLogs(@NotNull String planKey, int buildNumber) throws RemoteApiException {
		String buildResultUrl = new StringBuilder().append(getBaseUrl())
				.append("/download/")
				.append(UrlUtil.encodeUrl(planKey))
				.append("/build_logs/")
				.append(UrlUtil.encodeUrl(planKey))
				.append("-")
				.append(buildNumber)
				.append(".log")
				.toString();

		try {
			return doUnconditionalGetForTextNonXmlResource(buildResultUrl);
		} catch (IOException e) {
			throw new RemoteApiException(e.getMessage(), e);
		}
	}

	@Override
	protected void adjustHttpHeader(HttpMethod method) {
		// Bamboo does not require custom headers
	}

	@Override
	protected void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException {
		String error = getExceptionMessages(doc);
		if (error != null) {
			if (error.startsWith(AUTHENTICATION_ERROR_MESSAGE)) {
				throw new RemoteApiSessionExpiredException("Session expired.");
			}
		}
	}

}
