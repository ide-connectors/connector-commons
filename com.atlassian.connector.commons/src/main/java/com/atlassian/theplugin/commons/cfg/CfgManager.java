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
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface CfgManager {
	/**
	 * @param projectId id of the project
	 * @return project configuration of selected project or null if no such project is registered
	 *         <p/>
	 *         ProjectCfgManager should be used for this purpose (instead of this method).
	 *         Otherwise we are facing various nasty race conditions while closing/initializaing project
	 */
	@Deprecated
	@Nullable
	ProjectConfiguration getProjectConfiguration(ProjectId projectId);

	Collection<ServerCfg> getAllServers(ProjectId projectId);

	Collection<ServerCfg> getAllServers(ProjectId projectId, ServerType serverType);

	Collection<ServerCfg> getProjectSpecificServers(ProjectId projectId);

	Collection<ServerCfg> getGlobalServers();

	Collection<ServerCfg> getAllEnabledServers(ProjectId projectId);

	Collection<ServerCfg> getAllEnabledServers(ProjectId projectId, ServerType serverType);

	Collection<ServerCfg> getAllServersWithDefaultCredentials(ProjectId projectId, ServerType serverType);

	Collection<ServerCfg> getAllServersWithDefaultCredentials(ProjectId projectId);

	void updateProjectConfiguration(ProjectId projectId, ProjectConfiguration projectConfiguration);

	void updateGlobalConfiguration(GlobalConfiguration globalConfiguration);

	void addProjectSpecificServer(ProjectId projectId, ServerCfg serverCfg);

	void addGlobalServer(ServerCfg serverCfg);

	ProjectConfiguration removeProject(ProjectId projectId);

	ServerCfg removeGlobalServer(ServerId serverId);

	ServerCfg removeProjectSpecificServer(ProjectId projectId, ServerId serverId);

	ServerCfg getServer(ProjectId projectId, ServerId serverId);

	void addProjectConfigurationListener(ProjectId projectId, ConfigurationListener configurationListener);

	boolean removeProjectConfigurationListener(ProjectId projectId, ConfigurationListener configurationListener);

	Collection<CrucibleServerCfg> getAllEnabledCrucibleServers(final ProjectId projectId);

	Collection<JiraServerCfg> getAllEnabledJiraServers(final ProjectId projectId);

	Collection<BambooServerCfg> getAllEnabledBambooServers(final ProjectId projectId);

	Collection<ServerCfg> getAllUniqueServers();

	boolean hasProject(ProjectId projectId);

	Collection<CrucibleServerCfg> getAllCrucibleServers(ProjectId projectId);

	ServerCfg getServer(ProjectId projectId, ServerData serverData);
}
