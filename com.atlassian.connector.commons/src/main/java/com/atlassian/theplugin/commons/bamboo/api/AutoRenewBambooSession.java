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

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooProject;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class AutoRenewBambooSession implements BambooSession {
	private final BambooSession delegate;
	private String userName;
	private char[] password;

	public AutoRenewBambooSession(ServerData serverCfg, HttpSessionCallback callback) throws RemoteApiException {
		this.delegate = new BambooSessionImpl(serverCfg, callback);
	}

	AutoRenewBambooSession(BambooSession bambooSession) throws RemoteApiException {
		this.delegate = bambooSession;
	}

	public void addCommentToBuild(@NotNull String planKey, int buildNumber, String buildComment) throws RemoteApiException {
		try {
			delegate.addCommentToBuild(planKey, buildNumber, buildComment);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.addCommentToBuild(planKey, buildNumber, buildComment);
		}
	}

	public void executeBuild(@NotNull String buildKey) throws RemoteApiException {
		try {
			delegate.executeBuild(buildKey);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.executeBuild(buildKey);
		}
	}

	public void addLabelToBuild(@NotNull String planKey, int buildNumber, String buildLabel) throws RemoteApiException {
		try {
			delegate.addLabelToBuild(planKey, buildNumber, buildLabel);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			delegate.addLabelToBuild(planKey, buildNumber, buildLabel);
		}
	}

	@NotNull
	public BuildDetails getBuildResultDetails(@NotNull String planKey, int buildNumber) throws RemoteApiException {
		try {
			return delegate.getBuildResultDetails(planKey, buildNumber);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getBuildResultDetails(planKey, buildNumber);
		}
	}

	@NotNull
	public List<String> getFavouriteUserPlans() throws RemoteApiException {
		try {
			return delegate.getFavouriteUserPlans();
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getFavouriteUserPlans();
		}
	}

	@NotNull
	public BambooBuild getLatestBuildForPlan(@NotNull final String planKey, final boolean isPlanEnabled,
			final int timezoneOffset)
			throws RemoteApiException {
		try {
			return delegate.getLatestBuildForPlan(planKey, isPlanEnabled, timezoneOffset);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getLatestBuildForPlan(planKey, isPlanEnabled, timezoneOffset);
		}
	}

	@NotNull
	public BambooBuild getLatestBuildForPlan(@NotNull String planKey, final int timezoneOffset) throws RemoteApiException {
		try {
			return delegate.getLatestBuildForPlan(planKey, timezoneOffset);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getLatestBuildForPlan(planKey, timezoneOffset);
		}
	}

	public boolean isLoggedIn() {
		return delegate.isLoggedIn();
	}

	public String getBuildLogs(@NotNull String planKey, int buildNumber) throws RemoteApiException {
		try {
			return delegate.getBuildLogs(planKey, buildNumber);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getBuildLogs(planKey, buildNumber);
		}
	}

	public Collection<BambooBuild> getRecentBuildsForPlan(@NotNull final String planKey, final int timezoneOffset)
			throws RemoteApiException {
		try {
			return delegate.getRecentBuildsForPlan(planKey, timezoneOffset);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getRecentBuildsForPlan(planKey, timezoneOffset);
		}
	}

	public Collection<BambooBuild> getRecentBuildsForUser(final int timezoneOffset)
			throws RemoteApiException {
		try {
			return delegate.getRecentBuildsForUser(timezoneOffset);
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getRecentBuildsForUser(timezoneOffset);
		}
	}

	@NotNull
	public List<BambooPlan> listPlanNames() throws RemoteApiException {
		try {
			return delegate.listPlanNames();
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.listPlanNames();
		}
	}

	@NotNull
	public List<BambooProject> listProjectNames() throws RemoteApiException {
		try {
			return delegate.listProjectNames();
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.listProjectNames();
		}
	}

	public void login(String name, char[] aPassword) throws RemoteApiLoginException {
		this.userName = name;
		this.password = new char[aPassword.length];
		System.arraycopy(aPassword, 0, password, 0, aPassword.length);
		delegate.login(name, aPassword);
	}

	public void logout() {
		delegate.logout();
	}

	public int getBamboBuildNumber() throws RemoteApiException {
		try {
			return delegate.getBamboBuildNumber();
		} catch (RemoteApiSessionExpiredException e) {
			delegate.login(userName, password);
			return delegate.getBamboBuildNumber();
		}
	}
}
