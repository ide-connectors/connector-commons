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

package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Set;

/**
 * Build information retrieved from Bamboo server.
 */
public interface BambooBuild {
	BambooServerData getServer();

	String getServerUrl();

	String getProjectName();

	String getBuildUrl();

	String getPlanName();

	@NotNull
	String getPlanKey();

	boolean getEnabled();

	boolean isValid();

	/**
	 * @return build number
	 * @throws UnsupportedOperationException in case this object represents invalid build
	 */
	int getNumber() throws UnsupportedOperationException;

	String getResultUrl();

	@NotNull
	BuildStatus getStatus();

	@Nullable
	String getErrorMessage();

	/**
	 * @return human readable info about unit tests like "267 passed"
	 */
	@Nullable
	String getTestSummary();

	int getTestsPassed();

	int getTestsFailed();

	@Nullable
	String getReason();

	/**
	 * @return human readable info about the time taken by given build - e.g. "3 minutes"
	 */
	@Nullable
	String getDurationDescription();

	@Nullable
	Date getStartDate();

	@Nullable
	Date getCompletionDate();

	/**
	 * Relative build completion date on Bamboo server. Unfortunately it does not respect calling client timezone,
	 * so in most cases it's useless. Instead it's preferable to use {@link #getCompletionDate()} and then use
	 * some utility method like {@link com.atlassian.theplugin.commons.util.DateUtil#getRelativeBuildTime(java.util.Date)}
	 * to transform Date to relative string describing relative date.
	 *
	 * @return human readable string like "2 months ago"
	 */
	String getRelativeBuildDate();

	boolean isMyBuild();

	Set<String> getCommiters();

	Date getPollingTime();

	Throwable getException();
}
