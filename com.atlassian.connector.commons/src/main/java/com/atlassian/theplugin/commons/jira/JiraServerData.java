package com.atlassian.theplugin.commons.jira;

import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

/**
 * User: kalamon
 * Date: Aug 19, 2009
 * Time: 3:52:44 PM
 */
public class JiraServerData extends ServerData {

    public JiraServerData(Server server) {
        super(server);
    }

    public JiraServerData(Server server, UserCfg defaultUser) {
        super(server, defaultUser); 
    }

    public static class Builder extends ServerData.Builder {

        public Builder(Server server) {
            super(server);
        }

        @Override
        public JiraServerData build() {
            return new JiraServerData(super.getServer(), defaultUser);
        }

    }

}
