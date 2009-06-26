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

public class JiraServerCfg extends ServerCfg {
	public JiraServerCfg(final String name, final ServerIdImpl serverId) {
		super(true, name, serverId);
	}

	public JiraServerCfg(final JiraServerCfg other) {
		super(other);
	}

	public JiraServerCfg(boolean enabled, String name, ServerIdImpl serverId) {
		super(enabled, name, serverId);
	}

	@Override
	public ServerType getServerType() {
		return ServerType.JIRA_SERVER;
	}

	@Override
	public JiraServerCfg getClone() {
		return new JiraServerCfg(this);
	}

}
