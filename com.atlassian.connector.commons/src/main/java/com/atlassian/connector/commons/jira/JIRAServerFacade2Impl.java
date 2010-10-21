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

package com.atlassian.connector.commons.jira;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.beans.JIRAAttachment;
import com.atlassian.connector.commons.jira.beans.JIRAComment;
import com.atlassian.connector.commons.jira.beans.JIRAComponentBean;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRAResolutionBean;
import com.atlassian.connector.commons.jira.beans.JIRASecurityLevelBean;
import com.atlassian.connector.commons.jira.beans.JIRAUserBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.connector.commons.jira.rss.JIRARssClient;
import com.atlassian.connector.commons.jira.rss.JiraRssAutoRenewClient;
import com.atlassian.connector.commons.jira.soap.AxisSessionCallback;
import com.atlassian.connector.commons.jira.soap.JIRASession;
import com.atlassian.connector.commons.jira.soap.JIRASessionImpl;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.remoteapi.CaptchaRequiredException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.jira.JiraCaptchaRequiredException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.util.Logger;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JIRAServerFacade2Impl implements JIRAServerFacade2 {

    private final HttpSessionCallback callback;
    private final AxisSessionCallback axisCallback;
    private static Logger logger;

    private final Map<String, JiraRssAutoRenewClient> rssSessions = new HashMap<String, JiraRssAutoRenewClient>();
    private final Map<String, JIRASession> soapSessions = new HashMap<String, JIRASession>();

    private static final long FIVE_MINUTES = 5 * 60 * 1000;

    private String getSessionKey(ConnectionCfg httpConnectionCfg) {
        return httpConnectionCfg.getUrl() + "_" + httpConnectionCfg.getUsername() + "_" + httpConnectionCfg.getPassword();
    }

    public JIRAServerFacade2Impl(HttpSessionCallback callback, AxisSessionCallback axisCallback) {
        this.callback = callback;
        this.axisCallback = axisCallback;
    }

    private synchronized JIRASession getSoapSession(ConnectionCfg connectionCfg) throws RemoteApiException {
        String key = getSessionKey(connectionCfg);

        JIRASession session = soapSessions.get(key);
        if (session == null) {
            try {
                session = new JIRASessionImpl(logger, connectionCfg, axisCallback);
            } catch (MalformedURLException e) {
                throw new RemoteApiException(e);
            } catch (ServiceException e) {
                throw new RemoteApiException(e);
            }

            session.login(connectionCfg.getUsername(), connectionCfg.getPassword());
            soapSessions.put(key, session);
        }
        return session;
    }

    private synchronized JiraRssAutoRenewClient getRssSession(ConnectionCfg server) throws RemoteApiException {
        String key = getSessionKey(server);

        JiraRssAutoRenewClient session = rssSessions.get(key);

        // sessions should time out after 5 mins to avoid silent session drop on JIRA side

        if (session == null || (session.getLastUsed().getTime() < new Date().getTime() - FIVE_MINUTES)) {


            JIRARssClient client = new JIRARssClient(server, callback);
            ((HttpSessionCallbackImpl) callback).disposeClient(server);
            try {
                client.login();
            } catch (JiraCaptchaRequiredException e) {
                throw new CaptchaRequiredException(e);
            } catch (JIRAException e) {
                throw new RemoteApiException(e);
            }

            session = new JiraRssAutoRenewClient(client);
            rssSessions.put(key, session);
        }
        session.setLastUsed(new Date());
        return session;
    }

    private synchronized void removeRssSession(ConnectionCfg server) {
        rssSessions.remove(getSessionKey(server));
    }

    public void testServerConnection(ConnectionCfg httpConnectionCfg)
            throws RemoteApiException {
        JIRASession session;
        try {
            session = new JIRASessionImpl(logger, httpConnectionCfg, axisCallback);
        } catch (MalformedURLException e) {
            throw new RemoteApiException(e);
        } catch (ServiceException e) {
            throw new RemoteApiLoginException(e.getMessage(), e);
        }
        session.login(httpConnectionCfg.getUsername(), httpConnectionCfg.getPassword());
    }

    public ServerType getServerType() {
        return ServerType.JIRA_SERVER;
    }

    public static void setLogger(Logger logger) {
        JIRAServerFacade2Impl.logger = logger;
    }

    public List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg, String queryString, String sort,
                                     String sortOrder, int start, int size) throws JIRAException {
        try {
            JiraRssAutoRenewClient rss = getRssSession(httpConnectionCfg);
            return rss.getIssues(queryString, sort, sortOrder, start, size);
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            removeRssSession(httpConnectionCfg);
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg,
                                     List<JIRAQueryFragment> query,
                                     String sort,
                                     String sortOrder,
                                     int start,
                                     int size) throws JIRAException {
        try {
            JiraRssAutoRenewClient rss = getRssSession(httpConnectionCfg);
            return rss.getIssues(query, sort, sortOrder, start, size);
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            removeRssSession(httpConnectionCfg);
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAIssue> getSavedFilterIssues(ConnectionCfg httpConnectionCfg,
                                                List<JIRAQueryFragment> query,
                                                String sort,
                                                String sortOrder,
                                                int start,
                                                int size) throws JIRAException {
        try {
            JiraRssAutoRenewClient rss = getRssSession(httpConnectionCfg);
            if (query.size() != 1) {
                throw new JIRAException("Only one saved filter could be used for query");
            } else {
                return rss.getSavedFilterIssues(query.get(0), sort, sortOrder, start, size);
            }
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            removeRssSession(httpConnectionCfg);
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public JIRAIssue getIssue(ConnectionCfg httpConnectionCfg, String key) throws JIRAException {
        try {
            JiraRssAutoRenewClient rss = getRssSession(httpConnectionCfg);
            return rss.getIssue(key);
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            removeRssSession(httpConnectionCfg);
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAProject> getProjects(ConnectionCfg server) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(server);
            return soap.getProjects();
        } catch (CaptchaRequiredException e) {
            removeRssSession(server);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(server));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAConstant> getIssueTypes(ConnectionCfg httpConnectionCfg) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(httpConnectionCfg);
            return soap.getIssueTypes();
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(httpConnectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAConstant> getIssueTypesForProject(ConnectionCfg httpConnectionCfg, String project)
            throws JIRAException {
        try {
            JIRASession soap = getSoapSession(httpConnectionCfg);
            return soap.getIssueTypesForProject(project);
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(httpConnectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAConstant> getSubtaskIssueTypes(ConnectionCfg httpConnectionCfg) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(httpConnectionCfg);
            return soap.getSubtaskIssueTypes();
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(httpConnectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(ConnectionCfg connectionCfg, String project)
            throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getSubtaskIssueTypesForProject(project);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAConstant> getStatuses(ConnectionCfg connection) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connection);
            return soap.getStatuses();
        } catch (CaptchaRequiredException e) {
            removeRssSession(connection);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connection));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void addComment(ConnectionCfg connectionCfg, String issueKey, String comment) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            soap.addComment(issueKey, comment);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void addAttachment(ConnectionCfg connectionCfg, String issueKey, String name, byte[] content)
            throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            soap.addAttachment(issueKey, name, content);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public JIRAIssue createIssue(ConnectionCfg connectionCfg, JIRAIssue issue) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            JIRAIssue i = soap.createIssue(issue);
            return getIssue(connectionCfg, i.getKey());
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void logWork(ConnectionCfg connectionCfg, JIRAIssue issue, String timeSpent, Calendar startDate,
                        String comment, boolean updateEstimate, String newEstimate)
            throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            soap.logWork(issue, timeSpent, startDate, comment, updateEstimate, newEstimate);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAComponentBean> getComponents(ConnectionCfg connectionCfg, String projectKey) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getComponents(projectKey);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAVersionBean> getVersions(ConnectionCfg connectionCfg, String projectKey) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getVersions(projectKey);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            if (e == null) {
                logger.warn("PL-1710: e is null");
            } else if (e.getMessage() == null) {
                logger.warn("PL-1710: e.getMessage() is null");
            }
//			if (e == null || e.getMessage() == null) {
//				throw new JIRAException("Cannot retrieve versions from the server", e);
//			}
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAPriorityBean> getPriorities(ConnectionCfg connectionCfg) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getPriorities();
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAResolutionBean> getResolutions(ConnectionCfg connectionCfg) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getResolutions();
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAQueryFragment> getSavedFilters(ConnectionCfg connectionCfg) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getSavedFilters();
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAAction> getAvailableActions(ConnectionCfg connectionCfg, JIRAIssue issue) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getAvailableActions(issue);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAActionField> getFieldsForAction(ConnectionCfg connectionCfg, JIRAIssue issue, JIRAAction action)
            throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getFieldsForAction(issue, action);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void setField(ConnectionCfg connectionCfg, JIRAIssue issue, String fieldId, String value)
            throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            soap.setField(issue, fieldId, value);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void setField(ConnectionCfg connectionCfg, JIRAIssue issue, String fieldId, String[] values)
            throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            soap.setField(issue, fieldId, values);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void setFields(ConnectionCfg connectionCfg, JIRAIssue issue, List<JIRAActionField> fields) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            soap.setFields(issue, fields);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }


    public List<JIRAComment> getComments(ConnectionCfg connectionCfg, JIRAIssue issue) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getComments(issue);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public Collection<JIRAAttachment> getIssueAttachements(ConnectionCfg connectionCfg, JIRAIssue issue)
            throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getIssueAttachements(issue);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void progressWorkflowAction(ConnectionCfg connectionCfg, JIRAIssue issue, JIRAAction action)
            throws JIRAException {
        progressWorkflowAction(connectionCfg, issue, action, null);
    }

    public void progressWorkflowAction(ConnectionCfg connectionCfg, JIRAIssue issue,
                                       JIRAAction action, List<JIRAActionField> fields) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            soap.progressWorkflowAction(issue, action, fields);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public JIRAIssue getIssueDetails(ConnectionCfg connectionCfg, JIRAIssue issue) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getIssueDetails(issue);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public JIRAUserBean getUser(ConnectionCfg connectionCfg, String loginName)
            throws JIRAException, JiraUserNotFoundException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getUser(loginName);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(ConnectionCfg connectionCfg, String projectKey) throws JIRAException {
        try {
            JIRASession soap = getSoapSession(connectionCfg);
            return soap.getSecurityLevels(projectKey);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            throw new JIRAException(e.getMessage(), e);
        }
    }
}
