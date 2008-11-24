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

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @author Jacek Jaroczynski
 */
public class CfgManagerNotificationSecondTest extends TestCase {

	private CfgManager cfgManager;
	private ConfigurationListener listener;
	private ProjectConfiguration baseConf;
	private static final String SUFFIX = "SUFFIX";

	@Override
	public void setUp() throws Exception {
		super.setUp();
		cfgManager = new CfgManagerImpl();
		listener = EasyMock.createMock(ConfigurationListener.class);
		cfgManager.addProjectConfigurationListener(PROJECT_ID, listener);

		populateServerCfgs();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	private static final ProjectId PROJECT_ID = new ProjectId();

	private final BambooServerCfg bamboo1 = new BambooServerCfg("bamboo1", new ServerId());
	private final BambooServerCfg bamboo2 = new BambooServerCfg("bamboo2", new ServerId());
	private final CrucibleServerCfg crucible1 = new CrucibleServerCfg("crucible1", new ServerId());
	private final CrucibleServerCfg crucible2 = new CrucibleServerCfg("crucible2", new ServerId());
	private final JiraServerCfg jira1 = new JiraServerCfg("jira1", new ServerId());
	private final JiraServerCfg jira2 = new JiraServerCfg("jira2", new ServerId());

	private void populateServerCfgs() {

		cfgManager.addProjectSpecificServer(PROJECT_ID, bamboo1);
		cfgManager.addProjectSpecificServer(PROJECT_ID, jira1);
		cfgManager.addProjectSpecificServer(PROJECT_ID, crucible1);

		baseConf = new ProjectConfiguration(cfgManager.getProjectConfiguration(PROJECT_ID));
	}

	public void testServerAdded() {

		baseConf.getServers().add(bamboo2);
		baseConf.getServers().add(crucible2);
		baseConf.getServers().add(jira2);

		// record
		listener.configurationUpdated(baseConf);
		listener.serverAdded(bamboo2);
		listener.serverAdded(crucible2);
		listener.serverAdded(jira2);

		// test
		EasyMock.replay(listener);
		cfgManager.updateProjectConfiguration(PROJECT_ID, baseConf);
		
		EasyMock.verify(listener);
	}

	public void testServerRemoved() {

		baseConf.getServers().remove(bamboo1);
		baseConf.getServers().remove(crucible1);
		baseConf.getServers().remove(jira1);

		// record
		listener.configurationUpdated(baseConf);
		listener.serverRemoved(bamboo1);
		listener.serverRemoved(crucible1);
		listener.serverRemoved(jira1);

		// test
		EasyMock.replay(listener);
		cfgManager.updateProjectConfiguration(PROJECT_ID, baseConf);

		EasyMock.verify(listener);
	}

	public void testServerDisabledEnabled() {

		baseConf.getServerCfg(bamboo1.getServerId()).setEnabled(false);
		baseConf.getServerCfg(crucible1.getServerId()).setEnabled(false);
		baseConf.getServerCfg(jira1.getServerId()).setEnabled(false);

		// record
		listener.configurationUpdated(baseConf);
		listener.serverDisabled(bamboo1.getServerId());
		listener.serverDisabled(crucible1.getServerId());
		listener.serverDisabled(jira1.getServerId());
		listener.serverDataUpdated(bamboo1.getServerId());
		listener.serverDataUpdated(crucible1.getServerId());
		listener.serverDataUpdated(jira1.getServerId());

		// test disabled
		EasyMock.replay(listener);
		cfgManager.updateProjectConfiguration(PROJECT_ID, baseConf);

		EasyMock.verify(listener);

		// reset
		EasyMock.reset(listener);

		ProjectConfiguration newConf = new ProjectConfiguration(baseConf);

		newConf.getServerCfg(bamboo1.getServerId()).setEnabled(true);
		newConf.getServerCfg(crucible1.getServerId()).setEnabled(true);
		newConf.getServerCfg(jira1.getServerId()).setEnabled(true);

		// record
		listener.configurationUpdated(newConf);
		listener.serverEnabled(bamboo1.getServerId());
		listener.serverEnabled(crucible1.getServerId());
		listener.serverEnabled(jira1.getServerId());
		listener.serverDataUpdated(bamboo1.getServerId());
		listener.serverDataUpdated(crucible1.getServerId());
		listener.serverDataUpdated(jira1.getServerId());

		// test enabled
		EasyMock.replay(listener);
		cfgManager.updateProjectConfiguration(PROJECT_ID, newConf);

		EasyMock.verify(listener);
	}

	public void testServerLabelChanged() {

		baseConf.getServerCfg(bamboo1.getServerId()).setName(bamboo1.getName() + SUFFIX);
		baseConf.getServerCfg(crucible1.getServerId()).setName(crucible1.getName() + SUFFIX);
		baseConf.getServerCfg(jira1.getServerId()).setName(jira1.getName() + SUFFIX);

		// record
		listener.configurationUpdated(baseConf);
		listener.serverNameUpdated(bamboo1.getServerId());
		listener.serverNameUpdated(crucible1.getServerId());
		listener.serverNameUpdated(jira1.getServerId());
		listener.serverDataUpdated(bamboo1.getServerId());
		listener.serverDataUpdated(crucible1.getServerId());
		listener.serverDataUpdated(jira1.getServerId());

		// test
		EasyMock.replay(listener);
		cfgManager.updateProjectConfiguration(PROJECT_ID, baseConf);

		EasyMock.verify(listener);
	}

	public void testServerConnectionDataChanged() {

		baseConf.getServerCfg(bamboo1.getServerId()).setUrl(bamboo1.getUrl() + SUFFIX);
		baseConf.getServerCfg(crucible1.getServerId()).setUsername(crucible1.getUsername() + SUFFIX);
		baseConf.getServerCfg(jira1.getServerId()).setPassword(jira1.getPassword() + SUFFIX);

		// record
		listener.configurationUpdated(baseConf);
		listener.serverConnectionDataUpdated(bamboo1.getServerId());
		listener.serverConnectionDataUpdated(crucible1.getServerId());
		listener.serverConnectionDataUpdated(jira1.getServerId());
		listener.serverDataUpdated(bamboo1.getServerId());
		listener.serverDataUpdated(crucible1.getServerId());
		listener.serverDataUpdated(jira1.getServerId());

		// test
		EasyMock.replay(listener);
		cfgManager.updateProjectConfiguration(PROJECT_ID, baseConf);

		EasyMock.verify(listener);
	}

	public void testConfigurationMixedUpdate() {

		baseConf.getServerCfg(bamboo1.getServerId()).setUrl(bamboo1.getUrl() + SUFFIX);
		baseConf.getServerCfg(bamboo1.getServerId()).setName(bamboo1.getName() + SUFFIX);
		baseConf.getServerCfg(bamboo1.getServerId()).setEnabled(false);

		baseConf.getServers().remove(crucible1);
		baseConf.getServers().add(bamboo2);

		// record
		listener.configurationUpdated(baseConf);
		listener.serverConnectionDataUpdated(bamboo1.getServerId());
		listener.serverNameUpdated(bamboo1.getServerId());
		listener.serverDisabled(bamboo1.getServerId());
		listener.serverDataUpdated(bamboo1.getServerId());
		listener.serverRemoved(crucible1);
		listener.serverAdded(bamboo2);

		// test
		EasyMock.replay(listener);
		cfgManager.updateProjectConfiguration(PROJECT_ID, baseConf);

		EasyMock.verify(listener);
	}

	public void testServerDataChange() {
		baseConf.getServerCfg(bamboo1.getServerId()).setPasswordStored(!bamboo1.isPasswordStored());

		// record
		listener.configurationUpdated(baseConf);
		listener.serverDataUpdated(bamboo1.getServerId());

		// test
		EasyMock.replay(listener);
		cfgManager.updateProjectConfiguration(PROJECT_ID, baseConf);

		EasyMock.verify(listener);

	}
}
