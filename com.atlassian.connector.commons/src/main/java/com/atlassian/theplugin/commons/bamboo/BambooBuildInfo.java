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

import java.util.*;

public final class BambooBuildInfo implements BambooBuild {
	private final Date pollingTime;
	private final ServerData server;
	private final String projectName;
	private final String planName;
	@NotNull
	private final String planKey;
	private final boolean enabled;
	@NotNull
	private final BuildStatus status;
	private final Integer number;
	@Nullable
	private final String reason;
	private final String relativeBuildDate;
	@Nullable
	private final String durationDescription;
	private final String testSummary;
	private final String commitComment;
	private final int testsPassedCount;
	private final int testsFailedCount;
	private final String message;

	@Nullable
	private final Date startDate;
	private final Date completionDate;
	private final Set<String> commiters;


	public BambooBuildInfo(@NotNull String planKey, @Nullable String planName, @NotNull ServerData serverData,
			@NotNull Date pollingTime, @Nullable String projectName, boolean isEnabled, @Nullable Integer number,
			@NotNull BuildStatus status, @Nullable String reason, @Nullable Date startDate,
			@Nullable String testSummary, @Nullable String commitComment, final int testsPassedCount,
			final int testsFailedCount, @Nullable Date completionDate, @Nullable String message,
			@Nullable String relativeBuildDate, @Nullable String durationDescription,
			@Nullable Collection<String> commiters) {
		this.pollingTime = new Date(pollingTime.getTime());
		this.planKey = planKey;
		this.planName = planName;
		this.server = serverData;
		this.projectName = projectName;
		this.enabled = isEnabled;
		this.number = number;
		this.status = status;
		this.reason = reason;
		this.testSummary = testSummary;
		this.commitComment = commitComment;
		this.testsPassedCount = testsPassedCount;
		this.testsFailedCount = testsFailedCount;
		this.message = message;
		this.relativeBuildDate = relativeBuildDate;
		this.durationDescription = durationDescription;
		this.startDate = (startDate != null) ? new Date(startDate.getTime()) : null;
		this.completionDate = (completionDate != null) ? new Date(completionDate.getTime()) : null;
		if (commiters != null) {
			this.commiters = new TreeSet<String>(commiters);
		} else {
			this.commiters = new HashSet<String>();
		}
	}

	public ServerData getServer() {
		return server;
	}

	@Nullable
	public Date getCompletionDate() {
		return completionDate == null ? null : new Date(completionDate.getTime());
	}

	public String getServerUrl() {
		return server.getUrl();
	}

	public String getBuildUrl() {
		return getServerUrl() + "/browse/" + this.planKey;
	}

	public String getResultUrl() {
		String url = getServerUrl() + "/browse/" + this.planKey;
		if (this.getStatus() != BuildStatus.UNKNOWN || this.number != null) {
			url += "-" + this.number;
		}

		return url;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getPlanName() {
		return planName;
	}

	@NotNull
	public String getPlanKey() {
		return planKey;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public boolean isValid() {
		return number != null;
	}

	/**
	 * @return build number
	 * @throws UnsupportedOperationException in case this object represents invalid build
	 */
	public int getNumber() throws UnsupportedOperationException {
		if (number == null) {
			throw new UnsupportedOperationException("This build has no number information");
		}
		return number;
	}

	@Nullable
	public String getReason() {
		return reason;
	}

	public String getRelativeBuildDate() {
		return relativeBuildDate;
	}

	@Nullable
	public String getDurationDescription() {
		return durationDescription;
	}

	public String getTestSummary() {
		return testSummary;
	}

	public String getCommitComment() {
		return commitComment;
	}

	@NotNull
	public BuildStatus getStatus() {
		return status;
	}

	public String getErrorMessage() {
		return this.message;
	}

	public int getTestsPassed() {
		return this.testsPassedCount;
	}

	public int getTestsFailed() {
		return this.testsFailedCount;
	}

	@Nullable
	public Date getStartDate() {
		return startDate != null ? new Date(this.startDate.getTime()) : null;
	}

	@Override
	public String toString() {
		return projectName
				+ " " + planName
				+ " " + planKey
				+ " " + status
				+ " " + reason
				+ " " + startDate
				+ " " + durationDescription
				+ " " + testSummary
				+ " " + commitComment;
	}

	/**
	 * @return wheather I'm one of the commiters to that build or not
	 */
	public boolean isMyBuild() {
		return commiters.contains(server.getUserName());
	}

	/**
	 * @return list of commiters for this build
	 */
	public Set<String> getCommiters() {
		return commiters;
	}

	public Date getPollingTime() {
		return new Date(pollingTime.getTime());
	}

	@SuppressWarnings({"InnerClassFieldHidesOuterClassField"})
	public static class Builder {
		private final String planKey;
		private final String planName;
		private final ServerData serverData;
		private final String projectName;
		private final Integer buildNumber;
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

		public Builder(@NotNull String planKey, @NotNull ServerData serverData, @NotNull BuildStatus state) {
			this.planKey = planKey;
			this.serverData = serverData;
			this.buildState = state;
			planName = null;
			projectName = null;
			buildNumber = null;
		}

		public Builder(@NotNull String planKey, @Nullable String planName, @NotNull ServerData serverData,
				@Nullable String projectName, @Nullable Integer buildNumber, @NotNull BuildStatus state) {
			this.planKey = planKey;
			this.planName = planName;
			this.serverData = serverData;
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

		public Builder errorMessage(String aMessage) {
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
			return new BambooBuildInfo(planKey, planName, serverData, pollingTime, projectName,
					isEnabled, buildNumber, buildState, buildReason, startTime, testSummary, commitComment, testsPassedCount,
					testsFailedCount, completionTime, message, relativeBuildDate, durationDescription, commiters);
		}
	}
}
