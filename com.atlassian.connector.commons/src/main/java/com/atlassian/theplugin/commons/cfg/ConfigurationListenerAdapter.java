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

/**
 * @author Jacek Jaroczynski
 */
public abstract class ConfigurationListenerAdapter implements ConfigurationListener {

	public void configurationUpdated(ProjectConfiguration aProjectConfiguration) {
	}

	public void projectUnregistered() {
	}

	public void serverDataChanged(IServerId serverId) {
	}

	public void serverConnectionDataChanged(IServerId serverId) {
	}

	public void serverNameChanged(IServerId serverId) {
	}

	public void serverAdded(ServerCfg newServer) {
	}

	public void serverRemoved(ServerCfg oldServer) {
	}

	public void serverEnabled(IServerId serverId) {
	}

	public void serverDisabled(IServerId serverId) {
	}

	public void jiraServersChanged(ProjectConfiguration newConfiguration) {
	}

	public void bambooServersChanged(ProjectConfiguration newConfiguration) {
	}

	public void crucibleServersChanged(ProjectConfiguration newConfiguration) {
	}

	public void fisheyeServersChanged(ProjectConfiguration newConfiguration) {
	}
}
