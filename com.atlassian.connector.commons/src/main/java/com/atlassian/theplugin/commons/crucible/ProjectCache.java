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
package com.atlassian.theplugin.commons.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleSessionImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jacek Jaroczynski
 */
public class ProjectCache {
	private Map<String, CrucibleProject> projects = new HashMap<String, CrucibleProject>();
	private CrucibleSessionImpl session;

	public ProjectCache(CrucibleSessionImpl session) {
		this.session = session;
	}

	/**
	 * N@param projectKey key of the searched project
	 * @return CrucibleProject if found or null otherwise
	 * @throws RemoteApiException in case of connection problems
	 */
	public CrucibleProject getProjectBean(String projectKey) throws RemoteApiException {

		// if project is not on the list then ask server (refresh cache)
		if (!projects.containsKey(projectKey)) {
			List<CrucibleProject> list = session.getProjects();
			for (CrucibleProject project : list) {
				projects.put(project.getKey(), project);
			}
		}

		return projects.get(projectKey);
	}

}
