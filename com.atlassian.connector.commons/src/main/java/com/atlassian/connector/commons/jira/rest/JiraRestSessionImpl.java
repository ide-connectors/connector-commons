package com.atlassian.connector.commons.jira.rest;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.*;
import com.atlassian.connector.commons.jira.beans.*;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.OptionalIterable;
import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.jira.JiraCaptchaRequiredException;
import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

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

    public List<JIRAProject> getProjects() throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAProject>>() {
            @Override
            public List<JIRAProject> call() throws Exception {
                Iterable<BasicProject> projects = restClient.getProjectClient().getAllProjects(pm);
                List<JIRAProject> result = Lists.newArrayList();
                for (BasicProject project : projects) {
                    Long id = project.getId();
                    result.add(new JIRAProjectBean(id != null ? id : -1, project.getKey(), project.getName()));
                }
                return result;
            }
        });
    }

    public List<JIRAConstant> getIssueTypes() throws RemoteApiException {
        return getIssueTypes(false);
    }

    public List<JIRAConstant> getSubtaskIssueTypes() throws RemoteApiException {
        return getIssueTypes(true);
    }

    private List<JIRAConstant> getIssueTypes(final boolean subtasks) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAConstant>>() {
            @Override
            public List<JIRAConstant> call() throws Exception {
                Iterable<IssueType> issueTypes = restClient.getMetadataClient().getIssueTypes(pm);
                List<JIRAConstant> result = Lists.newArrayList();
                for (IssueType type : issueTypes) {
                    Long id = type.getId();
                    if (type.isSubtask() != subtasks || id == null) continue;
                    result.add(new JIRAIssueTypeBean(id, type.getName(), type.getIconUri().toURL()));
                }
                return result;
            }
        });
    }

    public List<JIRAConstant> getIssueTypesForProject(long projectId, String projectKey) throws RemoteApiException {
        return getIssueTypesForProject(projectKey, false);
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(long projectId, String projectKey) throws RemoteApiException {
        return getIssueTypesForProject(projectKey, true);
    }

    private List<JIRAConstant> getIssueTypesForProject(final String projectKey, final boolean subtasks) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAConstant>>() {
            @Override
            public List<JIRAConstant> call() throws Exception {
                OptionalIterable<IssueType> issueTypes = restClient.getProjectClient().getProject(projectKey, pm).getIssueTypes();
                List<JIRAConstant> result = Lists.newArrayList();
                for (IssueType issueType : issueTypes) {
                    if (subtasks != issueType.isSubtask()) continue;
                    Long id = issueType.getId();
                    result.add(new JIRAIssueTypeBean(id != null ? id : -1, issueType.getName(), issueType.getIconUri().toURL()));
                }
                return result;
            }
        });
    }

    public List<JIRAConstant> getStatuses() throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAConstant>>() {
            @Override
            public List<JIRAConstant> call() throws Exception {
                Iterable<Status> statuses = restClient.getMetadataClient().getStatuses(pm);
                List<JIRAConstant> result = Lists.newArrayList();
                for (Status status : statuses) {
                    Long id = status.getId();
                    result.add(new JIRAStatusBean(id != null ? id : -1, status.getName(), status.getIconUrl().toURL()));
                }
                return result;
            }
        });
    }

    public List<JIRAComponentBean> getComponents(final String projectKey) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAComponentBean>>() {
            @Override
            public List<JIRAComponentBean> call() throws Exception {
                Iterable<BasicComponent> components = restClient.getProjectClient().getProject(projectKey, pm).getComponents();
                List<JIRAComponentBean> result = Lists.newArrayList();
                for (BasicComponent component : components) {
                    Long id = component.getId();
                    result.add(new JIRAComponentBean(id != null ? id : -1, component.getName()));
                }
                return result;
            }
        });
    }

    public List<JIRAVersionBean> getVersions(final String projectKey) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAVersionBean>>() {
            @Override
            public List<JIRAVersionBean> call() throws Exception {
                Iterable<Version> versions = restClient.getProjectClient().getProject(projectKey, pm).getVersions();
                List<JIRAVersionBean> result = Lists.newArrayList();
                for (Version version : versions) {
                    Long id = version.getId();
                    result.add(new JIRAVersionBean(id != null ? id : -1, version.getName(), version.isReleased()));
                }
                return result;
            }
        });
    }

    public List<JIRAPriorityBean> getPriorities() throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAPriorityBean>>() {
            @Override
            public List<JIRAPriorityBean> call() throws Exception {
                Iterable<Priority> priorities = restClient.getMetadataClient().getPriorities(pm);
                List<JIRAPriorityBean> result = Lists.newArrayList();
                int order = 0;
                for (Priority priority : priorities) {
                    Long id = priority.getId();
                    result.add(new JIRAPriorityBean(id != null ? id : -1, order++, priority.getName(), priority.getIconUri().toURL()));
                }
                return result;
            }
        });
    }

    public List<JIRAResolutionBean> getResolutions() throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAResolutionBean>>() {
            @Override
            public List<JIRAResolutionBean> call() throws Exception {
                Iterable<Resolution> resolutions = restClient.getMetadataClient().getResolutions(pm);
                List<JIRAResolutionBean> result = Lists.newArrayList();
                for (Resolution status : resolutions) {
                    Long id = status.getId();
                    result.add(new JIRAResolutionBean(id != null ? id : -1, status.getName()));
                }
                return result;
            }
        });
    }

    public List<JIRAQueryFragment> getSavedFilters() throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAQueryFragment>>() {
            @Override
            public List<JIRAQueryFragment> call() throws Exception {
                Iterable<FavouriteFilter> filters = restClient.getSearchClient().getFavouriteFilters(pm);
                List<JIRAQueryFragment> result = Lists.newArrayList();
                for (FavouriteFilter filter : filters) {
                    Long id = filter.getId();
                    result.add(new JIRASavedFilterBean(filter.getName(), id != null ? id : -1, filter.getJql(), filter.getSearchUrl(), filter.getViewUrl()));
                }
                return result;
            }
        });
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

    public List<JIRAIssue> getIssues(
            JiraFilter filter, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        return getIssues(filter.getJql(), sortBy, sortOrder, start, max);
    }

    public List<JIRAIssue> getSavedFilterIssues(
            JIRASavedFilter filter, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        return getIssues(filter.getJql(), sortBy, sortOrder, start, max);
    }

    private List<JIRAIssue> getIssues(
            final String jql, final String sortBy, final String sortOrder, final int start, final int max)
            throws JIRAException {
        return wrapWithJiraException(new Callable<List<JIRAIssue>>() {
            @Override
            public List<JIRAIssue> call() throws Exception {
                String sort = StringUtils.isNotEmpty(sortBy) && StringUtils.isNotEmpty(sortOrder) ? " order by " + sortBy + " " + sortOrder : "";
                SearchResult result = restClient.getSearchClient().searchJqlWithFullIssues(jql + sort, max, start, pm);
                List<JIRAIssue> list = Lists.newArrayList();
                for (BasicIssue issue : result.getIssues()) {
                    JIRAIssueBean bean = new JIRAIssueBean(server.getUrl(), (Issue) issue);
                    list.add(bean);
                }
                return list;
            }
        });
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
