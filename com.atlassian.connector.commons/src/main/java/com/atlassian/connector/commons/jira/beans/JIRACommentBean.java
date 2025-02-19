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

import java.util.Calendar;

public class JIRACommentBean implements JIRAComment {
	private String id;
	private String author;
	private String authorFullName;
	private String body;
	private Calendar created;

	public JIRACommentBean(String id, String author, String body, Calendar created) {
		this.id = id;
		this.author = author;
		this.authorFullName = author;
		this.body = body;
		this.created = created;
	}
	public String getId() {
		return id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthorFullName(String authorFullName) {
		this.authorFullName = authorFullName;
	}

	public String getAuthorFullName() {
		return authorFullName;
	}

	public String getBody() {
		return body;  
	}

	public Calendar getCreationDate() {
		return created;
	}
}
