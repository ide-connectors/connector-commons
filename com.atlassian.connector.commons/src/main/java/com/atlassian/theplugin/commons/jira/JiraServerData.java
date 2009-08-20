package com.atlassian.theplugin.commons.jira;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import org.jetbrains.annotations.NotNull;

/**
 * User: kalamon
 * Date: Aug 19, 2009
 * Time: 3:52:44 PM
 */
public class JiraServerData extends ServerData {
    private boolean useBasicAuth;

    public JiraServerData(Server server, String userName, String password, boolean useBasicAuth) {
        super(server, userName, password);
        this.useBasicAuth = useBasicAuth;
    }

    public JiraServerData(@NotNull Server server, @NotNull UserCfg defaultCredentials, boolean useBasicAuth) {
        super(server, defaultCredentials);
        this.useBasicAuth = useBasicAuth;
    }

    public boolean isUseBasicAuth() {
        return useBasicAuth;
    }
}
