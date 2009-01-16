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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.util.MiscUtil;

import java.util.ArrayList;
import java.util.Collection;

public class ProjectConfiguration {
	private Collection<ServerCfg> servers;

	private ServerId defaultCrucibleServerId;
	private ServerId defaultJiraServerId;
	private String defaultCrucibleProject;
	private String defaultJiraProject;
	private String defaultCrucibleRepo;
	private ServerId defaultFishEyeServerId;
	private String defaultFishEyeRepo;
	private String fishEyeProjectPath;
	private static final int HASHCODE_MAGIC = 31;


	public ProjectConfiguration(final ProjectConfiguration other) {
		servers = cloneArrayList(other.getServers());
		defaultCrucibleServerId = other.defaultCrucibleServerId;
		defaultJiraServerId = other.defaultJiraServerId;
		defaultFishEyeServerId = other.defaultFishEyeServerId;
		defaultCrucibleProject = other.defaultCrucibleProject;
		defaultCrucibleRepo = other.defaultCrucibleRepo;
		defaultFishEyeRepo = other.defaultFishEyeRepo;
		fishEyeProjectPath = other.fishEyeProjectPath;
		defaultJiraProject = other.defaultJiraProject;
	}

	public static Collection<ServerCfg> cloneArrayList(final Collection<ServerCfg> collection) {
		final ArrayList<ServerCfg> res = new ArrayList<ServerCfg>(collection.size());
		for (ServerCfg serverCfg : collection) {
			res.add(serverCfg.getClone());
		}
		return res;
	}


	public ProjectConfiguration(final Collection<ServerCfg> servers) {
		if (servers == null) {
			throw new NullPointerException("Servers cannot be null");
		}
		this.servers = servers;
	}

	public ProjectConfiguration() {
		this.servers = MiscUtil.buildArrayList();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ProjectConfiguration)) {
			return false;
		}

		final ProjectConfiguration that = (ProjectConfiguration) o;

		if (defaultCrucibleProject != null
				? !defaultCrucibleProject.equals(that.defaultCrucibleProject)
				: that.defaultCrucibleProject != null) {
			return false;
		}
		if (defaultJiraProject != null
				? !defaultJiraProject.equals(that.defaultJiraProject)
				: that.defaultJiraProject != null) {
			return false;
		}
		if (defaultCrucibleServerId != null
				? !defaultCrucibleServerId.equals(that.defaultCrucibleServerId)
				: that.defaultCrucibleServerId != null) {
			return false;
		}
		if (defaultJiraServerId != null
				? !defaultJiraServerId.equals(that.defaultJiraServerId)
				: that.defaultJiraServerId != null) {
			return false;
		}
		if (defaultCrucibleRepo != null
				? !defaultCrucibleRepo.equals(that.defaultCrucibleRepo)
				: that.defaultCrucibleRepo != null) {
			return false;
		}
		if (defaultFishEyeServerId != null
				? !defaultFishEyeServerId.equals(that.defaultFishEyeServerId)
				: that.defaultFishEyeServerId != null) {
			return false;
		}
		if (defaultFishEyeRepo != null
				? !defaultFishEyeRepo.equals(that.defaultFishEyeRepo)
				: that.defaultFishEyeRepo != null) {
			return false;
		}
		if (fishEyeProjectPath != null
				? !fishEyeProjectPath.equals(that.fishEyeProjectPath)
				: that.fishEyeProjectPath != null) {
			return false;
		}
		//noinspection RedundantIfStatement
		if (!servers.equals(that.servers)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = servers.hashCode();
		result = HASHCODE_MAGIC * result + (defaultCrucibleServerId != null ? defaultCrucibleServerId.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultJiraServerId != null ? defaultJiraServerId.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultFishEyeServerId != null ? defaultFishEyeServerId.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultCrucibleProject != null ? defaultCrucibleProject.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultJiraProject != null ? defaultJiraProject.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultCrucibleRepo != null ? defaultCrucibleRepo.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (fishEyeProjectPath != null ? fishEyeProjectPath.hashCode() : 0);
		return result;
	}

	public Collection<ServerCfg> getServers() {
		return servers;
	}

	public ServerCfg getServerCfg(ServerId serverId) {
		for (ServerCfg serverCfg : servers) {
			if (serverId.equals(serverCfg.getServerId())) {
				return serverCfg;
			}
		}
		return null;
	}

	public static ProjectConfiguration emptyConfiguration() {
		return new ProjectConfiguration();
	}

	public ProjectConfiguration getClone() {
		return new ProjectConfiguration(this);
	}

	public ServerId getDefaultCrucibleServerId() {
		return defaultCrucibleServerId;
	}

	public ServerId getDefaultJiraServerId() {
		return defaultJiraServerId;
	}

	public CrucibleServerCfg getDefaultCrucibleServer() {
		if (defaultCrucibleServerId == null) {
			return null;
		}

		ServerCfg serverCfg = getServerCfg(defaultCrucibleServerId);

		// no additional check - let IDE handle such error in a standard way (error dialog)
		// in unlikely event of some fuck-up
		final CrucibleServerCfg crucible = (CrucibleServerCfg) serverCfg;
		if (!crucible.isEnabled()) {
			return null;
		}
		return crucible;
	}

	public JiraServerCfg getDefaultJiraServer() {
		if (defaultJiraServerId == null) {
			return null;
		}

		ServerCfg serverCfg = getServerCfg(defaultJiraServerId);

		// no additional check - let IDE handle such error in a standard way (error dialog)
		// in unlikely event of some fuck-up
		final JiraServerCfg jira = (JiraServerCfg) serverCfg;
		if (!jira.isEnabled()) {
			return null;
		}
		return jira;
	}

	public void setDefaultCrucibleServerId(final ServerId defaultCrucibleServerId) {
		this.defaultCrucibleServerId = defaultCrucibleServerId;
		if (defaultCrucibleServerId == null) {
			setDefaultCrucibleProject(null);
			setDefaultCrucibleRepo(null);
		}
	}

	public void setDefaultJiraServerId(final ServerId defaultJiraServerId) {
		this.defaultJiraServerId = defaultJiraServerId;
		if (defaultJiraServerId == null) {
			setDefaultJiraProject(null);
		}
	}

	public ServerId getDefaultFishEyeServerId() {
		return defaultFishEyeServerId;
	}

	public FishEyeServer getDefaultFishEyeServer() {
		if (defaultFishEyeServerId == null) {
			return null;
		}

		final ServerCfg serverCfg = getServerCfg(defaultFishEyeServerId);

		// no additional check - let IDE handle such error in a standard way (error dialog)
		// in unlikely event of some fuck-up
		if (serverCfg == null || serverCfg.isEnabled() == false) {
			return null;
		}

		FishEyeServer res = serverCfg.asFishEyeServer();
		if (res == null || res.isEnabled() == false) {
			return null;
		}
		return res;
	}

	public void setDefaultFishEyeServerId(final ServerId defaultFishEyeServerId) {
		this.defaultFishEyeServerId = defaultFishEyeServerId;
		if (defaultFishEyeServerId == null) {
			defaultFishEyeRepo = null;
		}
	}

	public String getDefaultCrucibleProject() {
		return defaultCrucibleProject;
	}

	public void setDefaultCrucibleProject(final String defaultCrucibleProject) {
		this.defaultCrucibleProject = defaultCrucibleProject;
	}

	public String getDefaultJiraProject() {
		return defaultJiraProject;
	}

	public void setDefaultJiraProject(final String defaultJiraProject) {
		this.defaultJiraProject = defaultJiraProject;
	}

	public String getDefaultCrucibleRepo() {
		return defaultCrucibleRepo;
	}

	public void setDefaultCrucibleRepo(final String defaultCrucibleRepo) {
		this.defaultCrucibleRepo = defaultCrucibleRepo;
	}

	public String getFishEyeProjectPath() {
		return fishEyeProjectPath;
	}

	public void setFishEyeProjectPath(final String fishEyeProjectPath) {
		this.fishEyeProjectPath = fishEyeProjectPath;
	}

	public String getDefaultFishEyeRepo() {
		return defaultFishEyeRepo;
	}

	public void setDefaultFishEyeRepo(final String defaultFishEyeRepo) {
		this.defaultFishEyeRepo = defaultFishEyeRepo;
	}

	public boolean isDefaultFishEyeServerValid() {
		if (defaultFishEyeServerId == null) {
			return true;
		}

		ServerCfg serverCfg = getServerCfg(defaultFishEyeServerId);
		if (serverCfg == null) {
			return false;
		}

		FishEyeServer fishEye = serverCfg.asFishEyeServer();
		return fishEye != null && fishEye.isEnabled();
	}

	public boolean isDefaultCrucibleServerValid() {
		if (defaultCrucibleServerId == null) {
			return true;
		}

		ServerCfg serverCfg = getServerCfg(defaultCrucibleServerId);

		// no additional check - let IDE handle such error in a standard way (error dialog)
		// in unlikely event of some fuck-up
		final CrucibleServerCfg crucible = (CrucibleServerCfg) serverCfg;
		return crucible != null && crucible.isEnabled();
	}

	public boolean isDefaultJiraServerValid() {
		if (defaultJiraServerId == null) {
			return true;
		}

		ServerCfg serverCfg = getServerCfg(defaultJiraServerId);

		// no additional check - let IDE handle such error in a standard way (error dialog)
		// in unlikely event of some fuck-up
		final JiraServerCfg jira = (JiraServerCfg) serverCfg;
		return jira != null && jira.isEnabled();
	}

	public Collection<JiraServerCfg> getAllJIRAServers() {
		Collection<JiraServerCfg> jiraServers = MiscUtil.buildArrayList();

		for (ServerCfg server : servers) {
			if (server.getServerType() == ServerType.JIRA_SERVER && server instanceof JiraServerCfg) {
				jiraServers.add((JiraServerCfg) server);
			}
		}

		return jiraServers;
	}

	public Collection<BambooServerCfg> getAllBambooServers() {
		Collection<BambooServerCfg> bambooServers = MiscUtil.buildArrayList();

		for (ServerCfg server : servers) {
			if (server.getServerType() == ServerType.BAMBOO_SERVER && server instanceof BambooServerCfg) {
				bambooServers.add((BambooServerCfg) server);
			}
		}

		return bambooServers;
	}

	public Collection<CrucibleServerCfg> getAllCrucibleServers() {
		Collection<CrucibleServerCfg> crucibleServers = MiscUtil.buildArrayList();

		for (ServerCfg server : servers) {
			if (server.getServerType() == ServerType.CRUCIBLE_SERVER && server instanceof CrucibleServerCfg) {
				crucibleServers.add((CrucibleServerCfg) server);
			}
		}

		return crucibleServers;

	}

	public Collection<FishEyeServerCfg> getAllFisheyeServers() {
		Collection<FishEyeServerCfg> fisheyeServers = MiscUtil.buildArrayList();

		for (ServerCfg server : servers) {
			if (server.getServerType() == ServerType.FISHEYE_SERVER && server instanceof FishEyeServerCfg) {
				fisheyeServers.add((FishEyeServerCfg) server);
			}
		}

		return fisheyeServers;
	}
}
