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
import com.atlassian.theplugin.commons.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-05-28
 * Time: 11:47:32
 * To change this template use File | Settings | File Templates.
 */
public class BambooBuildAdapter {
	protected BambooBuild build;
	public static final SimpleDateFormat BAMBOO_BUILD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public BambooBuildAdapter(BambooBuild build) {
		this.build = build;
	}

	public BambooServerCfg getServer() {
		return build.getServer();
	}

	public String getServerName() {
		if (build.getServer() != null) {
			return build.getServer().getName() == null ? "" : build.getServer().getName();
		} else {
			return "";
		}
	}

	public boolean isBamboo2() {
		if (build.getServer() != null) {
			return build.getServer().isBamboo2();
		} else {
			return false;
		}
	}

	public String getServerUrl() {
		return build.getServerUrl() == null ? "" : build.getServerUrl();
	}

	public Collection<String> getCommiters() {
		return build.getCommiters();
	}

	public String getProjectName() {
		return build.getProjectName() == null ? "" : build.getProjectName();
	}

//	public String getProjectKey() {
//		return build.getProjectKey() == null ? "" : build.getProjectKey();
//	}

	public String getProjectUrl() {
		return build.getProjectUrl() == null ? "" : build.getProjectUrl();
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

	public Date getBuildTime() {
		return build.getBuildTime();
	}

	public Date getBuildCompletedDate() {
		return build.getBuildCompletedDate();
	}

	public String getBuildRelativeBuildDate() {
		return build.getBuildRelativeBuildDate() == null ? "" : build.getBuildRelativeBuildDate();
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
