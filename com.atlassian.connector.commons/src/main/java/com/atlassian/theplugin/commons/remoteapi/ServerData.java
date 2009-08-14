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
package com.atlassian.theplugin.commons.remoteapi;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import org.jetbrains.annotations.NotNull;

/**
 * @author pmaruszak
 */
public class ServerData implements Comparable<ServerData> {
	private final Server server;
	private final String username;
	private final String password;

	/**
	 * That constructor should not be used as it is not compatible with default credentials. UnitTest usages should be removed
	 * and replaced by other mechanism.
	 *
	 * @param server
	 * @param userName
	 * @param password
	 */
	@Deprecated
	public ServerData(final Server server, final String userName, final String password) {
		this.server = server;
		this.username = userName;
		this.password = password;
	}

	public ServerData(@NotNull Server server, @NotNull UserCfg defaultCredentials) {
		this.server = server;

		if (server.isUseDefaultCredentials()) {
			this.username = defaultCredentials.getUsername();
			this.password = defaultCredentials.getPassword();
		} else {
			this.username = server.getUsername();
			this.password = server.getPassword();
		}
	}


	protected Server getServer() {
		return server;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getUrl() {
		return server.getUrl();
	}

	public ServerId getServerId() {
		return server.getServerId();
	}

	public String getName() {
		return server.getName();
	}


	public boolean isEnabled() {
		return server.isEnabled();
	}

	public ServerType getServerType() {
		return server.getServerType();
	}

	@Override
	/**
	 * Beware when overriding this method. It uses instanceof instead of getClass().
	 * Remember to keep 'symmetry'
	 */
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof ServerData)) { //getClass() != o.getClass()) {
			return false;
		}

		final ServerData that = (ServerData) o;

		if (getServerId() != null ? !getServerId().equals(that.getServerId()) : that.getServerId() != null) {
			return false;
		}
		if (getUrl() != null ? !getUrl().equals(that.getUrl()) : that.getUrl() != null) {
			return false;
		}
		if (password != null ? !password.equals(that.password) : that.password != null) {
			return false;
		}
		if (username != null ? !username.equals(that.username) : that.username != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		// todo do we want to use name for hashCode and Equals???
//		result = (name != null ? name.hashCode() : 0);
		result = (getServerId() != null ? getServerId().hashCode() : 0);
		result = 31 * result + (username != null ? username.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
		return result;
	}

	public ConnectionCfg toConnectionCfg() {
		return new ConnectionCfg(getServerId().getId(), getUrl(), getUsername(), getPassword());
	}

	public int compareTo(ServerData o) {
		ServerDataComparator c = new ServerDataComparator();
		return c.compare(this, o);
    }
}
