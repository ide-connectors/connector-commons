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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * @author Jacek Jaroczynski
 */
public class BambooBuildAdapter {
	protected final BambooBuild build;
	public static final SimpleDateFormat BAMBOO_BUILD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public BambooBuildAdapter(BambooBuild build) {
		this.build = build;
	}

	public BambooServerCfg getServer() {
		return build.getServer();
	}

	public String getServerName() {
		final BambooServerCfg server = build.getServer();
		if (server != null) {
			return server.getName() == null ? "" : server.getName();
		} else {
			return "";
		}
	}

	public boolean isBamboo2() {
		final BambooServerCfg server = build.getServer();
		return server != null && server.isBamboo2();
	}

	public Collection<String> getCommiters() {
		return build.getCommiters();
	}

	public String getProjectName() {
		return build.getProjectName() == null ? "" : build.getProjectName();
	}

	public String getBuildUrl() {
		return build.getBuildUrl() == null ? "" : build.getBuildUrl();
	}

	public String getBuildName() {
		return build.getBuildName() == null ? "" : build.getBuildName();
	}

	public String getBuildKey() {
		return build.getBuildKey() == null ? "" : build.getBuildKey();
	}

	public boolean getEnabled() {
		return build.getEnabled();
	}

	public String getBuildNumber() {
		return build.getBuildNumber() == null ? "" : build.getBuildNumber();
	}

	public String getBuildResultUrl() {
		return build.getBuildResultUrl() == null ? "" : build.getBuildResultUrl();
	}

	public BuildStatus getStatus() {
		return build.getStatus();
	}

	public String getMessage() {
		return build.getMessage() == null ? "" : build.getMessage();
	}

	public int getTestsPassed() {
		return build.getTestsPassed();
	}

	public int getTestsFailed() {
		return build.getTestsFailed();
	}

	public int getTestsNumber() {
		return build.getTestsPassed() + build.getTestsFailed();
	}

	public String getTestsPassedSummary() {
		if (getStatus() == BuildStatus.UNKNOWN) {
			return "-/-";
		} else {
			return getTestsFailed() + "/" + getTestsNumber();
		}
	}

	public Date getBuildCompletedDate() {
		return build.getBuildCompletedDate();
	}

	public Date getPollingTime() {
		return build.getPollingTime();
	}

	public String getBuildReason() {
		return build.getBuildReason() == null ? "" : build.getBuildReason();
	}

	public BambooBuild getBuild() {
		return build;
	}

	public boolean isMyBuild() {
		return build.isMyBuild();
	}
}
