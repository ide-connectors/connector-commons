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

package com.atlassian.connector.commons.jira.beans;

import java.net.URL;
import java.util.Map;

public class JIRAStatusBean extends AbstractJIRAConstantBean {
    public JIRAStatusBean(Map<String, String> map) {
        super(map);
    }

	public JIRAStatusBean(long id, String name, URL iconUrl) {
		super(id, name, iconUrl);
	}

	public JIRAStatusBean(JIRAStatusBean other) {
		this(other.getMap());
	}

	public String getQueryStringFragment() {
        return "status=" + getId();
    }

	public JIRAStatusBean getClone() {
		return new JIRAStatusBean(this);
	}
}
