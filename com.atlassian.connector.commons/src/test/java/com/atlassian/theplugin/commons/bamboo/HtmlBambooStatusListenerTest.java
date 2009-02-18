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

import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.easymock.EasyMock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * StausIconBambooListener Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/30/2008</pre>
 */
public class HtmlBambooStatusListenerTest extends TestCase {

	private StatusListenerResultCatcher output;
	private StatusIconBambooListener testedListener;

	private static final String DEFAULT_PLAN_KEY = "PLAN-ID";
	private static final int DEFAULT_BUILD_NO = 777;
	private static final String DEFAULT_PLAN_NAME = "Default Plan";
	private static final String DEFAULT_ERROR_MESSAGE = "default error message";
    private static final String DEFAULT_SERVER_URL = "http://test.atlassian.com/bamboo";
    private static final String DEFAULT_PROJECT_NAME = "ThePlugin";
	private static final String DEFAULT_PLAN_KEY_2 = "PLAN2-ID";
	private static final BambooServerCfg BAMBOO = new BambooServerCfg("mybamboo", DEFAULT_SERVER_URL, new ServerId());


	@Override
	protected void tearDown() throws Exception {
		output = null;
		testedListener = null;
		super.tearDown();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		output = new StatusListenerResultCatcher();
        testedListener = new StatusIconBambooListener(output);
	}

	public void testUpdateDisplay() {
		// create mock and tested object
		BambooStatusDisplay mockDisplay = EasyMock.createMock(BambooStatusDisplay.class);
		StatusIconBambooListener bambooListener = new StatusIconBambooListener(mockDisplay);

		// record mock
		mockDisplay.updateBambooStatus(EasyMock.eq(BuildStatus.UNKNOWN), EasyMock.isA(BambooPopupInfo.class));
		EasyMock.replay(mockDisplay);

		// test: empty builds (error connection) should be considered as unknown builds
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();
		final BambooBuildInfo buildUnknown = new BambooBuildInfo.Builder("whatever", null, BAMBOO, null, null,
				BuildStatus.UNKNOWN).build();
		builds.add(buildUnknown);
		bambooListener.updateBuildStatuses(builds);

		EasyMock.verify(mockDisplay);

	}

	public void testUpdateDisplayUnknownSuccessful() {

		BambooBuild buildUnknown = generateBuildInfo(BuildStatus.UNKNOWN);
		BambooBuild buildSuccessful = generateBuildInfo2(BuildStatus.SUCCESS);

		// create mock display and tested listener
		BambooStatusDisplay mockDisplay = EasyMock.createMock(BambooStatusDisplay.class);
		StatusIconBambooListener bambooListener = new StatusIconBambooListener(mockDisplay);

		// record mock
		mockDisplay.updateBambooStatus(EasyMock.eq(BuildStatus.SUCCESS), EasyMock.isA(BambooPopupInfo.class));
		EasyMock.replay(mockDisplay);

		// test: unknown and successful build should generate green (successful) state
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();
		builds.add(buildSuccessful);
		builds.add(buildUnknown);
		bambooListener.updateBuildStatuses(builds);

		EasyMock.verify(mockDisplay);
	}

	public void testUpdateDisplaySuccessful() {

		BambooBuild buildSuccessful = generateBuildInfo(BuildStatus.SUCCESS);

		// create mock display and tested listener
		BambooStatusDisplay mockDisplay = EasyMock.createMock(BambooStatusDisplay.class);
		StatusIconBambooListener bambooListener = new StatusIconBambooListener(mockDisplay);

		// record mock
		mockDisplay.updateBambooStatus(EasyMock.eq(BuildStatus.SUCCESS), EasyMock.isA(BambooPopupInfo.class));
		EasyMock.replay(mockDisplay);

		// test: successful build should generate green (successful) state
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();
		builds.add(buildSuccessful);
		bambooListener.updateBuildStatuses(builds);

		EasyMock.verify(mockDisplay);
	}

	public void testUpdateDisplayUnknown() {

		BambooBuild buildUnknown = generateBuildInfo(BuildStatus.UNKNOWN);

		// create mock display and tested listener
		BambooStatusDisplay mockDisplay = EasyMock.createMock(BambooStatusDisplay.class);
		StatusIconBambooListener bambooListener = new StatusIconBambooListener(mockDisplay);

		// record mock
		mockDisplay.updateBambooStatus(EasyMock.eq(BuildStatus.UNKNOWN), EasyMock.isA(BambooPopupInfo.class));
		EasyMock.replay(mockDisplay);

		// test: unknown build should generate grey (unknown) state
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();
		builds.add(buildUnknown);
		bambooListener.updateBuildStatuses(builds);

		EasyMock.verify(mockDisplay);
	}

	public void testNullStatusCollection() throws Exception {
		testedListener.updateBuildStatuses(null);
		assertEquals(1, output.count);
		assertSame(BuildStatus.UNKNOWN, output.buildStatus);
//		assertEquals(
//                "<html>" + StatusIconBambooListener.BODY_WITH_STYLE + "No plans defined.</body></html>",
//                output.htmlPage);
	}

	public void testEmptyStatusCollection() throws Exception {
		testedListener.updateBuildStatuses(new ArrayList<BambooBuild>());
		assertEquals(1, output.count);
		assertSame(BuildStatus.UNKNOWN, output.buildStatus);
//        assertEquals(
//                "<html>" + StatusIconBambooListener.BODY_WITH_STYLE + "No plans defined.</body></html>",
//                output.htmlPage);
	}

	public void testSingleSuccessResult() throws Exception {
		Collection<BambooBuild> buildInfo = new ArrayList<BambooBuild>();

		buildInfo.add(generateBuildInfo(BuildStatus.SUCCESS));
		testedListener.updateBuildStatuses(buildInfo);

        assertSame(BuildStatus.SUCCESS, output.buildStatus);

////		HtmlTable table = output.response.getTheTable();
////		assertEquals(2, table.getRowCount());
//
//		testSuccessRow(table.getRow(1));

	}

	public void testSingleSuccessResultForDisabledBuild() throws Exception {
		Collection<BambooBuild> buildInfo = new ArrayList<BambooBuild>();

		buildInfo.add(generateDisabledBuildInfo(BuildStatus.SUCCESS));
		testedListener.updateBuildStatuses(buildInfo);

		// disabled builds are not considered
		assertSame(BuildStatus.UNKNOWN, output.buildStatus);

//		HtmlTable table = output.response.getTheTable();
//		assertEquals(2, table.getRowCount());
//
//		testDisabledSuccessRow(table.getRow(1));

	}

	public void testSingleFailedResult() throws Exception {
		Collection<BambooBuild> buildInfo = new ArrayList<BambooBuild>();
		buildInfo.add(generateBuildInfo(BuildStatus.FAILURE));
		testedListener.updateBuildStatuses(buildInfo);

		// disabled builds are not considered
		assertSame(BuildStatus.FAILURE, output.buildStatus);

//		HtmlTable table = output.response.getTheTable();
//		assertEquals(2, table.getRowCount());
//
//		testFailedRow(table.getRow(1));

	}

	public void testSingleFailedResultForDisabledBuild() throws Exception {
		Collection<BambooBuild> buildInfo = new ArrayList<BambooBuild>();
		buildInfo.add(generateDisabledBuildInfo(BuildStatus.FAILURE));
		testedListener.updateBuildStatuses(buildInfo);
		assertSame(BuildStatus.UNKNOWN, output.buildStatus);

//		HtmlTable table = output.response.getTheTable();
//		assertEquals(2, table.getRowCount());
//
//		testDisabledFailedRow(table.getRow(1));

	}

	public void testSingleErrorResult() throws Exception {
		Collection<BambooBuild> buildInfo = new ArrayList<BambooBuild>();
		buildInfo.add(generateBuildInfo(BuildStatus.UNKNOWN));
		testedListener.updateBuildStatuses(buildInfo);
		assertSame(BuildStatus.UNKNOWN, output.buildStatus);
//
//		HtmlTable table = output.response.getTheTable();
//		assertEquals(2, table.getRowCount());
//
//		testErrorRow(table.getRow(1));

	}

	@SuppressWarnings("unchecked")
	private static void testSuccessRow(HtmlTableRow tableRow) throws Exception {
		List<HtmlTableCell> cells = tableRow.getCells();
		assertEquals(3, cells.size());

        assertEquals("<td width=\"1%\"><a href=\"" + DEFAULT_SERVER_URL + "/browse/PLAN-ID\"/></td>", trimWhitespace(cells.get(0).asXml()));
		assertEquals(DEFAULT_PROJECT_NAME + " " + DEFAULT_PLAN_NAME + " > PLAN-ID-777", cells.get(1).asText());

		String buildTime = cells.get(2).asText().trim();
		assertTrue(buildTime.length() > 1);
		assertFalse("---".equals(buildTime));
	}

	@SuppressWarnings("unchecked")
	private static void testDisabledSuccessRow(HtmlTableRow tableRow) throws Exception {
		List<HtmlTableCell> cells = tableRow.getCells();
		assertEquals(3, cells.size());

        assertEquals("<td width=\"1%\"><a href=\"" + DEFAULT_SERVER_URL + "/browse/PLAN-ID\"/></td>", trimWhitespace(cells.get(0).asXml()));
		assertEquals(DEFAULT_PROJECT_NAME + " " + DEFAULT_PLAN_NAME + " > Disabled", cells.get(1).asText());

		String buildTime = cells.get(2).asText().trim();
		assertTrue(buildTime.length() == 0);
		assertFalse("---".equals(buildTime));
	}

	private static String trimWhitespace(String s) {
        StringBuffer result = new StringBuffer("");
        for (StringTokenizer stringTokenizer = new StringTokenizer(s, "\n"); stringTokenizer.hasMoreTokens();) {
            result.append(stringTokenizer.nextToken().trim());
        }
        return result.toString();
    }

    @SuppressWarnings("unchecked")
	private static void testFailedRow(HtmlTableRow tableRow) throws Exception {
		List<HtmlTableCell> cells = tableRow.getCells();
		assertEquals(3, cells.size());

        assertEquals("<td width=\"1%\"><a href=\"" + DEFAULT_SERVER_URL + "/browse/PLAN-ID\"/></td>", trimWhitespace(cells.get(0).asXml()));
		assertEquals(DEFAULT_PROJECT_NAME + " " + DEFAULT_PLAN_NAME + " > PLAN-ID-777", cells.get(1).asText());

		String buildTime = cells.get(2).asText().trim();
		assertFalse("&nbsp;".equals(buildTime));
	}

    @SuppressWarnings("unchecked")
	private static void testDisabledFailedRow(HtmlTableRow tableRow) throws Exception {
		List<HtmlTableCell> cells = tableRow.getCells();
		assertEquals(3, cells.size());

        assertEquals("<td width=\"1%\"><a href=\"" + DEFAULT_SERVER_URL + "/browse/PLAN-ID\"/></td>", trimWhitespace(cells.get(0).asXml()));
		assertEquals(DEFAULT_PROJECT_NAME + " " + DEFAULT_PLAN_NAME + " > Disabled", cells.get(1).asText());

		String buildTime = cells.get(2).asText().trim();
		assertTrue(buildTime.length() == 0);
	}

	@SuppressWarnings("unchecked")
	private static void testErrorRow(HtmlTableRow tableRow) throws Exception {
		List<HtmlTableCell> cells = tableRow.getCells();
		assertEquals(3, cells.size());

        assertEquals("<td width=\"1%\"><a href=\"" + DEFAULT_SERVER_URL + "/browse/PLAN-ID\"/></td>", trimWhitespace(cells.get(0).asXml()));
		assertEquals(DEFAULT_ERROR_MESSAGE, cells.get(1).asText());
        assertEquals("<td width=\"100%\"><font color=\"#999999\">" + DEFAULT_ERROR_MESSAGE + "</font></td>", trimWhitespace(cells.get(1).asXml()));

		assertEquals("", cells.get(2).asText().trim());
	}

	public static BambooBuild generateBuildInfo(BuildStatus status) {
		BambooBuildInfo.Builder builder = new BambooBuildInfo.Builder(DEFAULT_PLAN_KEY, DEFAULT_PLAN_NAME, BAMBOO,
				DEFAULT_PROJECT_NAME, DEFAULT_BUILD_NO, status).enabled(true).pollingTime(new Date());

		switch (status) {
			case UNKNOWN:
				builder.errorMessage(DEFAULT_ERROR_MESSAGE);
				break;
			case SUCCESS:
				builder.startTime(new Date());
				break;
			case FAILURE:
				builder.startTime(new Date());
				break;
		}

		return builder.build();
	}

	public static BambooBuild generateDisabledBuildInfo(BuildStatus status) {
		BambooBuildInfo.Builder builder = new BambooBuildInfo.Builder(DEFAULT_PLAN_KEY, DEFAULT_PLAN_NAME, BAMBOO,
				DEFAULT_PROJECT_NAME, DEFAULT_BUILD_NO, status).enabled(false).pollingTime(new Date());

		switch (status) {
			case UNKNOWN:
				builder.errorMessage(DEFAULT_ERROR_MESSAGE);
				break;
			case SUCCESS:
				builder.startTime(new Date());
				break;
			case FAILURE:
				builder.startTime(new Date());
				break;
			default:
				break;
		}

		return builder.build();
	}

	public static BambooBuild generateBuildInfo2(BuildStatus status) {
		BambooBuildInfo.Builder builder = new BambooBuildInfo.Builder(DEFAULT_PLAN_KEY_2, DEFAULT_PLAN_NAME, BAMBOO,
				DEFAULT_PROJECT_NAME, DEFAULT_BUILD_NO, status).pollingTime(new Date());

        switch (status) {
			case UNKNOWN:
				builder.errorMessage(DEFAULT_ERROR_MESSAGE);
				break;
			case SUCCESS:
				builder.startTime(new Date());
				break;
			case FAILURE:
				builder.startTime(new Date());
				break;
			default:
				break;
		}

		return builder.build();
	}

	public static Test suite() {
		return new TestSuite(HtmlBambooStatusListenerTest.class);
	}


	static class StatusListenerResultCatcher implements BambooStatusDisplay {
		private BuildStatus buildStatus;
		private String htmlPage;
		private ResponseWrapper response;

		private int count;

		public void updateBambooStatus(BuildStatus generalBuildStatus, BambooPopupInfo info) {
			buildStatus = generalBuildStatus;
			this.htmlPage = info.toHtml();

			++count;

			try {
				response = new ResponseWrapper(info.toHtml());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public String getHtmlPage() {
			return htmlPage;
		}
	}
}

