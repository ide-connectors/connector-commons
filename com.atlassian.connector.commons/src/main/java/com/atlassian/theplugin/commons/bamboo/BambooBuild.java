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

import com.atlassian.theplugin.commons.RequestData;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;

import java.util.Date;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

/**
 * Build information retrieved from Bamboo server.
 */
public interface BambooBuild extends RequestData {
	BambooServerCfg getServer();

	String getServerUrl();

    String getProjectName();

	String getBuildUrl();

    String getBuildName();

	String getBuildKey();

	boolean getEnabled();

	String getBuildNumber();

    String getBuildResultUrl();

	BuildStatus getStatus();

	String getMessage();

	/**
	 * @return human readable info about unit tests like "267 passed"
	 */
	@Nullable
	String getBuildTestSummary();

	int getTestsPassed();

	int getTestsFailed();

	String getBuildReason();

	Date getBuildStartedDate();

	Date getBuildCompletedDate();

	/**
	 * Relative build completion date on Bamboo server. Unfortunately it does not respect calling client timezone,
	 * so in most cases it's useless. Instead it's preferable to use {@link #getBuildCompletedDate()} and then use
	 * some utility method like {@link com.atlassian.theplugin.commons.util.DateUtil#getRelativeBuildTime(java.util.Date)}
	 * to transform Date to relative string describing relative date.
	 *
	 * @return human readable string like "2 months ago"
	 */
	String getBuildRelativeBuildDate();

	boolean isMyBuild();

	Set<String> getCommiters();
}
