package com.atlassian.connector.commons.jira;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.beans.*;
import com.atlassian.connector.commons.jira.rest.JiraRestSessionImpl;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;
import com.atlassian.theplugin.commons.util.Logger;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * User: kalamon
 * Date: 14.11.12
 * Time: 15:32
 */
public class JiraRESTFacade2Impl implements JIRAServerFacade2, JiraRESTSupportTester {
    private static Logger logger;

    private Map<ConnectionCfg, JiraRestSessionImpl> sessions = new HashMap<ConnectionCfg, JiraRestSessionImpl>();

    public static void setLogger(Logger logger) {
        JiraRESTFacade2Impl.logger = logger;
    }

    public boolean supportsRest(ConnectionCfg server) throws JIRAException {
        JiraRestSessionImpl session = get(server);
        return session.supportsRest();
    }

    public List<JIRAIssue> getIssues(ConnectionCfg server, String queryString, String sort, String sortOrder, int start, int size) throws JIRAException {
        JiraRestSessionImpl session = get(server);
        return session.getIssues(queryString, sort, sortOrder, start, size);
    }

    public List<JIRAIssue> getIssues(ConnectionCfg server, List<JIRAQueryFragment> query, String sort, String sortOrder, int start, int size) throws JIRAException {
        JiraRestSessionImpl session = get(server);
        return session.getIssues(query, sort, sortOrder, start, size);
    }

    public List<JIRAIssue> getSavedFilterIssues(ConnectionCfg server, List<JIRAQueryFragment> query, String sort, String sortOrder, int start, int size) throws JIRAException {
        JiraRestSessionImpl session = get(server);
        return session.getSavedFilterIssues(query.get(0), sort, sortOrder, start, size);
    }

    public List<JIRAProject> getProjects(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAProject>>() {
            public List<JIRAProject> call() throws Exception {
                return get(server).getProjects();
            }
        });
    }

    public List<JIRAConstant> getStatuses(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return get(server).getStatuses();
            }
        });
    }

    public List<JIRAConstant> getIssueTypes(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return get(server).getIssueTypes();
            }
        });
    }

    public List<JIRAConstant> getIssueTypesForProject(final ConnectionCfg server, final String project) throws JIRAException {
        return withJiraException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return get(server).getIssueTypesForProject(project);
            }
        });
    }

    public List<JIRAConstant> getSubtaskIssueTypes(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return get(server).getSubtaskIssueTypes();
            }
        });
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(final ConnectionCfg server, final String project) throws JIRAException {
        return withJiraException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return get(server).getSubtaskIssueTypesForProject(project);
            }
        });
    }

    public List<JIRAQueryFragment> getSavedFilters(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAQueryFragment>>() {
            public List<JIRAQueryFragment> call() throws Exception {
                return get(server).getSavedFilters();
            }
        });
    }

    public List<JIRAComponentBean> getComponents(final ConnectionCfg server, final String projectKey) throws JIRAException {
        return withJiraException(new Callable<List<JIRAComponentBean>>() {
            public List<JIRAComponentBean> call() throws Exception {
                return get(server).getComponents(projectKey);
            }
        });
    }

    public List<JIRAVersionBean> getVersions(final ConnectionCfg server, final String projectKey) throws JIRAException {
        return withJiraException(new Callable<List<JIRAVersionBean>>() {
            public List<JIRAVersionBean> call() throws Exception {
                return get(server).getVersions(projectKey);
            }
        });
    }

    public List<JIRAPriorityBean> getPriorities(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAPriorityBean>>() {
            public List<JIRAPriorityBean> call() throws Exception {
                return get(server).getPriorities();
            }
        });
    }

    public List<JIRAResolutionBean> getResolutions(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAResolutionBean>>() {
            public List<JIRAResolutionBean> call() throws Exception {
                return get(server).getResolutions();
            }
        });
    }

    public List<JIRAAction> getAvailableActions(final ConnectionCfg server, final JIRAIssue issue) throws JIRAException {
        return withJiraException(new Callable<List<JIRAAction>>() {
            public List<JIRAAction> call() throws Exception {
                return get(server).getAvailableActions(issue);
            }
        });
    }

    public List<JIRAActionField> getFieldsForAction(final ConnectionCfg server, final JIRAIssue issue, final JIRAAction action) throws JIRAException {
        return withJiraException(new Callable<List<JIRAActionField>>() {
            public List<JIRAActionField> call() throws Exception {
                return get(server).getFieldsForAction(issue, action);
            }
        });
    }

    public void progressWorkflowAction(final ConnectionCfg server, final JIRAIssue issue, final JIRAAction action) throws JIRAException {
        withJiraException(new Callable<Object>() {
            public Object call() throws Exception {
                get(server).progressWorkflowAction(issue, action, null);
                return null;
            }
        });
    }

    public void progressWorkflowAction(final ConnectionCfg server, final JIRAIssue issue, final JIRAAction action, final List<JIRAActionField> fields) throws JIRAException {
        withJiraException(new Callable<Object>() {
            public Object call() throws Exception {
                get(server).progressWorkflowAction(issue, action, fields);
                return null;
            }
        });
    }

    public void addComment(ConnectionCfg server, String issueKey, String comment) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public void addAttachment(ConnectionCfg server, String issueKey, String name, byte[] content) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public JIRAIssue createIssue(ConnectionCfg server, JIRAIssue issue) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public JIRAIssue getIssue(ConnectionCfg server, String key) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public JIRAIssue getIssueDetails(ConnectionCfg server, JIRAIssue issue) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public void logWork(ConnectionCfg server, JIRAIssue issue, String timeSpent, Calendar startDate, String comment, boolean updateEstimate, String newEstimate) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public void setField(ConnectionCfg server, JIRAIssue issue, String fieldId, String value) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public void setField(ConnectionCfg server, JIRAIssue issue, String fieldId, String[] values) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public void setFields(ConnectionCfg server, JIRAIssue issue, List<JIRAActionField> fields) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public JIRAUserBean getUser(ConnectionCfg server, String loginName) throws JIRAException, JiraUserNotFoundException {
        JiraRestSessionImpl session = get(server);
    }

    public List<JIRAComment> getComments(ConnectionCfg server, JIRAIssue issue) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public Collection<JIRAAttachment> getIssueAttachements(ConnectionCfg server, JIRAIssue issue) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(ConnectionCfg server, String projectKey) throws JIRAException {
        JiraRestSessionImpl session = get(server);
    }

    public void testServerConnection(ConnectionCfg httpConnectionCfg) throws RemoteApiException {
        throw new RemoteApiException(NOT_IMPLEMENTED_YET_COME_BACK_SOON);
    }

    public ServerType getServerType() {
        return ServerType.JIRA_SERVER;
    }

    public static class NotYetImplemented extends JIRAException {
        public NotYetImplemented() {
            super(NOT_IMPLEMENTED_YET_COME_BACK_SOON);
        }
    }

    public static final String NOT_IMPLEMENTED_YET_COME_BACK_SOON = "Not implemented yet. Come back soon";

    private JiraRestSessionImpl get(ConnectionCfg connectionCfg) {

        JiraRestSessionImpl session = sessions.get(connectionCfg);
        if (session == null) {
            boolean useIdeaProxySettings =
                    ConfigurationFactory.getConfiguration().getGeneralConfigurationData().getUseIdeaProxySettings();
            HttpConfigurableAdapter proxyInfo = ConfigurationFactory.getConfiguration().transientGetHttpConfigurable();

            session = new JiraRestSessionImpl(connectionCfg, useIdeaProxySettings ? proxyInfo : null);
            sessions.put(connectionCfg, session);
        }
        return session;
    }

    private <T> T withJiraException(Callable<T> callable) throws JIRAException {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new JIRAException(e.getMessage(), e);
        }
    }
}
