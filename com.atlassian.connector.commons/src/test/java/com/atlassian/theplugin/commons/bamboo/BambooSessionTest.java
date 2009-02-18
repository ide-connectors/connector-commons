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

package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.theplugin.api.AbstractSessionTest;
import com.atlassian.theplugin.bamboo.api.bamboomock.AddCommentToBuildCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.AddLabelToBuildCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.BuildDetailsResultCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.ErrorMessageCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.ExecuteBuildCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.FavouritePlanListCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.LatestBuildResultCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.LoginCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.LogoutCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.PlanListCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.ProjectListCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.Util;
import com.atlassian.theplugin.bamboo.api.bamboomock.LatestBuildResultVelocityBallback;
import com.atlassian.theplugin.commons.bamboo.api.AutoRenewBambooSession;
import com.atlassian.theplugin.commons.bamboo.api.BambooSession;
import com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import com.spartez.util.junit3.TestUtil;
import com.spartez.util.junit3.IAction;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Date;

import junit.framework.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;


/**
 * Test case for {#link BambooSessionImpl}
 */
public class BambooSessionTest extends AbstractSessionTest {
	public void testSuccessBambooLogin() throws Exception {

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);

		String[] usernames = { "user", "+-=&;<>", "", "a;&username=other", "!@#$%^&*()_-+=T " };
		String[] passwords = { "password", "+-=&;<>", "", "&password=other", ",./';[]\t\\ |}{\":><?" };

		for (int i = 0; i < usernames.length; ++i) {
			mockServer.expect("/api/rest/login.action", new LoginCallback(usernames[i], passwords[i]));
			mockServer.expect("/api/rest/logout.action", new LogoutCallback());

            apiHandler.login(usernames[i], passwords[i].toCharArray());
			assertTrue(apiHandler.isLoggedIn());
			apiHandler.logout();
			assertFalse(apiHandler.isLoggedIn());
		}

		mockServer.verify();
	}

	public void testSuccessBambooLoginURLWithSlash() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback(LoginCallback.AUTH_TOKEN));

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl + "/");
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		assertTrue(apiHandler.isLoggedIn());
		apiHandler.logout();
		assertFalse(apiHandler.isLoggedIn());

		mockServer.verify();
	}

	public void testNullParamsLogin() throws Exception {
		try {
			BambooSession apiHandler = new BambooSessionImpl(null);
			apiHandler.login(null, null);
			fail();
		} catch (RemoteApiException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	@Override
	protected ProductSession getProductSession(final String url) throws RemoteApiMalformedUrlException {
		return new BambooSessionImpl(url);
	}



	@Override
	protected JettyMockServer.Callback getLoginCallback(final boolean isFail) {
		return new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL);
	}

		@Override
		protected String getLoginUrl() {
		return "/api/rest/login.action";
	}

	public void testWrongParamsBambooLogin() throws Exception {
		try {
			BambooSession apiHandler = new BambooSessionImpl("");
			apiHandler.login("", "".toCharArray());
			fail();
		} catch (RemoteApiException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}


	public void testProjectList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		List<BambooProject> projects = apiHandler.listProjectNames();
		apiHandler.logout();

		Util.verifyProjectListResult(projects);

		mockServer.verify();
	}

	public void testPlanList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		List<BambooPlan> plans = apiHandler.listPlanNames();
		apiHandler.logout();

		Util.verifyPlanListResult(plans);
		mockServer.verify();
	}

	public void testFavouritePlanList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		List<String> plans = apiHandler.getFavouriteUserPlans();
		apiHandler.logout();

		Util.verifyFavouriteListResult(plans);
		mockServer.verify();
	}

	public void testBuildForPlanSuccessNoTimezone() throws Exception {
	    implTestBuildForPlanSuccess(0);
	}

	public void testBuildForPlanSuccessNegativeTimezone() throws Exception {
	    implTestBuildForPlanSuccess(-5);
	}

	public void testBuildForPlanSuccessPositiveTimezone() throws Exception {
	    implTestBuildForPlanSuccess(7);
	}

	private void implTestBuildForPlanSuccess(int timezoneOffset) throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooServerCfg bambooServerCfg = new BambooServerCfg("mybamboo", mockBaseUrl, new ServerId());
		bambooServerCfg.setTimezoneOffset(timezoneOffset);
		BambooSession apiHandler = new BambooSessionImpl(bambooServerCfg, new HttpSessionCallbackImpl());
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF");
		apiHandler.logout();

		Util.verifySuccessfulBuildResult(build, mockBaseUrl);
		assertEquals(30, build.getTestsPassed());
		assertEquals(10, build.getTestsFailed());
		final DateTime expectedDate = new DateTime(2008, 1, 29, 14, 49, 36, 0).plusHours(timezoneOffset);
		assertEquals(expectedDate.toDate(), build.getBuildCompletedDate());
		assertEquals(expectedDate.toDate(), build.getBuildStartedDate());

		mockServer.verify();
	}

	public void testGetLatestBuildForNeverExecutedPlan() throws RemoteApiException {
		implTestGetLatestBuildForNeverExecutedPlan(
				"/mock/bamboo/2_1_5/api/rest/getLatestBuildForPlanResponse-never-executed.xml");
	}

	public void testGetLatestBuildForNeverExecutedPlan2() throws RemoteApiException {
		implTestGetLatestBuildForNeverExecutedPlan(
				"/mock/bamboo/2_1_5/api/rest/getLatestBuildForPlanResponse-never-executed2.xml");
	}

	private void implTestGetLatestBuildForNeverExecutedPlan(final String fullFilePath) throws RemoteApiException {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("", fullFilePath));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		Date now = new Date();
		BambooServerCfg bambooServerCfg = new BambooServerCfg("mybamboo", mockBaseUrl, new ServerId());
		BambooSession apiHandler = new BambooSessionImpl(bambooServerCfg, new HttpSessionCallbackImpl());
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		final BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF");
		apiHandler.logout();
		assertEquals(BuildStatus.UNKNOWN, build.getStatus());
		TestUtil.assertThrows(UnsupportedOperationException.class, new IAction() {
			public void run() throws Throwable {
				build.getBuildNumber();
			}
		});
		assertNull(build.getBuildStartedDate());
		assertNull(build.getBuildCompletedDate());
		TestUtil.assertHasOnlyElements(build.getCommiters());
		assertNull(build.getBuildDurationDescription());
		assertEquals(mockBaseUrl + "/browse/TP-DEF", build.getBuildUrl());
		assertNull(build.getBuildTestSummary());
		assertEquals(bambooServerCfg, build.getServer());
		assertTrue(build.getEnabled());
		assertNull(build.getProjectName());
		assertTrue(build.getPollingTime().getTime() >= now.getTime()
				&& build.getPollingTime().getTime() <= new Date().getTime());
		assertNull(build.getErrorMessage());
		assertEquals("Never built", build.getBuildReason());
	}

	public void testGetLatestBuildForPlanBamboo2x1x5() throws RemoteApiException {
		implTestGetLatestBuildForPlanBamboo2x1x5(0);
	}

	private void implTestGetLatestBuildForPlanBamboo2x1x5(int timezoneOffset) throws RemoteApiException {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("",
				"/mock/bamboo/2_1_5/api/rest/getLatestBuildForPlanResponse.xml"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooServerCfg bambooServerCfg = new BambooServerCfg("mybamboo", mockBaseUrl, new ServerId());
		bambooServerCfg.setTimezoneOffset(timezoneOffset);
		BambooSession apiHandler = new BambooSessionImpl(bambooServerCfg, new HttpSessionCallbackImpl());
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF");
		apiHandler.logout();

		Assert.assertEquals("ACC-TST", build.getBuildKey());
		Assert.assertEquals(193, build.getBuildNumber());
		assertEquals(BuildStatus.SUCCESS, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertEquals(mockBaseUrl, build.getServerUrl());
		Assert.assertEquals(mockBaseUrl + "/browse/ACC-TST-193", build.getBuildResultUrl());
		Assert.assertEquals(mockBaseUrl + "/browse/ACC-TST", build.getBuildUrl());
		assertEquals("Atlassian Connector Commons", build.getProjectName());
		assertEquals(267, build.getTestsPassed());
		assertEquals(0, build.getTestsFailed());
		assertEquals("Code has changed", build.getBuildReason());
		assertEquals("267 passed", build.getBuildTestSummary());
		assertEquals("3 minutes ago", build.getBuildRelativeBuildDate());
		assertEquals("28 seconds", build.getBuildDurationDescription());
		assertEquals(new DateTime(2009, 2, 9, 7, 38, 36, 0, DateTimeZone.forOffsetHours(-6)).toDate(),
				build.getBuildCompletedDate());
		TestUtil.assertHasOnlyElements(build.getCommiters(), "wseliga", "mwent");
		mockServer.verify();
	}

	public void testGetLatestBuildForPlanBamboo2x1x5WithTimeZoneOffset() throws RemoteApiException {
		implTestGetLatestBuildForPlanBamboo2x1x5(3);
	}


	public void testBuildForPlanFailure() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("FAILED"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF", true);
		apiHandler.logout();

		Util.verifyFailedBuildResult(build, mockBaseUrl);

		mockServer.verify();
	}

	public void testBuildForNonExistingPlan() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("WRONG"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF", false);
		apiHandler.logout();

		Util.verifyErrorBuildResult(build);

		mockServer.verify();
	}

	public void testBuildDetailsFor1CommitFailedSuccessTests() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action",
				new BuildDetailsResultCallback("buildResult-1Commit-FailedTests-SuccessfulTests.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
		apiHandler.logout();

		mockServer.verify();

		assertNotNull(build);
		assertEquals("13928", build.getVcsRevisionKey());
		// commit
		assertEquals(1, build.getCommitInfo().size());
		assertEquals("author", build.getCommitInfo().iterator().next().getAuthor());
		assertNotNull(build.getCommitInfo().iterator().next().getCommitDate());
		assertEquals("commit comment", build.getCommitInfo().iterator().next().getComment());
		assertEquals(3, build.getCommitInfo().iterator().next().getFiles().size());
		assertEquals("13928", build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getRevision());
		assertEquals(
				"/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
				build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getUrl());

		// failed tests
		assertEquals(2, build.getFailedTestDetails().size());
		assertEquals("com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerTest",
				build.getFailedTestDetails().iterator().next().getTestClassName());
		assertEquals("testSingleSuccessResultForDisabledBuild",
				build.getFailedTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.012,
				build.getFailedTestDetails().iterator().next().getTestDuration());
		assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_FAILED,
				build.getFailedTestDetails().iterator().next().getTestResult());

		// successful tests
		assertEquals(117, build.getSuccessfulTestDetails().size());
		assertEquals("com.atlassian.theplugin.commons.bamboo.BambooServerFacadeTest",
				build.getSuccessfulTestDetails().iterator().next().getTestClassName());
		assertEquals("testProjectList",
				build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.046,
				build.getSuccessfulTestDetails().iterator().next().getTestDuration());
		assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_SUCCEED,
				build.getSuccessfulTestDetails().iterator().next().getTestResult());
	}

	public void testBuildDetailsFor1CommitFailedTests() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action",
				new BuildDetailsResultCallback("buildResult-1Commit-FailedTests.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
		apiHandler.logout();
		
		mockServer.verify();

		assertNotNull(build);
		assertEquals("13928", build.getVcsRevisionKey());
		// commit
		assertEquals(1, build.getCommitInfo().size());
		assertEquals("author", build.getCommitInfo().iterator().next().getAuthor());
		assertNotNull(build.getCommitInfo().iterator().next().getCommitDate());
		assertEquals("commit comment", build.getCommitInfo().iterator().next().getComment());
		assertEquals(3, build.getCommitInfo().iterator().next().getFiles().size());
		assertEquals("13928", build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getRevision());
		assertEquals(
				"/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
				build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getUrl());

		// failed tests
		assertEquals(2, build.getFailedTestDetails().size());
		assertEquals("com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerTest",
				build.getFailedTestDetails().iterator().next().getTestClassName());
		assertEquals("testSingleSuccessResultForDisabledBuild",
				build.getFailedTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.012,
				build.getFailedTestDetails().iterator().next().getTestDuration());
		assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_FAILED,
				build.getFailedTestDetails().iterator().next().getTestResult());

		// successful tests
		assertEquals(0, build.getSuccessfulTestDetails().size());
	}

	public void testBuildDetailsFor1CommitSuccessTests() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("buildResult-1Commit-SuccessfulTests.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
		apiHandler.logout();

		mockServer.verify();

		assertNotNull(build);
		assertEquals("13928", build.getVcsRevisionKey());
		// commit
		assertEquals(1, build.getCommitInfo().size());
		assertEquals("author", build.getCommitInfo().iterator().next().getAuthor());
		assertNotNull(build.getCommitInfo().iterator().next().getCommitDate());
		assertEquals("commit comment", build.getCommitInfo().iterator().next().getComment());
		assertEquals(3, build.getCommitInfo().iterator().next().getFiles().size());
		assertEquals("13928", build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getRevision());
		assertEquals(
				"/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
				build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getUrl());

		// failed tests
		assertEquals(0, build.getFailedTestDetails().size());

		// successful tests
		assertEquals(117, build.getSuccessfulTestDetails().size());
		assertEquals("com.atlassian.theplugin.commons.bamboo.BambooServerFacadeTest",
				build.getSuccessfulTestDetails().iterator().next().getTestClassName());
		assertEquals("testProjectList",
				build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.046,
				build.getSuccessfulTestDetails().iterator().next().getTestDuration());
		assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_SUCCEED,
				build.getSuccessfulTestDetails().iterator().next().getTestResult());
	}

	public void testBuildDetailsFor3CommitFailedSuccessTests() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action",
				new BuildDetailsResultCallback("buildResult-3Commit-FailedTests-SuccessfulTests.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
		apiHandler.logout();

		mockServer.verify();

		assertNotNull(build);
		assertEquals("13928", build.getVcsRevisionKey());
		// commit
		assertEquals(3, build.getCommitInfo().size());
		assertEquals("author", build.getCommitInfo().get(0).getAuthor());
		assertNotNull(build.getCommitInfo().get(0).getCommitDate());
		assertEquals("commit comment", build.getCommitInfo().get(0).getComment());
		assertEquals(3, build.getCommitInfo().get(0).getFiles().size());
		assertEquals("13928", build.getCommitInfo().get(0).getFiles().iterator().next().getFileDescriptor().getRevision());
		assertEquals(
				"/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
				build.getCommitInfo().get(0).getFiles().iterator().next().getFileDescriptor().getUrl());
		assertEquals(2, build.getCommitInfo().get(1).getFiles().size());
		assertEquals(1, build.getCommitInfo().get(2).getFiles().size());		

		// failed tests
		assertEquals(2, build.getFailedTestDetails().size());
		assertEquals("com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerTest",
				build.getFailedTestDetails().iterator().next().getTestClassName());
		assertEquals("testSingleSuccessResultForDisabledBuild",
				build.getFailedTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.012,
				build.getFailedTestDetails().iterator().next().getTestDuration());
		assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_FAILED,
				build.getFailedTestDetails().iterator().next().getTestResult());

		assertEquals("error 1\n", build.getFailedTestDetails().get(0).getErrors());
		assertEquals("error 2\n", build.getFailedTestDetails().get(1).getErrors());		

		// successful tests
		assertEquals(117, build.getSuccessfulTestDetails().size());
		assertEquals("com.atlassian.theplugin.commons.bamboo.BambooServerFacadeTest",
				build.getSuccessfulTestDetails().iterator().next().getTestClassName());
		assertEquals("testProjectList",
				build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.046,
				build.getSuccessfulTestDetails().iterator().next().getTestDuration());
		assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_SUCCEED,
				build.getSuccessfulTestDetails().iterator().next().getTestResult());

		assertEquals("com.atlassian.theplugin.crucible.CrucibleServerFacadeConnectionTest",
				build.getSuccessfulTestDetails().get(116).getTestClassName());
		assertEquals("testConnectionTestFailedNullPassword",
				build.getSuccessfulTestDetails().get(116).getTestMethodName());
		assertEquals(0.001,
				build.getSuccessfulTestDetails().get(116).getTestDuration());
		assertNull(build.getSuccessfulTestDetails().get(116).getErrors());
		assertEquals(TestResult.TEST_SUCCEED,
				build.getSuccessfulTestDetails().get(116).getTestResult());
	}

	public void testBuildDetailsForNoCommitFailedSuccessTests() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action",
				new BuildDetailsResultCallback("buildResult-NoCommit-FailedTests-SuccessfulTests.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
		apiHandler.logout();

		mockServer.verify();

		assertNotNull(build);
		assertEquals("13928", build.getVcsRevisionKey());
		// commit
		assertEquals(0, build.getCommitInfo().size());

		// failed tests
		assertEquals(2, build.getFailedTestDetails().size());
		assertEquals("com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerTest",
				build.getFailedTestDetails().iterator().next().getTestClassName());
		assertEquals("testSingleSuccessResultForDisabledBuild",
				build.getFailedTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.012,
				build.getFailedTestDetails().iterator().next().getTestDuration());
		assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_FAILED,
				build.getFailedTestDetails().iterator().next().getTestResult());

		// successful tests
		assertEquals(117, build.getSuccessfulTestDetails().size());
		assertEquals("com.atlassian.theplugin.commons.bamboo.BambooServerFacadeTest",
				build.getSuccessfulTestDetails().iterator().next().getTestClassName());
		assertEquals("testProjectList",
				build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.046,
				build.getSuccessfulTestDetails().iterator().next().getTestDuration());
		assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_SUCCEED,
				build.getSuccessfulTestDetails().iterator().next().getTestResult());
	}

	public void testBuildDetailsForNonExistingBuild() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("buildNotExistsResponse.xml", "200"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		try {
			apiHandler.getBuildResultDetails("TP-DEF", 200);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		apiHandler.logout();

		mockServer.verify();
	}

	public void testBuildDetailsMalformedResponse() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("malformedBuildResult.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		try {
			apiHandler.getBuildResultDetails("TP-DEF", 100);
			fail();
		} catch (RemoteApiException e) {
			assertEquals("org.jdom.input.JDOMParseException", e.getCause().getClass().getName());
		}
		apiHandler.logout();

		mockServer.verify();
	}

	public void testBuildDetailsEmptyResponse() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("emptyResponse.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
		apiHandler.logout();

		assertEquals(0, build.getCommitInfo().size());
		assertEquals(0, build.getSuccessfulTestDetails().size());
		assertEquals(0, build.getFailedTestDetails().size());

		mockServer.verify();
	}

	public void testAddSimpleLabel() throws Exception {
		String label = "label siple text";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addLabelToBuild("TP-DEF", 100, label);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddEmptyLabel() throws Exception {
		String label = "";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addLabelToBuild("TP-DEF", 100, label);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddMultiLineLabel() throws Exception {
		String label = "Label first line\nLabel second line	\nLabel third line";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addLabelToBuild("TP-DEF", 100, label);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddLabelToNonExistingBuild() throws Exception {
		String label = "Label";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label, "200", AddLabelToBuildCallback.NON_EXIST_FAIL));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		try {
			apiHandler.addLabelToBuild("TP-DEF", 200, label);
			fail();
		} catch (RemoteApiException e) {

		}
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddComment() throws Exception {
		String comment = "comment siple text";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(comment));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addCommentToBuild("TP-DEF", 100, comment);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddEmptyComment() throws Exception {
		String comment = "";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(comment));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addCommentToBuild("TP-DEF", 100, comment);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddMultiLineComment() throws Exception {
		String comment = "Comment first line\nComment ; second line	\nComment third line";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(comment));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addCommentToBuild("TP-DEF", 100, comment);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddCommentToNonExistingBuild() throws Exception {
		String comment = "Comment";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(comment, "200", AddCommentToBuildCallback.NON_EXIST_FAIL));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		try {
			apiHandler.addCommentToBuild("TP-DEF", 200, comment);
			fail();
		} catch (RemoteApiException e) {

		}
		apiHandler.logout();

		mockServer.verify();
	}

	public void testExecuteBuild() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/executeBuild.action", new ExecuteBuildCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.executeBuild("TP-DEF");
		apiHandler.logout();

		mockServer.verify();
	}

	public void testExecuteBuildFailed() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/executeBuild.action", new ExecuteBuildCallback(ExecuteBuildCallback.NON_EXIST_FAIL));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		try {
			apiHandler.executeBuild("TP-DEF");
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		apiHandler.logout();

		mockServer.verify();
	}

	public void testRenewSession() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());
		mockServer.expect("/api/rest/listProjectNames.action", new ErrorMessageCallback("authExpiredResponse.xml"));
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new AutoRenewBambooSession(new BambooServerCfg("mockbamboo", mockBaseUrl, new ServerId()),
				new HttpSessionCallbackImpl());
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.listProjectNames();
		List<BambooProject> projects = apiHandler.listProjectNames();		
		apiHandler.logout();

		Util.verifyProjectListResult(projects);

		mockServer.verify();
	}

	public void testOutOfRangePort() {
		try {
			BambooSession apiHandler = new BambooSessionImpl("http://localhost:80808");
			apiHandler.login(USER_NAME, PASSWORD.toCharArray());
			fail("Exception expected");
		} catch (RemoteApiException e) {
			assertTrue("MalformedURLException expected", e.getCause() instanceof IOException);
		}

	}

	public void testEnabledStatus() throws RemoteApiException {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultVelocityBallback("PO-TP", 123));
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultVelocityBallback("PT-TOP", 45));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession session = new BambooSessionImpl(mockBaseUrl);
		session.login(USER_NAME, PASSWORD.toCharArray());

		BambooBuild bbi1 = session.getLatestBuildForPlan("PO-TP");
		assertEquals(123, bbi1.getBuildNumber());
		assertTrue(bbi1.getEnabled());

		BambooBuild bbi2 = session.getLatestBuildForPlan("PT-TOP");
		assertEquals(45, bbi2.getBuildNumber());
		assertFalse(bbi2.getEnabled());
		session.logout();

		mockServer.verify();

	}

	public void testGetBuildLogs() throws RemoteApiException, UnsupportedEncodingException {
		final String TEXT = "ĄŚĆŹ$&#";
		final String charset1 = "UTF-8";
		final String charset2 = "UTF-16";
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/download/myplan/build_logs/myplan-123.log", new BuildLogCallback(TEXT, charset1));
		mockServer.expect("/download/myplan/build_logs/myplan-123.log", new BuildLogCallback(TEXT, charset2));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		final BambooSession session = new BambooSessionImpl(mockBaseUrl);
		session.login(USER_NAME, PASSWORD.toCharArray());

		assertEquals(TEXT, session.getBuildLogs("myplan", 123));
		assertEquals(TEXT, session.getBuildLogs("myplan", 123));
		session.logout();
		mockServer.verify();

	}

	private static class BuildLogCallback implements JettyMockServer.Callback {
		private final String text;
		private final String charsetName;

		public BuildLogCallback(final String text, final String charsetName) {
			this.text = text;
			this.charsetName = charsetName;
		}

		public void onExpectedRequest(final String target, final HttpServletRequest request,
				final HttpServletResponse response) throws Exception {
			final ServletOutputStream out = response.getOutputStream();
			response.setContentType("text/plain; charset=" + charsetName + "");
			out.write(text.getBytes(charsetName));
			out.close();
		}
	}
}
