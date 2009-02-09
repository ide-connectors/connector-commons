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

import com.atlassian.theplugin.commons.cfg.BambooServerCfg;

import java.util.Date;
import java.util.Set;
import java.util.Collection;
import java.util.TreeSet;
import java.util.HashSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BambooBuildInfo implements BambooBuild {
	private final Date pollingTime;
	private final BambooServerCfg server;
	private final String projectName;
	private final String planName;
	private final String planKey;
	private final boolean enabled;
	@NotNull
	private final BuildStatus buildState;
	private final String buildNumber;
	private final String buildReason;
	private final String buildRelativeBuildDate;
	private final String buildDurationDescription;
	private final String buildTestSummary;
	private final String commitComment;
	private final int testsPassedCount;
	private final int testsFailedCount;
	private final String message;

	private final Date buildTime;
	private final Date buildCompletedDate;
	private final Set<String> commiters;


	public BambooBuildInfo(@NotNull String planKey, @Nullable String planName, @NotNull BambooServerCfg bambooServerCfg,
			@NotNull Date pollingTime, @Nullable String projectName, boolean isEnabled, @Nullable String buildNumber,
			@NotNull BuildStatus buildState, @Nullable String buildReason, @Nullable Date startTime,
			@Nullable String buildTestSummary, @Nullable String commitComment, final int testsPassedCount,
			final int testsFailedCount, @Nullable Date completedDate, @Nullable String message,
			@Nullable String relativeBuildDate, @Nullable String buildDurationDescription,
			@Nullable Collection<String> commiters) {
		this.pollingTime = pollingTime;
		this.planKey = planKey;
		this.planName = planName;
		this.server = bambooServerCfg;
		this.projectName = projectName;
		this.enabled = isEnabled;
		this.buildNumber = buildNumber;
		this.buildState = buildState;
		this.buildReason = buildReason;
		this.buildTestSummary = buildTestSummary;
		this.commitComment = commitComment;
		this.testsPassedCount = testsPassedCount;
		this.testsFailedCount = testsFailedCount;
		this.message = message;
		this.buildRelativeBuildDate = relativeBuildDate;
		this.buildDurationDescription = buildDurationDescription;
		this.buildTime =  (startTime != null) ? new Date(startTime.getTime()) : null;
		this.buildCompletedDate =  (completedDate != null) ? new Date(completedDate.getTime()) : null;
		if (commiters != null) {
			this.commiters = new TreeSet<String>(commiters);
		} else {
			this.commiters = new HashSet<String>();
		}
	}

	public BambooServerCfg getServer() {
		return server;
	}

	public Date getBuildCompletedDate() {
		return buildCompletedDate;
	}

	public String getServerUrl() {
		return server.getUrl();
	}

	public String getBuildUrl() {
		return getServerUrl() + "/browse/" + this.planKey;
	}

	public String getBuildResultUrl() {
		String url = getServerUrl() + "/browse/" + this.planKey;
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

	public String getBuildDurationDescription() {
		return buildDurationDescription;
	}

	public String getBuildTestSummary() {
		return buildTestSummary;
	}

	public String getCommitComment() {
		return commitComment;
	}

	@NotNull
	public BuildStatus getStatus() {
		return buildState;
	}

	public String getMessage() {
		return this.message;
	}

	public int getTestsPassed() {
		return this.testsPassedCount;
	}

	public int getTestsFailed() {
		return this.testsFailedCount;
	}

	public Date getBuildStartedDate() {
		return buildTime != null ? new Date(this.buildTime.getTime()) : null;
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
				+ " " + commitComment;
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

	public Date getPollingTime() {
		return pollingTime;
	}

	@SuppressWarnings({"InnerClassFieldHidesOuterClassField"})
	public static class Builder {
		private final String planKey;
		private final String planName;
		private final BambooServerCfg bambooServerCfg;
		private final String projectName;
		private final String buildNumber;
		@NotNull
		private final BuildStatus buildState;
		private boolean isEnabled = true;
		private String message;
		private Date startTime;
		private Collection<String> commiters;
		private Date pollingTime = new Date();
		private String buildReason;
		@Nullable
		private String testSummary;
		@Nullable
		private String commitComment;
		private int testsPassedCount;
		private int testsFailedCount;
		private Date completionTime;
		@Nullable
		private String relativeBuildDate;
		@Nullable
		private String durationDescription;

		public Builder(@NotNull String planKey, @Nullable String planName, @NotNull BambooServerCfg bambooServerCfg,
				@Nullable String projectName, @Nullable String buildNumber, @NotNull BuildStatus state) {
			this.planKey = planKey;
			this.planName = planName;
			this.bambooServerCfg = bambooServerCfg;
			this.projectName = projectName;
			this.buildNumber = buildNumber;
			this.buildState = state;
		}

		public Builder enabled(boolean aIsEnabled) {
			isEnabled = aIsEnabled;
			return this;
		}

		public Builder reason(String aReason) {
			this.buildReason = aReason;
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

		public Builder completionTime(Date aCompletionTime) {
			this.completionTime = aCompletionTime;
			return this;
		}


		public Builder commiters(final Collection<String> aCommiters) {
			this.commiters = aCommiters;
			return this;
		}

		public Builder testSummary(@Nullable final String aTestSummary) {
			this.testSummary = aTestSummary;
			return this;
		}

		public Builder commitComment(@Nullable final String aCommitComment) {
			this.commitComment = aCommitComment;
			return this;
		}

		public Builder pollingTime(final Date aPollingTime) {
			this.pollingTime = aPollingTime;
			return this;
		}

		public Builder testsPassedCount(int aTestsPassedCount) {
			this.testsPassedCount = aTestsPassedCount;
			return this;
		}

		public Builder testsFailedCount(int aTestsFailedCount) {
			this.testsFailedCount = aTestsFailedCount;
			return this;
		}

		public Builder relativeBuildDate(@Nullable String aRelativeBuildDate) {
			this.relativeBuildDate = aRelativeBuildDate;
			return this;
		}

		public Builder durationDescription(@Nullable String aDurationDescription) {
			this.durationDescription = aDurationDescription;
			return this;
		}


		public BambooBuildInfo build() {
			return new BambooBuildInfo(planKey, planName, bambooServerCfg, pollingTime, projectName, 
					isEnabled, buildNumber, buildState, buildReason, startTime, testSummary, commitComment, testsPassedCount,
					testsFailedCount, completionTime, message, relativeBuildDate, durationDescription, commiters);
		}
	}
}
