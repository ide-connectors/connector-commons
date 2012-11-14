package com.atlassian.connector.commons.jira.rest;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.*;
import com.atlassian.connector.commons.jira.beans.*;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.jira.JiraCaptchaRequiredException;
import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * User: kalamon
 * Date: 14.11.12
 * Time: 16:29
 */
public class JiraRestSessionImpl implements JIRASessionPartOne, JIRASessionPartTwo {
    private final ConnectionCfg server;
    private final HttpConfigurableAdapter proxyInfo;

    public JiraRestSessionImpl(ConnectionCfg server, HttpConfigurableAdapter proxyInfo) {
        //To change body of created methods use File | Settings | File Templates.
        this.server = server;
        this.proxyInfo = proxyInfo;
    }

    public boolean supportsRest() throws JIRAException {
        throw new JiraRESTFacade2Impl.NotYetImplemented();
    }

    public void login(String userName, String password) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public void logout() {
    }

    public void logWork(JIRAIssue issue, String timeSpent, Calendar startDate, String comment, boolean updateEstimate, String newEstimate) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public void addComment(String issueKey, String comment) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public void addAttachment(String issueKey, String name, byte[] content) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public JIRAIssue createIssue(JIRAIssue issue) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public JIRAIssue getIssueDetails(JIRAIssue issue) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAProject> getProjects() throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAConstant> getIssueTypes() throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAConstant> getIssueTypesForProject(String project) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAConstant> getSubtaskIssueTypes() throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(String project) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAConstant> getStatuses() throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAComponentBean> getComponents(String projectKey) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAVersionBean> getVersions(String projectKey) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAPriorityBean> getPriorities() throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAResolutionBean> getResolutions() throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAQueryFragment> getSavedFilters() throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAAction> getAvailableActions(JIRAIssue issue) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAActionField> getFieldsForAction(JIRAIssue issue, JIRAAction action) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public void progressWorkflowAction(JIRAIssue issue, JIRAAction action, List<JIRAActionField> fields) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public void setField(JIRAIssue issue, String fieldId, String value) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public void setField(JIRAIssue issue, String fieldId, String[] values) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public void setFields(JIRAIssue issue, List<JIRAActionField> fields) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public JIRAUserBean getUser(String loginName) throws RemoteApiException, JiraUserNotFoundException {
        throw new NotYetImplemented();
    }

    public List<JIRAComment> getComments(JIRAIssue issue) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public Collection<JIRAAttachment> getIssueAttachements(JIRAIssue issue) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(String projectKey) throws RemoteApiException {
        throw new NotYetImplemented();
    }

    public List<JIRAIssue> getIssues(String queryString, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        throw new JiraRESTFacade2Impl.NotYetImplemented();
    }

    public List<JIRAIssue> getIssues(List<JIRAQueryFragment> fragments, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        throw new JiraRESTFacade2Impl.NotYetImplemented();
    }

    public List<JIRAIssue> getAssignedIssues(String assignee) throws JIRAException {
        throw new JiraRESTFacade2Impl.NotYetImplemented();
    }

    public List<JIRAIssue> getSavedFilterIssues(JIRAQueryFragment fragment, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        throw new JiraRESTFacade2Impl.NotYetImplemented();
    }

    public JIRAIssue getIssue(String issueKey) throws JIRAException {
        throw new JiraRESTFacade2Impl.NotYetImplemented();
    }

    public void login() throws JIRAException, JiraCaptchaRequiredException {
        throw new JiraRESTFacade2Impl.NotYetImplemented();
    }

    public boolean isLoggedIn(ConnectionCfg server) {
        // is this even used anywhere?
        return false;
    }

    public boolean isLoggedIn() {
        return isLoggedIn(server);
    }

    public static class NotYetImplemented extends RemoteApiException {
        public NotYetImplemented() {
            super(JiraRESTFacade2Impl.NOT_IMPLEMENTED_YET_COME_BACK_SOON);
        }
    }
}
