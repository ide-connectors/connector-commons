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

package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.theplugin.commons.exception.IncorrectVersionException;

/**
 * Created by IntelliJ IDEA. User: marek Date: Jul 21, 2008 Time: 3:48:41 PM To change this template use File | Settings | File
 * Templates.
 */
public class CrucibleVersionInfoBean implements CrucibleVersionInfo {
	private String buildDate;

	private String releaseNumber;

	private Integer major;
	private Integer minor;
	private Integer maintanance;
	private String build;

	public CrucibleVersionInfoBean() {
	}

	public String getBuildDate() {
		return buildDate;
	}

	public void setBuildDate(String buildDate) {
		this.buildDate = buildDate;
	}

	public String getReleaseNumber() {
		return releaseNumber;
	}

	public boolean isVersion2OrGreater() throws IncorrectVersionException {
		if (major == null) {
			throw new IncorrectVersionException("Incorrect version of Crucible: " + releaseNumber);
		}

		if (major >= 2) {
			return true;
		}

		return false;
	}

	public boolean isVersion21OrGreater() throws IncorrectVersionException {

		if (major == null || minor == null) {
			throw new IncorrectVersionException("Incorrect version of Crucible: " + releaseNumber);
		}

		if (major > 2 || (major == 2 && minor >= 1)) {
			return true;
		}

		return false;
	}

	private void tokenizeVersion() {
		String[] tokens = releaseNumber.split("[.]");

		major = minor = maintanance = null;
		build = null;

		try {
			if (tokens.length > 0) {
				major = Integer.valueOf(tokens[0]);
				if (tokens.length > 1) {
					minor = Integer.valueOf(tokens[1]);
					if (tokens.length > 2) {
						maintanance = Integer.valueOf(tokens[2]);
						if (tokens.length > 3) {
							build = tokens[3];
						}
					}
				}
			}

		} catch (NumberFormatException e) {
			// stop parsing
		}
	}

	public void setReleaseNumber(String releaseNumber) {
		this.releaseNumber = releaseNumber;
		tokenizeVersion();
	}
}
