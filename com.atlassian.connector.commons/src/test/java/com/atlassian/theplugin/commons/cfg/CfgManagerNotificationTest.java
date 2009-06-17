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
package com.atlassian.theplugin.commons.cfg;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @author Jacek Jaroczynski
 */
public class CfgManagerNotificationTest extends TestCase {

	private CfgManager cfgManager;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		cfgManager = new CfgManagerImpl() {
			public ServerData getServerData(final Server serverCfg) {
				return null;
			}

			public ServerData getServerData(final ProjectId projectId, final ServerId serverId) {
				return null;
			}
		};
		populateServerCfgs();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	private static final ProjectId PROJECT_ID_1 = new ProjectId();
	private static final ProjectId PROJECT_ID_2 = new ProjectId();
	private static final ProjectId PROJECT_ID_3 = new ProjectId("emptyProject");

	private final BambooServerCfg bamboo1 = new BambooServerCfg("bamboo1", new ServerId());
	private final BambooServerCfg bamboo = new BambooServerCfg("bamboo", new ServerId());
	private final CrucibleServerCfg crucible2 = new CrucibleServerCfg("crucible2", new ServerId());
	private final JiraServerCfg jira1 = new JiraServerCfg("jira1", new ServerId());
	private final JiraServerCfg jira = new JiraServerCfg("jira", new ServerId());

	private void populateServerCfgs() {
//		cfgManager.addGlobalServer(bamboo);
//		cfgManager.addGlobalServer(jira);

		cfgManager.addProjectSpecificServer(PROJECT_ID_1, bamboo1);
		cfgManager.addProjectSpecificServer(PROJECT_ID_1, jira1);

		cfgManager.addProjectSpecificServer(PROJECT_ID_2, crucible2);

		cfgManager.updateProjectConfiguration(PROJECT_ID_3, new ProjectConfiguration());
	}


	public void testlNotifications() {

		final ProjectConfiguration emptyCfg = ProjectConfiguration.emptyConfiguration();
		ConfigurationListener project1Listener = EasyMock.createMock(ConfigurationListener.class);
		ConfigurationListener project2Listener = EasyMock.createMock(ConfigurationListener.class);
		Object[] mocks = {project1Listener, project2Listener};

		// record
		project1Listener.configurationUpdated(emptyCfg);
		project1Listener.serverRemoved(bamboo1);
		project1Listener.serverRemoved(jira1);
		project1Listener.bambooServersChanged(emptyCfg);
		project1Listener.jiraServersChanged(emptyCfg);

		// test
		EasyMock.replay(mocks);
		cfgManager.addProjectConfigurationListener(PROJECT_ID_1, project1Listener);
		cfgManager.addProjectConfigurationListener(PROJECT_ID_2, project2Listener);

//		cfgManager.updateGlobalConfiguration(new GlobalConfiguration());
		cfgManager.updateProjectConfiguration(PROJECT_ID_1, emptyCfg);

		EasyMock.verify(mocks);
		EasyMock.reset(mocks);

		// record
		project2Listener.configurationUpdated(emptyCfg);
		project2Listener.serverRemoved(crucible2);
		project2Listener.crucibleServersChanged(emptyCfg);

		// test
		EasyMock.replay(mocks);
//		cfgManager.updateGlobalConfiguration(new GlobalConfiguration());
		cfgManager.updateProjectConfiguration(PROJECT_ID_2, ProjectConfiguration.emptyConfiguration());

		EasyMock.verify(mocks);
		EasyMock.reset(mocks);

		cfgManager.removeProjectConfigurationListener(PROJECT_ID_1, project1Listener);
		final ProjectConfiguration nonEmptyCfg = new ProjectConfiguration(MiscUtil.<ServerCfg>buildArrayList(bamboo1));

		// record
		// now only project2Listener will be notified
		project2Listener.configurationUpdated(nonEmptyCfg);
		project2Listener.serverAdded(bamboo1);
		project2Listener.bambooServersChanged(nonEmptyCfg);

		// test
		EasyMock.replay(mocks);
//		cfgManager.updateGlobalConfiguration(new GlobalConfiguration());
		cfgManager.updateProjectConfiguration(PROJECT_ID_2, nonEmptyCfg);
		cfgManager.updateProjectConfiguration(PROJECT_ID_1, nonEmptyCfg);
		EasyMock.verify(mocks);
	}


	public void testAddListener() {
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Throwable {
				cfgManager.addProjectConfigurationListener(PROJECT_ID_1, null);
			}
		});
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Throwable {
				cfgManager.addProjectConfigurationListener(null, EasyMock.createNiceMock(ConfigurationListener.class));
			}
		});

	}

}
