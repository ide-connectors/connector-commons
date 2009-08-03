package com.atlassian.theplugin.commons.remoteapi;

import java.util.Comparator;

/**
 * @author pmaruszak
 */
public class ServerDataComparator implements Comparator<ServerData> {
    public int compare(ServerData serverData, ServerData serverData1) {
        	if (serverData == null || serverData1 == null) {
			return 0;
		}

        if (!serverData.getServerType().equals(serverData1.getServerType())) {
            return serverData.compareTo(serverData1);
        }

        if (!(serverData.getUrl().equals(serverData1.getUrl()))) {
            return serverData.getUrl().compareTo(serverData1.getUrl());
        }

        return serverData.getName().compareTo(serverData1.getName());

    }
}
