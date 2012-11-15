package com.atlassian.connector.commons.jira.rest;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.*;
import com.atlassian.connector.commons.jira.beans.*;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.jira.JiraCaptchaRequiredException;
import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * User: kalamon
 * Date: 14.11.12
 * Time: 16:29
 */
public class JiraRestSessionImpl implements JIRASessionPartOne, JIRASessionPartTwo {
    private final ConnectionCfg server;
    private final HttpConfigurableAdapter proxyInfo;
    private final JiraRestClient restClient;
    final NullProgressMonitor pm = new NullProgressMonitor();

    public JiraRestSessionImpl(ConnectionCfg server, HttpConfigurableAdapter proxyInfo) throws URISyntaxException {
        this.server = server;
        this.proxyInfo = proxyInfo;

        JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
        // TODO: support proxies - see https://studio.atlassian.com/browse/JRJC-107
        restClient = factory.createWithBasicHttpAuthentication(new URI(server.getUrl()), server.getUsername(), server.getPassword());
    }

    public boolean supportsRest() throws JIRAException {
        try {
            return restClient.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_5;
        } catch (Exception e) {
            return false;
        }


    }

    public void login(String userName, String password) throws RemoteApiException {
        wrapWithRemoteApiException(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                restClient.getMetadataClient().getServerInfo(pm);
                return null;
            }
        });
    }

    public void logout() {
    }

    public void logWork(JIRAIssue issue, String timeSpent, Calendar startDate, String comment, boolean updateEstimate, String newEstimate) throws RemoteApiException {
        throw nyi();
    }

    public void addComment(String issueKey, String comment) throws RemoteApiException {
        throw nyi();
    }

    public void addAttachment(String issueKey, String name, byte[] content) throws RemoteApiException {
        throw nyi();
    }

    public JIRAIssue createIssue(JIRAIssue issue) throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAProject> getProjects() throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAConstant> getIssueTypes() throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAConstant> getIssueTypesForProject(String project) throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAConstant> getSubtaskIssueTypes() throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(String project) throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAConstant> getStatuses() throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAComponentBean> getComponents(String projectKey) throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAVersionBean> getVersions(String projectKey) throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAPriorityBean> getPriorities() throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAResolutionBean> getResolutions() throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAQueryFragment> getSavedFilters() throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAAction> getAvailableActions(JIRAIssue issue) throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAActionField> getFieldsForAction(JIRAIssue issue, JIRAAction action) throws RemoteApiException {
        throw nyi();
    }

    public void progressWorkflowAction(JIRAIssue issue, JIRAAction action, List<JIRAActionField> fields) throws RemoteApiException {
        throw nyi();
    }

    public void setField(JIRAIssue issue, String fieldId, String value) throws RemoteApiException {
        throw nyi();
    }

    public void setField(JIRAIssue issue, String fieldId, String[] values) throws RemoteApiException {
        throw nyi();
    }

    public void setFields(JIRAIssue issue, List<JIRAActionField> fields) throws RemoteApiException {
        throw nyi();
    }

    public JIRAUserBean getUser(String loginName) throws RemoteApiException, JiraUserNotFoundException {
        throw nyi();
    }

    public List<JIRAComment> getComments(JIRAIssue issue) throws RemoteApiException {
        throw nyi();
    }

    public Collection<JIRAAttachment> getIssueAttachements(JIRAIssue issue) throws RemoteApiException {
        throw nyi();
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(String projectKey) throws RemoteApiException {
        throw nyi();
    }

    public List<JIRAIssue> getIssues(String queryString, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        throw nyij();
    }

    public List<JIRAIssue> getIssues(List<JIRAQueryFragment> fragments, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        throw nyij();
    }

    public List<JIRAIssue> getAssignedIssues(String assignee) throws JIRAException {
        throw nyij();
    }

    public List<JIRAIssue> getSavedFilterIssues(JIRAQueryFragment fragment, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        throw nyij();
    }

    public JIRAIssue getIssue(String issueKey) throws JIRAException {
        throw nyij();
    }

    public JIRAIssue getIssueDetails(JIRAIssue issue) throws RemoteApiException {
        try {
            return getIssue(issue.getKey());
        } catch (JIRAException e) {
            throw new RemoteApiException(e);
        }
    }

    public void login() throws JIRAException, JiraCaptchaRequiredException {
        throw nyij();
    }

    public boolean isLoggedIn(ConnectionCfg server) {
        // is this even used anywhere?
        return false;
    }

    public void testConnection() throws RemoteApiException {
        try {
            supportsRest();
        } catch (JIRAException e) {
            throw new RemoteApiException(e);
        }
    }

    public boolean isLoggedIn() {
        return isLoggedIn(server);
    }

    private <T> T wrapWithJiraException(Callable<T> c) throws JIRAException {
        try {
            return c.call();
        } catch (Exception e) {
            throw new JIRAException(e.getMessage(), e);
        }
    }

    private <T> T wrapWithRemoteApiException(Callable<T> c) throws RemoteApiException {
        try {
            return c.call();
        } catch (Exception e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    private RemoteApiException nyi() {
        return new RemoteApiException(NOT_IMPLEMENTED_YET_COME_BACK_SOON);
    }

    private JIRAException nyij() {
        return new JIRAException(NOT_IMPLEMENTED_YET_COME_BACK_SOON);
    }

    private static final String NOT_IMPLEMENTED_YET_COME_BACK_SOON = "Not implemented yet. Come back soon";
}
