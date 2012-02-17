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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * @autrhor pmaruszak
 * @date Jun 11, 2010
 */
public class SharedServerList extends ArrayList<ServerCfg> {
	public static SharedServerList merge(SharedServerList currentConfig, SharedServerList loadedFromFile) {
		LinkedList<ServerCfg> sharedList = new LinkedList<ServerCfg>();
		HashSet<String> storedIds = new HashSet<String>();
		SharedServerList newList = new SharedServerList();

        //current config are priotity cfg
        sharedList.addAll(currentConfig);
        sharedList.addAll(loadedFromFile);

		for (ServerCfg server : sharedList) {
			if (server.getUrl() != null && server.getUrl().length() > 0
					&& !storedIds.contains(server.getServerId().toString())) {
				newList.add(server);
				storedIds.add(server.getServerId().toString());
			}
		}
		return newList;
	}
}
