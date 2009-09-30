package com.atlassian.theplugin.commons.remoteapi;

import com.atlassian.connector.commons.api.HttpConnectionCfg;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import org.jetbrains.annotations.NotNull;

/**
 * @author pmaruszak
 * @date Sep 29, 2009
 */
public class HttpServerData extends ServerData {
    private final boolean useBasicAuth;

    public HttpServerData(@NotNull Server server, @NotNull UserCfg defaultCredentials, @NotNull boolean useBasicAuth) {
        super(server, defaultCredentials);
        this.useBasicAuth = useBasicAuth;
    }

     public HttpServerData(@NotNull Server server, @NotNull UserCfg defaultCredentials) {
        super(server, defaultCredentials);
        this.useBasicAuth = false;
    }

    public boolean isUseBasicAuth() {
        return useBasicAuth;
    }

    public HttpConnectionCfg toHttpConnectionCfg() {
		return new HttpConnectionCfg(getServerId().getId(), getUrl(), getUsername(), getPassword(), isUseBasicAuth());
    }
}
