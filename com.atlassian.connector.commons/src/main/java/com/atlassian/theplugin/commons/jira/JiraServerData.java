package com.atlassian.theplugin.commons.jira;

import com.atlassian.connector.commons.api.HttpConnectionCfg;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.jetbrains.annotations.NotNull;

/**
 * User: kalamon
 * Date: Aug 19, 2009
 * Time: 3:52:44 PM
 */
public class JiraServerData extends ServerData {
    private boolean dontUseBasicAuth;

    public JiraServerData(Server server, String userName, String password, boolean dontUseBasicAuth) {
        super(server, userName, password);
        this.dontUseBasicAuth = dontUseBasicAuth;
    }

    public JiraServerData(@NotNull Server server, @NotNull UserCfg defaultCredentials, boolean dontUseBasicAuth) {
        super(server, defaultCredentials);
        this.dontUseBasicAuth = dontUseBasicAuth;
    }

    public boolean isDontUseBasicAuth() {
        return dontUseBasicAuth;
    }

    public HttpConnectionCfg toHttpConnectionCfg() {
        return new HttpConnectionCfg(getServerId().toString(), getUrl(), getUsername(), getPassword(), !dontUseBasicAuth);
    }
}
