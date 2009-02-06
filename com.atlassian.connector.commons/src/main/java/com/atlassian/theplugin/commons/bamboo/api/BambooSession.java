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

package com.atlassian.theplugin.commons.bamboo.api;

import com.atlassian.theplugin.commons.bamboo.BambooProject;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-03-06
 * Time: 11:16:09
 */
public interface BambooSession extends ProductSession {
	int getBamboBuildNumber() throws RemoteApiException;

	@NotNull
	List<BambooProject> listProjectNames() throws RemoteApiException;

	@NotNull
	List<BambooPlan> listPlanNames() throws RemoteApiException;

	/**
	 * This result of this method does not contain properly set information about whether given build is enabled
	 * or not. There are two reasons for that:<ul>
	 * <li>remote API does not return this information and additional call is needed to retrieve all plan information
	 * to actually know whether given plan is enabled or not</li>
	 * <li>I tend to think that information whether given build is enabled or not does not make sense. "Enableness"
	 * belongs to plans rather than to builds. When build was executed it must have been enabled! So even though
	 * the plan may be disabled now it has nothing to do with the build for this plan which was executed before that!</li>
	 * </ul>
	 *
	 * Avoid calling this method, use rather {@link #getLatestBuildForPlan(String, boolean)}. In the future the whole
	 * concept may be probably rethought.
	 *
	 * @param planKey id of the plan
	 * @return last build for selected plan
	 * @throws RemoteApiException in case of some communication problem or malformed response
	 */
	@NotNull
	BambooBuildInfo getLatestBuildForPlan(String planKey) throws RemoteApiException;

	@NotNull
	BambooBuildInfo getLatestBuildForPlan(String planKey, boolean isPlanEnabled) throws RemoteApiException;

	@NotNull
	List<String> getFavouriteUserPlans() throws RemoteApiException;

	@NotNull
	BuildDetails getBuildResultDetails(String buildKey, String buildNumber) throws RemoteApiException;

	void addLabelToBuild(String buildKey, String buildNumber, String buildLabel) throws RemoteApiException;

	void addCommentToBuild(String buildKey, String buildNumber, String buildComment) throws RemoteApiException;

	void executeBuild(String buildKey) throws RemoteApiException;	

    byte[] getBuildLogs(String buildKey, String buildNumber) throws RemoteApiException;
}
