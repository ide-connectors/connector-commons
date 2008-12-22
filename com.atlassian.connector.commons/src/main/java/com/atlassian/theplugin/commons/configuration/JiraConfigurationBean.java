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

package com.atlassian.theplugin.commons.configuration;

public class JiraConfigurationBean {

	private static final int JIRA_DEFAULT_ISSUE_PAGE_SIZE = 25;

	private int pageSize = JIRA_DEFAULT_ISSUE_PAGE_SIZE;

	private static final int HASHCODE_MAGIC = 31;

	public JiraConfigurationBean() {
	}

	public JiraConfigurationBean(JiraConfigurationBean cfg) {
		this.pageSize = cfg.getPageSize();
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JiraConfigurationBean)) {
			return false;
		}

		final JiraConfigurationBean that = (JiraConfigurationBean) o;

		if (pageSize != that.pageSize) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = pageSize;
		result = HASHCODE_MAGIC * result;
		return result;
	}
}