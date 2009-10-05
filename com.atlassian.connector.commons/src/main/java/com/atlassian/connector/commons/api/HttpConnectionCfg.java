package com.atlassian.connector.commons.api;

/**
 * @author pmaruszak
 * @date Sep 25, 2009
 */
public class HttpConnectionCfg extends ConnectionCfg {
    private boolean useBasicHttpAuth = false;

    public HttpConnectionCfg(String id, String url, String username, String password) {
        this(id, url, username, password, false);
    }
    
    public HttpConnectionCfg(String id, String url, String username, String password, boolean useBasicHttpAuth) {
        super(id, url, username, password);
        this.useBasicHttpAuth = useBasicHttpAuth;
    }

    public boolean isUseBasicHttpAuth() {
        return useBasicHttpAuth;
    }

    public ConnectionCfg toConnectionCfg() {
        return new ConnectionCfg(getId(), getUrl(), getUsername(), getPassword());
    }
}
