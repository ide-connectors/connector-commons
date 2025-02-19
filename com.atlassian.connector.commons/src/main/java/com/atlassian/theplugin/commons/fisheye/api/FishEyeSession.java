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

package com.atlassian.theplugin.commons.fisheye.api;

import com.atlassian.theplugin.commons.fisheye.api.model.FisheyePathHistoryItem;
import com.atlassian.theplugin.commons.fisheye.api.model.changeset.Changeset;
import com.atlassian.theplugin.commons.fisheye.api.model.changeset.ChangesetIdList;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Date;

public interface FishEyeSession extends ProductSession {

	Collection<String> getRepositories() throws RemoteApiException;

    Collection<FisheyePathHistoryItem> getPathHistory(String repo, String path) throws RemoteApiException;

	@NotNull
	ChangesetIdList getChangesetList(@NotNull String repository, @Nullable String path, @Nullable Date start,
			@Nullable Date end, @Nullable Integer maxReturn) throws RemoteApiException;

	@NotNull
	Changeset getChangeset(@NotNull String repository, @NotNull String csid) throws RemoteApiException;

}
