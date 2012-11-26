package com.atlassian.connector.commons.jira.rest;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.*;
import com.atlassian.connector.commons.jira.beans.*;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.OptionalIterable;
import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.jira.JiraCaptchaRequiredException;
import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.ByteArrayInputStream;
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
                    result.add(new JIRASavedFilterBean(filter.getName(), id != null ? id : -1, filter.getJql().replace("\\\"", "\""), filter.getSearchUrl(), filter.getViewUrl()));
                }
                return result;
            }
        });
    }

    public List<JIRAAction> getAvailableActions(final JIRAIssue issue) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAAction>>() {
            @Override
            public List<JIRAAction> call() throws Exception {
                List<JIRAAction> result = Lists.newArrayList();
                Iterable<Transition> transitions = restClient.getIssueClient().getTransitions((Issue) issue.getApiIssueObject(), pm);
                for (Transition transition : transitions) {
                    result.add(new JIRAActionBean(transition.getId(), transition.getName()));
                }
                return result;
            }
        });
    }

    public List<JIRAActionField> getFieldsForAction(final JIRAIssue issue, final JIRAAction action) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAActionField>>() {
            @Override
            public List<JIRAActionField> call() throws Exception {
                Iterable<Transition> transitions = restClient.getIssueClient().getTransitions((Issue) issue.getApiIssueObject(), pm);
                List<JIRAActionField> result = Lists.newArrayList();
                for (Transition transition : transitions) {
                    if (transition.getId() != action.getId()) {
                        continue;
                    }
                    for (Transition.Field field : transition.getFields()) {
                        JIRAActionFieldBean f = new JIRAActionFieldBean(field.getId(), field.getName());
                        result.add(f);
                    }
                    break;
                }
                return result;
            }
        });
    }

    public void progressWorkflowAction(final JIRAIssue issue, final JIRAAction action, final List<JIRAActionField> fields) throws RemoteApiException {
        final List<FieldInput> fieldValues = Lists.newArrayList();
        if (fields == null || fields.size() == 0) {
            wrapWithRemoteApiException(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    TransitionInput t = new TransitionInput((int) action.getId(), fieldValues);
                    restClient.getIssueClient().transition((Issue) issue.getApiIssueObject(), t, pm);
                    return null;
                }
            });
        } else {
            wrapWithRemoteApiException(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Issue iszju = restClient.getIssueClient().getIssue(issue.getKey(), ImmutableList.of(IssueRestClient.Expandos.EDITMETA), pm);
                    fieldValues.addAll(generateFieldValues(issue, iszju, fields));
                    TransitionInput t = new TransitionInput((int) action.getId(), fieldValues);
                    restClient.getIssueClient().transition((Issue) issue.getApiIssueObject(), t, pm);
                    return null;
                }
            });
        }
    }

    public void setField(JIRAIssue issue, String fieldId, String value) throws RemoteApiException {
        JIRAActionFieldBean f = new JIRAActionFieldBean(fieldId, null);
        f.addValue(value);
        setFields(issue, ImmutableList.of((JIRAActionField) f));
    }

    public void setField(JIRAIssue issue, String fieldId, String[] values) throws RemoteApiException {
        JIRAActionFieldBean f = new JIRAActionFieldBean(fieldId, null);
        for (String value : values) {
            f.addValue(value);
        }
        setFields(issue, ImmutableList.of((JIRAActionField) f));
    }

    public void setFields(final JIRAIssue issue, final List<JIRAActionField> fields) throws RemoteApiException {
        wrapWithRemoteApiException(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Issue iszju = restClient.getIssueClient().getIssue(issue.getKey(), ImmutableList.of(IssueRestClient.Expandos.EDITMETA), pm);
                restClient.getIssueClient().update(iszju, generateFieldValues(issue, iszju, fields), pm);
                return null;
            }
        });
    }

    private Collection<FieldInput> generateFieldValues(final JIRAIssue issue, final Issue iszju, List<JIRAActionField> fieldValues) throws RemoteApiException {
        JSONObject editmeta = JsonParseUtil.getOptionalJsonObject(iszju.getRawObject(), "editmeta");
        if (editmeta == null) {
            throw new RemoteApiException("Unable to retrieve issue's editmeta information");
        }
        JSONObject fields = JsonParseUtil.getOptionalJsonObject(editmeta, "fields");
        try {
            if (fields != null) {
                List<FieldInput> result = Lists.newArrayList();
                for (JIRAActionField field : fieldValues) {
                    JSONObject fieldDef = JsonParseUtil.getOptionalJsonObject(fields, field.getFieldId());
                    if (fieldDef != null) {
                        FieldInput fieldInput = field.generateFieldValue(issue, fieldDef);
                        if (fieldInput != null) {
                            result.add(fieldInput);
                        }
                    }
                }
                return result;
            }
        } catch (JSONException e) {
            throw new RemoteApiException("Unable to generate field values", e);
        }
        return null;
    }

    public JIRAUserBean getUser(final String loginName) throws RemoteApiException, JiraUserNotFoundException {
        return wrapWithRemoteApiException(new Callable<JIRAUserBean>() {
            @Override
            public JIRAUserBean call() throws Exception {
                User user = restClient.getUserClient().getUser(loginName, pm);
                JIRAUserBean u = new JIRAUserBean(-1, user.getDisplayName(), user.getName()) {
                    @Override
                    public String getQueryStringFragment() {
                        return null;
                    }

                    @Override
                    public JIRAQueryFragment getClone() {
                        return null;
                    }
                };
                return u;
            }
        });
    }

    public List<JIRAComment> getComments(final JIRAIssue issue) throws RemoteApiException {
        if (issue.getComments() == null) {
            return Lists.newArrayList();
        }
        return issue.getComments();
    }

    public Collection<JIRAAttachment> getIssueAttachements(final JIRAIssue issue) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<Collection<JIRAAttachment>>() {
            @Override
            public Collection<JIRAAttachment> call() throws Exception {
//                Issue iszju = (Issue) issue.getApiIssueObject();
                Issue iszju = restClient.getIssueClient().getIssue(issue.getKey(), pm);
                List<JIRAAttachment> result = Lists.newArrayList();
                for (Attachment attachment : iszju.getAttachments()) {
                    Long id = attachment.getId();
                    JIRAAttachment a = new JIRAAttachment(
                        id != null ? id.toString() : "-1", attachment.getAuthor().getName(), attachment.getFilename(),
                        attachment.getSize(), attachment.getMimeType(), attachment.getCreationDate().toGregorianCalendar());
                    result.add(a);
                }
                return result;
            }
        });
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
                String sort =
                        jql.toLowerCase().contains("order by")
                            ? ""
                            : (StringUtils.isNotEmpty(sortBy) && StringUtils.isNotEmpty(sortOrder)
                                ? " order by " + sortBy + " " + sortOrder
                                : "");
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

    public JIRAIssue getIssue(final String issueKey) throws JIRAException {
        return wrapWithJiraException(new Callable<JIRAIssue>() {
            @Override
            public JIRAIssue call() throws Exception {
                Issue issue = restClient.getIssueClient().getIssue(issueKey,
                        ImmutableList.of(IssueRestClient.Expandos.RENDERED_FIELDS, IssueRestClient.Expandos.EDITMETA), pm);
                return new JIRAIssueBean(server.getUrl(), issue);
            }
        });
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

    public void addComment(final String issueKey, final String comment) throws RemoteApiException {
        wrapWithRemoteApiException(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Issue issue = restClient.getIssueClient().getIssue(issueKey, pm);
                restClient.getIssueClient().addComment(pm, issue.getCommentsUri(), Comment.valueOf(comment));
                return null;
            }
        });
    }

    public void addAttachment(final String issueKey, final String name, final byte[] content) throws RemoteApiException {
        wrapWithRemoteApiException(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Issue issue = restClient.getIssueClient().getIssue(issueKey, pm);
                restClient.getIssueClient().addAttachment(pm, issue.getAttachmentsUri(), new ByteArrayInputStream(content), name);
                return null;
            }
        });
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
