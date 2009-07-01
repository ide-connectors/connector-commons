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

package com.atlassian.connector.cfg;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ProjectCfgManager {
	@NotNull
	ServerData getServerData(@NotNull Server serverCfg);

	Collection<BambooServerCfg> getAllEnabledBambooServers();

	Collection<ServerCfg> getAllServers();

	Collection<ServerCfg> getAllServers(ServerType serverType);

	Collection<ServerCfg> getAllEnabledServers();

	Collection<ServerCfg> getAllEnabledServers(ServerType serverType);

	Collection<ServerCfg> getAllEnabledServersWithDefaultCredentials();

	Collection<ServerCfg> getAllEnabledServersWithDefaultCredentials(ServerType serverType);

	Collection<BambooServerCfg> getAllBambooServers();

	Collection<JiraServerCfg> getAllJiraServers();

	Collection<CrucibleServerCfg> getAllCrucibleServers();

	Collection<JiraServerCfg> getAllEnabledJiraServers();

	Collection<CrucibleServerCfg> getAllEnabledCrucibleServers();

	@Nullable
	ServerData getDefaultJiraServer();

	@Nullable
	ServerData getDefaultCrucibleServer();

	@Nullable
	ServerData getDefaultFishEyeServer();

	String getDefaultCrucibleRepo();

	String getDefaultCrucibleProject();

	String getDefaultFishEyeRepo();

	String getFishEyeProjectPath();

	ServerCfg getServer(ServerId serverId);

	void addProjectConfigurationListener(ConfigurationListener configurationListener);

	boolean removeProjectConfigurationListener(ConfigurationListener configurationListener);

	boolean isDefaultJiraServerValid();

	Collection<FishEyeServerCfg> getAllFishEyeServers();

	Collection<ServerData> getAllEnabledServerss(final ServerType serverType);

	Collection<ServerData> getAllServerss(final ServerType serverType);
}
