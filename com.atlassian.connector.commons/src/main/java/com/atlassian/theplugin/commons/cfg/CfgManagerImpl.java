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
import static com.atlassian.theplugin.commons.util.MiscUtil.buildConcurrentHashMap;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class CfgManagerImpl implements CfgManager {
	private final Map<ProjectId, ProjectConfiguration> projectConfigurations = buildConcurrentHashMap(INITIAL_CAPACITY);
	private Collection<ServerCfg> globalServers = MiscUtil.buildArrayList();
	private final Map<ProjectId, Collection<ConfigurationListener>> listeners = buildConcurrentHashMap(100);
	private final Map<ProjectId, Collection<ConfigurationCredentialsListener>> credentialListeners =
			buildConcurrentHashMap(100);
	private BambooCfg bambooCfg;
	private static final int INITIAL_CAPACITY = 4;

	private static final ProjectListenerAction PROJECT_UNREGISTERED_LISTENER_ACTION = new ProjectListenerAction() {
		public void run(final ConfigurationListener projectListener, final ProjectId projectId,
				final CfgManagerImpl cfgManager) {
			projectListener.projectUnregistered();
		}
	};
	
	public CfgManagerImpl() {
		// TODO wseliga remove it later on and handle properly null values
		update(new GlobalConfiguration());
	}


	public ProjectConfiguration getProjectConfiguration(final ProjectId projectId) {
		verifyProjectId(projectId);
		return projectConfigurations.get(projectId);
	}

	public Collection<ServerCfg> getAllServers(final ProjectId projectId) {
        Collection<ServerCfg> tmp = new ArrayList<ServerCfg>(getProjectSpecificServers(projectId));
        tmp.addAll(globalServers);
        return tmp;
    }

	public Collection<CrucibleServerCfg> getAllCrucibleServers(ProjectId projectId) {

		Collection<ServerCfg> tmp = getAllServers(projectId);
		Collection<CrucibleServerCfg> res = MiscUtil.buildArrayList();
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg instanceof CrucibleServerCfg) {
				res.add((CrucibleServerCfg) serverCfg);
			}
		}
		return res;

	}

	public Collection<ServerCfg> getProjectSpecificServers(final ProjectId projectId) {
		verifyProjectId(projectId);
		ProjectConfiguration res = projectConfigurations.get(projectId);
        if (res == null) {
            return Collections.emptyList();
        }
        return MiscUtil.buildArrayList(res.getServers());
    }

	public Collection<ServerCfg> getGlobalServers() {
        return new ArrayList<ServerCfg>(globalServers);
    }

	public Collection<ServerCfg> getAllEnabledServers(final ProjectId projectId) {
        Collection<ServerCfg> tmp = new ArrayList<ServerCfg>();
        for (ServerCfg serverCfg : getAllServers(projectId)) {
            if (serverCfg.isEnabled()) {
                tmp.add(serverCfg);
            }
        }
        return tmp;
    }


	public BambooCfg getGlobalBambooCfg() {
		return bambooCfg;
    }

	public void updateProjectConfiguration(final ProjectId projectId, final ProjectConfiguration projectConfiguration) {
		verifyProjectId(projectId);
		if (projectConfiguration == null) {
			throw new NullPointerException("Project configuration cannot be null");
		}

//		notifyCredentialsListeners(projectId, projectConfiguration, getProjectConfiguration(projectId));

		ProjectConfiguration oldConfiguration = null;
		if (getProjectConfiguration(projectId) != null) {
			oldConfiguration = new ProjectConfiguration(getProjectConfiguration(projectId));
		}

		// internalize the list to be private and put it to array
		projectConfigurations.put(projectId, projectConfiguration);

		notifyListeners(projectId, projectConfiguration, oldConfiguration);
//		notifyListeners(projectId, new UpdateConfigurationListenerAction(projectConfiguration));

	}

	private void notifyListeners(ProjectId projectId,
								 ProjectConfiguration newConfiguration,
								 ProjectConfiguration oldConfiguration) {

		ProjectListenerAction[] actions = {
				new UpdateConfigurationListenerAction(newConfiguration),
				new ConfigurationTypeChangedAction(newConfiguration, oldConfiguration),
				new ServerChangedAction(newConfiguration, oldConfiguration),
				new ServerAddedAction(newConfiguration, oldConfiguration),
				new ServerRemovedAction(newConfiguration, oldConfiguration),
				new ServerEnabledDisabledAction(newConfiguration, oldConfiguration)
		};

		for (ProjectListenerAction action : actions) {
			notifyListeners(projectId, action);
		}
	}

	private void notifyListeners(final ProjectId projectId, ProjectListenerAction listenerAction) {
		Collection<ConfigurationListener> projectListeners = listeners.get(projectId);
		if (projectListeners != null) {
			for (ConfigurationListener projectListener : projectListeners) {
				listenerAction.run(projectListener, projectId, this);
				//projectListener.updateConfiguration(projectId, this);
			}
		}
	}

	public void addProjectConfigurationListener(final ProjectId projectId, final ConfigurationListener configurationListener) {
		if (configurationListener == null) {
			throw new IllegalArgumentException(ProjectId.class.getSimpleName() + " cannot be null");
		}
		verifyProjectId(projectId);

		Collection<ConfigurationListener> tmp = listeners.get(projectId);
		if (tmp == null) {
			tmp = new CopyOnWriteArraySet<ConfigurationListener>(); //MiscUtil.buildHashSet();
			listeners.put(projectId, tmp);
		}
		tmp.add(configurationListener);
	}

	public boolean removeProjectConfigurationListener(final ProjectId projectId,
													  final ConfigurationListener configurationListener) {
		if (configurationListener == null) {
			throw new IllegalArgumentException(ProjectId.class.getSimpleName() + " cannot be null");
		}
		verifyProjectId(projectId);
		Collection<ConfigurationListener> tmp = listeners.get(projectId);
		return tmp.remove(configurationListener);
	}

	public void updateGlobalConfiguration(final GlobalConfiguration globalConfiguration) {
		if (globalConfiguration == null) {
			throw new NullPointerException("Global configuration cannot be null");
		}
		// internalize the list to be private
		globalServers = MiscUtil.buildArrayList(globalConfiguration.getGlobalServers());
	}

	public void addProjectSpecificServer(final ProjectId projectId, final ServerCfg serverCfg) {
		verifyProjectId(projectId);
		if (serverCfg == null) {
			throw new IllegalArgumentException(ServerCfg.class.getSimpleName() + " cannot be null");
		}

		ProjectConfiguration projectCfg = getProjectConfiguration(projectId);

        if (projectCfg == null) {
            projectCfg = new ProjectConfiguration();
            projectConfigurations.put(projectId, projectCfg);
        }
		if (!projectCfg.getServers().contains(serverCfg)) {
			projectCfg.getServers().add(serverCfg);
		}

	}

	public void addGlobalServer(final ServerCfg serverCfg) {
        globalServers.add(serverCfg);
    }

	public ProjectConfiguration removeProject(final ProjectId projectId) {
		final ProjectConfiguration res = projectConfigurations.remove(projectId);
		if (res != null) {
			notifyListeners(projectId, PROJECT_UNREGISTERED_LISTENER_ACTION);
		}
		return res;
	}


	public ServerCfg removeGlobalServer(final ServerId serverId) {
		verifyServerId(serverId);
		return removeServer(serverId, globalServers);
    }

	private void verifyServerId(final ServerId serverId) {
		if (serverId == null) {
			throw new IllegalArgumentException(ServerId.class.getSimpleName() + " cannot be null");
		}
	}

	public ServerCfg removeProjectSpecificServer(final ProjectId projectId, final ServerId serverId) {
		verifyProjectId(projectId);
		verifyServerId(serverId);

		ProjectConfiguration projectCfg = getProjectConfiguration(projectId);
		if (projectCfg == null) {
			return null;
		}

		return removeServer(serverId, projectCfg.getServers());
	}

	private void verifyProjectId(final ProjectId projectId) {
		if (projectId == null) {
			throw new IllegalArgumentException(ProjectId.class.getSimpleName() + " cannot be null");
		}
	}

	public boolean hasProject(ProjectId projectId) {
		return projectConfigurations.containsKey(projectId);
	}

	public Collection<BambooServerCfg> getAllEnabledBambooServers(final ProjectId projectId) {
		Collection<ServerCfg> tmp = getAllEnabledServers(projectId);
		Collection<BambooServerCfg> res = MiscUtil.buildArrayList();
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg instanceof BambooServerCfg) {
				BambooServerCfg bambooServerCfg = (BambooServerCfg) serverCfg;
				res.add(bambooServerCfg);
			}
		}
		return res;
	}

	public ServerCfg getServer(final ProjectId projectId, final ServerId serverId) {
		final Collection<ServerCfg> tmp = getAllServers(projectId);
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerId().equals(serverId)) {
				return serverCfg;
			}
		}
		return null;
	}

	public Collection<CrucibleServerCfg> getAllEnabledCrucibleServers(final ProjectId projectId) {
		Collection<ServerCfg> tmp = getAllEnabledServers(projectId);
		Collection<CrucibleServerCfg> res = MiscUtil.buildArrayList();
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg instanceof CrucibleServerCfg) {
				CrucibleServerCfg bambooServerCfg = (CrucibleServerCfg) serverCfg;
				res.add(bambooServerCfg);
			}
		}
		return res;
	}


	public Collection<JiraServerCfg> getAllEnabledJiraServers(final ProjectId projectId) {
		Collection<ServerCfg> tmp = getAllEnabledServers(projectId);
		Collection<JiraServerCfg> res = MiscUtil.buildArrayList();
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg instanceof JiraServerCfg) {
				JiraServerCfg bambooServerCfg = (JiraServerCfg) serverCfg;
				res.add(bambooServerCfg);
			}
		}
		return res;
	}


	public Collection<ServerCfg> getAllEnabledServers(final ProjectId projectId, ServerType serverType) {
		Collection<ServerCfg> tmp = getAllEnabledServers(projectId);
		Collection<ServerCfg> res = MiscUtil.buildArrayList();
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == serverType) {
				res.add(serverCfg);
			}
		}
		return res;
	}

	private ServerCfg removeServer(final ServerId serverId, final Collection<ServerCfg> servers) {
        Iterator<ServerCfg> it = servers.iterator();
        while (it.hasNext()) {
            ServerCfg serverCfg = it.next();
            if (serverCfg.getServerId().equals(serverId)) {
                it.remove();
                return serverCfg;
            }
        }
        return null;
    }


	public void update(GlobalConfiguration globalConfiguration) {
		bambooCfg = globalConfiguration.getBambooCfg();
	}

	private interface ProjectListenerAction {
		void run(final ConfigurationListener projectListener, final ProjectId projectId, final CfgManagerImpl cfgManager);
	}


	public Collection<ServerCfg> getAllUniqueServers() {
		final Set<ServerCfg> res = new HashSet<ServerCfg>(globalServers);
		for (ProjectConfiguration projectCfg : projectConfigurations.values()) {
			res.addAll(projectCfg.getServers());
		}
		return res;
	}


	private static class UpdateConfigurationListenerAction implements ProjectListenerAction {

		private final ProjectConfiguration projectConfiguration;

		public UpdateConfigurationListenerAction(final ProjectConfiguration projectConfiguration) {
			this.projectConfiguration = projectConfiguration;
		}

		public void run(final ConfigurationListener projectListener, final ProjectId projectId,
				final CfgManagerImpl cfgManager) {
			projectListener.configurationUpdated(projectConfiguration);
		}
	}

	private class ServerChangedAction implements ProjectListenerAction {
		protected final ProjectConfiguration newConfiguration;
		protected ProjectConfiguration oldConfiguration;

		public ServerChangedAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
			this.newConfiguration = newConfiguration;
			this.oldConfiguration = oldConfiguration;
		}

		public void run(ConfigurationListener projectListener, ProjectId projectId, CfgManagerImpl cfgManager) {
			if (oldConfiguration == null || newConfiguration == null) {
				return;
			}

			for (ServerCfg oldServer : oldConfiguration.getServers()) {
				ServerCfg newServer = newConfiguration.getServerCfg(oldServer.getServerId());

				// server general update
				if (newServer != null && !oldServer.equals(newServer)) {
					projectListener.serverDataChanged(oldServer.getServerId());

					// server url or credentials updated
					if (checkCredentialsChanged(oldServer, newServer)
							|| checkUrlChanged(oldServer, newConfiguration.getServerCfg(oldServer.getServerId()))) {
						projectListener.serverConnectionDataChanged(oldServer.getServerId());
					}

					// server name updated
					if (!oldServer.getName().equals(newServer.getName())) {
						projectListener.serverNameChanged(oldServer.getServerId());
					}

				}
			}
		}

		protected boolean checkCredentialsChanged(final ServerCfg oldServer, final ServerCfg newServer) {
			if (newServer == null) {
				return false;
			}

			if (!oldServer.getUsername().equals(newServer.getUsername())
					|| !oldServer.getPassword().equals(newServer.getPassword())) {
				return true;
			}

			return false;
		}

		private boolean checkUrlChanged(final ServerCfg oldServer, final ServerCfg newServer) {
			if (newServer == null) {
				return false;
			}
			return !oldServer.getUrl().equals(newServer.getUrl());
		}
	}

	private class ServerAddedAction implements ProjectListenerAction {
		private final ProjectConfiguration newConfiguration;
		private final ProjectConfiguration oldConfiguration;

		public ServerAddedAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
			this.newConfiguration = newConfiguration;
			this.oldConfiguration = oldConfiguration;
		}

		public void run(ConfigurationListener projectListener, ProjectId projectId, CfgManagerImpl cfgManager) {
			if (oldConfiguration == null || newConfiguration == null) {
				return;
			}

			for (ServerCfg newServer : newConfiguration.getServers()) {
				if (oldConfiguration.getServerCfg(newServer.getServerId()) == null) {
					projectListener.serverAdded(newServer);
				}
			}
		}
	}

	private class ServerRemovedAction implements ProjectListenerAction {
		private final ProjectConfiguration newConfiguration;
		private final ProjectConfiguration oldConfiguration;

		public ServerRemovedAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
			this.newConfiguration = newConfiguration;
			this.oldConfiguration = oldConfiguration;
		}

		public void run(ConfigurationListener projectListener, ProjectId projectId, CfgManagerImpl cfgManager) {
			if (oldConfiguration == null || newConfiguration == null) {
				return;
			}

			for (ServerCfg oldServer : oldConfiguration.getServers()) {
				if (newConfiguration.getServerCfg(oldServer.getServerId()) == null) {
					projectListener.serverRemoved(oldServer);
				}
			}
		}
	}

	private class ServerEnabledDisabledAction implements ProjectListenerAction {

		private final ProjectConfiguration newConfiguration;
		private final ProjectConfiguration oldConfiguration;

		public ServerEnabledDisabledAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
			this.newConfiguration = newConfiguration;
			this.oldConfiguration = oldConfiguration;
		}

		public void run(ConfigurationListener projectListener, ProjectId projectId, CfgManagerImpl cfgManager) {
			if (oldConfiguration == null || newConfiguration == null) {
				return;
			}

			for (ServerCfg oldServer : oldConfiguration.getServers()) {
				ServerCfg newServer = newConfiguration.getServerCfg(oldServer.getServerId());
				if (newServer != null) {
					if (!oldServer.isEnabled() && newServer.isEnabled()) {
						projectListener.serverEnabled(oldServer.getServerId());
					} else if (oldServer.isEnabled() && !newServer.isEnabled()) {
						projectListener.serverDisabled(oldServer.getServerId());
					}
				}
			}
		}

	}

	private class ConfigurationTypeChangedAction implements ProjectListenerAction {
		private final ProjectConfiguration newConfiguration;
		private final ProjectConfiguration oldConfiguration;

		public ConfigurationTypeChangedAction(
				ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
			this.newConfiguration = newConfiguration;
			this.oldConfiguration = oldConfiguration;
		}

		public void run(ConfigurationListener projectListener, ProjectId projectId, CfgManagerImpl cfgManager) {
			if (oldConfiguration == null || newConfiguration == null) {
				return;
			}

			// Collections.constainsAll is used in both directions below instead of Collection.equlas
			// as equals for Collection compares only references
			// and we cannot be sure if used implementation overrides equals correctly
			// and e.g. equals for List requires the same order of elements

			// JIRA servers changed
			Collection<JiraServerCfg> newJiraServers = newConfiguration.getAllJIRAServers();
			Collection<JiraServerCfg> oldJiraServers = oldConfiguration.getAllJIRAServers();
			if (!newJiraServers.containsAll(oldJiraServers) || !oldJiraServers.containsAll(newJiraServers)) {
				projectListener.jiraServersChanged(newConfiguration);
			}

			// Bamboo servers changed
			Collection<BambooServerCfg> newBambooServers = newConfiguration.getAllBambooServers();
			Collection<BambooServerCfg> oldBambooServers = oldConfiguration.getAllBambooServers();
			if (!newBambooServers.containsAll(oldBambooServers) || !oldBambooServers.containsAll(newBambooServers)) {
				projectListener.bambooServersChanged(newConfiguration);
			}

			// Crucible servers changed
			Collection<CrucibleServerCfg> newCrucibleServers = newConfiguration.getAllCrucibleServers();
			Collection<CrucibleServerCfg> oldCrucibleServers = oldConfiguration.getAllCrucibleServers();
			if (!newCrucibleServers.containsAll(oldCrucibleServers) || !oldCrucibleServers.containsAll(newCrucibleServers)) {
				projectListener.crucibleServersChanged(newConfiguration);
			}

			// Fisheye servers changed
			Collection<FishEyeServerCfg> newFisheyeServers = newConfiguration.getAllFisheyeServers();
			Collection<FishEyeServerCfg> oldFisheyeServers = oldConfiguration.getAllFisheyeServers();
			if (!newFisheyeServers.containsAll(oldFisheyeServers) || !oldFisheyeServers.containsAll(newFisheyeServers)) {
				projectListener.fisheyeServersChanged(newConfiguration);
			}
		}
	}
}
