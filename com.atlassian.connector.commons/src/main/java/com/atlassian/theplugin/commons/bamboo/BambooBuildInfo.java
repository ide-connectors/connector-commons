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

import com.atlassian.theplugin.commons.RequestDataInfo;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BambooBuildInfo extends RequestDataInfo implements BambooBuild {
	private BambooServerCfg server;
	private final String serverUrl;
	private final String projectName;
	private final String planName;
	private final String planKey;
	private final boolean enabled;
	private final String buildState;
	private final String buildNumber;
	private final String buildReason;
	private String buildRelativeBuildDate;
	private String buildDurationDescription;
	private String buildTestSummary;
	private String buildCommitComment;
	private int buildTestsPassed;
	private int buildTestsFailed;
	private String message;

	private Date buildTime;
	private Date buildCompletedDate;
	public static final String BUILD_SUCCESSFUL = "Successful";
	public static final String BUILD_FAILED = "Failed";
	private Set<String> commiters = new HashSet<String>();


	public BambooBuildInfo(@NotNull String planKey, @Nullable String planName, @Nullable String serverUrl,
			@Nullable String projectName, boolean isEnabled, final String buildNumber,
			@Nullable String buildState, @Nullable String buildReason) {
		this.planKey = planKey;
		this.planName = planName;
		this.serverUrl = serverUrl;
		this.projectName = projectName;
		this.enabled = isEnabled;
		this.buildNumber = buildNumber;
		this.buildState = buildState;
		this.buildReason = buildReason;
	}

	public BambooServerCfg getServer() {
		return server;
	}

	public Date getBuildCompletedDate() {
		return buildCompletedDate;
	}

	public void setBuildCompletedDate(final Date buildCompletedDate) {
		this.buildCompletedDate = buildCompletedDate;
	}

	public void setServer(BambooServerCfg server) {
		this.server = server;
	}
		
	public String getServerUrl() {
		return this.serverUrl;
	}

//	public void setServerUrl(String serverUrl) {
//		this.serverUrl = serverUrl;
//	}
//
	public String getBuildUrl() {
		return this.serverUrl + "/browse/" + this.planKey;
	}

	public String getBuildResultUrl() {
		String url = this.serverUrl + "/browse/" + this.planKey;
		if (this.getStatus() != BuildStatus.UNKNOWN || this.buildNumber != null) {
			url += "-" + this.buildNumber;
		}

		return url;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getBuildName() {
		return planName;
	}

	public String getBuildKey() {
		return planKey;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public String getBuildReason() {
		return buildReason;
	}

	public String getBuildRelativeBuildDate() {
		return buildRelativeBuildDate;
	}

	public void setBuildRelativeBuildDate(String buildRelativeBuildDate) {
		this.buildRelativeBuildDate = buildRelativeBuildDate;
	}

	public String getBuildDurationDescription() {
		return buildDurationDescription;
	}

	public void setBuildDurationDescription(String buildDurationDescription) {
		this.buildDurationDescription = buildDurationDescription;
	}

	public String getBuildTestSummary() {
		return buildTestSummary;
	}

	public void setBuildTestSummary(String buildTestSummary) {
		this.buildTestSummary = buildTestSummary;
	}

	public String getBuildCommitComment() {
		return buildCommitComment;
	}

	public BuildStatus getStatus() {
		if (BUILD_SUCCESSFUL.equalsIgnoreCase(buildState)) {
			return BuildStatus.BUILD_SUCCEED;
		} else if (BUILD_FAILED.equalsIgnoreCase(buildState)) {
			return BuildStatus.BUILD_FAILED;
		} else {
			return BuildStatus.UNKNOWN;
		}
	}

	public String getMessage() {
		return this.message;
	}

	public void setBuildTestsPassed(int buildTestsPassed) {
		this.buildTestsPassed = buildTestsPassed;
	}

	public void setBuildTestsFailed(int buildTestsFailed) {
		this.buildTestsFailed = buildTestsFailed;
	}

	public int getTestsPassed() {
		return this.buildTestsPassed;
	}

	public int getTestsFailed() {
		return this.buildTestsFailed;
	}

	public void setBuildTime(Date buildTime) {
        if (buildTime != null) {
            this.buildTime = new Date(buildTime.getTime());
        }
    }

	public Date getBuildStartedDate() {
		return buildTime != null ? new Date(this.buildTime.getTime()) : null;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setBuildCommitComment(String buildCommitComment) {
		this.buildCommitComment = buildCommitComment;
	}

	@Override
	public String toString() {
		return projectName
				+ " " + planName
				+ " " + planKey
				+ " " + buildState
				+ " " + buildReason
				+ " " + buildTime
				+ " " + buildDurationDescription
				+ " " + buildTestSummary
				+ " " + buildCommitComment;
	}

	/**
	 * @return wheather I'm one of the commiters to that build or not
	 */
	public boolean isMyBuild() {
		return commiters.contains(server.getUsername());
	}

	/**
	 * @return list of commiters for this build
	 */
	public Set<String> getCommiters() {
		return commiters;
	}

	public void setCommiters(final Collection<String> commiters) {
		if (commiters != null) {
			this.commiters = new TreeSet<String>(commiters);
		}
	}

	@SuppressWarnings({"InnerClassFieldHidesOuterClassField"})
	public static class Builder {
		private String planKey;
		private String planName;
		private String serverUrl;
		private String projectName;
		private final String buildNumber;
		private boolean isEnabled = true;
		private String buildState;
		private String message;
		private Date startTime;
		private Collection<String> commiters;
		private Date pollingTime;
		private String buildReason;

		public Builder(@NotNull String planKey, @Nullable String planName, @Nullable String serverUrl,
				@Nullable String projectName, String buildNumber) {
			this.planKey = planKey;
			this.planName = planName;
			this.serverUrl = serverUrl;
			this.projectName = projectName;
			this.buildNumber = buildNumber;
		}

		public Builder enabled(boolean aIsEnabled) {
			isEnabled = aIsEnabled;
			return this;
		}

		public Builder reason(String aReason) {
			this.buildReason = aReason;
			return this;
		}

		public Builder state(String aState) {
			this.buildState = aState;
			return this;
		}

		public Builder message(String aMessage) {
			this.message = aMessage;
			return this;
		}

		public Builder startTime(Date aStartTime) {
			this.startTime = aStartTime;
			return this;
		}

		public Builder commiters(final Collection<String> aCommiters) {
			this.commiters = aCommiters;
			return this;
		}

		public Builder pollingTime(final Date aPollingTime) {
			this.pollingTime = aPollingTime;
			return this;
		}

		public BambooBuildInfo build() {
			final BambooBuildInfo buildInfo = new BambooBuildInfo(planKey, planName, serverUrl, projectName, isEnabled,
					buildNumber, buildState, buildReason);
			buildInfo.setMessage(message);
			buildInfo.setBuildTime(startTime);
			buildInfo.setCommiters(commiters);
			if (pollingTime != null) {
				buildInfo.setPollingTime(pollingTime);
			}
			return buildInfo;

		}
	}
}
