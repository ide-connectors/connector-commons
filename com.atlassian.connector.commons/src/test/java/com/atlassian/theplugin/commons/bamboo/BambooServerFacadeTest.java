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

import com.atlassian.theplugin.bamboo.api.bamboomock.*;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.bamboo.api.BambooSession;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.remoteapi.ErrorResponse;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;

/**
 * {@link com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl} test.
 */
public class BambooServerFacadeTest extends TestCase {

	private static DateTimeFormatter buildDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	private static final String USER_NAME = "someUser";
	private static final String PASSWORD = "somePassword";
	private static final String PLAN_ID = "TP-DEF"; // always the same - mock does the logic

	private org.mortbay.jetty.Server httpServer;
	private JettyMockServer mockServer;
	private String mockBaseUrl;
	private BambooServerFacade testedBambooServerFacade;
	private BambooServerCfg bambooServerCfg;

	@Override
	protected void setUp() throws Exception {
		//log
		// @TODO all remove it when ConfigurationFactory is not needed by HttpClientFactory 
		ConfigurationFactory.setConfiguration(new PluginConfigurationBean());

		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();

		mockServer = new JettyMockServer(httpServer);
		bambooServerCfg = createBambooTestConfiguration(mockBaseUrl, true);

		testedBambooServerFacade = BambooServerFacadeImpl.getInstance(LoggerImpl.getInstance());
	}

	private static BambooServerCfg createBambooTestConfiguration(String serverUrl, boolean isPassInitialized) {

		BambooServerCfg server = new BambooServerCfg("TestServer", new ServerIdImpl());
		server.setName("TestServer");
		server.setUrl(serverUrl);
		server.setUsername(USER_NAME);

		server.setPassword(isPassInitialized ? PASSWORD : "");
//		server.transientSetIsConfigInitialized(isPassInitialized);

		ArrayList<SubscribedPlan> plans = new ArrayList<SubscribedPlan>();
		for (int i = 1; i <= 3; ++i) {
			SubscribedPlan plan = new SubscribedPlan(PLAN_ID);
			plans.add(plan);
		}

		server.setPlans(plans);
		return server;
	}

	@Override
	protected void tearDown() throws Exception {
		mockServer = null;
		mockBaseUrl = null;
		httpServer.stop();
	}

	public void testGetSessionTwoTheSameServers() throws RemoteApiException {
		BambooServerFacadeImpl facade = new BambooServerFacadeImpl(LoggerImpl.getInstance(), new BambooSessionFactory() {
			public BambooSession createSession(final ServerData serverData, final HttpSessionCallback callback) throws
					RemoteApiException {
				BambooSession session = EasyMock.createMock(BambooSession.class);
				EasyMock.expect(session.isLoggedIn()).andReturn(true).anyTimes();
				EasyMock.replay(session);
				return session;
			}
		});

		BambooServerCfg server1 = createBambooServerCfg("http://atlassian.com", "", "");
		BambooServerCfg server1clone = createBambooServerCfg("http://atlassian.com", "", "");
		BambooServerCfg server2 = createBambooServerCfg("http://spartez.com", "", "");

		assertEquals(facade.getSession(getServerData(server1)), facade.getSession(getServerData(server1)));

		// servers with different serverId should have different sessions event if url, username and password are equals
		TestUtil.assertNotEquals(facade.getSession(getServerData(server1)), facade.getSession(getServerData(server1clone)));
		TestUtil.assertNotEquals(facade.getSession(getServerData(server1)), facade.getSession(getServerData(server2)));
	}

	public void testSubscribedBuildStatus() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("FAILED"));
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("WRONG"));

		Collection<BambooBuild> plans = testedBambooServerFacade.getSubscribedPlansResults(
				getServerData(bambooServerCfg), bambooServerCfg.getPlans(),
				bambooServerCfg.isUseFavourites(), bambooServerCfg.getTimezoneOffset());

		assertNotNull(plans);
		assertEquals(3, plans.size());
		Iterator<BambooBuild> iterator = plans.iterator();
		Util.verifySuccessfulBuildResult(iterator.next(), mockBaseUrl);
		Util.verifyFailedBuildResult(iterator.next(), mockBaseUrl);
		Util.verifyErrorBuildResult(iterator.next());

		mockServer.verify();
	}

	public void testBuildCompletedDate() throws Exception {
		implTestBuildCompletedDate(0);
	}

	private void implTestBuildCompletedDate(int timezoneOffset) throws Exception {
		bambooServerCfg.setTimezoneOffset(timezoneOffset);
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("bc"));
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("bt"));

		final Collection<BambooBuild> plans = testedBambooServerFacade.getSubscribedPlansResults(
				getServerData(bambooServerCfg), bambooServerCfg.getPlans(),
				bambooServerCfg.isUseFavourites(), bambooServerCfg.getTimezoneOffset());
		assertNotNull(plans);
		assertEquals(3, plans.size());
		Date completedDate = parseBuildDate("2008-12-12 03:08:10");
		final DateTime adjustedDate = new DateTime(completedDate.getTime()).plusHours(timezoneOffset);

		Iterator<BambooBuild> iterator = plans.iterator();
		Util.verifyBuildCompletedDate(iterator.next(), adjustedDate.toDate());
		Util.verifyBuildCompletedDate(iterator.next(), adjustedDate.toDate());

		mockServer.verify();
	}

	public void testBuildCompletedDateWithTimezoneOffset() throws Exception {
		implTestBuildCompletedDate(3);
	}

	public void testBuildCompletedDateWithTimezoneOffset2() throws Exception {
		implTestBuildCompletedDate(-5);
	}

	private BambooBuild createBambooBuildInfo(BambooServerCfg serverCfg,
			String planKey, String planName, DateTime buildCompletionDate) {
		return new BambooBuildInfo.Builder(planKey, planName, getServerData(serverCfg), null, 123,
				BuildStatus.UNKNOWN)
				.completionTime(buildCompletionDate.toDate())
				.build();
	}

	public void testBuildCompletedDateWithTimeZoneForFavouritesMinus()
			throws RemoteApiException, ServerPasswordNotProvidedException {
		final BambooSession mockSession = EasyMock.createMock(BambooSession.class);
		BambooServerFacade facade = new BambooServerFacadeImpl(LoggerImpl.getInstance(), new BambooSessionFactory() {
			public BambooSession createSession(final ServerData serverData, final HttpSessionCallback callback)
					throws RemoteApiException {
				return mockSession;
			}
		});

		final String key1 = "pl";
		final DateTime buildDate1 = new DateTime(2009, 1, 10, 21, 29, 4, 0);
		final BambooPlan plan1 = new BambooPlan("planname1", key1);
		final BambooPlan plan2 = new BambooPlan("planname2-nofavourite", "keya");
		final String key3 = "keyb";
		final DateTime buildDate3 = new DateTime(2009, 3, 27, 1, 9, 0, 0);
		final BambooPlan plan3 = new BambooPlan("planname3", key3, false);

		List<BambooPlan> plans = MiscUtil.buildArrayList(plan1, plan2, plan3);
		EasyMock.expect(mockSession.listPlanNames()).andReturn(plans).anyTimes();
		EasyMock.expect(mockSession.getFavouriteUserPlans()).andReturn(MiscUtil.buildArrayList("pl", "keyb")).anyTimes();
		EasyMock.expect(mockSession.isLoggedIn()).andReturn(true).anyTimes();
		EasyMock.expect(mockSession.getLatestBuildForPlan(key1, true, -7)).andAnswer(new IAnswer<BambooBuild>() {
			public BambooBuild answer() throws Throwable {
				synchronized (bambooServerCfg) {
					return createBambooBuildInfo(bambooServerCfg, key1, plan1.getPlanName(),
							buildDate1.plusHours(bambooServerCfg.getTimezoneOffset()));
				}
			}
		}).anyTimes();
		EasyMock.expect(mockSession.getLatestBuildForPlan(key3, false, -7)).andAnswer(new IAnswer<BambooBuild>() {
			public BambooBuild answer() throws Throwable {
				synchronized (bambooServerCfg) {
					return createBambooBuildInfo(bambooServerCfg, key3, plan3.getPlanName(),
							buildDate3.plusHours(bambooServerCfg.getTimezoneOffset()));
				}
			}
		}).anyTimes();
		EasyMock.replay(mockSession);
		bambooServerCfg.setUseFavourites(true);


		getAndVerifyDates(facade, buildDate1, buildDate3, -7);
		EasyMock.verify(mockSession);
	}

	public void testBuildCompletedDateWithTimeZoneForFavouritesPlus()
			throws RemoteApiException, ServerPasswordNotProvidedException {
		final BambooSession mockSession = EasyMock.createMock(BambooSession.class);
		BambooServerFacade facade = new BambooServerFacadeImpl(LoggerImpl.getInstance(), new BambooSessionFactory() {
			public BambooSession createSession(final ServerData serverData, final HttpSessionCallback callback)
					throws RemoteApiException {
				return mockSession;
			}
		});

		final String key1 = "pl";
		final DateTime buildDate1 = new DateTime(2009, 1, 10, 21, 29, 4, 0);
		final BambooPlan plan1 = new BambooPlan("planname1", key1);
		final BambooPlan plan2 = new BambooPlan("planname2-nofavourite", "keya");
		final String key3 = "keyb";
		final DateTime buildDate3 = new DateTime(2009, 3, 27, 1, 9, 0, 0);
		final BambooPlan plan3 = new BambooPlan("planname3", key3, false);
		int timeZoneOffset = 0;
		List<BambooPlan> plans = MiscUtil.buildArrayList(plan1, plan2, plan3);
		EasyMock.expect(mockSession.listPlanNames()).andReturn(plans).anyTimes();
		EasyMock.expect(mockSession.getFavouriteUserPlans()).andReturn(MiscUtil.buildArrayList("pl", "keyb")).anyTimes();
		EasyMock.expect(mockSession.isLoggedIn()).andReturn(true).anyTimes();
		EasyMock.expect(mockSession.getLatestBuildForPlan(key1, true, 2)).andAnswer(new IAnswer<BambooBuild>() {
			public BambooBuild answer() throws Throwable {
				synchronized (bambooServerCfg) {
					return createBambooBuildInfo(bambooServerCfg, key1, plan1.getPlanName(),
							buildDate1.plusHours(bambooServerCfg.getTimezoneOffset()));
				}
			}
		}).anyTimes();
		EasyMock.expect(mockSession.getLatestBuildForPlan(key3, false, 2)).andAnswer(new IAnswer<BambooBuild>() {
			public BambooBuild answer() throws Throwable {
				synchronized (bambooServerCfg) {
					return createBambooBuildInfo(bambooServerCfg, key3, plan3.getPlanName(),
							buildDate3.plusHours(bambooServerCfg.getTimezoneOffset()));
				}
			}
		}).anyTimes();
		EasyMock.replay(mockSession);
		bambooServerCfg.setUseFavourites(true);

		timeZoneOffset = 2;
		getAndVerifyDates(facade, buildDate1, buildDate3, 2);
		EasyMock.verify(mockSession);
	}

	private Collection<BambooBuild> getAndVerifyDates(final BambooServerFacade facade, final DateTime buildDate1,
			final DateTime buildDate3, final int hourOffset) throws ServerPasswordNotProvidedException {
		synchronized (bambooServerCfg) {
			bambooServerCfg.setTimezoneOffset(hourOffset);
		}
		final Collection<BambooBuild> res = facade.getSubscribedPlansResults(getServerData(bambooServerCfg),
				bambooServerCfg.getPlans(), bambooServerCfg.isUseFavourites(), bambooServerCfg.getTimezoneOffset());
		assertNotNull(res);
		assertEquals(2, res.size());

		Iterator<BambooBuild> iterator = res.iterator();
		Util.verifyBuildCompletedDate(iterator.next(), buildDate1.plusHours(hourOffset).toDate());
		Util.verifyBuildCompletedDate(iterator.next(), buildDate3.plusHours(hourOffset).toDate());
		return res;
	}

	public void testFailedLoginSubscribedBuildStatus() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));
		bambooServerCfg.setPassword(PASSWORD);
		Collection<BambooBuild> plans = testedBambooServerFacade.getSubscribedPlansResults(
				getServerData(bambooServerCfg), bambooServerCfg.getPlans(),
				bambooServerCfg.isUseFavourites(), bambooServerCfg.getTimezoneOffset());
		assertNotNull(plans);
		assertEquals(3, plans.size());
		Iterator<BambooBuild> iterator = plans.iterator();
		Util.verifyLoginErrorBuildResult(iterator.next());
		Util.verifyLoginErrorBuildResult(iterator.next());
		Util.verifyLoginErrorBuildResult(iterator.next());

		mockServer.verify();
	}

	public void testUninitializedPassword() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, "", LoginCallback.ALWAYS_FAIL));
		BambooServerCfg server = createBambooTestConfiguration(mockBaseUrl, false);
		try {
			testedBambooServerFacade.getSubscribedPlansResults(getServerData(server), server.getPlans(),
					server.isUseFavourites(), server.getTimezoneOffset());
			fail("Testing uninitialized password");

		} catch (ServerPasswordNotProvidedException e) {
			// ok: connection succeeded but server returned error
		}

		mockServer.expect("/api/rest/login.action", new ErrorResponse(400, ""));
		mockServer.expect("/api/rest/login.action", new ErrorResponse(400, ""));
		// connection error, just report without asking for the pass
		Collection<BambooBuild> plans = testedBambooServerFacade
				.getSubscribedPlansResults(getServerData(server), server.getPlans(),
						server.isUseFavourites(), server.getTimezoneOffset());
		assertNotNull(plans);
		assertEquals(3, plans.size());
		Iterator<BambooBuild> iterator = plans.iterator();
		Util.verifyError400BuildResult(iterator.next());
		Util.verifyError400BuildResult(iterator.next());
		Util.verifyError400BuildResult(iterator.next());

		server.setUrl("malformed");
		plans = testedBambooServerFacade.getSubscribedPlansResults(getServerData(server), server.getPlans(),
				server.isUseFavourites(), server.getTimezoneOffset());
		assertNotNull(plans);
		assertEquals(3, plans.size());
		iterator = plans.iterator();
		assertEquals("Malformed server URL: malformed", iterator.next().getErrorMessage());
		assertEquals("Malformed server URL: malformed", iterator.next().getErrorMessage());
		assertEquals("Malformed server URL: malformed", iterator.next().getErrorMessage());

		mockServer.verify();

	}

	public void testProjectList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());

		Collection<BambooProject> projects = testedBambooServerFacade.getProjectList(
				getServerData(bambooServerCfg));
		Util.verifyProjectListResult(projects);

		mockServer.verify();
	}

	public void testFailedProjectList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		try {
			testedBambooServerFacade.getProjectList(getServerData(bambooServerCfg));
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testPlanList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());

		Collection<BambooPlan> plans = testedBambooServerFacade.getPlanList(getServerData(bambooServerCfg));
		Util.verifyPlanListWithFavouritesResult(plans);

		mockServer.verify();
	}

	public void testFailedPlanList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		try {
			testedBambooServerFacade.getPlanList(getServerData(bambooServerCfg));
			fail();
		} catch (RemoteApiLoginException e) {
			// expected exception
		}
		mockServer.verify();
	}

	private BambooServerCfg createBambooServerCfg(String url, String username, String password) {
		BambooServerCfg bambooServerCfg = new BambooServerCfg("mybamboo", url, new ServerIdImpl());
		bambooServerCfg.setUsername(username);
		bambooServerCfg.setPassword(password);
		return bambooServerCfg;
	}

	/**
	 * Regression for https://studio.atlassian.com/browse/ACC-40
	 *
	 * @throws Exception
	 */
	public void testConnectionTestInvalidUrlIncludesPassword() throws Exception {
		try {
			testedBambooServerFacade
					.testServerConnection(getServerData(createBambooServerCfg("http://invalid url", USER_NAME, PASSWORD)));
			fail("Should throw RemoteApiLoginException");
		} catch (RemoteApiException e) {
			assertFalse("Message should not include users's password", e.getMessage().contains(PASSWORD));
		}
	}

	public void testConnectionTest() throws Exception {

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());
		testedBambooServerFacade.testServerConnection(new ServerData(createServerCfg(), USER_NAME, PASSWORD));

		TestUtil.assertThrows(RemoteApiMalformedUrlException.class, new IAction() {
			public void run() throws Throwable {
				final ServerCfg serverCfg = createServerCfg();
				serverCfg.setUrl("");
				testedBambooServerFacade.testServerConnection(new ServerData(serverCfg, "", ""));
			}
		});
//
//
		mockServer.expect("/api/rest/login.action", new LoginCallback("", "", LoginCallback.ALWAYS_FAIL));
		TestUtil.assertThrows(RemoteApiLoginException.class, new IAction() {
			public void run() throws Throwable {
				testedBambooServerFacade.testServerConnection(new ServerData(createServerCfg(), "", ""));
			}
		});

		TestUtil.assertThrows(RemoteApiMalformedUrlException.class, new IAction() {
			public void run() throws Throwable {
				final ServerCfg serverCfg = createServerCfg();
				serverCfg.setUrl("");
				testedBambooServerFacade.testServerConnection(new ServerData(serverCfg, USER_NAME, ""));
			}
		});

		TestUtil.assertThrows(RemoteApiMalformedUrlException.class, new IAction() {
			public void run() throws Throwable {
				final ServerCfg serverCfg = createServerCfg();
				serverCfg.setUrl("");
				testedBambooServerFacade.testServerConnection(new ServerData(serverCfg, "", PASSWORD));
			}
		});

		mockServer.verify();
	}

	private ServerCfg createServerCfg() {
		return new ServerCfg(true, "name", mockBaseUrl, new ServerIdImpl()) {
			public ServerType getServerType() {
				return null;
			}

			public ServerCfg getClone() {
				return null;
			}
		};
	}


	public void testBambooConnectionWithEmptyPlan()
			throws RemoteApiLoginException, CloneNotSupportedException, ServerPasswordNotProvidedException {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());

		bambooServerCfg.getSubscribedPlans().clear();
		BambooServerFacade facade = BambooServerFacadeImpl.getInstance(LoggerImpl.getInstance());
		Collection<BambooBuild> plans = facade.getSubscribedPlansResults(getServerData(bambooServerCfg),
				bambooServerCfg.getPlans(), bambooServerCfg.isUseFavourites(),
				bambooServerCfg.getTimezoneOffset());
		assertEquals(0, plans.size());

		mockServer.verify();
	}

	public void testAddLabel() throws Exception {
		String label = "label";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));

		testedBambooServerFacade.addLabelToBuild(getServerData(bambooServerCfg),
				"TP-DEF", 100, label);

		mockServer.verify();
	}

	public void testAddEmptyLabel() throws Exception {
		String label = "";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));

		testedBambooServerFacade.addLabelToBuild(getServerData(bambooServerCfg), "TP-DEF", 100,
				label);

		mockServer.verify();
	}

	public void testAddLabelToNonExistingBuild() throws Exception {
		String label = "label";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addLabelToBuildResults.action",
				new AddLabelToBuildCallback(label, "200", AddLabelToBuildCallback.NON_EXIST_FAIL));

		try {
			testedBambooServerFacade.addLabelToBuild(getServerData(bambooServerCfg),
					"TP-DEF", 200, label);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testAddComment() throws Exception {
		String label = "label";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(label));

		testedBambooServerFacade.addCommentToBuild(getServerData(bambooServerCfg), "TP-DEF", 100,
				label);

		mockServer.verify();
	}

	public void testAddEmptyComment() throws Exception {
		String label = "";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(label));

		testedBambooServerFacade.addCommentToBuild(getServerData(bambooServerCfg), "TP-DEF", 100,
				label);

		mockServer.verify();
	}

	public void testAddCommentToNonExistingBuild() throws Exception {
		String label = "label";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addCommentToBuildResults.action",
				new AddCommentToBuildCallback(label, "200", AddCommentToBuildCallback.NON_EXIST_FAIL));

		try {
			testedBambooServerFacade.addCommentToBuild(getServerData(bambooServerCfg), "TP-DEF",
					200, label);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testExecuteBuild() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/executeBuild.action", new ExecuteBuildCallback());

		testedBambooServerFacade.executeBuild(getServerData(bambooServerCfg), "TP-DEF");

		mockServer.verify();
	}

	public void testFailedExecuteBuild() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/executeBuild.action", new ExecuteBuildCallback(ExecuteBuildCallback.NON_EXIST_FAIL));

		try {
			testedBambooServerFacade.executeBuild(getServerData(bambooServerCfg), "TP-DEF");
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testGetBuildDetails() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/getBuildResultsDetails.action",
				new BuildDetailsResultCallback("buildResult-3Commit-FailedTests-SuccessfulTests.xml", "100"));

		BuildDetails details = testedBambooServerFacade
				.getBuildDetails(getServerData(bambooServerCfg), "TP-DEF", 100);
		assertEquals(3, details.getCommitInfo().size());
		assertEquals(2, details.getFailedTestDetails().size());
		assertEquals(117, details.getSuccessfulTestDetails().size());

		mockServer.verify();
	}

	public void testGetBuildDetailsNonExistingBuild() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		//mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/getBuildResultsDetails.action",
				new BuildDetailsResultCallback("buildNotExistsResponse.xml", "200"));

		try {
			testedBambooServerFacade.getBuildDetails(getServerData(bambooServerCfg), "TP-DEF", 200);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}


		mockServer.verify();
	}

	private Date parseBuildDate(String date) {
		try {
			return buildDateFormat.parseDateTime(date).toDate();
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private ServerData getServerData(final Server serverCfg) {
		return new ServerData(serverCfg, serverCfg.getUserName(), serverCfg.getPassword());
	}
}
