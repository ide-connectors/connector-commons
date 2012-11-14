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
import com.atlassian.connector.commons.jira.beans.*;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.connector.commons.jira.soap.AxisSessionCallback;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public final class JIRAServerFacade2Impl implements JIRAServerFacade2 {
    private static Logger logger;

    private JIRASoapAndXmlServerFacade2Impl soapAndXmlFacade;
    private JiraRESTFacade2Impl restFacade;

    private Set<ConnectionCfg> restCapable = new HashSet<ConnectionCfg>();
    private Set<ConnectionCfg> notRestCapable = new HashSet<ConnectionCfg>();

    JIRAServerFacade2 worker;

    public JIRAServerFacade2Impl(HttpSessionCallback callback, AxisSessionCallback axisCallback) {
        soapAndXmlFacade = new JIRASoapAndXmlServerFacade2Impl(callback, axisCallback);
        restFacade = new JiraRESTFacade2Impl();

        worker = (JIRAServerFacade2) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { JIRAServerFacade2.class }, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                ConnectionCfg connection = (ConnectionCfg) args[0];
                boolean useRest = restCapable.contains(connection);
                boolean useSoapAndXml = notRestCapable.contains(connection);
                if (!useRest && !useSoapAndXml) {
                    if (restFacade.supportsRest(connection)) {
                        restCapable.add(connection);
                        useRest = true;
                    } else {
                        notRestCapable.add(connection);
                    }
                }
                if (useRest) {
                    return method.invoke(restFacade, args);
                }
                return method.invoke(soapAndXmlFacade, args);
            }
        });
    }

    public static void setLogger(Logger logger) {
        JIRASoapAndXmlServerFacade2Impl.setLogger(logger);
        JiraRESTFacade2Impl.setLogger(logger);
    }

    public ServerType getServerType() {
        return ServerType.JIRA_SERVER;
    }

    public List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg, String queryString, String sort, String sortOrder, int start, int size) throws JIRAException {
        return worker.getIssues(httpConnectionCfg, queryString, sort, sortOrder, start, size);
    }

    public List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg, List<JIRAQueryFragment> query, String sort, String sortOrder, int start, int size) throws JIRAException {
        return worker.getIssues(httpConnectionCfg, query, sort, sortOrder, start, size);
    }

    public List<JIRAIssue> getSavedFilterIssues(ConnectionCfg httpConnectionCfg, List<JIRAQueryFragment> query, String sort, String sortOrder, int start, int size) throws JIRAException {
        return worker.getSavedFilterIssues(httpConnectionCfg, query, sort, sortOrder, start, size);
    }

    public List<JIRAProject> getProjects(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getProjects(httpConnectionCfg);
    }

    public List<JIRAConstant> getStatuses(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getStatuses(httpConnectionCfg);
    }

    public List<JIRAConstant> getIssueTypes(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getIssueTypes(httpConnectionCfg);
    }

    public List<JIRAConstant> getIssueTypesForProject(ConnectionCfg httpConnectionCfg, String project) throws JIRAException {
        return worker.getIssueTypesForProject(httpConnectionCfg, project);
    }

    public List<JIRAConstant> getSubtaskIssueTypes(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getSubtaskIssueTypes(httpConnectionCfg);
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(ConnectionCfg httpConnectionCfg, String project) throws JIRAException {
        return worker.getSubtaskIssueTypesForProject(httpConnectionCfg, project);
    }

    public List<JIRAQueryFragment> getSavedFilters(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getSavedFilters(httpConnectionCfg);
    }

    public List<JIRAComponentBean> getComponents(ConnectionCfg httpConnectionCfg, String projectKey) throws JIRAException {
        return worker.getComponents(httpConnectionCfg, projectKey);
    }

    public List<JIRAVersionBean> getVersions(ConnectionCfg httpConnectionCfg, String projectKey) throws JIRAException {
        return worker.getVersions(httpConnectionCfg, projectKey);
    }

    public List<JIRAPriorityBean> getPriorities(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getPriorities(httpConnectionCfg);
    }

    public List<JIRAResolutionBean> getResolutions(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getResolutions(httpConnectionCfg);
    }

    public List<JIRAAction> getAvailableActions(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
        return worker.getAvailableActions(httpConnectionCfg, issue);
    }

    public List<JIRAActionField> getFieldsForAction(ConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action) throws JIRAException {
        return worker.getFieldsForAction(httpConnectionCfg, issue, action);
    }

    public void progressWorkflowAction(ConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action) throws JIRAException {
        worker.progressWorkflowAction(httpConnectionCfg, issue, action);
    }

    public void progressWorkflowAction(ConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action, List<JIRAActionField> fields) throws JIRAException {
        worker.progressWorkflowAction(httpConnectionCfg, issue, action, fields);
    }

    public void addComment(ConnectionCfg httpConnectionCfg, String issueKey, String comment) throws JIRAException {
        worker.addComment(httpConnectionCfg, issueKey, comment);
    }

    public void addAttachment(ConnectionCfg httpConnectionCfg, String issueKey, String name, byte[] content) throws JIRAException {
        worker.addAttachment(httpConnectionCfg, issueKey, name, content);
    }

    public JIRAIssue createIssue(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
        return worker.createIssue(httpConnectionCfg, issue);
    }

    public JIRAIssue getIssue(ConnectionCfg httpConnectionCfg, String key) throws JIRAException {
        return worker.getIssue(httpConnectionCfg, key);
    }

    public JIRAIssue getIssueDetails(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
        return worker.getIssueDetails(httpConnectionCfg, issue);
    }

    public void logWork(ConnectionCfg httpConnectionCfg, JIRAIssue issue, String timeSpent, Calendar startDate, String comment, boolean updateEstimate, String newEstimate) throws JIRAException {
        worker.logWork(httpConnectionCfg, issue, timeSpent, startDate, comment, updateEstimate, newEstimate);
    }

    public void setField(ConnectionCfg httpConnectionCfg, JIRAIssue issue, String fieldId, String value) throws JIRAException {
        worker.setField(httpConnectionCfg, issue, fieldId, value);
    }

    public void setField(ConnectionCfg httpConnectionCfg, JIRAIssue issue, String fieldId, String[] values) throws JIRAException {
        worker.setField(httpConnectionCfg, issue, fieldId, values);
    }

    public void setFields(ConnectionCfg httpConnectionCfg, JIRAIssue issue, List<JIRAActionField> fields) throws JIRAException {
        worker.setFields(httpConnectionCfg, issue, fields);
    }

    public JIRAUserBean getUser(ConnectionCfg httpConnectionCfg, String loginName) throws JIRAException, JiraUserNotFoundException {
        return worker.getUser(httpConnectionCfg, loginName);
    }

    public List<JIRAComment> getComments(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
        return worker.getComments(httpConnectionCfg, issue);
    }

    public Collection<JIRAAttachment> getIssueAttachements(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
        return worker.getIssueAttachements(httpConnectionCfg, issue);
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(ConnectionCfg connectionCfg, String projectKey) throws JIRAException {
        return worker.getSecurityLevels(connectionCfg, projectKey);
    }

    public void testServerConnection(ConnectionCfg httpConnectionCfg) throws RemoteApiException {
        worker.testServerConnection(httpConnectionCfg);
    }
}
