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
package com.atlassian.connector.commons.jira.rss;

import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;

import java.util.List;

/**
 * @autrhor pmaruszak
 * @date May 11, 2010
 * For future use. Here should be renewed session if expires
 */
public class JiraRssAutoRenewClient {
    private final JIRARssClient rssClient;


    public JiraRssAutoRenewClient(JIRARssClient rssClient) {
        this.rssClient = rssClient;
    }

    public List<JIRAIssue> getIssues(String queryString, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        if (!rssClient.isLoggedIn()) {
            rssClient.login();
        }
        return rssClient.getIssues(queryString, sortBy, sortOrder, start, max);  
    }

    public List<JIRAIssue> getIssues(List<JIRAQueryFragment> fragments, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        if (!rssClient.isLoggedIn()) {
            rssClient.login();
        }
        return rssClient.getIssues(fragments, sortBy, sortOrder, start, max);
    }

    public List<JIRAIssue> getAssignedIssues(String assignee) throws JIRAException {
        if (!rssClient.isLoggedIn()) {
            rssClient.login();
        }
        return rssClient.getAssignedIssues(assignee);
    }

    public List<JIRAIssue> getSavedFilterIssues(JIRAQueryFragment fragment, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        if (!rssClient.isLoggedIn()) {
            rssClient.login();
        }
        return rssClient.getSavedFilterIssues(fragment, sortBy, sortOrder, start, max);
    }

    public JIRAIssue getIssue(String issueKey) throws JIRAException {
        if (!rssClient.isLoggedIn()) {
            rssClient.login();
        }
        return rssClient.getIssue(issueKey);
    }
}
