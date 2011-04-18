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

/**
 * @autrhor pmaruszak
 * @date Jun 11, 2010
 */
public class SharedServerList extends ArrayList<ServerCfg> {
	public static SharedServerList merge(SharedServerList list1, SharedServerList list2) {
		HashSet<ServerCfg> sharedMergeSet = new HashSet<ServerCfg>();
		sharedMergeSet.addAll(list1);
		sharedMergeSet.addAll(list2);
		SharedServerList newList = new SharedServerList();

		for (ServerCfg server : sharedMergeSet) {
			if (server.getUrl() != null && server.getUrl().length() > 0) {
				newList.add(server);
			}
		}
		return newList;
	}
}
