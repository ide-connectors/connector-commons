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

/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 13/03/2004
 * Time: 23:19:19
 */
package com.atlassian.connector.commons.jira.rss;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.JIRAIssueBean;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.cache.CacheConstants;
import com.atlassian.connector.commons.jira.cache.CachedIconLoader;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.StringUtil;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.atlassian.theplugin.commons.util.UrlUtil.encodeUrl;

public class JIRARssClient extends AbstractHttpSession {

    private final ConnectionCfg httpConnectionCfg;
    private Header[] setCookieHeaders;
    private boolean login = false;

    public JIRARssClient(final ConnectionCfg httpConnectionCfg, final HttpSessionCallback callback)
            throws RemoteApiMalformedUrlException {
        super(httpConnectionCfg, callback);
        this.httpConnectionCfg = httpConnectionCfg;
    }

    @Override
    protected void adjustHttpHeader(HttpMethod method) {
        if (httpConnectionCfg instanceof ServerData && ((ServerData) httpConnectionCfg).isUseBasicUser()) {
            method.addRequestHeader(new Header("Authorization", getAuthBasicHeaderValue()));

        }

        if (login) {
            if (httpConnectionCfg != null && method instanceof PostMethod) {
                ((PostMethod) method).addParameter("os_username", httpConnectionCfg.getUsername());
                ((PostMethod) method).addParameter("os_password", httpConnectionCfg.getPassword());
                login = false;
            }
        }
        if (setCookieHeaders != null) {
            for (Header setCookieHeader : setCookieHeaders) {
                method.addRequestHeader(setCookieHeader);
            }

        }
    }

    @Override
    protected void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException {
    }

    @Override
    protected void preprocessMethodResult(HttpMethod method) {
        if (method != null) {
            setCookieHeaders = method.getResponseHeaders("Set-Cookie");
        }
    }

    private String getAuthBasicHeaderValue() {
        UserCfg basicUser = ((ServerData) httpConnectionCfg).getBasicUser();

        if (basicUser != null && basicUser.getUsername() != null && basicUser.getPassword() != null) {
            return "Basic " + StringUtil.encode(basicUser.getUsername() + ":" + basicUser.getPassword());
        }

        return "";
    }


    public List<JIRAIssue> getIssues(String queryString, String sortBy, String sortOrder, int start, int max)
            throws JIRAException {

        StringBuilder url =
                new StringBuilder(getBaseUrl() + "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?");

        url.append(queryString);

        url.append("&sorter/field=").append(sortBy);
        url.append("&sorter/order=").append(sortOrder);
        url.append("&pager/start=").append(start);
        url.append("&tempMax=").append(max);

        try {
            Document doc = retrieveGetResponse(url.toString());
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");
            if (channel != null && !channel.getChildren("item").isEmpty()) {
                return makeIssues(channel.getChildren("item"));
            }
            return Collections.emptyList();
        } catch (AuthenticationException e) {
            throw new JIRAException("Authentication error", e);
        } catch (IOException e) {
            throw new JIRAException("Connection error: " + e.getMessage(), e);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAIssue> getIssues(List<JIRAQueryFragment> fragments, String sortBy,
                                     String sortOrder, int start, int max) throws JIRAException {

        StringBuilder query = new StringBuilder();

        List<JIRAQueryFragment> fragmentsWithoutAnys = new ArrayList<JIRAQueryFragment>();
        for (JIRAQueryFragment jiraQueryFragment : fragments) {
            if (jiraQueryFragment.getId() != CacheConstants.ANY_ID) {
                fragmentsWithoutAnys.add(jiraQueryFragment);
            }
        }

        for (JIRAQueryFragment fragment : fragmentsWithoutAnys) {
            if (fragment.getQueryStringFragment() != null) {
                query.append("&").append(fragment.getQueryStringFragment());
            }
        }

        return getIssues(query.toString(), sortBy, sortOrder, start, max);
    }

    public List<JIRAIssue> getAssignedIssues(String assignee) throws JIRAException {
        String url = getBaseUrl() + "/sr/jira.issueviews:searchrequest-xml"
                + "/temp/SearchRequest.xml?resolution=-1&assignee=" + encodeUrl(assignee)
                + "&sorter/field=updated&sorter/order=DESC&tempMax=100";

        try {
            Document doc = retrieveGetResponse(url);
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");
            if (channel != null && !channel.getChildren("item").isEmpty()) {
                return makeIssues(channel.getChildren("item"));
            }


            return Collections.emptyList();
        } catch (IOException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAIssue> getSavedFilterIssues(JIRAQueryFragment fragment,
                                                String sortBy,
                                                String sortOrder,
                                                int start,
                                                int max) throws JIRAException {

        StringBuilder url = new StringBuilder(getBaseUrl() + "/sr/jira.issueviews:searchrequest-xml/");

        if (fragment.getQueryStringFragment() != null) {
            url.append(fragment.getQueryStringFragment())
                    .append("/SearchRequest-")
                    .append(fragment.getQueryStringFragment())
                    .append(".xml");
        }

        url.append("?sorter/field=").append(sortBy);
        url.append("&sorter/order=").append(sortOrder);
        url.append("&pager/start=").append(start);
        url.append("&tempMax=").append(max);

        try {
            Document doc = retrieveGetResponse(url.toString());
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");
            if (channel != null && !channel.getChildren("item").isEmpty()) {
                return makeIssues(channel.getChildren("item"));
            }
            return Collections.emptyList();
        } catch (IOException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new JIRAException(e.getMessage(), e);
        }

    }

    public JIRAIssue getIssue(String issueKey) throws JIRAException {

        StringBuffer url = new StringBuffer(getBaseUrl() + "/si/jira.issueviews:issue-xml/");
        url.append(issueKey).append('/').append(issueKey).append(".xml");

        try {
            Document doc = retrieveGetResponse(url.toString());
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");
            if (channel != null) {
                @SuppressWarnings("unchecked")
                final List<Element> items = channel.getChildren("item");
                if (!items.isEmpty()) {
                    return makeIssues(items).get(0);
                }
            }
            throw new JIRAException("Cannot parse response from JIRA: " + doc.toString());
        } catch (IOException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new JIRAException(e.getMessage(), e);
        }
    }

    private List<JIRAIssue> makeIssues(@NotNull List<Element> issueElements) {
        List<JIRAIssue> result = new ArrayList<JIRAIssue>(issueElements.size());
        for (final Element issueElement : issueElements) {
            JIRAIssueBean jiraIssue = new JIRAIssueBean(httpConnectionCfg.getUrl(), issueElement);
            CachedIconLoader.loadIcon(jiraIssue.getTypeIconUrl());
            CachedIconLoader.loadIcon(jiraIssue.getPriorityIconUrl());
            CachedIconLoader.loadIcon(jiraIssue.getStatusTypeUrl());
            result.add(jiraIssue);
        }
        return result;
    }


    public void login() throws JIRAException {
        try {
            login = true;
            super.retrievePostResponse(httpConnectionCfg.getUrl() + "/secure/Dashboard.jspa", "", false);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage());
        } catch (RemoteApiException e) {
            throw new JIRAException(e.getMessage());
        }
    }

    public boolean isLoggedIn() {
        return setCookieHeaders != null && setCookieHeaders.length > 0;
    }
}